package dev.xframe.utils.proto;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@SuppressWarnings({"rawtypes", "unchecked"})
interface RepeatedHandler<T> {
	
	public T make(int len);
	public void add(T c, int index, Object e);
	public void each(T c, Consumer<Object> ec);
	
	static class ArrayHandler implements RepeatedHandler<Object> {
		Class<?> componentType;
		public ArrayHandler(Class<?> componentType) {
			this.componentType = componentType;
		}
		public Object make(int len) {
			return Array.newInstance(componentType, len);
		}
		public void add(Object c, int index, Object e) {
			Array.set(c, index, e);;
		}
		@Override
		public void each(Object c, Consumer<Object> ec) {
			int length = Array.getLength(c);
			for (int i = 0; i < length; i++) {
				ec.accept(Array.get(c, i));
			}
		}
	}
	
	RepeatedHandler<?> LIST = new RepeatedHandler<List>() {
		public List make(int len) {
			return new ArrayList<>(len);
		}
		public void add(List c, int index, Object e) {
			c.add(e);
		}
		public void each(List c, Consumer<Object> ec) {
			for (Object e : c) ec.accept(e);
		}
	};
	
	RepeatedHandler<?> SET = new RepeatedHandler<Set>() {
		public Set make(int len) {
			return new HashSet<>(len*2);
		}
		public void add(Set c, int index, Object e) {
			c.add(e);
		}
		public void each(Set c, Consumer<Object> ec) {
			for (Object e : c) ec.accept(e);
		}
	};
	
	public static RepeatedHandler<?> get4Array(Class<?> c) {
		return new ArrayHandler(c);
	}
	
	public static RepeatedHandler<?> get4Collection(Class<?> t) {
		return List.class.isAssignableFrom(t) ? LIST : SET;
	}
	
}