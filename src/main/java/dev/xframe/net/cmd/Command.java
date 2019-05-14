package dev.xframe.net.cmd;

import dev.xframe.net.codec.IMessage;
import dev.xframe.net.session.Session;

public interface Command {

	void execute(Session session, IMessage req) throws Exception;

}
