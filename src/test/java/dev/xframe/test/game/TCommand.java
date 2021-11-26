package dev.xframe.test.game;

import org.junit.Assert;

import dev.xframe.game.cmd.ModularCmd;
import dev.xframe.inject.Inject;
import dev.xframe.net.cmd.Cmd;
import dev.xframe.test.game.GameProto.ValueMsg;

@Cmd(100)
public class TCommand extends ModularCmd<TPlayer, TPlayerInventory, ValueMsg> {
    
    @Inject
    private TComp tComp;
    @Inject
    private TestExecution execution;

    @Override
    public void exec(TPlayer player, TPlayerInventory module, ValueMsg value) throws Exception {
        Assert.assertNotNull(tComp);
        Assert.assertNotNull(player);
        Assert.assertNotNull(module);
        Assert.assertNotNull(value);
        execution.executing(TCommand.class);
    }
	
}