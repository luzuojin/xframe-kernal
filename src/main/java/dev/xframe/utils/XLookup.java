package dev.xframe.utils;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;

/**
 * Lookup.IMPL_LOOKUP
 */
public class XLookup {
    
    private static final Lookup Trusted = getTrusted();
    private static Lookup getTrusted() {
        try {
            Field lookupField = Lookup.class.getDeclaredField("IMPL_LOOKUP");
            return (Lookup) XUnsafe.getObject(XUnsafe.staticFieldBase(lookupField), XUnsafe.staticFieldOffset(lookupField));
        } catch (Exception e) {
            return null;
        }
    }
    
    public static Lookup lookup() {
        return Trusted;
    }
    public static Lookup in(Class<?> cls) {
        return Trusted.in(cls);
    }

}
