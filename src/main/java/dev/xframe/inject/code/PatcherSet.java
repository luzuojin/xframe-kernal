package dev.xframe.inject.code;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

public class PatcherSet {

    static final Logger logger = LoggerFactory.getLogger(PatcherSet.class);

    static final List<Patcher> patchers = new ArrayList<Patcher>(
            Arrays.asList(
                    new JavaBeanPatcher(),
                    new PrototypePatcher()
                    ));
    static {
        ServiceLoader<Patcher> provided = ServiceLoader.load(Patcher.class);
        for (Patcher patcher : provided) addPatcher(patcher);
    }

    public static void addPatcher(Patcher patcher) {
        patchers.add(patcher);
    }

    public static void makePatch(Collection<String> classNames) {
        ClassPool pool = CtHelper.getClassPool();
        Set<CtClass> patched = new LinkedHashSet<>();//需要顺序, 先patch先load

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
