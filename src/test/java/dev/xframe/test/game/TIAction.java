package dev.xframe.test.game;

import dev.xframe.game.action.ModularAction;
import org.junit.Assert;

public class TIAction extends ModularAction<TPlayer, TPlayerInventory, TIAction.Msg> {

    public static class Msg {}

    @Override
    public void exec(TPlayer p, TPlayerInventory m, Msg msg) throws Exception {
        Assert.assertNotNull(p);
        Assert.assertNotNull(m);
        m.dosomething();
    }

}
