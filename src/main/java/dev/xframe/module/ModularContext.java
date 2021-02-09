package dev.xframe.module;

import java.util.List;
import java.util.function.Predicate;

import dev.xframe.inject.Bean;
import dev.xframe.inject.Inject;
import dev.xframe.inject.beans.BeanBinder;
import dev.xframe.inject.beans.BeanDefiner;
import dev.xframe.inject.beans.BeanDiscovery;
import dev.xframe.inject.beans.BeanIndexing;
import dev.xframe.inject.beans.BeanPretreater;
import dev.xframe.inject.beans.BeanRegistrator;
import dev.xframe.inject.beans.Injector;
import dev.xframe.inject.code.Codes;
import dev.xframe.module.beans.AgentBinder;
import dev.xframe.module.beans.DeclaredBinder;
import dev.xframe.module.beans.ModularBinder;
import dev.xframe.module.beans.ModularIndexes;
import dev.xframe.module.beans.ModuleContainer;
import dev.xframe.utils.XReflection;

@Bean
public class ModularContext {
	
	@Inject
	private BeanRegistrator registrator;
	@Inject
	private BeanIndexing gIndexing;
	@Inject
	private BeanDefiner gDefiner;
	@Inject
	private BeanDiscovery gDiscovery;
	
	private ModularIndexes indexes;
	
	private int assembleIndex;
	
	public synchronized void initialize(Class<?> assembleClass) {
		if(indexes == null) {
			List<Class<?>> scanned = Codes.getDeclaredClasses();
			indexes = new ModularIndexes(gIndexing);
			//registrator.regist(BeanBinder.instanced(indexes, ModularIndexes.class));
			//assembleClass作为第一个Bean记录
			assembleIndex = indexes.regist(new DeclaredBinder(assembleClass, Injector.of(assembleClass, indexes)));
			pretreatModules(scanned).forEach(c->indexes.regist(buildBinder(c, indexes)));
			gDiscovery.discover(scanned, indexes);
			indexes.integrate();
		}
	}
	
	public ModuleContainer initContainer(ModuleContainer mc, Object assemble) {
		mc.setup(gDefiner, indexes);
		int index = assembleIndex;
		//为assembleClass赋值 @see initialize() 
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

	private List<Class<?>> pretreatModules(List<Class<?>> scanned) {
	    @SuppressWarnings("unchecked")
	    BeanPretreater.Annotated anns = new BeanPretreater.Annotated(new Class[] {ModularAgent.class, ModularComponent.class, Module.class});
		return new BeanPretreater(scanned).filter(isModularClass()).pretreat(anns.comparator()).collect();
	}
	private Predicate<Class<?>> isModularClass() {
		return c -> isModularClass(c);
	}
	private static boolean isModularClass(Class<?> clazz) {
		return isModule(clazz) || isComponent(clazz) || isAgent(clazz);
	}
	private static boolean isAgent(Class<?> clazz) {
        return clazz.isInterface() && clazz.isAnnotationPresent(ModularAgent.class);
    }
	private static boolean isComponent(Class<?> clazz) {
        return clazz.isAnnotationPresent(ModularComponent.class) && !clazz.isAnnotationPresent(ModularIgnore.class);
    }
	//由于@Module可继承 需要过滤掉抽象类
	private static boolean isModule(Class<?> clazz) {
        return XReflection.isImplementation(clazz) && clazz.isAnnotationPresent(Module.class) && !clazz.isAnnotationPresent(ModularIgnore.class);
    }
}
