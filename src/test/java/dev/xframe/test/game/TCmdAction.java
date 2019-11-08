package dev.xframe.test.game;

import org.junit.Assert;

import dev.xframe.game.cmd.PlayerCmdLiteAction;
import dev.xframe.inject.Inject;
import dev.xframe.module.ModularInject;
import dev.xframe.net.cmd.Cmd;
import dev.xframe.test.game.GameProto.ValueMsg;

@Cmd(101)
public class TCmdAction extends PlayerCmdLiteAction<TPlayer, TPlayerInventory, ValueMsg>{
    
    @Inject
    private TTemplates templates;
    @Inject
    private TestExecution execution;
    @ModularInject
    private TSharableDep dep;
    @ModularInject
    private TAgent agent;

    @Override
    public void exec(TPlayer player, TPlayerInventory module, ValueMsg value) throws Exception {
        Assert.assertNotNull(templates);
        Assert.assertNotNull(player);
        Assert.assertNotNull(module);
        Assert.assertNotNull(value);
        Assert.assertNotNull(dep);
        Assert.assertNotNull(agent);
        execution.executing(TCmdAction.class);
    }

}
