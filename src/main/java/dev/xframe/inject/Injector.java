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
            return XCaught.throwException(e);
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
        protected final Field field;
        protected final Class<?> type;
        protected final boolean nullable;
        protected final boolean isLazy;
        
        public FieldInjector(Field field) {
            this.field = field;
            this.field.setAccessible(true);
            this.type = field.getType();
            this.nullable = field.getAnnotation(Inject.class).nullable();
            this.isLazy = field.getAnnotation(Inject.class).lazy();
        }
        
        public final void inject(Object bean, BeanContainer bc) throws Exception {
            Object obj = get(bc);
            if(obj == null && !nullable)
                throw new IllegalArgumentException(type.getName() + " doesn`t loaded");
            field.set(bean, obj);
        }
        
        protected Object fetch(BeanContainer bc) {
            return bc.get(type);
        }
        
        private Object cache;
        private Object lazing(BeanContainer bc) {
            return nullable ? null : ProxyBuilder.buildBySupplier(type, ()->fetch(bc));
        }
        protected final Object get(BeanContainer bc) {
            if(cache == null && (cache = fetch(bc)) == null)
            	cache = lazing(bc);
            return cache;
        }
        @Override
        public String toString() {
            return field.getType().getName() + " " + field.getName() + (isLazy ? " lazy" : "");
        }
    }
    
    static class NormalInjector extends FieldInjector {
        public NormalInjector(Field field) {
            super(field);
        }
        protected Object fetch(BeanContainer bc) {
            return bc.get(type);
        }
    }
    
    static class NamedInjector extends FieldInjector {
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