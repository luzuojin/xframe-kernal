package dev.xframe.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import dev.xframe.net.codec.IMessage;
import dev.xframe.net.codec.Message;
import dev.xframe.net.codec.MessageCrypts;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

public class MessageCryptTest extends MessageCrypts.SimpleCryption {
	
	public MessageCryptTest() {
	}
	
	private Map<AttributeKey<byte[]>, byte[]> ciphers = new HashMap<>();
	
	@Override
	protected byte[] getCipher(ChannelHandlerContext ctx, AttributeKey<byte[]> key) {
		byte[] cipher = ciphers.get(key);
		if(cipher == null) {
			cipher = initCipher();
			ciphers.put(key, cipher);
		}
		return cipher;
	}

	@Test
	public void test() {
		byte[] bytes = new byte[]{1,1,0,1,1,4,1,1,9};
        Message message = Message.of(10086, bytes);
        message.addParam("key", "val");
        ByteBuf buf = Unpooled.buffer();
		message.writeTo(buf);
		
		//skip first
		encrypt(null, buf);
		
		decrypt(null, buf, buf.readableBytes());
		
		IMessage xmessage = Message.readFrom(buf);
		
		Assert.assertEquals(xmessage.getFlag(), Message.HDR_FLAG);
		Assert.assertEquals(xmessage.getParam("key"), "val");
		Assert.assertArrayEquals(bytes, xmessage.getBody());
	}
	

}
