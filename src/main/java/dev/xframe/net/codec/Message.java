package dev.xframe.net.codec;

import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.MessageLite;

import dev.xframe.utils.XStrings;
import io.netty.buffer.ByteBuf;

/**
 * 前后端通信消 消息头+消息体, 消息体由protobuff生成
 * 
 * @author luzj
 */
public class Message implements IMessage, WritableMessage {

    public static final byte[] EMPTY_BODY = new byte[0];

    public static final short HDR_SIZE = 24;
    public static final short HDR_FLAG = 0X5362;
    
    public static final int MAX_PARAMS_LEN = 0XFFFF;
    public static final int MAX_BODY_LEN = 0X7FFFFFFF;

    private short flag;		// 消息头标识, 从此字符开始为一个新的消息

    private int code;		// 协议号,对应一个处理类(command)

    private long id;		// 玩家在游戏中的唯一标识符

    private int version;	// 前端版本号
    
    private short paramsLen;// 参数长度(len, content pairs)
    
    private int bodyLen;	// 消息体长度, 指body的长度

    private byte[] body;	// 消息体, 由具体业务逻辑来解析
    
    private Map<String, Param> params;

    private Message() {
        this.flag = HDR_FLAG;
        this.body = EMPTY_BODY;
    }

    public short getFlag() {
        return flag;
    }

