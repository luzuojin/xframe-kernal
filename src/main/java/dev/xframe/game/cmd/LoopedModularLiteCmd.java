package dev.xframe.game.cmd;

import com.google.protobuf.MessageLite;

import dev.xframe.game.player.ModularPlayer;
import dev.xframe.net.codec.IMessage;
import dev.xframe.tools.LiteParser;

public abstract class LoopedModularLiteCmd<T extends ModularPlayer, V, L extends MessageLite> extends LoopedModularCmd<T, V> {

	private LiteParser parser = new LiteParser(this.getClass(), LoopedModularLiteCmd.class);
	
	@Override
	public final void exec(T player, V module, IMessage req) throws Exception {
		exec(player, module, parser.<L>parse(req.getBody()));
	}
	
	public abstract void exec(T player,  V module, L req) throws Exception;

}