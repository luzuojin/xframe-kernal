package dev.xframe.net.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

public class MessageCodecs {

    public static final String STRICT_NAME = "message.strict";
    
    protected boolean strict;
    
    public MessageCodecs() {
        this.strict = Boolean.parseBoolean(System.getProperty(STRICT_NAME, "false"));
    }
    
    public MessageDecoder newDecoder() {
        return strict ? newStrictDecoder() : new MessageDecoder();
    }
    private MessageDecoder newStrictDecoder() {
        return new MessageDecoder(){
            protected void decode(ChannelHandlerContext ctx, IMessage message) {
                MessageCodecs.this.decode(ctx, message);
            }
        };
    }
    
    public MessageEncoder newEncoder() {
        return strict ? newStrictEncoder() : new MessageEncoder();
    }
    private MessageEncoder newStrictEncoder() {
        return new MessageEncoder(){
            @Override
            protected void encode(ChannelHandlerContext ctx, IMessage message) {
                MessageCodecs.this.encode(ctx, message);
            }
        };
    }
    
    
    private AttributeKey<byte[]> ENCRYPT_CIPHER = AttributeKey.valueOf("ENCRYPTION");
    private AttributeKey<byte[]> DECRYPT_CIPHER = AttributeKey.valueOf("DECRYPTION");
    
    protected void encode(ChannelHandlerContext ctx, IMessage message) {
        byte[] cipher = getCipher(ctx, ENCRYPT_CIPHER);
        
        short flag = flagCodec(message, cipher);
          int code = codeCodec(message, cipher);
        
        setCipher(cipher, message);
        
        message.setFlag(flag);
        message.setCode(code);
    }
    
    protected void decode(ChannelHandlerContext ctx, IMessage message) {
        byte[] cipher = getCipher(ctx, DECRYPT_CIPHER);
        
        short flag = flagCodec(message, cipher);
          int code = codeCodec(message, cipher);
        
        message.setFlag(flag);
        message.setCode(code);
        
        setCipher(cipher, message);
    }

    protected short flagCodec(IMessage message, byte[] cipher) {
        return (short) codec(message.getFlag(), cipher[0], cipher[1]);
    }
    
    protected int codeCodec(IMessage message, byte[] cipher) {
        return codec(message.getCode(), cipher[2], cipher[3]);
    }
    
    protected int codec(int src, byte cipher1, byte cipher2) {
        return (src ^ (((cipher1 & 0xFF) << 8) | (cipher2 & 0xFF)) & 0xFFFF);
    }

    protected void setCipher(byte[] cipher, IMessage message) {
        int bodyLen = message.getBodyLen();
        if(bodyLen > 4) {
            byte[] body = message.getBody();
            cipher[0] = body[bodyLen-1];
            cipher[1] = body[bodyLen-2];
            cipher[2] = body[bodyLen-3];
            cipher[3] = body[bodyLen-4];
        } else {
            cipher[0] = (byte) (message.getCode() >> 8 & 0XFF);
            cipher[1] = (byte) (message.getCode() & 0XFF);
            cipher[2] = (byte) (message.getFlag() >> 8 & 0XFF);
            cipher[3] = (byte) (message.getFlag() & 0XFF);
        }
    }

    protected byte[] getCipher(ChannelHandlerContext ctx, AttributeKey<byte[]> key) {
        byte[] cipher = ctx.attr(key).get();
        if(cipher == null) {
            cipher = initialCipher();
            ctx.attr(key).set(cipher);
        }
        return cipher;
    }

	protected byte[] initialCipher() {
		byte[] cipher = new byte[4];
		for (int i = 0; i < 4; i++) {
		    cipher[i] = (byte) i;
		}
		return cipher;
	}
    
}
