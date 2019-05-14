package dev.xframe.test.game;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import dev.xframe.game.callable.ModularCallable;
import dev.xframe.game.cmd.PlayerCommand;
import dev.xframe.game.player.PlayerContext;
import dev.xframe.injection.Inject;
import dev.xframe.injection.junit.ContextScan;
import dev.xframe.injection.junit.NebulaJunit4ClassRunner;
import dev.xframe.modular.ModuleType;
import dev.xframe.net.cmd.CommandContext;
import dev.xframe.net.codec.Message;
import dev.xframe.test.game.GameProto.ValueMsg;

@RunWith(NebulaJunit4ClassRunner.class)
@ContextScan(includes="dev.xframe.*", excludes="*.jar")
public class TBootstrap {
    
    @Inject
    private PlayerContext playerCtx;
    @Inject
    private TestExecution testExecution;
    @Inject
    private CommandContext commandCtx;
    
    @Test
    @SuppressWarnings("unchecked")
	public void test() throws Exception {
        testExecution.assertExecuted(TTemplates.class);
        
		long playerId = 10086;
        TPlayer player = playerCtx.getPlayerImmediately(playerId);
		
		player.load(ModuleType.TRANSIENT);
		
		testExecution.assertExecuted(TPlayerInventory.class);
		testExecution.assertExecuted(TDepInventory.class);
		testExecution.assertExecuted(TComponent.class);
        
        PlayerCommand<TPlayer> cmd1 = (PlayerCommand<TPlayer>) commandCtx.get((short) 100);
        cmd1.execute(null, Message.build((short)100, ValueMsg.newBuilder().setVal("hey").build().toByteArray()).copy(playerId));
        testExecution.assertExecuted(TCommand.class);
        
        PlayerCommand<TPlayer> cmd2 = (PlayerCommand<TPlayer>)  commandCtx.get((short) 101);
        cmd2.exec(player, Message.build((short)101, ValueMsg.newBuilder().setVal("hey").build().toByteArray()).copy(playerId));
        testExecution.assertExecuted(TCmdAction.class);
        
        player.player.dosomething();
        testExecution.assertExecuted(TPlayerInventory.class);
        
        new ModularCallable<TPlayer, TPlayerInventory>() {
            @Override
            public void call(TPlayer player, TPlayerInventory module) {
                Assert.assertNotNull(player);
                Assert.assertNotNull(module);
                module.dosomething();
            }
        }.call(player);
        testExecution.assertExecuted(TPlayerInventory.class);
        
        player.post(new TEvent());
        testExecution.assertExecuted(TPlayerInventory.class);
        
        player.save();
        testExecution.assertExecuted(TPlayerInventory.class);
        testExecution.assertExecuted(TRepository.class);
        
        player.unload(ModuleType.TRANSIENT);
        testExecution.assertExecuted(TDepInventory.class);
        
		new TPrototype().dosomething();
		
		new TPlayerInfo().dosomething();
		
		player.unload(ModuleType.RESIDENT);
		testExecution.assertExecuted(TPlayerInventory.class);
	}

}
