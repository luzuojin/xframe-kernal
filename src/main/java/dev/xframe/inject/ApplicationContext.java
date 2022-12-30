package dev.xframe.inject;

import java.util.function.Predicate;

import dev.xframe.inject.beans.BeanBinder;
import dev.xframe.inject.beans.BeanFetcher;
import dev.xframe.inject.beans.BeanHelper;
import dev.xframe.inject.beans.BeanIndexing;
import dev.xframe.inject.beans.GlobalContainer;
import dev.xframe.inject.beans.GlobalContainer.ReloadableBinder;
import dev.xframe.inject.code.Codes;
import dev.xframe.inject.code.ProxyBuilder;
import dev.xframe.inject.code.ProxyBuilder.IProxy;

public class ApplicationContext {
	
	final static GlobalContainer gContainer = new GlobalContainer();
	
	public static void initialize(String includes, String excludes) {
		gContainer.initial(Codes.scan(includes, excludes));
	}
	
	public static void reload(Predicate<Class<?>> preficate) {
		gContainer.binders().stream()
			.filter(b->(b instanceof ReloadableBinder))
			.map(b->((ReloadableBinder) b).baseClass())
			.filter(preficate)
			.forEach(ApplicationContext::reload);
	}
	
	/**
     * 重新执行load(替换delegate, 线程安全)
     */
	public static void reload(Class<?> clazz) {
        Object obj = gContainer.getBean(clazz);
        if(obj instanceof IProxy) {
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
        Object obj = gContainer.getBean(clazz);
        if(obj instanceof IProxy) {
            ProxyBuilder.setDelegate(obj, ProxyBuilder.build(clazz, BeanHelper.inject(newClazz)));
        }
    }
	
	public static <T> T getBean(Class<T> c) {
		return gContainer.getBean(c);
	}
	
	public static void registBean(BeanBinder binder) {
	    gContainer.regist(binder);
	}
	
	public static class Internal {
	    public static BeanFetcher fetcher() {
	    	return gContainer;
	    }
	    public static BeanIndexing indexing() {
	    	return gContainer;
	    }
	}
}
