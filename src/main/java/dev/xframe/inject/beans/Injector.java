package dev.xframe.inject.beans;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import dev.xframe.inject.Inject;
import dev.xframe.utils.XCaught;
import dev.xframe.utils.XStrings;

public class Injector {
	
	public static final Injector NIL = new Injector(Object.class, new Member[0]);
	
	private final Class<?> type;
	private final Member[] mebmers;

	public Injector(Class<?> clazz, Member[] members) {
		this.type = clazz;
		this.mebmers = members;
	}
	
	public Class<?> getType() {
		return type;
	}
	public Member[] getMebmers() {
		return mebmers;
	}

	@Override
	public String toString() {
		return "Injector [" + type + "]";
	}

	public void inject(Object bean, BeanDefiner definer) {
		try {
			for (Member member : mebmers) {
				member.inject(bean, definer);
			}
		} catch (Exception e) {
			XCaught.throwException(e);
		}
	}

	public static Injector of(Class<?> clazz, BeanIndexing indexing) {
		return of(clazz, indexing, true);
	}
	
	public static Injector of(Class<?> clazz, BeanIndexing indexing, boolean upward) {
		List<Member> members = new ArrayList<>();
		Class<?> t = clazz;
		do {
			Field[] fields = t.getDeclaredFields();
			for (Field field : fields) {
				if(field.isAnnotationPresent(Inject.class)) {
					members.add(new Member(field, indexing));
				}
			}
			t = t.getSuperclass();
		} while(upward && !Object.class.equals(t));
		return new Injector(clazz, members.stream().toArray(Member[]::new));
	}

	public static class Member {
		private Field field;
		private int index;
		private BeanIndexing indexing;
		public Member(Field field, BeanIndexing indexing) {
			field.setAccessible(true);
			this.field = field;
			this.indexing = indexing;
			this.index = -1;
		}
		private boolean isNullable() {
			return field.getAnnotation(Inject.class).nullable();
		}
		private Object getKeyword() {
			Inject an = field.getAnnotation(Inject.class);
			return XStrings.isEmpty(an.value()) ? field.getType() : an.value();
		}
		public Field getField() {
			return field;
		}
		public int getIndex() {
			if(index == -1) {
				index = indexing.indexOf(getKeyword());
			}
			return index;
		}
		public void inject(Object bean, BeanDefiner definer) throws Exception {
			Object obj = definer.define(getIndex());
			if(obj == null && !isNullable()) {
				throw new IllegalArgumentException("Bean [" + field.getType().getName() + "] doesn`t registed");
			}
			field.set(bean, obj);
		}
	}

}
