package dev.xframe.injection.code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.tools.CtHelper;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;

public class Patchers {
    
    static final Logger logger = LoggerFactory.getLogger(Patchers.class);
    
    static final List<Patcher> patchers = new ArrayList<Patcher>(
            Arrays.asList(
                    new JavaBeanPatcher(),
                    new PrototypePatcher()
                    ));
    public static void addPatcher(Patcher patcher) {
        patchers.add(patcher);
    }

    public static void makePatch(List<String> classNames) {
        ClassPool pool = ClassPool.getDefault();
        Set<CtClass> patched = new LinkedHashSet<CtClass>();//需要顺序, 先patch先load
        
        for (String className : classNames) {
            try {
                makePatch(patched, pool.get(className));
            } catch (Throwable e) {
                logger.error("Patch code: ", e);
            }
        }
        
        for (CtClass clazz : patched) {
            try {
                clazz.toClass();
            } catch (Throwable e) {
                logger.error("Patch code: ", e);
            }
        }
    }

    private static void makePatch(Set<CtClass> patched, CtClass clazz) throws Exception {
        if(patched.contains(clazz) || dispensablePatch(clazz)) return;
        
        makePatch(patched, clazz.getSuperclass());//先处理父类
        
        if(makePatch(clazz)) patched.add(clazz);
    }

    public static boolean makePatch(CtClass clazz) throws Exception {
        boolean patched = false;
        for(Patcher patcher : patchers) {
            if(patcher.required(clazz)) {
                patcher.patch(clazz);
                patched = true;
            }
        }
        return patched;
    }

    private static boolean dispensablePatch(CtClass clazz) throws NotFoundException {
        return CtHelper.isObjectType(clazz) || Modifier.isInterface(clazz.getModifiers());
    }

}
