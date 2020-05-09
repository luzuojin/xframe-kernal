package dev.xframe.inject.code;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.utils.XScanner;
import dev.xframe.utils.XScanner.ClassEntry;
import dev.xframe.utils.XScanner.ScanMatcher;
import javassist.CannotCompileException;
import javassist.ClassMap;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

public class Codes {
	
	private static final Logger logger = LoggerFactory.getLogger(Codes.class);
	
	private static Map<String, ClassEntry> classEntryMap = new HashMap<>();
	private static Map<String, AtomicInteger> classVersionMap = new HashMap<>();
	
	private static ScanMatcher matcher = new ScanMatcher("dev.xframe.*", "dev.xframe.inject.junit.*");
	
	private static List<Class<?>> declares;
	
	private static int addEntry(ClassEntry entry) {
	    classEntryMap.put(entry.name, entry);
	    
	    if(!classVersionMap.containsKey(entry.name)) {
	        classVersionMap.put(entry.name, new AtomicInteger(0));
	    }
	    return classVersionMap.get(entry.name).getAndIncrement();
	}
	
	public static List<Class<?>> getClasses(String includes, String excludes) {
	    matcher.merge(new ScanMatcher(includes, excludes));
		return getClasses0();
	}

	synchronized static List<Class<?>> getClasses0() {
	    if(declares == null) {
	        List<ClassEntry> entries = XScanner.scan(matcher);
	        List<String> names = new ArrayList<>();
	        for (ClassEntry entry : entries) {
	            addEntry(entry);
	            names.add(entry.name);
	        }
	        
	        Patchers.makePatch(names);
	        declares = loadClasses(names);
	    }
		return declares;
	}
	
	public static boolean isDeclared(String className) {
	    return classEntryMap.containsKey(className);
	}
	
	public static boolean isMatching(String className) {
	    return matcher.match(className);
	}
	
	public static List<Class<?>> getDeclaredClasses() {
		return declares == null ? Collections.emptyList() : declares;
	}
	
	private static Class<?> defineClass(ClassPool pool, String name) {
        try {
             return Class.forName(name);//如果在classloader中存在 直接返回
        } catch (ClassNotFoundException e) {
            //ignore;
        }
	    try {
	        return ClassPool.getDefault().get(name).toClass();//从文件中读取
	    } catch (NotFoundException | CannotCompileException e) {
	        //ignore
	    }
	    return null;
    }
	
	public static boolean redefineClass(File classFile) throws Exception {
	    ClassPool pool = getClassPool(classFile);
        CtClass ctClass = pool.makeClass(new FileInputStream(classFile));
        Patchers.makePatch(ctClass);
        Class<?> theClass = defineClass(pool, ctClass.getName());
        return XInstrument.redefine(new ClassDefinition(theClass, ctClass.toBytecode()));
    }
	
	public static Class<?> versioningClass(File classFile) throws Exception {
		ClassPool pool = getClassPool(classFile);
		CtClass ctClass = pool.makeClass(new FileInputStream(classFile));
		return renameClass(pool, new ClassEntry(ctClass.getName(), classFile.length(), classFile.lastModified()), ctClass);
	}
	
	public static Class<?> rebaseClass(Class<?> clazz) {
	    String clazzName = clazz.getName();
	    int idx = clazzName.lastIndexOf("_v");
        return idx == -1 ? clazz : defineClass(ClassPool.getDefault(), clazzName.substring(0, idx));
	}
	
    private static ClassPool getClassPool(File classFile) {
        ClassPool pool = new ClassPool();
        pool.appendClassPath(new DirClassPath(classFile.getParent()));
		pool.appendSystemPath();
        return pool;
    }
	
	private static Class<?> renameClass(ClassPool pool, ClassEntry entry, CtClass ctClass) throws Exception {
		if(classEntryMap.containsKey(entry.name) &&
		        classEntryMap.get(entry.name).size == entry.size) return null;//none modify
		int ver = addEntry(entry);
		ClassMap refs = tryLoadRefClasses(pool, ctClass, ver);
		ctClass.setName(naming(entry.name, ver));
		ctClass.replaceClassName(refs);
		return ctClass.toClass();
	}
	
	public static String naming(String originName, int version) {
        return version == 0 ? originName : (originName + String.format("_v%d", version));
    }
	
	//加载依赖类 如果为匿名内部时 同样重命名
	@SuppressWarnings("unchecked")
    private static ClassMap tryLoadRefClasses(ClassPool pool, CtClass ctClass, int ver) throws Exception {
        ClassMap cm = new ClassMap();
		for (String refClass : (Collection<String>) ctClass.getRefClasses()) {
			if(refClass.equals(ctClass.getName())) continue;
			if(refClass.startsWith(ctClass.getName() + "$")) {//内部类
		        CtClass ref = pool.get(refClass);
	        	String newName = naming(refClass, ver);
	        	ref.setName(newName);
	        	ref.replaceClassName(ctClass.getName(), naming(ctClass.getName(), ver));
	        	ref.toClass();
	        	cm.put(refClass, newName);
		    } else if(matcher.match(refClass)) {//同应用内的类, 如果有就不加载, 没有加载
                defineClass(pool, refClass);
            }
        }
        return cm;
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
	
	static class DirClassPath implements ClassPath {
	    final String dir;
        public DirClassPath(String dir) {
	        this.dir = dir;
        }
        public InputStream openClassfile(String classname) {
            try {
                return new FileInputStream(toFile(classname));
            } catch (Exception e) {}
            return null;
        }
        public URL find(String classname) {
            try {
                return toFile(classname).getCanonicalFile().toURI().toURL();
            }catch (Exception e) {}
            return null;
        }
        private File toFile(String classname) {
            File f = toFile0(classname);
            return f.exists() ? f : toFile1(classname);
        }
        private File toFile0(String classname) {
            return new File(dir + File.separatorChar + classname.replace('.', File.separatorChar) + ".class");
        }
        private File toFile1(String classname) {
            return new File(dir + File.separatorChar + classname.substring(classname.lastIndexOf('.')+1) + ".class");
        }
        public String toString() {
            return dir;
        }
        public void close() {
        }
	}
	
}
