package dev.xframe.inject.code;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.ProtectionDomain;

import dev.xframe.inject.Configurator;
import dev.xframe.inject.Eventual;
import javassist.CtClass;

@Configurator
public class XTransformer implements ClassFileTransformer, Eventual {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            if (classBeingRedefined != null && className != null && classfileBuffer != null) {
                className = className.replace(File.separator, ".");
                if(Codes.isMatching(className)) {
                    CtClass ctClass = CtHelper.newClassPool().makeClass(new ByteArrayInputStream(classfileBuffer));
                    PatcherSet.makePatch(ctClass);
                    return ctClass.toBytecode();
                }
            }
        } catch (Throwable e) {e.printStackTrace();}    //ignore
        return null;
    }

    @Override
    public void eventuate() {
        //开发环境, 开启动态Patch
        if(Files.isRegularFile(Paths.get(XInstrument.getProtectionPath())) && XInstrument.isTransportModel()) {
            if(XInstrument.loadAgent()) {
                XInstrument._inst.addTransformer(new XTransformer());
                XInstrument.logger.info("Listening class file transformer...");
            }
        }
    }
}