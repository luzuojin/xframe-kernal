package dev.xframe.inject.beans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import dev.xframe.inject.Composite;
import dev.xframe.inject.Bean;
import dev.xframe.inject.Templates;
import dev.xframe.inject.Configurator;
import dev.xframe.inject.Eventual;
import dev.xframe.inject.Prototype;
import dev.xframe.inject.Reloadable;
import dev.xframe.inject.Repository;
import dev.xframe.inject.code.CompositeBuilder;
import dev.xframe.inject.code.Factory;
import dev.xframe.inject.code.FactoryBuilder;
import dev.xframe.inject.code.ProxyBuilder;
import dev.xframe.utils.XReflection;

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
	
	public void initial(List<Class<?>> scanned) {
        this.registBeans(scanned);
        this.integrate(); //integrate beans #.load()
        this.getBean(Eventual.class).eventuate();//execute Eventuals
    }

    private void registBeans(List<Class<?>> classes) {
        BeanIndexes indexes = this.indexes();
        registFactories(classes, indexes);
        registAnnotated(classes, indexes);
        registDiscovery(classes, indexes);
    }
    private void registFactories(List<Class<?>> scanned, BeanIndexes reg) {
        Predicate<Class<?>> isFactory = c->c.isInterface()&&c.isAnnotationPresent(Factory.class);
        scanned.stream().filter(isFactory).forEach(c->reg.regist(BeanBinder.instanced(FactoryBuilder.build(c, scanned), c)));
    }
    private void registAnnotated(List<Class<?>> scanned, BeanIndexes reg) {
        @SuppressWarnings("unchecked")
        BeanPretreater.Annotated anns = new BeanPretreater.Annotated(new Class[]{
                Prototype.class,
                Composite.class,
                Configurator.class,
                Repository.class,
                Templates.class,
                Bean.class
        });
        //@Repository可继承,只处理实现类(接口/父类不处理)
        Predicate<Class<?>> isBeanClass = c->anns.isPresient(c) && (!c.isAnnotationPresent(Repository.class) || XReflection.isImplementation(c));
        Predicate<Class<?>> isPrototype = c->c.isAnnotationPresent(Prototype.class);
        new BeanPretreater(scanned).filter(isBeanClass).pretreat(anns.comparator(), isPrototype).forEach(c->reg.regist(newBinder(c)));
    }
    private void registDiscovery(List<Class<?>> scanned, BeanIndexes reg) {
        this.integrate(reg.getBinder(BeanDiscovery.class));   //提前组装完成
        this.integrate(reg.getBinder(BeanRegistrator.class));
        this.getBean(BeanDiscovery.class).discover(scanned, this.getBean(BeanRegistrator.class));
    }
    
    private BeanBinder newBinder(Class<?> c) {
        if(c.isAnnotationPresent(Composite.class)) {
            return new CompositeBinder(CompositeBuilder.buildBean(c), c);
        }
        if(c.isAnnotationPresent(Templates.class) || c.isAnnotationPresent(Reloadable.class)) {
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
