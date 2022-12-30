package dev.xframe.game.module;

import dev.xframe.game.module.beans.AgentBinder;
import dev.xframe.game.module.beans.DeclaredBinder;
import dev.xframe.game.module.beans.MInvokerFactory;
import dev.xframe.game.module.beans.ModularBinder;
import dev.xframe.game.module.beans.ModularIndexes;
import dev.xframe.game.module.beans.ModuleContainer;
import dev.xframe.inject.Bean;
import dev.xframe.inject.Inject;
import dev.xframe.inject.beans.BeanBinder;
import dev.xframe.inject.beans.BeanIndexing;
import dev.xframe.inject.beans.BeanPretreater;
import dev.xframe.inject.beans.BeanPretreater.Annotated;
import dev.xframe.inject.beans.BeanProvider;
import dev.xframe.inject.beans.BeanRegistrator;
import dev.xframe.inject.beans.Injector;
import dev.xframe.inject.code.Clazz;
import dev.xframe.inject.code.Codes;

import java.util.List;

@Bean
public class ModularContext {
	
	@Inject
	private BeanRegistrator registrator;
	@Inject
	private BeanIndexing gIndexing;
	@Inject
	private BeanProvider gProvider;
	@Inject
	private MInvokerFactory miFactory;
	
	private ModularIndexes indexes;
	
	private int assembleIndex;
	
	public synchronized void initial(Class<?> assembleClz) {
		if(indexes == null) {
			indexes = new ModularIndexes(gIndexing);
			//assembleClass作为第一个Bean记录
			assembleIndex = indexes.regist(new DeclaredBinder(assembleClz, Injector.of(assembleClz, indexes)));
			registrator.regist(indexes);
			pretreatModules().forEach(c->indexes.regist(buildBinder(c, indexes)));
			indexes.integrate(miFactory);
		}
	}
	
	public ModuleContainer newContainer(Object assemble) {
	    ModuleContainer mc = new ModuleContainer(gProvider, indexes);
		int index = assembleIndex;
		//为assembleClass赋值 @see initial() 
		mc.setBean(index, assemble);
		mc.integrate(indexes.getBinder(index));
		return mc;
	}
	
	public ModularBinder getBinder(Class<?> clazz) {
		return (ModularBinder) indexes.indexOf0(clazz);
	}
	
	public Injector newInjector(Class<?> c) {
		return Injector.of(c, indexes);
	}

	private BeanBinder buildBinder(Class<?> c, BeanIndexing indexing) {
		return c.isAnnotationPresent(ModularAgent.class) ? new AgentBinder(c) : new ModularBinder(c, Injector.of(c, indexing));
	}

	private List<Class<?>> pretreatModules() {
		Annotated annotated = new Annotated(ModularAgent.class, ModularComponent.class, Module.class);
		return new BeanPretreater(Codes.getScannedClasses(ModularContext::isModularClass)).pretreat(annotated.comparator()).collect();
	}
	private static boolean isModularClass(Clazz clazz) {
		return isModule(clazz) || isComponent(clazz) || isAgent(clazz);
	}
	private static boolean isAgent(Clazz clazz) {
        return clazz.isInterface() && clazz.isAnnotationPresent(ModularAgent.class);
    }
	private static boolean isComponent(Clazz clazz) {
        return clazz.isAnnotationPresent(ModularComponent.class) && !clazz.isAnnotationPresent(ModularIgnore.class);
    }
	//由于@Module可继承 需要过滤掉抽象类
	private static boolean isModule(Clazz clazz) {
        return clazz.isImplementation() && clazz.isAnnotationPresent(Module.class) && !clazz.isAnnotationPresent(ModularIgnore.class);
    }
}
