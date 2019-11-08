package dev.xframe.game;

import java.lang.reflect.Constructor;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.action.ActionExecutor;
import dev.xframe.action.ActionExecutors;
import dev.xframe.action.ActionLoop;
import dev.xframe.game.cmd.PlayerCmdAction;
import dev.xframe.game.cmd.PlayerCmdActionCmd;
import dev.xframe.game.player.Player;
import dev.xframe.game.player.PlayerContext;
import dev.xframe.game.player.PlayerFactory;
import dev.xframe.inject.ApplicationContext;
import dev.xframe.inject.Configurator;
import dev.xframe.inject.Injection;
import dev.xframe.inject.Loadable;
import dev.xframe.inject.code.Codes;
import dev.xframe.module.ModularConext;
import dev.xframe.module.ModuleContainer;
import dev.xframe.module.ModuleLoader;
import dev.xframe.module.code.MFactoryBuilder;
import dev.xframe.net.cmd.CommandBuilder;

@Configurator
public final class GameConfigurator implements Loadable {
    
    private static final Logger logger = LoggerFactory.getLogger(GameConfigurator.class);
    
    @Override
    public void load() {
        Class<?> assemble = Codes.getDeclaredClasses().stream().filter(c->c.isAnnotationPresent(Assemble.class)).findAny().orElse(null);
        if(assemble != null) {
            configure(assemble, getThreads(assemble));
        }
    }

    public int getThreads(Class<?> assemble) {
        int nThreads = assemble.getAnnotation(Assemble.class).threads();
        return nThreads == 0 ? Runtime.getRuntime().availableProcessors() * 2 : nThreads;
    }
    
    public void configure(Class<?> assemble, int threads) {
        newConfigurator(assemble).configure(assemble, threads, Codes.getDeclaredClasses());
    }

    public AbstConfigurator newConfigurator(Class<?> assemble) {
        return ModuleContainer.class.isAssignableFrom(assemble) ? new ModularConfigurator() : new NormalConfigurator();
    }
    
    abstract class AbstConfigurator {
        public void configure(Class<?> assemble, final int threads, List<Class<?>> clazzes) {
            configure0(assemble, clazzes);
            
            ActionExecutor executor = ActionExecutors.newFixed("logics", threads);//max: 2*threads
            PlayerFactory factory = new PlayerInjectFactory(newPlayerFactory());
            PlayerContext context = new PlayerContext(executor, factory);
            
            ApplicationContext.registBean(PlayerFactory.class, factory);
            ApplicationContext.registBean(PlayerContext.class, context);
            
            logger.info("Load compelete modular and logics threads[{}]", threads);
        }
        
        protected abstract void configure0(Class<?> assemble, List<Class<?>> clazzes);
        
        protected abstract PlayerFactory newPlayerFactory();
        
    }
    
    class PlayerInjectFactory implements PlayerFactory {
        final PlayerFactory factory;
        public PlayerInjectFactory(PlayerFactory factory) {
            this.factory = factory;
        }
        @Override
        public Player newPlayer(long playerId, ActionLoop loop) {
            Player player = factory.newPlayer(playerId, loop);
            Injection.inject(player);
            return player;
        }
    }

    class NormalConfigurator extends AbstConfigurator {
        Constructor<?> constructor;
        @Override
        protected void configure0(Class<?> assemble, List<Class<?>> clazzes) {
            try {
                constructor = assemble.getConstructor(long.class, ActionLoop.class);
            } catch (Throwable e) {
                throw new IllegalArgumentException(e);
            }
        }
        @Override
        protected PlayerFactory newPlayerFactory() {
            return (playerId, loop) -> {
                try {
                    return (Player) constructor.newInstance(playerId, loop);
                } catch (Throwable e) {
                    throw new IllegalArgumentException(e);
                }
            };
        }
    }
    
    class ModularConfigurator extends AbstConfigurator {
        @Override
        protected PlayerFactory newPlayerFactory() {
            return MFactoryBuilder.build(PlayerFactory.class);
        }
        @Override
        protected void configure0(Class<?> assemble, List<Class<?>> clazzes) {
            ModularConext.initialize(assemble, clazzes);
            ApplicationContext.registBean(ModuleLoader.class, ModularConext.getMLoader());
            ApplicationContext.fetchBean(CommandBuilder.class)
                .regist(c->PlayerCmdAction.class.isAssignableFrom(c), PlayerCmdActionCmd::new);
        }
    }

}
