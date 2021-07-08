package dev.xframe.test.game;

import org.junit.Assert;

import dev.xframe.game.action.ModularAction;
import dev.xframe.inject.Inject;
import dev.xframe.net.cmd.Cmd;
import dev.xframe.test.game.GameProto.ValueMsg;

@Cmd(101)
public class TAction extends ModularAction<TPlayer, TPlayerInventory, ValueMsg>{
    
    @Inject
    private TComp tComp;
    @Inject
    private TestExecution execution;
    @Inject
    private TSharableDep dep;
    @Inject
    private TAgent agent;

    @Override
    public void exec(TPlayer player, TPlayerInventory module, ValueMsg value) {
        Assert.assertNotNull(tComp);
        Assert.assertNotNull(player);
        Assert.assertNotNull(module);
        Assert.assertNotNull(value);
        Assert.assertNotNull(dep);
        Assert.assertNotNull(agent);
        execution.executing(TAction.class);
    }

}
