package dev.xframe.tools;

import java.lang.reflect.Method;

import com.google.protobuf.MessageLite;

/**
 * 
 * Message lite parser
 * @author luzj
 *
 */
public class LiteParser {
	
	final Method method;
	public LiteParser(Class<?> clazz, Class<?> gerenic) {
		try {
			Class<?> type = Generic.parse(clazz, gerenic).getByType(MessageLite.class);
			method = type.getMethod("parseFrom", byte[].class);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T parse(byte[] bytes) throws Exception {
		return (T) method.invoke(null, bytes);
	}

}
