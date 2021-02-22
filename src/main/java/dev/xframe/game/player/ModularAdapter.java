package dev.xframe.game.player;

import dev.xframe.inject.Bean;
import dev.xframe.inject.Inject;
import dev.xframe.inject.beans.Injector;
import dev.xframe.module.ModularContext;
import dev.xframe.module.beans.ModularBinder;

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
		injector.inject(bean, player.mc);
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
