package dev.xframe.tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

import dev.xframe.injection.Configurator;
import dev.xframe.injection.Eventual;
import dev.xframe.injection.code.CodePatcher;
import dev.xframe.injection.code.Codes;
import javassist.ClassPool;
import javassist.CtClass;

public class XInstrument {
    
    static boolean _loaded = false;
    static Instrumentation _inst;
    
    @SuppressWarnings("restriction")
    synchronized static void loadAgent() {
        if(_inst == null && !_loaded) {
            try {
                com.sun.tools.attach.VirtualMachine vm = com.sun.tools.attach.VirtualMachine.attach(XProcess.currentProcessId());
                vm.loadAgent(getProtectionPath());
                vm.detach();
            } catch (Throwable e) {}
            _loaded = true;
        }
    }

    @SuppressWarnings("restriction")
    static boolean isAgentModel() {
        try {
            com.sun.tools.attach.VirtualMachine vm = com.sun.tools.attach.VirtualMachine.attach(XProcess.currentProcessId());
            boolean r = vm.getAgentProperties().values().stream().filter(obj->obj.toString().contains("-agentlib")).findAny().isPresent();
            vm.detach();
            return r;
        } catch (Throwable e) {}
        return false;
    }

    static String getProtectionPath() {
        return XInstrument.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    }

    public static void agentmain(String agentArgs, Instrumentation inst) throws ClassNotFoundException, UnmodifiableClassException, InterruptedException {
        _inst = inst;
    }
    
    public static boolean redefine(ClassDefinition... definitions) throws ClassNotFoundException, UnmodifiableClassException {
        if(_inst == null)
            loadAgent();
        if(_inst != null)
            _inst.redefineClasses(definitions);
        return _inst != null;
    }
    
    @Configurator
    public static class XInstrumentTransformer implements ClassFileTransformer, Eventual {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            try {
                if (classBeingRedefined != null && className != null && classfileBuffer != null) {
                    className = className.replace("/", ".");
                    if(Codes.isMatching(className)) {
                        CtClass ctClass = ClassPool.getDefault().makeClass(new ByteArrayInputStream(classfileBuffer));
                        CodePatcher.makePatch(ctClass);
                        return ctClass.toBytecode();
                    }
                }
            } catch (Throwable e) {}    //ignore
            return null;
        }

        @Override
        public void eventuate() {
            if(!new File(getProtectionPath()).isDirectory() && isAgentModel()) {
                XInstrument.loadAgent();
                XInstrument._inst.addTransformer(new XInstrumentTransformer());
            }
        }
    }

}
