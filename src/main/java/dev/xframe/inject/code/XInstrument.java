package dev.xframe.inject.code;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.utils.XPaths;
import dev.xframe.utils.XReflection;

public class XInstrument {
    
    static Logger logger = LoggerFactory.getLogger(XInstrument.class);
    
    static boolean _loaded = false;
    static Instrumentation _inst;
    
    synchronized static void loadAgent() {
        if(_inst == null && !_loaded) {
            try {
                //使用反射代替以下代码. 去掉pom.xml中的tools.jar的编译依赖
                Class<?> vmClass = Class.forName("com.sun.tools.attach.VirtualMachine");
                Object vmObj = XReflection.getMethod(vmClass, "attach", String.class).invoke(null, getProcessId());
                XReflection.getMethod(vmClass, "loadAgent", String.class).invoke(vmObj, getProtectionPath());
                XReflection.getMethod(vmClass, "detach").invoke(vmObj);
                /*
                com.sun.tools.attach.VirtualMachine vm = com.sun.tools.attach.VirtualMachine.attach(getProcessId());
                vm.loadAgent(getProtectionPath());
                vm.detach();
                */
                logger.info("Load instrument success...");
            } catch (Throwable e) {
                logger.info("Load instrument failed...", e);
            }
            _loaded = true;
        }
    }
    
    static String getProcessId() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        return name.substring(0, name.indexOf("@"));
    }

    static String getProtectionPath() {
        return XPaths.toPath(XInstrument.class.getProtectionDomain().getCodeSource().getLocation());
    }

    static boolean isTransportModel() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments().stream().filter(XInstrument::isTransportArg).findAny().isPresent();
    }

    static boolean isTransportArg(String arg) {
        return arg.contains("jdwp") && arg.contains("transport");
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

}
