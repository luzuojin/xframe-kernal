package dev.xframe.inject.beans;

import dev.xframe.inject.Component;
import dev.xframe.inject.Composite;
import dev.xframe.inject.Configurator;
import dev.xframe.inject.Eventual;
import dev.xframe.inject.Ordered;
import dev.xframe.inject.Ordered.Collection;
import dev.xframe.inject.Prototype;
import dev.xframe.inject.Reloadable;
import dev.xframe.inject.Repository;
import dev.xframe.inject.code.Clazz;
import dev.xframe.inject.code.CompositeBuilder;
import dev.xframe.inject.code.Factory;
import dev.xframe.inject.code.FactoryBuilder;
import dev.xframe.inject.code.ProxyBuilder;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 
 * 正常BeanContainer的构建应该先构建完整的BeanIndexes
 * 然后BeanContainer负责完成Bean的加载...
 * GlobalBean为了支持加载Bean的过程中也可以动态的扩展BeanIndexes, 所以BeanIndexes的构建随着BeanContainer的构建一起进行
 * 
 * @author luzj
 *
 */
public class GlobalContainer extends BeanContainer implements BeanProvider, BeanIndexing {
	
	public GlobalContainer() {
		super(new BeanIndexes());
		this.regist(BeanBinder.instanced(this, getInterfaces()));
		this.regist(BeanBinder.instanced(new BeanRegistrator(indexes)));
	}
	
	public void initial(List<Clazz> scanned) {
        this.registBeans(scanned);
        this.integrate(); //integrate beans #.load()
        this.getBean(Eventual.class).eventuate();//execute Eventuals
    }

    private void registBeans(List<Clazz> clazzes) {
        BeanIndexes indexes = this.indexes();
        registFactories(clazzes, indexes);
        registAnnotated(clazzes, indexes);
        registDiscovery(clazzes, indexes);
    }
    private void registFactories(List<Clazz> scanned, BeanIndexes reg) {
        Predicate<Clazz> isFactory = c->c.isInterface()&&c.isAnnotationPresent(Factory.class);
        scanned.stream().filter(isFactory).map(Clazz::toClass).forEach(c->reg.regist(BeanBinder.instanced(FactoryBuilder.build(c, scanned), c)));
    }
    @SuppressWarnings("unchecked")
    private void registAnnotated(List<Clazz> scanned, BeanIndexes reg) {
        Collection<Class<?>> components = scanned.stream().filter(c->c.isAnnotation()&&c.isAnnotationPresent(Component.class)).map(Clazz::toClass).collect(Ordered.Collection::new, Ordered.Collection::add, (r1, r2)->{});
        Class<? extends Annotation>[] annCls = Stream.concat(Stream.of(Prototype.class, Composite.class, Configurator.class, Repository.class), components.stream()).toArray(Class[]::new);
        BeanPretreater.Annotated anns = new BeanPretreater.Annotated(annCls);
        //@Repository可继承,只处理实现类(接口/父类不处理)
        Predicate<Clazz> isBeanClass = c->anns.isPresient(c) && (!c.isAnnotationPresent(Repository.class) || c.isImplementation());
        Predicate<Class<?>> isPrototype = c->c.isAnnotationPresent(Prototype.class);
        new BeanPretreater(Clazz.filter(scanned, isBeanClass)).pretreat(BeanPretreater.comparator(anns, Composite.class), isPrototype).forEach(c->reg.regist(newBinder(c)));
    }
    private void registDiscovery(List<Clazz> scanned, BeanIndexes reg) {
        this.integrate(reg.getBinder(BeanDiscovery.class));   //提前组装完成
        this.integrate(reg.getBinder(BeanRegistrator.class));
        this.getBean(BeanDiscovery.class).discover(scanned, this.getBean(BeanRegistrator.class));
    }
    
    private BeanBinder newBinder(Class<?> c) {
        if(c.isAnnotationPresent(Composite.class)) {
            return new CompositeBinder(CompositeBuilder.buildBean(c), c);
        }
        //自身有@Reloadable标识或者有对应的annotation有标识
        if(c.isAnnotationPresent(Reloadable.class) ||
                Arrays.stream(c.getAnnotations()).map(Annotation::annotationType).anyMatch(cls->cls.isAnnotationPresent(Reloadable.class))) {
            return new ReloadableBinder(c, Injector.of(c, this));
        }
        return BeanBinder.classic(c, Injector.of(c, this));
    }
    
    public static class CompositeBinder extends BeanBinder.Instanced {
        public CompositeBinder(Object val, Class<?>... keys) {
            super(val, keys);
        }
        List<BeanBinder> impls = new ArrayList<>(); 
        protected void integrate(Object bean, BeanFetcher fetcher) {//append all implements
            impls.forEach(impl->CompositeBuilder.append(bean, fetcher.fetch(impl.getIndex())));
        }
        protected BeanBinder conflict(Object keyword, BeanBinder binder) {
            impls.add(binder);
            return this;
        }
    }
    
    public static class ReloadableBinder extends BeanBinder.Classic {
        public ReloadableBinder(Class<?> master, Injector injector) {
            super(master, injector);
        }
        protected void integrate(Object bean, BeanFetcher fetcher) {
            super.integrate(ProxyBuilder.getDelegate(bean), fetcher);
        }
        protected Object newInstance() {
            return ProxyBuilder.build(master, super.newInstance());
        }
        public Class<?> baseClass() {
            return master;
        }
    }
	
	@Override
	public BeanBinder indexOf0(Object keyword) {
		return indexes.indexOf0(keyword);
	}

	private Class<?>[] getInterfaces() {
		return Stream.concat(Arrays.stream(GlobalContainer.class.getInterfaces()), Arrays.stream(BeanContainer.class.getInterfaces())).toArray(Class[]::new);
	}
	
    @Override
	public <T> T getBean(Class<T> clazz) {
		return getBean(indexOf(clazz));
	}
    @Override
	public <T> T getBean(String name) {
		return getBean(indexOf(name));
	}
    
    public synchronized void regist(BeanBinder binder) {
        if(binder.getIndex() == -1) {//registed
            indexes.regist(binder);
        }
    }

}
