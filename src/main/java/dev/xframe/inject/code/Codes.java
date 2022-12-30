package dev.xframe.inject.code;

import dev.xframe.inject.code.Scanner.ClassEntry;
import dev.xframe.inject.code.Scanner.ScanMatcher;
import dev.xframe.utils.XOptional;
import javassist.CannotCompileException;
import javassist.ClassMap;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class Codes {
	
	private static final Logger logger = LoggerFactory.getLogger(Codes.class);
	
	private static Map<String, ClassEntry> classEntryMap = new LinkedHashMap<>();
	private static Map<String, AtomicInteger> classVersionMap = new LinkedHashMap<>();
	
	private static ScanMatcher matcher = new ScanMatcher("*xframe-*.jar;dev.xframe.*", "dev.xframe.inject.junit.*");

	private static List<Clazz> scanned;

	private static int addEntry(ClassEntry entry) {
	    classEntryMap.put(entry.name, entry);
	    
	    if(!classVersionMap.containsKey(entry.name)) {
	        classVersionMap.put(entry.name, new AtomicInteger(0));
	    }
	    return classVersionMap.get(entry.name).getAndIncrement();
	}
	
	public static List<Clazz> scan(String includes, String excludes) {
		return scan0(matcher.merge(new ScanMatcher(includes, excludes)));
	}

	private synchronized static List<Clazz> scan0(ScanMatcher matcher) {
	    if(scanned == null) {
			Scanner.scan(matcher).forEach(Codes::addEntry);
	        PatcherSet.makePatch(classEntryMap.keySet());
			scanned = loadClazzes(classEntryMap.keySet());
	    }
		return scanned;
	}
	
	public static boolean isScanned(String className) {
	    return classEntryMap.containsKey(className);
	}
	
	public static boolean isMatching(String className) {
	    return matcher.match(className);
	}
	
	public static List<Clazz> scannedClazzes() {
		return XOptional.orElse(scanned, Collections.emptyList());
	}

	@Deprecated
	public static List<Class<?>> getScannedClasses() {
		return getScannedClasses(clz->true);
	}
	public static List<Class<?>> getScannedClasses(Predicate<Clazz> predicate) {
		return Clazz.filter(scannedClazzes(), predicate);
	}
	
	private static Class<?> defineClass(ClassPool pool, String name) {
        try {
             return Class.forName(name);//如果在classloader中存在 直接返回
        } catch (ClassNotFoundException e) {
            //ignore;
        }
	    try {
	        return CtHelper.getClassPool().get(name).toClass();//从文件中读取
	    } catch (NotFoundException | CannotCompileException e) {
	        //ignore
	    }
	    return null;
    }
	
	public static boolean redefineClass(File classFile) throws Exception {
	    ClassPool pool = getClassPool(classFile);
        CtClass ctClass = pool.makeClass(Files.newInputStream(classFile.toPath()));
        PatcherSet.makePatch(ctClass);
        Class<?> theClass = defineClass(pool, ctClass.getName());
        return XInstrument.redefine(new ClassDefinition(theClass, ctClass.toBytecode()));
    }
	
	public static Class<?> versioningClass(File classFile) throws Exception {
		ClassPool pool = getClassPool(classFile);
		CtClass ctClass = pool.makeClass(Files.newInputStream(classFile.toPath()));
		return renameClass(pool, new ClassEntry(ctClass.getName(), classFile.length(), classFile.lastModified()), ctClass);
	}
	
	public static Class<?> rebaseClass(Class<?> clazz) {
	    String clazzName = clazz.getName();
	    int idx = clazzName.lastIndexOf("_v");
        return idx == -1 ? clazz : defineClass(CtHelper.getClassPool(), clazzName.substring(0, idx));
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
    private static ClassMap tryLoadRefClasses(ClassPool pool, CtClass ctClass, int ver) throws Exception {
        ClassMap cm = new ClassMap();
		for (String refClass : ctClass.getRefClasses()) {
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

	private static List<Clazz> loadClazzes(Collection<String> names) {
		ClassPool pool = CtHelper.getClassPool();
		List<Clazz> clazzes = new ArrayList<>();
		for (String name : names) {
			try {
				CtClass ctClass = pool.get(name);
				ctClass.freeze();
				clazzes.add(Clazz.of(ctClass));
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
                return Files.newInputStream(toFile(classname).toPath());
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
