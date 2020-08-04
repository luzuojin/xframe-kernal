package dev.xframe.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import sun.misc.Unsafe;

public interface XAccessor {
	
	Object get(Object obj);
	
	void set(Object obj, Object val);
	
	public static XAccessor of(Class<?> claz, String name) {
		return of(XReflection.getField(claz, name));
	}
	
	public static XAccessor of(Field field) {
		try {
			return new UnsafeBased(field);
		} catch (Throwable ignored) {}
		return new FieldBased(field);
	}
	
	static class UnsafeBased implements XAccessor {
		private static final Unsafe UNSAFE = getUnsafe();
		private static Unsafe getUnsafe() {
			Unsafe inst;
			try {
				final Field field = Unsafe.class.getDeclaredField("theUnsafe");
				field.setAccessible(true);
				inst = (Unsafe) field.get(null);
			} catch (Exception ignored) {
				try {
					Constructor<Unsafe> c = Unsafe.class.getDeclaredConstructor();
					c.setAccessible(true);
					inst = c.newInstance();
				} catch (Exception e) {
					inst = null;
				}
			}
			return inst;
		}
		
		private final long offset;
		UnsafeBased(Field field) {
			offset = UNSAFE.objectFieldOffset(field);
		}
		public void set(Object obj, Object val) {
			UNSAFE.putObject(obj, offset, val);
		}
		public Object get(Object obj) {
			return UNSAFE.getObject(obj, offset);
		}
	}
	
	static class FieldBased implements XAccessor {
		private final Field field;
		FieldBased(Field field) {
			field.setAccessible(true);
			this.field = field;
		}
		public void set(Object obj, Object val) {
			try {
				field.set(obj, val);
			} catch (Exception e) {
				XCaught.throwException(e);
			}
		}
		public Object get(Object obj) {
			try {
				return field.get(obj);
			} catch (Exception e) {
				return XCaught.throwException(e);
			}
		}
	}

}
