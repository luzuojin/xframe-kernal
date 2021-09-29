package dev.xframe.game.cmd;

import java.util.function.Function;

import dev.xframe.inject.Bean;
import dev.xframe.inject.Providable;
import dev.xframe.net.codec.IMessage;
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
    public Function<Object, Object> newParseFunc(Class<?> cls) {
        try {
            if(Void.class.equals(cls)) {
                return o -> null;
            }
            if(IMessage.class.isAssignableFrom(cls)) {
                return o -> o;
            }
            //basically use protobuf parse body by MessageLite.parseFrom
            return XLambda.create(Function.class, cls, "parseFrom", byte[].class);
        } catch (Throwable e) {
            throw XCaught.throwException(e);
        }
    }

}
