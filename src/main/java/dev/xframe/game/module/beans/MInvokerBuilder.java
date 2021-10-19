package dev.xframe.game.module.beans;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.xframe.game.module.ModularMethods;
import dev.xframe.inject.beans.BeanIndexing;
import dev.xframe.inject.beans.BeanPretreater;
import dev.xframe.inject.code.CtHelper;
import dev.xframe.inject.code.CtParser;
import dev.xframe.utils.XCaught;
import dev.xframe.utils.XReflection;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;

public class MInvokerBuilder {
	
	static final Map<String, String> cts = CtParser.parse("minvoker.ct");
	
	public static ModularInvoker build(Class<?> module, BeanIndexing indexing) {
		try {
			String moduleName = module.getName();
			String invokerName = cts.get("invoker_name").replace("${module_name}", moduleName);

			ClassPool pool = CtHelper.getClassPool();
			CtClass cc = pool.makeClass(invokerName);

			cc.addInterface(pool.get(ModularInvoker.class.getName()));

			cc.addField(CtField.make(cts.get("logger_field").replace("${module_name}", moduleName), cc));

			cc.addMethod(CtNewMethod.make(buildMethodBody("load_method", ModularMethods.Load.class, module, indexing), cc));
			cc.addMethod(CtNewMethod.make(buildMethodBody("unload_method", ModularMethods.Unload.class, module, indexing), cc));
			cc.addMethod(CtNewMethod.make(buildMethodBody("save_method", ModularMethods.Save.class, module, indexing), cc));
			cc.addMethod(CtNewMethod.make(buildMethodBody("tick_method", ModularMethods.Tick.class, module, indexing), cc));

			return XReflection.newInstance(cc.toClass());
		} catch (Exception e) {
		    throw XCaught.throwException(e);
		}
	}
	
	private static String buildMethodBody(String name, Class<? extends Annotation> anno, Class<?> module, BeanIndexing indexing) {
		return cts.get(name)
				.replace("${module_name}", module.getName())
				.replace("${module_index}", str(indexing.indexOf(module)))
				.replace("${methods_call}", buildCalls(anno, module, indexing));
	}

	private static String buildCalls(Class<? extends Annotation> anno, Class<?> module, BeanIndexing indexing) {
		return String.join("\n", findMethodsByAnno(module, anno).stream().map(m->buildCall(m, indexing)).collect(Collectors.toList()));
	}

	private static String buildCall(Method m, BeanIndexing indexing) {
		return cts.get("method_call").replace("${method_name}",m.getName()).replace("${method_params}", buildParams(m, indexing));
	}

	private static String buildParams(Method m, BeanIndexing indexing) {
		return String.join(",", Arrays.stream(m.getParameterTypes()).map(p->cts.get("param_getter").replace("${param_name}", p.getName()).replace("${param_index}", str(indexing.indexOf(p)))).collect(Collectors.toList()));
	}

	private static String str(int i) {
		return String.valueOf(i);
	}

	private static List<Method> findMethodsByAnno(Class<?> clazz, Class<? extends Annotation> anno) {
        return BeanPretreater.makeOrderly(findMethodsByAnno0(clazz, anno, new ArrayList<>()), Method::getName);
    }
	
    private static List<Method> findMethodsByAnno0(Class<?> clazz, Class<? extends Annotation> anno, List<Method> mets) {
        if(clazz != null && !clazz.equals(Object.class)) {
        	Arrays.stream(clazz.getDeclaredMethods()).filter(method->method.isAnnotationPresent(anno)).forEach(mets::add);
        	Arrays.stream(clazz.getInterfaces()).forEach(interfaze->findMethodsByAnno0(interfaze, anno, mets));
            findMethodsByAnno0(clazz.getSuperclass(), anno, mets);
        }
        return mets;
    }
	
}
