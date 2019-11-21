package dev.xframe.utils;

import java.util.function.Function;

import com.google.protobuf.MessageLite;

/**
 * 
 * MessageLite parser
 * 
 * @author luzj
 *
 */
@SuppressWarnings("unchecked")
public class LiteParser {
	
	final Function<Object, byte[]> func;
	
	public LiteParser(Class<?> clazz, Class<?> gerenic) {
		try {
			func = XLambda.create(Function.class, Generic.parse(clazz, gerenic).getByType(MessageLite.class), "parseFrom", byte[].class);
		} catch (Throwable e) {
			throw XCaught.wrapException(e);
		}
	}
	
	public <T> T parse(byte[] bytes) throws Exception {
		return (T) func.apply(bytes);
	}

}
