package dev.xframe.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Comparator;
import java.util.PriorityQueue;


/**
 * 用来标识相关Bean的处理顺序
 * @see Ordered.Collection
 * @author luzj
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface Ordered {
	
	int value() default 0;
	
	/**
	 * 从大到小
	 */
	@SuppressWarnings("serial")
    class Collection<E> extends PriorityQueue<E>{
		public Collection() {
			super(orderedComparator());
		}
		static Comparator<Object> orderedComparator() {
			return (o1, o2) -> Integer.compare(getOrderedValue(o2), getOrderedValue(o1));
		}
		static int getOrderedValue(Object o) {
			Class<?> cls = (o instanceof Class) ? ((Class<?>) o) : o.getClass();
            Ordered v = cls.getAnnotation(Ordered.class);
			return v == null ? 0 : v.value();
		}
	}
	
}
