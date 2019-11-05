package dev.xframe.net;

import dev.xframe.injection.Synthetic;
import dev.xframe.net.codec.IMessage;
import dev.xframe.net.session.Session;

/**
 * 拦截消息
 * @author luzj
 *
 */
@Synthetic
public interface MessageInterceptor {
    
    /**
     * @param session 
     * @param req
     * @return True已被拦截 不做后续处理
     *          False 继续处理
     */
    public boolean intercept(Session session, IMessage req);
    
}
