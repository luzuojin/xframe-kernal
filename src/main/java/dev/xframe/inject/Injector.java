package dev.xframe.inject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import dev.xframe.inject.Injection.BeanContainer;
import dev.xframe.inject.code.ProxyBuilder;
import dev.xframe.utils.XCaught;
import dev.xframe.utils.XStrings;

public class Injector {
    
    public final Class<?> master;
    public final FieldInjector[] fields;
    
    public Injector(Class<?> master, FieldInjector[] fields) {
        this.master = master;
        this.fields = fields;
    }
    
    public <T> T inject(T bean, BeanContainer bc) {
        try {
            for (FieldInjector field : fields) {
                field.inject(bean, bc);
            }
            return bean;
        } catch (Exception e) {
            XCaught.throwException(e);
            return bean;
        }
    }
    
    static Injector build(Class<?> clazz, Function<Field, FieldInjector> builder) {
        List<FieldInjector> injectFields = new ArrayList<FieldInjector>();
        Class<?> t = clazz;
        do {
            Field[] fields = t.getDeclaredFields();
            for (Field field : fields) {
                FieldInjector finjector = builder.apply(field);
                if(finjector != null) {
                    injectFields.add(finjector);
                }
            }
            t = t.getSuperclass();
        } while(requireUpwards(t) && !Object.class.equals(t));
        return new Injector(clazz, injectFields.stream().toArray(FieldInjector[]::new));
    }
    
    public static FieldInjector build(Field field) {
        Inject anno = field.getAnnotation(Inject.class);
        return anno == null ? null : newFieldInjector(field, anno.value());
    }
    public static FieldInjector newFieldInjector(Field field, String name) {
        return XStrings.isEmpty(name) ? new NormalInjector(field) : new NamedInjector(field);
    }

    public static boolean requireUpwards(Class<?> t) {
        return !t.isAnnotationPresent(Prototype.class);
    }
    
    @Override
    public String toString() {
        return master.toString();
    }
    
    public static abstract class FieldInjector {
        public final Field field;
        public final Class<?> type;
        public final boolean nullable;
        public final boolean isLazy;
        public FieldInjector(Field field) {
            this.field = field;
            this.field.setAccessible(true);
            this.type = field.getType();
            this.nullable = isNullable();
            this.isLazy = isLazy();
        }
        public final void inject(Object bean, BeanContainer bc) throws Exception {
            Object obj = get(bc);
            if(obj == null && !nullable)
                throw new IllegalArgumentException(type.getName() + " doesn`t loaded");
            field.set(bean, obj);
        }
        protected boolean isNullable() {
            return false;
        }
        protected boolean isLazy() {
            return false;
        }
        protected Object proxy(BeanContainer bc) {
            return nullable ? null : ProxyBuilder.buildBySupplier(type, ()->bc.get(type));
        }
        protected abstract Object get(BeanContainer bc) throws Exception;
    }
    
    static class NormalInjector extends FieldInjector {
        public NormalInjector(Field field) {
            super(field);
        }
        protected Object cache;
        protected final Object get(BeanContainer bc) {
            //优先偿试获取bean, 没有时获取bean proxy
            if(cache == null && (cache = fetch(bc)) == null && (cache = proxy(bc)) == null);
            return cache;
        }
        protected Object fetch(BeanContainer bc) {
            return bc.get(type);
        }
        @Override
        protected boolean isNullable() {
            return field.getAnnotation(Inject.class).nullable();
        }
        @Override
        protected boolean isLazy() {
            return field.getAnnotation(Inject.class).lazy();
        }
        @Override
        public String toString() {
            return field.getType().getName() + " " + field.getName() + (isLazy ? " lazy" : "");
        }
    }
    
    static class NamedInjector extends NormalInjector {
        final String name;
        public NamedInjector(Field field) {
            super(field);
            this.name = field.getAnnotation(Inject.class).value();
        }
        @Override
        protected Object fetch(BeanContainer bc) {
            return bc.get(name);
        }
        @Override
        public String toString() {
            return field.getType().getName() + "@" + this.name + " " + field.getName() + (isLazy ? " lazy" : "");
        }
    }
    
}