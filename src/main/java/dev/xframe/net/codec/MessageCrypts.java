package dev.xframe.net.codec;

import dev.xframe.utils.XProperties;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

public final class MessageCrypts {
    
    public static final String CRYPT_SYS_OP_KEY = "xframe.message.strict";
    
    public static final MessageCrypt SIMPLE = new SimpleCryption();
    
    public static final MessageCrypt   NONE = new MessageCrypt() {
        public void encrypt(ChannelHandlerContext ctx, IMessage message) {}
        public void decrypt(ChannelHandlerContext ctx, IMessage message) {}
    };
    
    public static MessageCrypt fromSysOps() {
        return XProperties.getAsBool(CRYPT_SYS_OP_KEY, false) ? SIMPLE : NONE;
    }

    //只支持Message
    public static class SimpleCryption implements MessageCrypt {
        @Override
        public void decrypt(ChannelHandlerContext ctx, IMessage message) {
            decrypt(ctx, (BuiltinAbstMessage)message);
        }
        @Override
        public void encrypt(ChannelHandlerContext ctx, IMessage message) {
            encrypt(ctx, (BuiltinAbstMessage)message);
        }

        private AttributeKey<byte[]> ENCRYPT_CIPHER = AttributeKey.valueOf("ENCRYPTION");
        private AttributeKey<byte[]> DECRYPT_CIPHER = AttributeKey.valueOf("DECRYPTION");
        
        public void encrypt(ChannelHandlerContext ctx, BuiltinAbstMessage message) {
            byte[] cipher = getCipher(ctx, ENCRYPT_CIPHER);
            
            short flag = flagCodec(message, cipher);
              int code = codeCodec(message, cipher);
            
            setCipher(cipher, message);
            
            message.setFlag(flag);
            message.setCode(code);
        }
        
        public void decrypt(ChannelHandlerContext ctx, BuiltinAbstMessage message) {
            byte[] cipher = getCipher(ctx, DECRYPT_CIPHER);
            
            short flag = flagCodec(message, cipher);
              int code = codeCodec(message, cipher);
            
            message.setFlag(flag);
            message.setCode(code);
            
            setCipher(cipher, message);
        }

        protected short flagCodec(BuiltinAbstMessage message, byte[] cipher) {
            byte cipher1 = cipher[0];
            byte cipher2 = cipher[1];
            short src = message.getFlag();
            return (short) (src ^ (((cipher1 & 0xFF) << 8) | (cipher2 & 0xFF)) & 0xFFFF);
        }
        
        protected int codeCodec(BuiltinAbstMessage message, byte[] cipher) {
            byte cipher1 = cipher[2];
            byte cipher2 = cipher[3];
            byte cipher3 = cipher[4];
            byte cipher4 = cipher[5];
            int src = message.getCode();
            return (src ^ (((cipher1 & 0xFF) << 24) | ((cipher2 & 0xFF) << 16) | ((cipher3 & 0xFF) << 8) | (cipher4 & 0xFF)) & 0xFFFFFFFF);
        }
        
        protected void setCipher(byte[] cipher, BuiltinAbstMessage message) {
            int bodyLen = message.getBodyLen();
            if(bodyLen > 6) {
                byte[] body = message.getBody();
                cipher[0] = body[bodyLen-1];
                cipher[1] = body[bodyLen-2];
                cipher[2] = body[bodyLen-3];
                cipher[3] = body[bodyLen-4];
                cipher[4] = body[bodyLen-5];
                cipher[5] = body[bodyLen-6];
            } else {
                //flag
                cipher[0] = (byte) ((message.getFlag() >> 8) & 0XFF);
                cipher[1] = (byte) ( message.getFlag() & 0XFF);
                //code
                cipher[2] = (byte) ((message.getCode() >> 24) & 0XFF);
                cipher[3] = (byte) ((message.getCode() >> 16) & 0XFF);
                cipher[4] = (byte) ((message.getCode() >>  8) & 0XFF);
                cipher[5] = (byte) ( message.getCode() & 0XFF);
            }
        }

        protected byte[] getCipher(ChannelHandlerContext ctx, AttributeKey<byte[]> key) {
            byte[] cipher = ctx.channel().attr(key).get();
            if(cipher == null) {
                cipher = initialCipher();
                ctx.channel().attr(key).set(cipher);
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

}
