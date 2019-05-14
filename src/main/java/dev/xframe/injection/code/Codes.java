package dev.xframe.injection.code;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.tools.XScanner;
import dev.xframe.tools.XScanner.ClassEntry;
import dev.xframe.tools.XScanner.ScanMatcher;
import javassist.CannotCompileException;
import javassist.ClassMap;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;

public class Codes {
	
	private static final Logger logger = LoggerFactory.getLogger(Codes.class);
	
	private static Map<String, ClassEntry> classEntryMap = new HashMap<>();
	private static Map<String, AtomicInteger> classVersionMap = new HashMap<>();
	
	private static ScanMatcher includes;
	private static ScanMatcher excludes;

	private static List<Class<?>> declaredClasses;
	
	private static int addEntry(ClassEntry entry) {
	    classEntryMap.put(entry.name, entry);
	    
	    if(!classVersionMap.containsKey(entry.name)) {
	        classVersionMap.put(entry.name, new AtomicInteger(0));
	    }
	    return classVersionMap.get(entry.name).getAndIncrement();
	}
	
	public static List<Class<?>> getClasses(String includes, String excludes) {
		return getClasses(new ScanMatcher(includes), new ScanMatcher(excludes));
	}

	public static List<Class<?>> getClasses(ScanMatcher includes, ScanMatcher excludes) {
		if(declaredClasses == null) {
			declaredClasses = getClasses0(includes, excludes);
		}
		return declaredClasses;
	}

	private static List<Class<?>> getClasses0(ScanMatcher includes, ScanMatcher excludes) {
		Codes.includes = includes; Codes.excludes = excludes;
		List<ClassEntry> entries = XScanner.scan(includes, excludes);
		List<String> names = new ArrayList<>();
		for (ClassEntry entry : entries) {
			addEntry(entry);
			names.add(entry.name);
		}
		
		CodePatcher.makePatch(names);
		
		return loadClasses(names);
	}
	
	public static List<Class<?>> getDeclaredClasses() {
		return declaredClasses == null ? Collections.emptyList() : declaredClasses;
	}
	
	public static Class<?> defineClass(String name) {
	    return defineClass(getDefaultClassPoolWithClear(), name);
	}
	
	private static Class<?> defineClass(ClassPool pool, String name) {
	    Class<?> clazz = defineClass0(pool, name);
	    if(clazz == null) throw new IllegalArgumentException("class not found: " + name);
	    return clazz;
	}

	private static Class<?> defineClass0(ClassPool pool, String name) {
        try {
             return Class.forName(name);//如果在classloader中存在 直接返回
        } catch (ClassNotFoundException e) {
            //ignore;
        }
	    try {
	        return pool.get(name).toClass();//从文件中读取
	    } catch (NotFoundException | CannotCompileException e) {
	        //ignore
	    }
	    return null;
    }
	
	public static ClassPool getDefaultClassPoolWithClear() {
	    try {
			Field field = ClassPool.class.getDeclaredField("defaultPool");
			field.setAccessible(true);
			field.set(null, null);
		} catch (Throwable e1) {}//ignore
		return ClassPool.getDefault();
	}
	
	public static Class<?> getClass4Modify(File classFile) throws Exception {
		ClassPool pool = getDefaultClassPoolWithClear();
		pool.appendClassPath(classFile.getParent());
		
		CtClass ctClass = pool.makeClass(new FileInputStream(classFile));
		return renameClassIfModified(pool, new ClassEntry(ctClass.getName(), classFile.length(), classFile.lastModified()), ctClass);
	}
	
	public static List<Class<?>> getClasses4Modify(String... classNames) {
	    return getClasses4Modify(ct->Arrays.asList(classNames).contains(ct.getName()));
	}
	
	public static List<Class<?>> getClasses4Modify(Class<?>... annos) {
	    return getClasses4Modify(ct->hasAnnotation(ct, annos));
	}
	
	public static List<Class<?>> getClasses4Modify(Predicate<CtClass> predicate) {
		ClassPool pool = getDefaultClassPoolWithClear();
        List<ClassEntry> entries = XScanner.scan(includes, excludes);
        List<Class<?>> ret = new ArrayList<>();
        for (ClassEntry entry : entries) {
            Class<?> clazz = getClass4Modify(pool, entry, predicate);
            if(clazz != null) ret.add(clazz);
        }
        return ret;
	}
	
	private static Class<?> getClass4Modify(ClassPool pool, ClassEntry entry, Predicate<CtClass> predicate) {
	    try {
            CtClass ctClass = pool.get(entry.name);
            if(predicate.test(ctClass)) {
                return renameClassIfModified(pool, entry, ctClass);
            }
        } catch (Throwable e) {
            logger.info("load class error", e);
        }
	    return null;
	}

	private static Class<?> renameClassIfModified(ClassPool pool, ClassEntry entry, CtClass ctClass) throws Exception {
		if(classEntryMap.containsKey(entry.name) &&
		        classEntryMap.get(entry.name).size == entry.size) return null;//none modify
		int ver = addEntry(entry);
		ClassMap refs = tryLoadRefClasses(pool, ctClass, ver);
		ctClass.setName(newName(entry.name, ver));
		ctClass.replaceClassName(refs);
		return ctClass.toClass();
	}
	
	public static Class<?> getBaseClass(Class<?> modifiedClazz) {
	    String clazzName = modifiedClazz.getName();
	    int idx = clazzName.lastIndexOf("_v");
        return idx == -1 ? modifiedClazz : defineClass0(ClassPool.getDefault(), clazzName.substring(0, idx));
	}

	private static String newName(String originName, int ver) {
        return ver == 0 ? originName : (originName + String.format("_v%d", ver));
    }
	
	//加载依赖类 如果为匿名内部时 同样重命名
	@SuppressWarnings("unchecked")
    private static ClassMap tryLoadRefClasses(ClassPool pool, CtClass ctClass, int ver) throws Exception {
        ClassMap cm = new ClassMap();
		for (String refClass : (Collection<String>) ctClass.getRefClasses()) {
			if(refClass.equals(ctClass.getName())) continue;
			if(refClass.startsWith(ctClass.getName() + "$")) {//内部类
		        CtClass ref = pool.get(refClass);
		        if(!Modifier.isStatic(ref.getModifiers())) {//非静态类均重命名
		        	String newName = newName(refClass, ver);
		        	ref.setName(newName);
		        	ref.replaceClassName(ctClass.getName(), newName(ctClass.getName(), ver));
		        	ref.toClass();
		        	cm.put(refClass, newName);
		        } else {
		        	defineClass(pool, refClass);
		        }
		    } else if(includes.match(refClass) && !excludes.match(refClass)) {//同应用内的类, 如果有就不加载, 没有加载
                defineClass(pool, refClass);
            }
        }
        return cm;
	}

	private static boolean hasAnnotation(CtClass ctClass, Class<?>... annos) {
		for (Class<?> anno : annos) {
			if(ctClass.hasAnnotation(anno)) return true;
		}
		return false;
	}
	
	public static List<Class<?>> loadClasses(List<String> names) {
		ClassPool pool = ClassPool.getDefault();
		List<Class<?>> clazzes = new ArrayList<>();
		for (String name : names) {
			try {
				pool.get(name).freeze();
				clazzes.add(Class.forName(name));
			} catch (Throwable e) {//ignore
				logger.debug("load class error", e);
			}
		}
		return clazzes;
	}
	
}
