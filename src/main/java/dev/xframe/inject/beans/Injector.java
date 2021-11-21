package dev.xframe.inject.beans;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import dev.xframe.inject.Inject;
import dev.xframe.utils.XLambda;
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

    public void inject(Object bean, BeanFetcher fetcher) {
        for (Member member : mebmers) {
            member.inject(bean, fetcher);
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
        private Setter setter;
        private BeanIndexing indexing;
        private int index;

        public Member(Field field, BeanIndexing indexing) {
            this.field = field;
            this.setter = XLambda.createBySetter(Setter.class, field);
            this.indexing = indexing;
            this.index = -1;
        }
        private boolean isRequired() {
            return field.getAnnotation(Inject.class).required();
        }
        private Object getKeyword() {
            Inject an = field.getAnnotation(Inject.class);
            return XStrings.isEmpty(an.value()) ? field.getType() : an.value();
        }
        public void set(Object bean, Object val) {
            setter.apply(bean, val);
        }
        public int getIndex() {
            if(index == -1) {//check injectable and cache index
                Object keyword = getKeyword();
                BeanBinder binder = indexing.indexOf0(keyword);
                if(binder != null && !binder.injectable(field)) {
                    throw new IllegalStateException("Bean [" + keyword + "] can`t inject to Field [" + field.getDeclaringClass().getTypeName() + "." + field.getName() + "]");
                }
                index = binder == null ? -1 : binder.getIndex();
            }
            return index;
        }
        public void inject(Object bean, BeanFetcher fetcher) {
            Object obj = fetcher.fetch(getIndex());
            if(obj == null && isRequired()) {
                throw new IllegalStateException("Bean [" + field.getType().getName() + "] doesn`t registed");
            }
            set(bean, obj);
        }
    }
    
    @FunctionalInterface
    public static interface Setter {
        void apply(Object ref, Object val);
    }

}
