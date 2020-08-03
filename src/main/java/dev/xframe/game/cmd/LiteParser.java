package dev.xframe.game.cmd;

import java.util.function.Function;

import dev.xframe.inject.Inject;
import dev.xframe.inject.Prototype;
import dev.xframe.utils.XGeneric;

/**
 * 
 * MessageLite parser
 * 
 * @author luzj
 *
 */
@Prototype
public class LiteParser {
	
	@Inject
	private LiteParserFactory factory;
	
	private Function<Object, Object> func;
	
	public LiteParser(Class<?> clazz, Class<?> gerenic, String typeName) {
	    func = factory.build(XGeneric.parse(clazz, gerenic).getByName(typeName));
	}
	
	public <T> T parse(Object body) throws Exception {
		return (T) func.apply(body);
	}

}
