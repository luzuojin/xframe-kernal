package dev.xframe.game.cmd;

import dev.xframe.game.player.Player;
import dev.xframe.net.codec.IMessage;

public abstract class LoopedLiteCmd<T extends Player, L> extends LoopedCmd<T> {

	private LiteParser parser = new LiteParser(this.getClass(), LoopedLiteCmd.class, "L");
	
	@Override
	public final void exec(T player, IMessage req) throws Exception {
		exec(player, parser.<L>parse(req.getBody()));
	}
	
	public abstract void exec(T player, L req) throws Exception;

}
