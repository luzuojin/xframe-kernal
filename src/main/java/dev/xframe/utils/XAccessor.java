package dev.xframe.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * FieldAccessor
 * 
 * @author luzj
 */
public interface XAccessor {

    Object get(Object obj);
    
    boolean getBoolean(Object obj);
    byte getByte(Object obj);
    char getChar(Object obj);
    short getShort(Object obj);
    int getInt(Object obj);
    long getLong(Object obj);
    float getFloat(Object obj);
    double getDouble(Object obj);

    void set(Object obj, Object val);

    void setBoolean(Object obj, boolean z);
    void setByte(Object obj, byte b);
    void setChar(Object obj, char c);
    void setShort(Object obj, short s);
    void setInt(Object obj, int i);
    void setLong(Object obj, long l);
    void setFloat(Object obj, float f);
    void setDouble(Object obj, double d);

    static abstract class AccessorImpl implements XAccessor {
        protected final Field field;
        protected final long fieldOffset;
        protected final boolean isFinal;
        AccessorImpl(Field field) {
            this.field = field;
            this.fieldOffset = Modifier.isStatic(field.getModifiers()) ? XUnsafe.staticFieldOffset(field) : XUnsafe.objectFieldOffset(field);
            this.isFinal = Modifier.isFinal(field.getModifiers());
        }
        protected void ensureObj(Object o) {
            // NOTE: will throw NullPointerException, as specified, if o is null
            if (!field.getDeclaringClass().isAssignableFrom(o.getClass())) {
                throwSetIllegalArgumentException(o);
            }
        }
        protected String getQualifiedFieldName() {
            return field.getDeclaringClass().getName() + "." + field.getName();
        }
        protected IllegalArgumentException newGetIllegalArgumentException(String type) {
            return new IllegalArgumentException("Attempt to get " + field.getType().getName() + " field \"" + getQualifiedFieldName() + "\" with illegal data type conversion to " + type);
        }
        protected String getSetMessage(String attemptedType, String attemptedValue) {
            String err = "Can not set";
            if (Modifier.isStatic(field.getModifiers()))
                err += " static";
            if (isFinal)
                err += " final";
            err += " " + field.getType().getName() + " field " + getQualifiedFieldName() + " to ";
            if (!attemptedValue.isEmpty()) {
                err += "(" + attemptedType + ")" + attemptedValue;
            } else {
                if (!attemptedType.isEmpty())
                    err += attemptedType;
                else
                    err += "null value";
            }
            return err;
        }
        protected void throwSetIllegalArgumentException(String attemptedType, String attemptedValue) {
            throw new IllegalArgumentException(getSetMessage(attemptedType, attemptedValue));
        }
        protected void throwSetIllegalArgumentException(Object o) {
            throwSetIllegalArgumentException(o != null ? o.getClass().getName() : "", "");
        }
        public boolean getBoolean(Object obj) {
            throw newGetIllegalArgumentException("boolean");
        }
        public byte getByte(Object obj) {
            throw newGetIllegalArgumentException("byte");
        }
        public char getChar(Object obj) {
            throw newGetIllegalArgumentException("char");
        }
        public short getShort(Object obj) {
            throw newGetIllegalArgumentException("short");
        }
        public int getInt(Object obj) {
            throw newGetIllegalArgumentException("int");
        }
        public long getLong(Object obj) {
            throw newGetIllegalArgumentException("long");
        }
        public float getFloat(Object obj) {
            throw newGetIllegalArgumentException("float");
        }
        public double getDouble(Object obj) {
            throw newGetIllegalArgumentException("double");
        }
        public void setBoolean(Object obj, boolean z) {
            throwSetIllegalArgumentException("boolean", Boolean.toString(z));
        }
        public void setByte(Object obj, byte b) {
            throwSetIllegalArgumentException("byte", Byte.toString(b));
        }
        public void setChar(Object obj, char c) {
            throwSetIllegalArgumentException("char", Character.toString(c));
        }
        public void setShort(Object obj, short s) {
            throwSetIllegalArgumentException("short", Short.toString(s));
        }
        public void setInt(Object obj, int i) {
            throwSetIllegalArgumentException("int", Integer.toString(i));
        }
        public void setLong(Object obj, long l) {
            throwSetIllegalArgumentException("long", Long.toString(l));
        }
        public void setFloat(Object obj, float f) {
            throwSetIllegalArgumentException("float", Float.toString(f));
        }
        public void setDouble(Object obj, double d) {
            throwSetIllegalArgumentException("double", Double.toString(d));
        }
    }
    
