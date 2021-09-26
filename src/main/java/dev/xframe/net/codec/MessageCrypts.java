package dev.xframe.net.codec;

import dev.xframe.utils.XProperties;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

public final class MessageCrypts {
    
    public static final String CRYPT_SYS_OP_KEY = "xframe.message.strict";
    
    public static final MessageCrypt SIMPLE = new SimpleCryption();
    
    public static final MessageCrypt   NONE = new MessageCrypt() {
        public void encrypt(ChannelHandlerContext ctx, ByteBuf message) {}
        public void decrypt(ChannelHandlerContext ctx, ByteBuf message, int readableBytes) {}
    };
    
    public static MessageCrypt fromSysOps() {
        return XProperties.getAsBool(CRYPT_SYS_OP_KEY, false) ? SIMPLE : NONE;
    }
    //只支持Message
    public static class SimpleCryption implements MessageCrypt {
        private AttributeKey<byte[]> ENCRYPT_CIPHER = AttributeKey.valueOf("ENCRYPTION");
        private AttributeKey<byte[]> DECRYPT_CIPHER = AttributeKey.valueOf("DECRYPTION");
        
        public void encrypt(ChannelHandlerContext ctx, ByteBuf message) {
            crypt0(ctx, message, message.readableBytes(), ENCRYPT_CIPHER);
        }
        public void decrypt(ChannelHandlerContext ctx, ByteBuf message, int readableBytes) {
            crypt0(ctx, message, readableBytes, DECRYPT_CIPHER);
        }
        
        protected void crypt0(ChannelHandlerContext ctx, ByteBuf message, int readableBytes, AttributeKey<byte[]> cipherKey) {
            byte[] cipher = getCipher(ctx, cipherKey);
            //flag
            byteCrypt(message, cipher, 0);
            byteCrypt(message, cipher, 1);
            //code
            byteCrypt(message, cipher, 2);
            byteCrypt(message, cipher, 3);
            byteCrypt(message, cipher, 4);
            byteCrypt(message, cipher, 5);
            //set next cipher
            setCipher(cipher, message, readableBytes);
        }
        protected void byteCrypt(ByteBuf message, byte[] cipher, int offset) {
            int bOffset = message.readerIndex() + offset;
            int crypted = cipher[offset] ^ message.getByte(bOffset);
            message.setByte(bOffset, crypted);
        }
        protected void setCipher(byte[] cipher, ByteBuf message, int readableBytes) {
            int headSize = Message.HDR_SIZE;
            int readable = readableBytes;
            //use body[0,6) or header[0,6)
            int offset = message.readerIndex() + (readable > headSize + 6 ? headSize : 0);
            cipher[0] = message.getByte(offset + 0);
            cipher[1] = message.getByte(offset + 1);
            cipher[2] = message.getByte(offset + 2);
            cipher[3] = message.getByte(offset + 3);
            cipher[4] = message.getByte(offset + 4);
            cipher[5] = message.getByte(offset + 5);
        }
        protected byte[] getCipher(ChannelHandlerContext ctx, AttributeKey<byte[]> key) {
            byte[] cipher = ctx.channel().attr(key).get();
            if(cipher == null) {
                cipher = initCipher();
                ctx.channel().attr(key).set(cipher);
            }
            return cipher;
        }
        protected byte[] initCipher() {
            byte[] cipher = new byte[6];
            for (int i = 0; i < 6; i++) {
                cipher[i] = (byte) i;
            }
            return cipher;
        }
    }

}
