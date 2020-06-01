package dev.xframe.module.beans;

import java.util.List;
import java.util.stream.Collectors;

import dev.xframe.inject.beans.BeanBinder;
import dev.xframe.inject.beans.BeanContainer;
import dev.xframe.inject.beans.BeanIndexing;
import dev.xframe.inject.beans.Injector;
import dev.xframe.utils.XReflection;

public class DeclaredBinder extends ModularBinder {

	public DeclaredBinder(Class<?> master, Injector injector) {
		super(master, injector);
	}

	@Override
	protected Object newInstance() {//应该在容器创建时设置
		throw new UnsupportedOperationException();
	}

	@Override
	protected List<?> getKeywords() {
		//过滤掉BeanContainer相关的接口
		List<Class<?>> bc = XReflection.getAssigners(BeanContainer.class);
		return XReflection.getAssigners(master).stream().filter(c->!bc.contains(c)).collect(Collectors.toList());
	}

	@Override
	protected BeanBinder conflict(Object keyword, BeanBinder binder) {
		return this;
	}

	@Override
	public void buildInvoker(BeanIndexing indexing) {
		//empty invoker
		invoker = new ModularInvoker() {
			public void invokeUnload(ModuleContainer mc) {}
			public void invokeSave(ModuleContainer mc) {}
			public void invokeLoad(ModuleContainer mc) {}
		};
	}
	
}
