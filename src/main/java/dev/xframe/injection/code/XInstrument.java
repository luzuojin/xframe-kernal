package dev.xframe.injection.code;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;

import dev.xframe.tools.XProcess;

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

    static boolean isTransportModel() {
    	return ManagementFactory.getRuntimeMXBean().getInputArguments().stream().filter(XInstrument::isTransportArg).findAny().isPresent();
    }

	static boolean isTransportArg(String arg) {
		return arg.contains("jdwp") && arg.contains("transport");
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

}