    public void setFlag(short flag) {
        this.flag = flag;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @SuppressWarnings("unchecked")
    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public short getParamsLen() {
        return (short) (paramsLen & MAX_PARAMS_LEN);
    }

    public int getBodyLen() {
        return bodyLen & MAX_BODY_LEN;
    }

    public void setBodyLen(int bodyLen) {
        this.bodyLen = bodyLen;
    }
    
    public void addParam(String key, String value) {
        if(this.params == null) params = new HashMap<String, Param>();
        
        Param param = new Param(key, value);
        if(param.getLen() > MAX_PARAMS_LEN) {
            throw new IllegalArgumentException("Message params is too long 4 encoding!!!");
        }
        
        Param old = params.put(key, param);
        if(old != null)
            paramsLen -= old.getLen();
        
        paramsLen += param.getLen();
    }

    @Override
    public String getParam(String key) {
        if(params == null) return null;
        Param param = params.get(key);
        return param == null ? null : param.val;
    }
    
    public static int readableBytes(ByteBuf buff) {
        int readableBytes = buff.readableBytes();
        if (readableBytes < HDR_SIZE)//header size
            return -1;
        int offset = buff.readerIndex();
        int plen = buff.getShort(offset + HDR_SIZE - 6);
        int blen = buff.getInt  (offset + HDR_SIZE - 4);
        int rlen = plen + blen + HDR_SIZE;
        //header + param + body size
        if (readableBytes < rlen) {
            return -1;
        }
        return rlen;
    }
    public static Message readFrom(ByteBuf buff) {
        Message message = Message.empty();
        message.readHeader(buff);
        message.readParams(buff);
        message.readBody(buff);
        return message;
    }
    
    protected void readHeader(ByteBuf buff) {
        this.flag = buff.readShort();
        this.code = buff.readInt();
        this.id = buff.readLong();
        this.version = buff.readInt();
        this.paramsLen = buff.readShort();
        this.bodyLen = buff.readInt();
    }
    
    protected void readParams(ByteBuf buff) {
        short paramsLen = this.getParamsLen();
        if(paramsLen == 0) return;
        if(params == null) params = new HashMap<String, Param>();
        
        short readBytes = 0;
        while(readBytes < paramsLen) {
            byte[] key = readBytes(buff, buff.readShort());
            byte[] val = readBytes(buff, buff.readShort());
            
            Param param = new Param(key, val);
            params.put(param.key, param);
            
            readBytes += param.getLen();
        }
        if(readBytes != paramsLen) {
            //TODO Decode wrong
        }
    }

    protected byte[] readBytes(ByteBuf buff, short len) {
        byte[] bytes = new byte[len];
        buff.readBytes(bytes);
        return bytes;
    }
    
    protected void readBody(ByteBuf buff) {
        int len = this.getBodyLen();
        if (len > 0) {
            byte[] bytes = new byte[len];
            buff.readBytes(bytes);
            this.body = bytes;
        }
    }

    public void writeTo(ByteBuf buff) {
        writeHeader(buff);
        writeParams(buff);
        writeBody(buff);
    }
    
    protected void writeHeader(ByteBuf buff) {
        writeHeader0(buff, this.id, this.code);
    }
    protected void writeHeader0(ByteBuf buff, long id, int code) {
        buff.writeShort(flag);
        buff.writeInt(code);
        buff.writeLong(id);
        
        buff.writeInt(this.version);
        buff.writeShort(this.paramsLen);
        buff.writeInt(this.bodyLen);
    }

    protected void writeParams(ByteBuf buff) {
        if(this.paramsLen == 0) return;
            
        for (Param param : params.values()) {
            buff.writeShort(param.keyBytes.length);
            buff.writeBytes(param.keyBytes);
            
            buff.writeShort(param.valBytes.length);
            buff.writeBytes(param.valBytes);
        }
    }
    
    protected void writeBody(ByteBuf buff) {
        if (this.getBodyLen() > 0) {
            buff.writeBytes(this.body);
        }
    }
    
    public int getLen() {
        return HDR_SIZE + getParamsLen() + getBodyLen();
    }

    public static Message empty() {
        return new Message();
    }
    public static Message of(int code) {
        Message message = empty();
        message.setCode(code);
        return message;
    }
    public static Message of(int code, long id) {
        Message message = of(code);
        message.setId(id);
        return message;
    }
    public static Message of(int code, MessageLite lite) {
        return of(code, lite==null ? null : lite.toByteArray());
    }
    public static Message of(int code, byte[] bytes) {
        Message message = of(code);
        if (bytes != null) {
            if(bytes.length > MAX_BODY_LEN) {
                throw new IllegalArgumentException("Message body is too long 4 encoding!!!");
            }
            message.setBodyLen(bytes.length);
            message.setBody(bytes);
        }
        return message;
    }

    public IMessage copy(long id) {
        return new Duplicated(this, id, this.code);
    }
    public IMessage copy(long id, int code) {
        return new Duplicated(this, id, code);
    }
    public IMessage copyViaCode(int code) {
        return new Duplicated(this, this.id, code);
    }
    
    private static class Param {
        public final byte[] keyBytes;
        public final String key;
        
        public final byte[] valBytes;
        public final String val;
        public Param(String key, String value) {
            this.key = key;
            this.keyBytes = XStrings.getBytesUtf8(key);
            this.val = value;
            this.valBytes = XStrings.getBytesUtf8(value);
        }
        public Param(byte[] keyBytes, byte[] valBytes) {
            this.key = XStrings.newStringUtf8(keyBytes);
            this.keyBytes = keyBytes;
            
            this.val = XStrings.newStringUtf8(valBytes);
            this.valBytes = valBytes;
        }
        public short getLen() {
            return (short) (2 + keyBytes.length + 2 + valBytes.length);
        }
        @Override
        public String toString() {
            return val;
        }
    }

    private static class Duplicated implements IMessage, WritableMessage {
    	private long id;
    	private int code;
    	private Message msg;
        public Duplicated(Message msg, long id, int code) {
            this.id = id;
            this.code = code;
            this.msg = msg;
        }
        @Override
        public short getFlag() {
            return msg.getFlag();
        }
        @Override
        public int getCode() {
            return this.code;
        }
        @Override
        public long getId() {
            return this.id;
        }
        @Override
        @SuppressWarnings("unchecked")
        public byte[] getBody() {
            return msg.getBody();
        }
        @Override
        public String getParam(String key) {
            return msg.getParam(key);
        }
        @Override
        public int getVersion() {
            return msg.getVersion();
        }
        @Override
        public void writeTo(ByteBuf buff) {
            msg.writeHeader0(buff, getId(), getCode());
            msg.writeParams(buff);
            msg.writeBody(buff);
        }
    }

}
