package dev.xframe.game.player;

import dev.xframe.game.module.ModularContext;
import dev.xframe.game.module.beans.ModularBinder;
import dev.xframe.inject.Bean;
import dev.xframe.inject.Inject;
import dev.xframe.inject.beans.Injector;

@Bean
public class ModularAdapter implements ModuleLoader {
	
	@Inject
	private ModularContext modularCtx;
	
	public void initial(Class<?> assemble) {
		modularCtx.initial(assemble);
	}

	public Injector newInjector(Class<?> c) {
		return modularCtx.newInjector(c);
	}
	
	public void runInject(Injector injector, Object bean, Player player) {
	    runInjectStatic(injector, bean, player);
	}
	public static final void runInjectStatic(Injector injector, Object bean, Player player) {
	    injector.inject(bean, player.mc);
	}
	
	public int indexOf(Class<?> moduleCls) {
		return modularCtx.getBinder(moduleCls).getIndex();
	}
	
	public <T> T loadModule(Player player, int moduleIndex) {
		return loadModuleStatic(player, moduleIndex);
	}
	
	public static final <T> T loadModuleStatic(Player player, int moduleIndex) {
	    return player.mc.getBean(moduleIndex);
	}
	
	@Override
	public <T> T loadModule(Player player, Class<T> clazz) {
		return modularCtx.getBinder(clazz).getModuleFrom(player.mc);
	}
	
	public void assemble(Player player) {
	    player.mc = modularCtx.newContainer(player);
	}
	
	public MTypedLoader getTypedLoader(Class<?> clazz) {
		final ModularBinder binder = modularCtx.getBinder(clazz);
		return new MTypedLoader() {
			public <T> T load(Player player) {
				return binder.getModuleFrom(player.mc);
			}
		};
	}

}