    static class BooleanAccessor extends AccessorImpl {
        BooleanAccessor(Field field) {
            super(field);
        }
        public Object get(Object obj) {
            return Boolean.valueOf(getBoolean(obj));
        }
        public boolean getBoolean(Object obj) {
            ensureObj(obj);
            return XUnsafe.getBoolean(obj, fieldOffset);
        }
        public void set(Object obj, Object val) {
            if (val == null || !(val instanceof Boolean)) {
                throwSetIllegalArgumentException(val);
            }
            setBoolean(obj, ((Boolean) val).booleanValue());
        }
        public void setBoolean(Object obj, boolean b) {
            ensureObj(obj);
            XUnsafe.putBoolean(obj, fieldOffset, b);
        }
    }
    static class ByteAccessor extends AccessorImpl {
        ByteAccessor(Field field) {
            super(field);
        }
        public Object get(Object obj) {
            return Byte.valueOf(getByte(obj));
        }
        public byte getByte(Object obj) {
            ensureObj(obj);
            return XUnsafe.getByte(obj, fieldOffset);
        }
        public short getShort(Object obj) {
            return getByte(obj);
        }
        public int getInt(Object obj) {
            return getByte(obj);
        }
        public long getLong(Object obj) {
            return getByte(obj);
        }
        public float getFloat(Object obj) {
            return getByte(obj);
        }
        public double getDouble(Object obj) {
            return getByte(obj);
        }
        public void set(Object obj, Object val) {
            if (val == null || !(val instanceof Byte)) {
                throwSetIllegalArgumentException(val);
            }
            setByte(obj, ((Byte) val).byteValue());
        }
        public void setByte(Object obj, byte b) {
            ensureObj(obj);
            XUnsafe.putByte(obj, fieldOffset, b);
        }
    }
    static class CharAccessor extends AccessorImpl {
        CharAccessor(Field field) {
            super(field);
        }
        public Object get(Object obj) {
            return Character.valueOf(getChar(obj));
        }
        public char getChar(Object obj) {
            ensureObj(obj);
            return XUnsafe.getChar(obj, fieldOffset);
        }
        public int getInt(Object obj) {
            return getChar(obj);
        }
        public long getLong(Object obj) {
            return getChar(obj);
        }
        public float getFloat(Object obj) {
            return getChar(obj);
        }
        public double getDouble(Object obj) {
            return getChar(obj);
        }
        public void set(Object obj, Object val) {
            if (val == null || !(val instanceof Character)) {
                throwSetIllegalArgumentException(val);
            }
            setChar(obj, ((Character) val).charValue());
        }
        public void setChar(Object obj, char c) {
            ensureObj(obj);
            XUnsafe.putChar(obj, fieldOffset, c);
        }
    }
    static class ShortAccessor extends AccessorImpl {
        ShortAccessor(Field field) {
            super(field);
        }
        public Object get(Object obj) {
            return Short.valueOf(getShort(obj));
        }
        public short getShort(Object obj) {
            ensureObj(obj);
            return XUnsafe.getShort(obj, fieldOffset);
        }
        public int getInt(Object obj) {
            return getShort(obj);
        }
        public long getLong(Object obj) {
            return getShort(obj);
        }
        public float getFloat(Object obj) {
            return getShort(obj);
        }
        public double getDouble(Object obj) {
            return getShort(obj);
        }
        public void set(Object obj, Object val) {
            if (val == null || !(val instanceof Short)) {
                throwSetIllegalArgumentException(val);
            }
            setShort(obj, ((Short) val).shortValue());
        }
        public void setShort(Object obj, short s) {
            ensureObj(obj);
            XUnsafe.putShort(obj, fieldOffset, s);
        }
    }
    static class IntAccessor extends AccessorImpl {
        IntAccessor(Field field) {
            super(field);
        }
        public Object get(Object obj) {
            return Integer.valueOf(getInt(obj));
        }
        public int getInt(Object obj) {
            ensureObj(obj);
            return XUnsafe.getInt(obj, fieldOffset);
        }
        public long getLong(Object obj) {
            return getInt(obj);
        }
        public float getFloat(Object obj) {
            return getInt(obj);
        }
        public double getDouble(Object obj) {
            return getInt(obj);
        }
        public void set(Object obj, Object val) {
            if (val == null || !(val instanceof Integer)) {
                throwSetIllegalArgumentException(val);
            }
            setInt(obj, ((Integer) val).intValue());
        }
        public void setInt(Object obj, int i) {
            ensureObj(obj);
            XUnsafe.putInt(obj, fieldOffset, i);
        }
    }
    static class LongAccessor extends AccessorImpl {
        LongAccessor(Field field) {
            super(field);
        }
        public Object get(Object obj) {
            return Long.valueOf(getLong(obj));
        }
        public long getLong(Object obj) {
            ensureObj(obj);
            return XUnsafe.getLong(obj, fieldOffset);
        }
        public float getFloat(Object obj) {
            return getLong(obj);
        }
        public double getDouble(Object obj) {
            return getLong(obj);
        }
        public void set(Object obj, Object val) {
            if (val == null || !(val instanceof Long)) {
                throwSetIllegalArgumentException(val);
            }
            setLong(obj, ((Long) val).longValue());
        }
        public void setLong(Object obj, long l) {
            ensureObj(obj);
            XUnsafe.putLong(obj, fieldOffset, l);
        }
    }
    static class FloatAccessor extends AccessorImpl {
        FloatAccessor(Field field) {
            super(field);
        }
        public Object get(Object obj) {
            return Float.valueOf(getFloat(obj));
        }
        public float getFloat(Object obj) {
            ensureObj(obj);
            return XUnsafe.getFloat(obj, fieldOffset);
        }
        public double getDouble(Object obj) {
            return getFloat(obj);
        }
        public void set(Object obj, Object val) {
            if (val == null || !(val instanceof Float)) {
                throwSetIllegalArgumentException(val);
            }
            setFloat(obj, ((Float) val).floatValue());
        }
        public void setFloat(Object obj, float f) {
            ensureObj(obj);
            XUnsafe.putFloat(obj, fieldOffset, f);
        }
    }
    static class DoubleAccessor extends AccessorImpl {
        DoubleAccessor(Field field) {
            super(field);
        }
        public Object get(Object obj) {
            return Double.valueOf(getDouble(obj));
        }
        public double getDouble(Object obj) {
            ensureObj(obj);
            return XUnsafe.getDouble(obj, fieldOffset);
        }
        public void set(Object obj, Object val) {
            if (val == null || !(val instanceof Double)) {
                throwSetIllegalArgumentException(val);
            }
            setDouble(obj, ((Double) val).doubleValue());
        }
        public void setDouble(Object obj, double d) {
            ensureObj(obj);
            XUnsafe.putDouble(obj, fieldOffset, d);
        }
    }
    static class ObjectAccessor extends AccessorImpl {
        ObjectAccessor(Field field) {
            super(field);
        }
        public Object get(Object obj) {
            ensureObj(obj);
            return XUnsafe.getObject(obj, fieldOffset);
        }
        public void set(Object obj, Object val) {
            ensureObj(obj);
            if (val != null && !field.getType().isAssignableFrom(val.getClass())) {
                throwSetIllegalArgumentException(val);
            }
            XUnsafe.putObject(obj, fieldOffset, val);
        }
    }
    
    public static XAccessor of(Field field) {
        Class<?> type = field.getType();
        if(type == boolean.class) return new BooleanAccessor(field);
        if(type == byte.class) return new ByteAccessor(field);
        if(type == char.class) return new CharAccessor(field);
        if(type == short.class) return new ShortAccessor(field);
        if(type == int.class) return new IntAccessor(field);
        if(type == long.class) return new LongAccessor(field);
        if(type == float.class) return new FloatAccessor(field);
        if(type == double.class) return new DoubleAccessor(field);
        return new ObjectAccessor(field);
    }

}
