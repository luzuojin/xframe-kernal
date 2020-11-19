package dev.xframe.game;

import dev.xframe.action.ActionExecutor;
import dev.xframe.action.ActionExecutors;
import dev.xframe.action.ActionLoop;
import dev.xframe.game.cmd.PlayerCmdAction;
import dev.xframe.game.cmd.PlayerCmdActionCmd;
import dev.xframe.game.player.ModularAdapter;
import dev.xframe.game.player.Player;
import dev.xframe.game.player.PlayerContext;
import dev.xframe.game.player.PlayerFactory;
import dev.xframe.inject.Configurator;
import dev.xframe.inject.Inject;
import dev.xframe.inject.Loadable;
import dev.xframe.inject.beans.BeanBinder;
import dev.xframe.inject.beans.BeanRegistrator;
import dev.xframe.inject.code.Codes;
import dev.xframe.net.cmd.CommandBuilder;
import dev.xframe.utils.XLambda;

@Configurator
public final class GameConfigurator implements Loadable {

	@Inject
	private BeanRegistrator registrator;
	@Inject
	private CommandBuilder cmdBuilder;
	@Inject
	private ModularAdapter modularAdapter;

	@Override
	public void load() {
		Class<?> assemble = Codes.getDeclaredClasses().stream().filter(c->c.isAnnotationPresent(Assemble.class)).findAny().orElse(null);
		if (assemble != null) {
			configure(assemble, getThreads(assemble));
		}
	}

	private int getThreads(Class<?> assemble) {
		int nThreads = assemble.getAnnotation(Assemble.class).threads();
		return nThreads == 0 ? Runtime.getRuntime().availableProcessors() : nThreads;
	}

	private void configure(Class<?> assemble, int threads) {
		modularAdapter.initial(assemble);
		
		cmdBuilder.regist(c -> PlayerCmdAction.class.isAssignableFrom(c), PlayerCmdActionCmd::new);

		ActionExecutor executor = ActionExecutors.newBindable("logic", threads);
		PlayerFactory factory = newPlayerFactory(assemble);
		PlayerContext context = new PlayerContext(executor, factory);

		registrator.regist(BeanBinder.instanced(factory));
		registrator.regist(BeanBinder.instanced(context));
	}

	private PlayerFactory newPlayerFactory(Class<?> assemble) {
		PlayerFactory factory = XLambda.createByConstructor(PlayerFactory.class, assemble, long.class, ActionLoop.class);
		return (long playerId, ActionLoop loop) -> {
					Player player = factory.newPlayer(playerId, loop);
					modularAdapter.initPlayer(player);
					return player;
				};
	}

}
