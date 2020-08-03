package dev.xframe.game.cmd;

import dev.xframe.game.player.Player;
import dev.xframe.net.codec.IMessage;

public abstract class DirectModularLiteCmd<T extends Player, V, L> extends DirectModularCmd<T, V> {

	private LiteParser parser = new LiteParser(this.getClass(), DirectModularLiteCmd.class, "L");
	
	@Override
	public final void exec(T player, V module, IMessage req) throws Exception {
		exec(player, module, parser.<L>parse(req.getBody()));
	}
	
	public abstract void exec(T player,  V module, L req) throws Exception;

}
