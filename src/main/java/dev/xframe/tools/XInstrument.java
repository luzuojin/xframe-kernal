package dev.xframe.tools;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class XInstrument {
    
    static boolean _loaded = false;
    static Instrumentation _inst;
    
    @SuppressWarnings("restriction")
    synchronized static void loadAgent() {
        if(_inst == null && !_loaded) {
            try {
                String jar = XInstrument.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                String pid = XProcess.currentProcessId();
                com.sun.tools.attach.VirtualMachine vm = com.sun.tools.attach.VirtualMachine.attach(pid);
                vm.loadAgent(jar);
                vm.detach();
            } catch (Throwable e) {e.printStackTrace();}
            _loaded = true;
        }
    }

    public static void agentmain(String agentArgs, Instrumentation inst) throws ClassNotFoundException, UnmodifiableClassException, InterruptedException {
        _inst = inst;
    }
    
    public static void redefine(ClassDefinition... definitions) throws ClassNotFoundException, UnmodifiableClassException {
        if(_inst == null)
            loadAgent();
        if(_inst != null)
            _inst.redefineClasses(definitions);
    }

}
