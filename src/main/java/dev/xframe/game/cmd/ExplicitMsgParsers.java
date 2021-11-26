package dev.xframe.game.cmd;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.protobuf.MessageLite;

import dev.xframe.inject.Bean;
import dev.xframe.net.codec.IMessage;
import dev.xframe.utils.XLambda;

/**
 * IMessage to explicit msg parsers
 * @author luzj
 */
@Bean
public class ExplicitMsgParsers {
    
    static final Map<Predicate<Class<?>>, Function<Class<?>, ExplicitMsgParser>> ParserFuncs = new HashMap<>();
    
    static {
        offer(Void.class::equals, cls -> o -> null);
        offer(IMessage.class::isAssignableFrom, cls -> o -> o);
        offer(MessageLite.class::isAssignableFrom, cls -> explicitProtobuf(cls));
    }
    
    //default use protobuf parse body by MessageLite.parseFrom
    @SuppressWarnings("unchecked")
    private static ExplicitMsgParser explicitProtobuf(Class<?> cls) {
        final Function<byte[], Object> func = XLambda.create(Function.class, cls, "parseFrom", byte[].class);
        return o -> func.apply(o.<byte[]>getBody());
    }
    
    public static void offer(Predicate<Class<?>> predicate, Function<Class<?>, ExplicitMsgParser> func) {
        ParserFuncs.put(predicate, func);
    }
    
    public static ExplicitMsgParser newParser(Class<?> cls) {
        for (Map.Entry<Predicate<Class<?>>, Function<Class<?>, ExplicitMsgParser>> entry : ParserFuncs.entrySet()) {
            if(entry.getKey().test(cls)) {
                return entry.getValue().apply(cls);
            }
        }
        throw new IllegalArgumentException(String.format("Unsupported explicit msg type[%s]", cls));
    }

}
