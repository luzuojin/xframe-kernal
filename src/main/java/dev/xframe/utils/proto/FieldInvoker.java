package dev.xframe.utils.proto;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dev.xframe.utils.XReflection;

public interface FieldInvoker {
	
	public Object get(Object obj);
	
	public void set(Object obj, Object val);

	public static FieldInvoker of(Field f) {
		return new PojoFieldBased(f);
	}
	public static FieldInvoker of(int index) {
	    return new ArrayElementBased(index);
	}
	
	/**
	 * 
	 * holder --> Pojo
	 */
	static final class PojoFieldBased implements FieldInvoker {
		final Field field;
		PojoFieldBased(Field field) {
			this.field = field;
		}
		public void set(Object obj, Object val) {
			XReflection.invoke(field, obj, val);
		}
		public Object get(Object obj) {
			return XReflection.invoke(field, obj);
		}
	}
	
	/**
	 * holder --> Object[]
	 */
	static final class ArrayElementBased implements FieldInvoker {
		final int index;
		public ArrayElementBased(int index) {
			this.index = index;
		}
		public Object get(Object obj) {
			return Array.get(obj, index);
		}
		public void set(Object obj, Object val) {
			Array.set(obj, index, val);
		}
	}
	
}
