package dev.xframe.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import dev.xframe.net.codec.IMessage;
import dev.xframe.net.codec.Message;
import dev.xframe.net.codec.MessageCodecs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

public class MessageTest extends MessageCodecs {
	
	public MessageTest() {
		strict = true;
	}
	
	private Map<AttributeKey<byte[]>, byte[]> ciphers = new HashMap<>();
	
	@Override
	protected byte[] getCipher(ChannelHandlerContext ctx, AttributeKey<byte[]> key) {
		byte[] cipher = ciphers.get(key);
		if(cipher == null) {
			cipher = initialCipher();
			ciphers.put(key, cipher);
		}
		return cipher;
	}

	@Test
	public void test() {
		byte[] bytes = new byte[]{1,1,0,1,1,4,1,1,9};
		
		Message message = Message.build(10086, bytes);
		message.addParam("key", "val");
		
		//skip first
		doOnce(Message.copy(message));
		
		Message xmessage = doOnce(Message.copy(message));
		
		Assert.assertEquals(xmessage.getFlag(), Message.HDR_FLAG);
		Assert.assertEquals(xmessage.getParam("key"), "val");
		Assert.assertArrayEquals(bytes, xmessage.getBody());
	}

	private Message doOnce(IMessage smessage) {
		this.encode(null, smessage);
		
		ByteBuf buff = ByteBufAllocator.DEFAULT.heapBuffer();
		smessage.writeHeader(buff);
		smessage.writeParams(buff);
		smessage.writeBody(buff);
		
		Message xmessage = Message.build();
		xmessage.readHeader(buff);
		xmessage.readParams(buff);
		xmessage.readBody(buff);
		this.decode(null, xmessage);
		return xmessage;
	}
	

}
