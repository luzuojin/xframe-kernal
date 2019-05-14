package dev.xframe.modular.code;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.xframe.injection.code.ProxyBuilder;
import dev.xframe.modular.ModularHelper;
import dev.xframe.modular.ModularShare;
import dev.xframe.modular.ModuleContainer;
import dev.xframe.modular.ModuleLoader;
import dev.xframe.modular.ModuleTypeLoader;
import dev.xframe.modular.ModuleTypeLoader.IModuleLoader;

/**
 * 
 * Module Loader gen
 * @author luzj
 *
 */
public class MLoaderBuilder {
    
    public static Map<Class<?>, ModuleTypeLoader> build(List<ModularElement> mes) throws Exception {
        Map<Class<?>, ModuleTypeLoader> ret = new HashMap<Class<?>, ModuleTypeLoader>();
        for (ModularElement me : mes) {
            ret.put(me.clazz, buildModuleTypeLoader(me.clazz, me.index));
            ret.put(me.sharable, buildModuleTypeLoader(me.sharable, me.index));
        }
        return ret;
    }
    
    private static ModuleTypeLoader buildModuleTypeLoader(Class<?> c, int n) {
        if(c == null) {
            return null;
        }
        if(c.isAnnotationPresent(ModularShare.class) && ModularHelper.isDebugEnabled()) {
            return new IndexedSharableLoader(c, n);
        }
        return new IndexedModuleLoader(n);
    }

    static class IndexedModuleLoader implements ModuleTypeLoader {
        final int n;
        public IndexedModuleLoader(int n) {
            this.n = n;
        }
        @SuppressWarnings("unchecked")
        public <T> T load(ModuleContainer container) {
            return (T) ((IModuleLoader)container)._loadModule(n);
        }
    }
    static class IndexedSharableLoader implements ModuleTypeLoader {
        final Class<?> c;
        final int n;
        public IndexedSharableLoader(Class<?> c, int n) {
            this.c = c;
            this.n = n;
        }
        public <T> T load(ModuleContainer container) {
            return ProxyBuilder.build(c, ((IModuleLoader)container)._loadModule(n));
        }
    }
    
    public static ModuleLoader build(Map<Class<?>, ModuleTypeLoader> loaders) {
        return new MappedModuleLoader(loaders);
    }
    
    static class MappedModuleLoader implements ModuleLoader {
        final Map<Class<?>, ModuleTypeLoader> loaders;
        public MappedModuleLoader(Map<Class<?>, ModuleTypeLoader> loaders) {
            this.loaders = loaders;
        }
        public <T> T loadModule(ModuleContainer container, Class<T> clazz) {
            return (container instanceof IModuleLoader) ? loaders.get(clazz).load(container) : null;
        }
    }
    
}
