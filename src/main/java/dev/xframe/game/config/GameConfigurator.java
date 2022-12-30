package dev.xframe.game.config;

import dev.xframe.game.action.Action;
import dev.xframe.game.cmd.ActionCmd;
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
import dev.xframe.task.TaskExecutor;
import dev.xframe.task.TaskExecutors;
import dev.xframe.task.TaskLoop;
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
		Codes.getScannedClasses(clz -> clz.isAnnotationPresent(Assemble.class)).stream().findAny().ifPresent(this::configure);
	}

	private TaskExecutor newExecutor(Class<?> assemble) {
	    String name = "biz";
	    Assemble anno = assemble.getAnnotation(Assemble.class);
        int nThreads = anno.threads() > 0 ? anno.threads() : Runtime.getRuntime().availableProcessors();
		return anno.sharding() ? TaskExecutors.newSharding(name, nThreads) : TaskExecutors.newFixed(name, nThreads);
	}

	private void configure(Class<?> assemble) {
		modularAdapter.initial(assemble);
		
		cmdBuilder.regist(Action.class::isAssignableFrom, ActionCmd::new);
		
		TaskExecutor executor = newExecutor(assemble);
		PlayerFactory factory = newPlayerFactory(assemble);
		PlayerContext context = new PlayerContext(executor, factory);

		registrator.regist(BeanBinder.instanced(factory));
		registrator.regist(BeanBinder.instanced(context));
	}

	private PlayerFactory newPlayerFactory(Class<?> assemble) {
		final ModularAdapter _adapter = modularAdapter;
		final PlayerFactory  _factory = XLambda.createByConstructor(PlayerFactory.class, assemble, long.class, TaskLoop.class);
		return (long playerId, TaskLoop loop) -> {
					Player player = _factory.newPlayer(playerId, loop);
					_adapter.assemble(player);
					return player;
				};
	}

}
