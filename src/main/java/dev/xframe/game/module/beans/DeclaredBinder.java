package dev.xframe.game.module.beans;

import dev.xframe.inject.beans.BeanBinder;
import dev.xframe.inject.beans.Injector;
import dev.xframe.inject.beans.Injector.Member;
import dev.xframe.utils.XReflection;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

public class DeclaredBinder extends ModularBinder {

	public DeclaredBinder(Class<?> master, Injector injector) {
		super(master, injector, null);
	}

	@Override
	protected Object newInstance() {//应该在容器创建时设置
		throw new UnsupportedOperationException();
	}

	@Override
	protected List<Class<?>> getKeywords() {
		//如果有.过滤掉ModuleContainer相关的接口
		List<Class<?>> bc = XReflection.getAssigners(ModuleContainer.class);
		return XReflection.getAssigners(master).stream().filter(c->!bc.contains(c)).collect(Collectors.toList());
	}
	
	@Override
	protected boolean injectable(Field field) {
		return true;
	}

	@Override
	protected BeanBinder conflict(Object keyword, BeanBinder binder) {
		return this;
	}

	@Override
	public void makeComplete(ModularIndexes indexes, MInvokerFactory miFactory) {
		for (Member member : injector.getMebmers()) {
			if(indexes.isValidIndex(member.getIndex())) {//Assemble的Index offset为0
			 	((ModularBinder)indexes.getBinder(member.getIndex())).registListener(newMemberListener(member));
			}
		}
		//empty invoker
		invoker = ModularInvoker.Empty;
	}

	private ModularListener newMemberListener(final Member member) {
		return new ModularListener() {
			public void onModuleLoaded(ModuleContainer mc, ModularBinder binder, Object module) {
				member.set(mc.getBean(getIndex()), module);
			}
			public void onModuleUnloaded(ModuleContainer mc, ModularBinder binder, Object module) {
				member.set(mc.getBean(getIndex()), null);
			}
		};
	}
	
}
