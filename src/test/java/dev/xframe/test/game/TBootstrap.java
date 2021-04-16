package dev.xframe.test.game;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import dev.xframe.game.callable.ModularCallable;
import dev.xframe.game.cmd.PlayerCmd;
import dev.xframe.game.player.ModularAdapter;
import dev.xframe.game.player.PlayerContext;
import dev.xframe.inject.Inject;
import dev.xframe.inject.junit.ContextScan;
import dev.xframe.inject.junit.Junit4ClassRunner;
import dev.xframe.module.ModuleType;
import dev.xframe.net.cmd.CommandContext;
import dev.xframe.net.codec.Message;
import dev.xframe.test.game.GameProto.ValueMsg;

@RunWith(Junit4ClassRunner.class)
@ContextScan(includes="dev.xframe.*", excludes="*.jar")
public class TBootstrap {
    
    @Inject
    private PlayerContext playerCtx;
    @Inject
    private TestExecution testExecution;
    @Inject
    private CommandContext commandCtx;
    @Inject
    private ModularAdapter adapter;
    
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
        
        PlayerCmd<TPlayer> cmd1 = (PlayerCmd<TPlayer>) commandCtx.get((short) 100);
        cmd1.execute(null, Message.of(100, ValueMsg.newBuilder().setVal("hey").build().toByteArray()).copy(playerId));
        TimeUnit.MILLISECONDS.sleep(100);//wait queued executed
        testExecution.assertExecuted(TCommand.class);
        
        PlayerCmd<TPlayer> cmd2 = (PlayerCmd<TPlayer>)  commandCtx.get((short) 101);
        cmd2.execute(null, Message.of(101, ValueMsg.newBuilder().setVal("hey").build().toByteArray()).copy(playerId));
        TimeUnit.MILLISECONDS.sleep(100);//wait queued executed
        testExecution.assertExecuted(TAction.class);
        
        player.player.dosomething();
        testExecution.assertExecuted(TPlayerInventory.class);
        
        new ModularCallable<TPlayer, TPlayerInventory>() {
            @Override
            public void exec(TPlayer player, TPlayerInventory module) {
                Assert.assertNotNull(player);
                Assert.assertNotNull(module);
                module.dosomething();
            }
        }.call(player);
        TimeUnit.MILLISECONDS.sleep(100);//wait queued executed
        testExecution.assertExecuted(TPlayerInventory.class);
        
        player.post(new TEvent());
        testExecution.assertExecuted(TPlayerInventory.class);
        
        player.save();
        testExecution.assertExecuted(TPlayerInventory.class);
        testExecution.assertExecuted(TRepository.class);
        
        player.unload(ModuleType.TRANSIENT);
        testExecution.assertExecuted(TDepInventory.class);
        Assert.assertNull(adapter.loadModule(player, TDepInventory.class));
        Assert.assertNull(player.dep);
        
		new TPrototype().dosomething();
		
		new TPlayerInfo().dosomething();
		
		player.unload(ModuleType.RESIDENT);
		testExecution.assertExecuted(TPlayerInventory.class);
		Assert.assertNull(adapter.loadModule(player, TPlayerInventory.class));
	}

}
