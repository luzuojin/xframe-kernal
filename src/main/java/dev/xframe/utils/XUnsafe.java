package dev.xframe.utils;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;

@SuppressWarnings("restriction")
public class XUnsafe {

    static sun.misc.Unsafe unsafe;
    static Lookup Trusted;
    static {
        try {
            Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafe = (sun.misc.Unsafe) unsafeField.get(null);
            
            Field lookupField = Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Trusted = (Lookup) getObject(staticFieldBase(lookupField), staticFieldOffset(lookupField));
        } catch (Exception e) {
            e.printStackTrace(); //ignore
        }
    }
    
    public static Lookup lookup() {
    	return Trusted;
    }
    
    public static void setBoolean(Object obj, long fieldOffset, boolean val) {
        unsafe.putBoolean(obj, fieldOffset, val);
    }
    public static void setInt(Object obj, long fieldOffset, int val) {
        unsafe.putInt(obj, fieldOffset, val);
    }
    public static void setLong(Object obj, long fieldOffset, long val) {
        unsafe.putLong(obj, fieldOffset, val);
    }
    public static void setShort(Object obj, long fieldOffset, short val) {
        unsafe.putShort(obj, fieldOffset, val);
    }
    public static void setByte(Object obj, long fieldOffset, byte val) {
        unsafe.putByte(obj, fieldOffset, val);
    }
    public static void setChar(Object obj, long fieldOffset, char val) {
        unsafe.putChar(obj, fieldOffset, val);
    }
    public static void setFloat(Object obj, long fieldOffset, float val) {
        unsafe.putFloat(obj, fieldOffset, val);
    }
    public static void setDouble(Object obj, long fieldOffset, double val) {
        unsafe.putDouble(obj, fieldOffset, val);
    }
    public static void setObject(Object obj, long fieldOffset, Object val) {
        unsafe.putObject(obj, fieldOffset, val);
    }
    
    public static boolean getBoolean(Object obj, long fieldOffset) {
        return unsafe.getBoolean(obj, fieldOffset);
    }
    public static int getInt(Object obj, long fieldOffset) {
        return unsafe.getInt(obj, fieldOffset);
    }
    public static long getLong(Object obj, long fieldOffset) {
        return unsafe.getLong(obj, fieldOffset);
    }
    public static short getShort(Object obj, long fieldOffset) {
        return unsafe.getShort(obj, fieldOffset);
    }
    public static byte getByte(Object obj, long fieldOffset) {
        return unsafe.getByte(obj, fieldOffset);
    }
    public static char getChar(Object obj, long fieldOffset) {
        return unsafe.getChar(obj, fieldOffset);
    }
    public static float getFloat(Object obj, long fieldOffset) {
        return unsafe.getFloat(obj, fieldOffset);
    }
    public static double getDouble(Object obj, long fieldOffset) {
        return unsafe.getDouble(obj, fieldOffset);
    }
    public static Object getObject(Object obj, long fieldOffset) {
        return unsafe.getObject(obj, fieldOffset);
    }
    
    public static long getFieldOffset(Field field) {
        return unsafe.objectFieldOffset(field);
    }
    
    public static long staticFieldOffset(Field field) {
    	return unsafe.staticFieldOffset(field);
    }
    public static Object staticFieldBase(Field field) {
    	return unsafe.staticFieldBase(field);
    }
}
