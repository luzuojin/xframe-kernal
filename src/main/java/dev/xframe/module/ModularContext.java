package dev.xframe.module;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import dev.xframe.inject.Inject;
import dev.xframe.inject.Prototype;
import dev.xframe.inject.beans.BeanBinder;
import dev.xframe.inject.beans.BeanDefiner;
import dev.xframe.inject.beans.BeanIndexing;
import dev.xframe.inject.beans.BeanPretreater;
import dev.xframe.inject.beans.BeanRegistrator;
import dev.xframe.inject.beans.Injector;
import dev.xframe.inject.code.Codes;
import dev.xframe.inject.code.SyntheticBuilder;
import dev.xframe.module.beans.DeclaredBinder;
import dev.xframe.module.beans.ModularBinder;
import dev.xframe.module.beans.ModuleContainer;
import dev.xframe.module.beans.ModularIndexes;
import dev.xframe.utils.XLambda;
import javassist.Modifier;

@Prototype
public class ModularContext implements ModuleLoader {
	
	@Inject
	private BeanRegistrator registrator;
	@Inject
	private BeanIndexing gIndexing;
	@Inject
	private BeanDefiner gDefiner;
	
	private ModularIndexes indexes;
	
	public ModuleContainer setupContainer(ModuleContainer mc, Class<?> assemble) {
		mc.setup(gDefiner, indexes);
		int index = indexes.getIndex(assemble);
		mc.setBean(index, mc);
		mc.integrate(indexes.getBinder(index));
		return mc;
	}
	
	public Injector newInjector(Class<?> c) {
		return Injector.of(c, indexes);
	}
	
	@Override
	public <T> T loadModule(ModuleContainer mc, Class<T> moduleType) {
		return getModuleLoader(moduleType).load(mc);
	}
	
	public ModuleTypeLoader getModuleLoader(Class<?> moduleType) {
		return (ModularBinder) indexes.getBinder(indexes.indexOf(moduleType));
	}

	public void initial(Class<?> assembleClass) {
		indexes = new ModularIndexes(gIndexing);
		registrator.regist(BeanBinder.instanced(this, ModularContext.class, ModuleLoader.class));
		registrator.regist(BeanBinder.instanced(indexes, ModularIndexes.class));
		indexes.regist(new DeclaredBinder(assembleClass, Injector.of(assembleClass, indexes)));
		pretreatModules().forEach(c->indexes.regist(buildBinder(c, indexes)));
		indexes.integrate();
	}

	static Class<?> buildAgentClass(Class<?> c) {
        ModularAgent an = c.getAnnotation(ModularAgent.class);
        return SyntheticBuilder.buildClass(c, an.invokable(), an.ignoreError(), an.boolByTrue());
	}
	
	private BeanBinder buildBinder(Class<?> c, BeanIndexing indexing) {
		if(c.isAnnotationPresent(ModularAgent.class)) {
			return new ModularBinder(c, Injector.NIL) {
				Supplier<Object> supplier = XLambda.createByConstructor(buildAgentClass(c));
				List<BeanBinder> impls = new ArrayList<>(); 
				protected void integrate(Object bean, BeanDefiner definer) {//append all implements
					impls.forEach(impl->SyntheticBuilder.append(bean, definer.define(impl.getIndex())));
				}
				protected BeanBinder conflict(Object keyword, BeanBinder binder) {
					assert keyword instanceof Class;
					assert ((Class<?>) keyword).isAnnotationPresent(ModularAgent.class);
					impls.add(binder);
					return this;
				}
				protected Object newInstance() {
					return supplier.get();
				}
			};
		}
		return new ModularBinder(c, Injector.of(c, indexing));
	}

	private List<Class<?>> pretreatModules() {
		return new BeanPretreater(Codes.getDeclaredClasses()).filter(isNecessary()).pretreat(annoComparator()).collect();
	}
	private Predicate<Class<?>> isNecessary() {
		return c -> isNecessaryClass(c);
	}
	private static boolean isNecessaryClass(Class<?> clazz) {
		return  isModule(clazz) || isComponent(clazz) || isAgent(clazz);
	}
	private static boolean isAgent(Class<?> clazz) {
        return clazz.isInterface() && clazz.isAnnotationPresent(ModularAgent.class);
    }
	private static boolean isComponent(Class<?> clazz) {
        return clazz.isAnnotationPresent(ModularComponent.class);
    }
	//由于@Module可继承 需要过滤掉抽象类
	private static boolean isModule(Class<?> clazz) {
        return !Modifier.isAbstract(clazz.getModifiers()) && !clazz.isInterface() && clazz.isAnnotationPresent(Module.class) && !clazz.isAnnotationPresent(ModularIgnore.class);
    }
	private static Class<? extends Annotation>[] annos = new Class[] {
    		ModularAgent.class,
    		ModularComponent.class,
    		Module.class
    		};
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

}
