package dev.xframe.inject;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import dev.xframe.inject.beans.BeanBinder;
import dev.xframe.inject.beans.BeanContext;
import dev.xframe.inject.beans.BeanDefiner;
import dev.xframe.inject.beans.BeanHelper;
import dev.xframe.inject.beans.BeanIndexing;
import dev.xframe.inject.beans.BeanPretreater;
import dev.xframe.inject.beans.Injector;
import dev.xframe.inject.code.Codes;
import dev.xframe.inject.code.Factory;
import dev.xframe.inject.code.FactoryBuilder;
import dev.xframe.inject.code.ProxyBuilder;
import dev.xframe.inject.code.ProxyBuilder.IProxy;
import dev.xframe.inject.code.SyntheticBuilder;

public class ApplicationContext {
	
	final static BeanContext bc = new BeanContext();
	
	public static void initialize(String includes, String excludes) {
		initialize(Codes.getClasses(includes, excludes));
	}
	
	private static void initialize(List<Class<?>> classes) {
		loadFactories(classes);
		loadBeans(classes);
		//完成load操作
		integrateBeans();
		doEventuate();//execute Eventuals
	}
	
	private static void integrateBeans() {
		bc.integrate();
	}
	
	private static void doEventuate() {
		bc.getBean(Eventual.class).eventuate();
	}

	private static void loadFactories(List<Class<?>> classes) {
		classes.stream().filter(isFactory()).forEach(c->bc.registBinder(BeanBinder.instanced(FactoryBuilder.build(c, classes), c)));
	}
	
	private static Predicate<Class<?>> isFactory() {
		return c -> c.isInterface() && c.isAnnotationPresent(Factory.class);
	}
	
	private static void loadBeans(List<Class<?>> classes) {
		new BeanPretreater(classes).filter(isAnnotated()).pretreat(annoComparator(), isPrototype()).forEach(registBinder());
	}

	private static Consumer<Class<?>> registBinder() {
		return c -> bc.registBinder(newBinder(c));
	}
	
    private static Class<? extends Annotation>[] annos = new Class[] {
    		Prototype.class,
    		Synthetic.class,
    		Configurator.class,
    		Repository.class,
    		Templates.class,
    		Bean.class
    		};
    
    private static Predicate<Class<?>> isAnnotated() {
    	return c -> Arrays.stream(annos).filter(a->c.isAnnotationPresent(a)).findAny().isPresent();
    }
    
    private static int annoOrder(Class<?> c) {
        for (int i = 0; i < annos.length; i++) {
            if(c.isAnnotationPresent(annos[i])) {
                return annos.length - i;
            }
        }
        return 0;
    }
    
    private static Comparator<Class<?>> annoComparator() {
    	return (c1, c2) -> Integer.compare(annoOrder(c2), annoOrder(c1));
    }
    
	private static Predicate<Class<?>> isPrototype() {
		return c -> c.isAnnotationPresent(Prototype.class);
	}
	
	private static BeanBinder newBinder(Class<?> c) {
		if(c.isAnnotationPresent(Synthetic.class)) {
			return new BeanBinder.Instanced(SyntheticBuilder.buildBean(c), c) {
				List<BeanBinder> impls = new ArrayList<>(); 
				protected void integrate(Object bean, BeanDefiner definer) {//append all implements
					impls.forEach(impl->SyntheticBuilder.append(bean, definer.define(impl.getIndex())));
				}
				protected BeanBinder conflict(Object keyword, BeanBinder binder) {
					assert keyword instanceof Class;
					assert ((Class<?>) keyword).isAnnotationPresent(Synthetic.class);
					impls.add(binder);
					return this;
				}
			};
		}
		if(c.isAnnotationPresent(Templates.class) || c.isAnnotationPresent(Reloadable.class)) {
			return new ReloadableBinder(c, Injector.of(c, bc));
		}
		return new BeanBinder.Classic(c, Injector.of(c, bc));
	}
	
	static class ReloadableBinder extends BeanBinder.Classic {
		public ReloadableBinder(Class<?> master, Injector injector) {
			super(master, injector);
		}
		protected void integrate(Object bean, BeanDefiner definer) {
			super.integrate(ProxyBuilder.getDelegate(bean), definer);
		}
		protected Object newInstance() {
			return ProxyBuilder.build(master, super.newInstance());
		}
		public Class<?> baseClass() {
			return master;
		}
	}
	
	public static void reload(Predicate<Class<?>> preficate) {
		bc.binders().stream()
			.filter(b->(b instanceof ReloadableBinder))
			.map(b->((ReloadableBinder) b).baseClass())
			.filter(preficate)
			.forEach(c->reload(c));
	}
	
	/**
     * 重新执行load(替换delegate, 线程安全)
     */
	public static void reload(Class<?> clazz) {
        Object obj = bc.getBean(clazz);
        if(obj != null && obj instanceof IProxy) {
            Object delegate = ProxyBuilder.getDelegate(obj);
            if(delegate instanceof IProxy) {
                ProxyBuilder.setDelegate(delegate, BeanHelper.inject(ProxyBuilder.getDelegate(delegate).getClass()));
            } else {
                ProxyBuilder.setDelegate(obj, BeanHelper.inject(delegate.getClass()));
            }
        }
    }
	
    /**
     * 使用新的class文件替换原有class
     */
    public static void replace(Class<?> clazz, Class<?> newClazz) {
        Object obj = bc.getBean(clazz);
        if(obj != null && obj instanceof IProxy) {
            ProxyBuilder.setDelegate(obj, ProxyBuilder.build(clazz, BeanHelper.inject(newClazz)));
        }
    }
	
    public static BeanDefiner definer() {
    	return bc;
    }
    
    public static BeanIndexing indexing() {
    	return bc;
    }
    
	public static <T> T fetchBean(Class<T> c) {
		return bc.getBean(c);
	}
	
	public static void registBean(BeanBinder binder) {
	    bc.regist(binder);;
	}
	
}
