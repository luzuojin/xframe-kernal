package dev.xframe.module;

import java.util.List;
import java.util.Map;

import dev.xframe.module.code.MContainerBuilder;
import dev.xframe.module.code.MLoaderBuilder;
import dev.xframe.module.code.ModularAnalyzer;
import dev.xframe.module.code.ModularElement;
import dev.xframe.utils.XCaught;

/**
 * 
 * @author luzj
 * 
 */
public class ModularConext {
	
	private ModularConext() {
	}
	
	/**
	 * 执行模块化 启动工作
	 * @param assembleClazz (moduleContainer)
	 * @param clazzes
	 * @return 
	 */
	public static void initialize(Class<?> assembleClazz, List<Class<?>> clazzes) {
		try {
            List<ModularElement> infos = ModularAnalyzer.analye(assembleClazz, clazzes);
            
            mccn = MContainerBuilder.build(assembleClazz, infos);
            lmap = MLoaderBuilder.build(infos);
            load = MLoaderBuilder.build(lmap);
            
            //add module classes for prototype dependence check
            infos.stream().filter(info->info.proxy!=null).forEach(info->clazzes.add(info.proxy));
            
        } catch (Throwable e) {
            XCaught.throwException(e);
        }
	}
	
	/**
	 * module container class name
	 */
	static String mccn;
	static ModuleLoader load;
	static Map<Class<?>, ModuleTypeLoader> lmap;
	
    public static ModuleTypeLoader getLoader(Class<?> clazz) {
        return lmap.get(clazz);
    }
    public static ModuleLoader getMLoader() {
        return load;
    }
    public static String getMCClassName() {
        return mccn;
    }
	
}
