package dev.xframe.module.code;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dev.xframe.inject.Inject;
import dev.xframe.inject.code.SyntheticBuilder;
import dev.xframe.module.Component;
import dev.xframe.module.ModularAgent;
import dev.xframe.module.ModularHelper;
import dev.xframe.module.ModularMethods;
import dev.xframe.module.ModularShare;
import dev.xframe.module.Module;
import dev.xframe.module.ModuleType;
import dev.xframe.module.ModuleTypeLoader.IModuleLoader;
import dev.xframe.utils.CtHelper;
import dev.xframe.utils.CtParser;
import dev.xframe.utils.XSorter;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;

/**
 * 
 * build module container
 * 
 * @author luzj
 *
 */
public class MContainerBuilder {
	
	public static String MODULES_LOAD_METHOD;

	public static String build(Class<?> mcClazz, List<ModularElement> mes) throws Exception {
		return new MContainerBuilder().build0(mcClazz, mes);
	}
	
	private String empty = "";
	private Map<String, String> cts;
	private Class<?> assembleClazz;
	private String assembleBasic;
	private String assembleName;
	private Map<Class<?>, ModularElement> injectModules;
	private Map<Class<?>, Field> injectFields;
	
	private Map<Class<?>, Field> calcInjectFields(Class<?> mcClazz) {
		Map<Class<?>, Field> injects = new HashMap<Class<?>, Field>();
		for (Field field : mcClazz.getFields()) {
			if(field.isAnnotationPresent(Inject.class) && ModularHelper.isModularClass(field.getType())) {
				Class<?> type = field.getType();
				if(!type.isInterface()) throw new IllegalArgumentException("inject field must be an interface : " + type);
				injects.put(type, field);
			}
		}
		return injects;
	}
	
	private Map<Class<?>, ModularElement> calcInjectModules(List<ModularElement> mes) {
	    Map<Class<?>, ModularElement> modules = new HashMap<>();
        mes.stream().peek(this::setupProxy).forEach(me->this.putToInjectModules(modules, me.clazz, me));
        return modules;
    }
	private void putToInjectModules(Map<Class<?>, ModularElement> modules, Class<?> clazz, ModularElement me) {
	    if(clazz == null || Object.class.equals(clazz)) return;
	    if(clazz == me.clazz || clazz.isAnnotationPresent(ModularAgent.class) || clazz.isAnnotationPresent(ModularShare.class)) modules.put(clazz, me);
	    putToInjectModules(modules, clazz.getSuperclass(), me);
	    Arrays.stream(clazz.getInterfaces()).forEach(c->putToInjectModules(modules, c, me));
    }

    private String build0(Class<?> mcClazz, List<ModularElement> mes) throws Exception {
		this.cts = CtParser.parse("assemble.ct");
		this.injectModules = calcInjectModules(mes);
		this.injectFields = calcInjectFields(mcClazz);
		
		this.assembleClazz = mcClazz;
		this.assembleBasic = mcClazz.getName();
		this.assembleName = cts.get("assemble_name").replace("${assemble_basic}", assembleBasic);
		ClassPool pool = ClassPool.getDefault(); 
		CtClass sc = pool.get(assembleBasic);
		CtClass cc = pool.makeClass(assembleName);
		cc.setSuperclass(sc);
		cc.addInterface(pool.get(IModuleLoader.class.getName()));
		
		cc.addField(CtField.make(cts.get("logger_field").replace("${assemble_basic}", assembleBasic), cc));
		cc.addField(CtField.make(cts.get("modules_field").replace("${module_count}", String.valueOf(mes.size())), cc));
		
		CtConstructor scc = sc.getConstructors()[0];
		String params = String.join(",", IntStream.rangeClosed(1, scc.getParameterTypes().length).mapToObj(i->String.format("$%d", i)).collect(Collectors.toList()));
		cc.addConstructor(CtHelper.copy(scc, cts.get("constructor").replace("${params}", params), cc));
		
		CtMethod method = CtNewMethod.make(cts.get("loadmodule_method"), cc);
        cc.addMethod(method);
        MODULES_LOAD_METHOD = method.getName();
		
		cc.addMethod(CtNewMethod.make(makeLoadMethodBody(mes), cc));
		cc.addMethod(CtNewMethod.make(makeUnloadMethodBody(mes), cc));
		cc.addMethod(CtNewMethod.make(makeSaveMethodBody(mes), cc));
		
		cc.toClass();
		return assembleName;
	}

    private Class<?> setupProxy(ModularElement me) {
		return me.proxy = me.isAgent ? buildAgent(me) : MPrototypeBuilder.build(me.clazz);
	}

	private String makeLoadMethodBody(List<ModularElement> mes) {
		return cts.get("load_method")
				.replace("${multi_resident_load}", String.join("\n", mes.stream().filter(me->!isTransient(me.clazz)).map(this::makeModuleLoadParts).collect(Collectors.toList())))
				.replace("${multi_transient_load}", String.join("\n", mes.stream().filter(me->isTransient(me.clazz)).map(this::makeModuleLoadParts).collect(Collectors.toList())))
				;
	}
	
	private String makeModuleLoadParts(ModularElement me) {
		String localName = toLocalName(me.clazz);
		return cts.get("moudle_load_part")
				.replace("${module_assign}", cts.get(me.isAgent ? "direct_module_assign" : "sharable_module_assign"))	//require first
				.replace("${module_class}", me.proxy.getName())
				.replace("${index}", String.valueOf(me.index))
				.replace("${local_name}", localName)
				.replace("${module_load_invoke_parts}", makeInvokeParts(findMethodsByAnno(me.clazz, ModularMethods.Load.class), localName))
				.replace("${inject_setup_part}", makeInjectParts("inject_setup_part", me, localName))
				.replace("${agent_setup_part}", makeAgentParts("agent_setup_part", me, localName))
				;
	}
	
