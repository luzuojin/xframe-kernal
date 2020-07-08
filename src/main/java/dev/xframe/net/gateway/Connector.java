package dev.xframe.net.gateway;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import dev.xframe.net.NetClient;
import dev.xframe.net.client.ClientLifecycleListener;
import dev.xframe.net.codec.IMessage;
import dev.xframe.net.session.Session;

/**
 * 
 * upstream connector
 * 
 * @author luzj
 *
 */
class Connector implements ClientLifecycleListener {
    
    private NetClient client;
    private ConnectThread thread;
    
    public Connector(NetClient client) {
        this.client = client;
    }
    
    public Session connect(String host, int port) {
        Session session = client.build(host, port, 0L);
        if(!session.connect()) {//连接失败 添加到重连列表
            getThread().add(session);
        }
        return session;
    }
    
    @Override
    public void onSessionUnRegister(Session session) {
        getThread().add(session);
    }
    
    private ConnectThread getThread() {
        if(thread == null) {
            createThread();
        }
        return thread;
    }

    synchronized void createThread() {
        if(thread == null) {
            thread = new ConnectThread();
            thread.setName("connector");
            thread.setDaemon(true);
            thread.start();
        }
    }

    static class ConnectThread extends Thread {
        DelayQueue<ConnectData> queue = new DelayQueue<>();
        @Override
        public void run() {
            for(;;) {
                try {
                    ConnectData conn = queue.take();
                    Session sess = conn.session;
                    if(!sess.isActive()) {
                        boolean connected = sess.connect();
                        if(!connected) {
                            long delay = Math.min(60, (conn.count / 5 + 1) * 5);//每隔5次 重试时间增加5s, 最大重试时间60s
                            conn.count ++;
                            conn.time = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(delay);
                            queue.put(conn);
                        }
                    }
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        }
        public void add(Session session) {
            if(session != null && !session.isActive()) {
                queue.put(new ConnectData(session));
            }
        }
    }
    
    static class ConnectData implements Delayed {
        Session session;
        int count;
        long time = System.currentTimeMillis();
        public ConnectData(Session session) {
            this.session = session;
        }
        @Override
        public int compareTo(Delayed o) {
            return Long.compare(this.time, ((ConnectData)o).time);
        }
        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(this.time - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }
    }
    
    
    @Override
    public void onSessionRegister(Session session) {
    }
    @Override
    public void onMessageRecieve(Session session, IMessage message) {
    }
    @Override
    public void onMessageSending(Session session, IMessage message) {
    }
    @Override
    public void onExceptionCaught(Session session, IMessage req, Throwable ex) {
    }
    @Override
    public void onMessageFlushSlow(Session session) {
    }
	@Override
	public void onSessionIdle(Session session) {
	}
}
