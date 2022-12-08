package dev.xframe.utils;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public class XOptional {

    public static <T> boolean isEmpty(T val) {
        return val == null;
    }
    public static <T> boolean isPresent(T val) {
        return val != null;
    }

    public static <T> void ifPresent(T val, Consumer<T> action) {
        if(val != null) {
            action.accept(val);
        }
    }
    public static <T> void ifPresentOrElse(T val, Consumer<T> action, Runnable emptyAction) {
        if(val != null) {
            action.accept(val);
        } else {
            emptyAction.run();
        }
    }

    /**
     * @param val
     * @param def
     * @return if val is null def : val
     * @param <T>
     */
    public static <T> T orElse(T val, T def) {
        return val != null ? val : def;
    }
    public static <T> T orElse(T val, Supplier<T> def) {
        return val != null ? val : def.get();
    }
    /**
     * @param val
     * @param filter
     * @param _else
     * @return if filter test is True return val else return _else
     * @param <T>
     */
    public static <T> T orElse(T val, Predicate<T> filter, T _else) {
        return filter.test(val) ? val : _else;
    }
    public static <T> T orElse(T val, Predicate<T> filter, Supplier<T> _else) {
        return filter.test(val) ? val : _else.get();
    }

    public static <T, R> R map(T val, Function<T, R> mapper) {
        return val != null ? mapper.apply(val) : null;
    }
    public static <T, R> R map(T val, Function<T, R> mapper, R def) {
        return val != null ? mapper.apply(val) : def;
    }
    public static <T, R> R map(T val, Function<T, R> mapper, Supplier<R> def) {
        return val != null ? mapper.apply(val) : def.get();
    }
    public static <T, R> R map(T val, Predicate<T> filter, Function<T, R> mapper) {
        return filter.test(val) ? mapper.apply(val) : null;
    }
    public static <T, R> R map(T val, Predicate<T> filter, Function<T, R> mapper, R def) {
        return filter.test(val) ? mapper.apply(val) : def;
    }
    public static <T, R> R map(T val, Predicate<T> filter, Function<T, R> mapper, Supplier<R> def) {
        return filter.test(val) ? mapper.apply(val) : def.get();
    }


    public static <T> int mapToInt(T val, ToIntFunction<T> mapper) {
        return val != null ? mapper.applyAsInt(val) : 0;
    }
    public static <T> int mapToInt(T val, ToIntFunction<T> mapper, int def) {
        return val != null ? mapper.applyAsInt(val) : def;
    }
    public static <T> int mapToInt(T val, ToIntFunction<T> mapper, IntSupplier def) {
        return val != null ? mapper.applyAsInt(val) : def.getAsInt();
    }
    public static <T> int mapToInt(T val, Predicate<T> filter, ToIntFunction<T> mapper) {
        return filter.test(val) ? mapper.applyAsInt(val) : 0;
    }
    public static <T> int mapToInt(T val, Predicate<T> filter, ToIntFunction<T> mapper, int def) {
        return filter.test(val) ? mapper.applyAsInt(val) : def;
    }
    public static <T> int mapToInt(T val, Predicate<T> filter, ToIntFunction<T> mapper, IntSupplier def) {
        return filter.test(val) ? mapper.applyAsInt(val) : def.getAsInt();
    }

    public static <T> long mapToLong(T val, ToLongFunction<T> mapper) {
        return val != null ? mapper.applyAsLong(val) : 0;
    }
    public static <T> long mapToLong(T val, ToLongFunction<T> mapper, long def) {
        return val != null ? mapper.applyAsLong(val) : def;
    }
    public static <T> long mapToLong(T val, ToLongFunction<T> mapper, IntSupplier def) {
        return val != null ? mapper.applyAsLong(val) : def.getAsInt();
    }
    public static <T> long mapToLong(T val, Predicate<T> filter, ToLongFunction<T> mapper) {
        return filter.test(val) ? mapper.applyAsLong(val) : 0;
    }
    public static <T> long mapToLong(T val, Predicate<T> filter, ToLongFunction<T> mapper, long def) {
        return filter.test(val) ? mapper.applyAsLong(val) : def;
    }
    public static <T> long mapToLong(T val, Predicate<T> filter, ToLongFunction<T> mapper, LongSupplier def) {
        return filter.test(val) ? mapper.applyAsLong(val) : def.getAsLong();
    }

    public static <T> boolean mapToBool(T val, Predicate<T> mapper) {
        return val != null ? mapper.test(val) : false;
    }
    public static <T> boolean mapToBool(T val, Predicate<T> mapper, boolean def) {
        return val != null ? mapper.test(val) : def;
    }
    public static <T> boolean mapToBool(T val, Predicate<T> mapper, BooleanSupplier def) {
        return val != null ? mapper.test(val) : def.getAsBoolean();
    }
    public static <T> boolean mapToBool(T val, Predicate<T> filter, Predicate<T> mapper) {
        return filter.test(val) ? mapper.test(val) : false;
    }
    public static <T> boolean mapToBool(T val, Predicate<T> filter, Predicate<T> mapper, boolean def) {
        return filter.test(val) ? mapper.test(val) : def;
    }
    public static <T> boolean mapToBool(T val, Predicate<T> filter, Predicate<T> mapper, BooleanSupplier def) {
        return filter.test(val) ? mapper.test(val) : def.getAsBoolean();
    }

}
