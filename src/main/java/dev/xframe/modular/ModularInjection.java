package dev.xframe.modular;

import java.lang.reflect.Field;

import javax.annotation.Resource;

import dev.xframe.injection.Injection;
import dev.xframe.injection.Injector;
import dev.xframe.injection.Injection.BeanContainer;
import dev.xframe.injection.Injector.FieldInjector;

public class ModularInjection {

    public static @Resource Injector build(Class<?> clazz) {
        return Injection.build(clazz, ModularInjection::build);
    }
    public static @Resource Object inject(Object bean, Injector injector, ModuleContainer mc) {
        return Injection.inject(bean, injector, beanContainer(mc));
    }

    static BeanContainer beanContainer(ModuleContainer mc) {
        return new ModularBeanContainer(mc);
    }

    static FieldInjector build(Field field) {
        if (field.isAnnotationPresent(ModularInject.class)) {
            return new ModularInjector(field);
        }
        return Injector.build(field);
    }
    
    static final ModuleTypeLoader mcLoader = new ModuleTypeLoader() {
        @SuppressWarnings("unchecked")
        public <T> T load(ModuleContainer container) {
            return (T) container;
        }
    };
    
    static class ModularInjector extends FieldInjector {
        final ModuleTypeLoader loader;
        public ModularInjector(Field field) {
            super(field);
            this.checkLegal();
            this.loader = getLoader(field.getType());
        }
        ModuleTypeLoader getLoader(Class<?> c) {
            return ModuleContainer.class.isAssignableFrom(c) ? mcLoader  : ModularEnigne.getLoader(c);
        }
        void checkLegal() {
            if(ModuleContainer.class.isAssignableFrom(type) ||
                    type.isAnnotationPresent(Component.class) ||
                    type.isInterface() ||
                    declaringIsComponentAndPackageMatch()
                    ) {
                return;
            }
            throw new IllegalArgumentException("Modular inject can`t be module impelements");
        }
        boolean declaringIsComponentAndPackageMatch() {
            return field.getDeclaringClass().isAnnotationPresent(Component.class) && field.getDeclaringClass().getPackage().getName().startsWith(type.getPackage().getName());
        }
        @Override
        protected Object get(BeanContainer bc) throws Exception {
            return ((ModularBeanContainer) bc).getModule(loader);
        }
    }
    
    static class ModularBeanContainer extends BeanContainer {
        private final ModuleContainer mc;
        public ModularBeanContainer(final ModuleContainer mc) {
            this.mc = mc;
        }
        public Object getModule(ModuleTypeLoader loader) {
            return loader.load(mc);
        }
    }

}
