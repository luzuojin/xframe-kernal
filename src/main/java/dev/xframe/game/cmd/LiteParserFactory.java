package dev.xframe.game.cmd;

import java.util.function.Function;

import dev.xframe.inject.Bean;
import dev.xframe.inject.Providable;
import dev.xframe.utils.XCaught;
import dev.xframe.utils.XLambda;

/**
 * 
 * MessageLite parser
 * 
 * @author luzj
 *
 */
@Bean
@Providable
public class LiteParserFactory {
	
    @SuppressWarnings("unchecked")
    public Function<Object, Object> build(Class<?> claz) {
        try {
            //basically use protobuf parse body by MessageLite.parseFrom
            return XLambda.create(Function.class, claz, "parseFrom", byte[].class);
        } catch (Throwable e) {
            throw XCaught.throwException(e);
        }
    }

}
