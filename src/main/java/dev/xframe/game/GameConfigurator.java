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
import dev.xframe.injection.ApplicationContext;
import dev.xframe.injection.Configurator;
import dev.xframe.injection.Injection;
import dev.xframe.injection.Loadable;
import dev.xframe.injection.code.Codes;
import dev.xframe.modular.ModularBridge;
import dev.xframe.modular.ModularEnigne;
import dev.xframe.modular.ModuleContainer;
import dev.xframe.modular.ModuleLoader;
import dev.xframe.modular.code.MBridgeBuilder;
import dev.xframe.modular.code.MFactoryBuilder;
import dev.xframe.net.cmd.Command;
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
            ApplicationContext.registBean(CommandBuilder.class, newCommandBuilder());
            
            logger.info("Load compelete modular and logics threads[{}]", threads);
        }
        
        protected abstract void configure0(Class<?> assemble, List<Class<?>> clazzes);
        
        protected abstract PlayerFactory newPlayerFactory();
        
        protected abstract CommandBuilder newCommandBuilder();
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
        @Override
        protected CommandBuilder newCommandBuilder() {
            return clazz -> {
                try {
                    return (Command) clazz.newInstance();
                } catch (Throwable e) {
                    throw new IllegalArgumentException(clazz.getName(), e);
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
            ModularEnigne.initialize(assemble, clazzes);
            ApplicationContext.registBean(ModuleLoader.class, ModularEnigne.getMLoader());
        }
        @Override
        protected CommandBuilder newCommandBuilder() {
            return clazz -> {
                try {
                    Class<?> t = clazz;
                    if(PlayerCmdAction.class.isAssignableFrom(clazz)) {
                        return new PlayerCmdActionCmd<>(clazz);
                    } else if (t.isAnnotationPresent(ModularBridge.class)) {
                        t = MBridgeBuilder.build(t);
                    }
                    return (Command) t.newInstance();
                } catch (Throwable e) {
                    throw new IllegalArgumentException(clazz.getName(), e);
                }
            };
        }
    }

}