	private String makeUnloadMethodBody(List<ModularElement> mes) {
		return cts.get("unload_method")
				.replace("${multi_resident_unload}", String.join("\n", mes.stream().filter(me->!isTransient(me.clazz)).map(this::makeModuleUnloadParts).collect(Collectors.toList())))
				.replace("${multi_transient_unload}", String.join("\n", mes.stream().filter(me->isTransient(me.clazz)).map(this::makeModuleUnloadParts).collect(Collectors.toList())))
				;
	}
	
	private String makeModuleUnloadParts(ModularElement me) {
		String localName = toLocalName(me.clazz);
		return cts.get("module_unload_part")
				.replace("${module_class}", me.clazz.getName())
				.replace("${index}", String.valueOf(me.index))
				.replace("${local_name}", localName)
				.replace("${module_unload_invoke_parts}", makeInvokeParts(findMethodsByAnno(me.clazz, ModularMethods.Unload.class), localName))
				.replace("${inject_remove_part}", makeInjectParts("inject_remove_part", me, localName))
				.replace("${agent_remove_part}", makeAgentParts("agent_remove_part", me, localName))
				;
	}
	
	private String makeSaveMethodBody(List<ModularElement> mes) {
		return cts.get("save_method").replace("${multi_module_save}", String.join("\n", mes.stream().map(me->makeModuleSaveParts(mes, me)).collect(Collectors.toList())));
	}
	
	private String makeModuleSaveParts(List<ModularElement> mes, ModularElement me) {
		List<Method> methods = findMethodsByAnno(me.clazz, ModularMethods.Save.class);
		if(!methods.isEmpty()) {
			String localName = toLocalName(me.clazz);
			return cts.get("module_save_part")
					.replace("${module_class}", me.clazz.getName())
					.replace("${index}", String.valueOf(me.index))
					.replace("${local_name}", localName)
					.replace("${module_save_invoke_parts}", makeInvokeParts(methods, localName))
					;
		}
		return empty;
	}
	
	private String makeInjectParts(String ctKey, ModularElement me, String localName) {
		Field field = injectFields.get(me.sharable==null ? me.clazz : me.sharable);
		if(field != null) {
			return cts.get(ctKey)
					.replace("${inject_assign}", cts.get(me.hasSharable() ? "sharable_inject_assign" : "direct_inject_assign"))	//require first
                    .replace("${sharable_class}", me.getSharableName())
					.replace("${inject_field_name}", field.getName())
					.replace("${local_name}", localName)
					;
		}
		return empty;
	}
	
	private String makeAgentParts(String ctKey, ModularElement me, String localName) {
		List<ModularElement> agents = Arrays.stream(me.clazz.getInterfaces()).map(i->injectModules.get(i)).filter(i->i!=null&&i.isAgent).collect(Collectors.toList());
		if(!agents.isEmpty()) {
			return String.join(",", agents.stream().map(agent->
				cts.get(ctKey)
					.replace("${local_name}", localName)
					.replace("${agent_class}", agent.proxy.getName())
					.replace("${agent_index}", String.valueOf(agent.index))
			).collect(Collectors.toList()));
		}
		return empty;
	}

	private String makeInvokeParts(List<Method> methods, String localName) {
		return methods.isEmpty() ? empty : String.join("\n", methods.stream().map(m->makeInvokePart(localName, m)).collect(Collectors.toList()));
	}

	private String makeInvokePart(String localName, Method m) {
		return String.format("%s.%s(%s);", localName, m.getName(), makeInvokePartParams(m));
	}
	
	private String makeInvokePartParams(Method m) {
	    return String.join(",", Arrays.stream(m.getParameterTypes()).map(this::makeInvokePartParam).collect(Collectors.toList()));
    }

    private String makeInvokePartParam(Class<?> type) {//(Type)localModule(1)
        return type.isAssignableFrom(assembleClazz) ? "this" : String.format("(%s)%s(%s)", type.getName(), MODULES_LOAD_METHOD, injectModules.get(type).index);
    }

    private String toLocalName(Class<?> clazz) {
		return clazz.getName().replace(".", "_");
	}

    private Class<?> buildAgent(ModularElement me) {
        return SyntheticBuilder.buildClass(me.clazz, me.clazz.getAnnotation(ModularAgent.class).invokable(), me.clazz.getAnnotation(ModularAgent.class).ignoreError(), me.clazz.getAnnotation(ModularAgent.class).boolByTrue());
    }

    private boolean isTransient(Class<?> clazz) {
        return (clazz.isAnnotationPresent(Component.class) && clazz.getAnnotation(Component.class).value() == ModuleType.TRANSIENT) ||
                clazz.isAnnotationPresent(Module.class) && clazz.getAnnotation(Module.class).value() == ModuleType.TRANSIENT;
    }
	
	private List<Method> findMethodsByAnno(Class<?> clazz, Class<? extends Annotation> anno) {
        return XSorter.bubble(findMethodsByAnno0(clazz, anno, new ArrayList<>()), (Method o1, Method o2) -> o1.getName().compareTo(o2.getName()));
    }

    private List<Method> findMethodsByAnno0(Class<?> clazz, Class<? extends Annotation> anno, List<Method> mets) {
        if(clazz != null && !clazz.equals(Object.class)) {
            for (Method method : clazz.getDeclaredMethods()) {
                if(method.isAnnotationPresent(anno)) {
                    mets.add(method);
                }
            }
            Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> interfaze : interfaces) {
                findMethodsByAnno0(interfaze, anno, mets);
            }
            findMethodsByAnno0(clazz.getSuperclass(), anno, mets);
        }
        return mets;
    }

}
