package dev.xframe.game.cmd;

import dev.xframe.game.player.MTypedLoader;
import dev.xframe.game.player.ModularAdapter;
import dev.xframe.game.player.Player;
import dev.xframe.inject.Inject;
import dev.xframe.utils.XGeneric;

/**
 * module 入口
 * Modular必然是Looped
 * @author luzj
 */
public abstract class ModularCmd<T extends Player, V, M> extends LoopedCmd<T, M> {
	
	@Inject
	private ModularAdapter mAdapter;
	private MTypedLoader mLoader;
    @Override
    protected void onLoad() {
        mLoader = mAdapter.getTypedLoader(getModuleCls());
    }

    protected Class<?> getModuleCls() {
        return XGeneric.parse(getClass(), ModularCmd.class).getByIndex(1);
    }

    public final void exec(T player, M msg) throws Exception {
        exec(player, mLoader.load(player), msg);
    }
    
	public abstract void exec(T player, V module, M msg) throws Exception;
    
}
