package dev.xframe.http;

import dev.xframe.http.service.ServiceContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class HttpChannelInitializer extends ChannelInitializer<SocketChannel>{
    
    private final ServiceContext ctx;
    
    public HttpChannelInitializer(ServiceContext ctx) {
        this.ctx = ctx;
    }
    
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new HttpRequestDecoder());
        // Uncomment the following line if you don't want to handle HttpChunks.
//        p.addLast(new HttpObjectAggregator(1048576));
//        p.addLast(new ChunkedWriteHandler());
        // Remove the following line if you don't want automatic content compression.
        //p.addLast(new HttpContentCompressor());
        p.addLast(new HttpResponseEncoder());
        p.addLast(new HttpMessageHandler(ctx));
        
    }

}
