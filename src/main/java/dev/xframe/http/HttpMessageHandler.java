package dev.xframe.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import dev.xframe.http.service.Request;
import dev.xframe.http.service.Response;
import dev.xframe.http.service.Response.ContentType;
import dev.xframe.http.service.ServiceContext;
import dev.xframe.http.service.ServiceInvoker;
import dev.xframe.utils.Mimetypes;
import dev.xframe.utils.XDateFormatter;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;

/**
 * 
 * Dispatch http service methods
 * @author luzj
 *
 */
public class HttpMessageHandler extends ChannelInboundHandlerAdapter {
    
    private final ServiceContext ctx;
    
    public HttpMessageHandler(ServiceContext ctx) {
        this.ctx = ctx;
    }
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        assert msg instanceof FullHttpRequest;
        Request req = new Request(((InetSocketAddress)(ctx.channel().remoteAddress())).getAddress(), (FullHttpRequest)msg);
        try {
            doRequest(ctx, req);
        } finally {
            req.destroy();
        }
           
   }

    protected void doRequest(ChannelHandlerContext ctx, Request req) {
        if(req.isSucc()) {
            ServiceInvoker invoker = this.ctx.get(req.xpath());
            if(invoker != null) {
                doInvoke(ctx, req, invoker);
            } else {
                sendNotFoudResponse(ctx);
            }
        } else {
            sendBadRequestResponse(ctx);
        }
    }

    protected void doInvoke(ChannelHandlerContext ctx, Request req, ServiceInvoker invoker) {
        try {
            sendResponse(ctx, req, invoker.invoke(req));
        } catch (Throwable ex) {
            sendResponse(ctx, req, this.ctx.config().getErrorhandler().handle(req, ex));
        }
    }

    protected void sendAsFile(ChannelHandlerContext ctx, Request req, String file) {
        if(file != null) {
            try {
                sendFileResponse(ctx, req, file); 
            } catch (Throwable ex) {
                sendResponse(ctx, req, this.ctx.config().getErrorhandler().handle(req, ex));
            }
        } else {
            sendNotFoudResponse(ctx);
        }
    }

   protected void sendResponse(ChannelHandlerContext ctx, Request req, Response resp) {
       if(resp.type == ContentType.FILE) {
           sendAsFile(ctx, req, resp.content.toString(CharsetUtil.UTF_8));
       } else {
           sendContentResponse(ctx, HttpResponseStatus.OK, resp);
       }
   }

   protected void sendNotFoudResponse(ChannelHandlerContext ctx) {
       sendContentResponse(ctx, HttpResponseStatus.NOT_FOUND, Response.NOT_FOUND.retain());
   }

   protected void sendBadRequestResponse(ChannelHandlerContext ctx) {
       sendContentResponse(ctx, HttpResponseStatus.BAD_REQUEST, Response.BAD_REQUEST.retain());
   }

   protected void sendContentResponse(ChannelHandlerContext ctx, HttpResponseStatus status, Response resp) {
       // Build the response object.
       FullHttpResponse response = new DefaultFullHttpResponse(
               HttpVersion.HTTP_1_1, 
               status,
               resp.content);

       setContentType(response, resp.type.val());
       setContentHeaders(response, resp);

       // Write the response.
       ctx.write(response);
       ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
   }

   protected void setContentHeaders(FullHttpResponse response, Response resp) {
       response.headers().set(HttpHeaderNames.CONTENT_LENGTH, resp.content.readableBytes());
       for(Map.Entry<String, String> header : resp.headers.entrySet()) {
           response.headers().set(header.getKey(), header.getValue());
       }
   }

    protected void setContentType(HttpResponse response, String contentType) {
    	response.headers().set(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE, "86400");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "*");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "*");
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().set(HttpHeaderNames.CONTENT_ENCODING, "UTF-8");
    }

   public static final int HTTP_CACHE_SECONDS = 60;
   
   @SuppressWarnings("resource")
   protected void sendFileResponse(ChannelHandlerContext ctx, Request req, String path) throws Exception {
       File file = new File(path);
       if (!file.exists() || !file.isFile() || file.isHidden()) {
           sendNotFoudResponse(ctx);
           return;
       }

       // Cache Validation
       String ifModifiedSince = req.headers().get(HttpHeaderNames.IF_MODIFIED_SINCE);
       if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
           Date ifModifiedSinceDate = XDateFormatter.toDate(ifModifiedSince);

           // Only compare up to the second because the datetime format we send to the client
           // does not have milliseconds
           long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
           long fileLastModifiedSeconds = file.lastModified() / 1000;
           if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
               sendNotModified(ctx);
               return;
           }
       }

       RandomAccessFile raf;
       try {
           raf = new RandomAccessFile(file, "r");
       } catch (FileNotFoundException ignore) {
           sendNotFoudResponse(ctx);
           return;
       }
       long fileLength = raf.length();

       HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
       
       setContentType(response, Mimetypes.get(file.getName()));
       
       response.headers().set(HttpHeaderNames.CONTENT_LENGTH, fileLength);
       
       setDateAndCacheHeaders(response, file);
       // Write the initial line and the header.
       ctx.write(response);

       if(!HttpMethod.HEAD.equals(req.method())) {
           // Write the content.
           ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());
       }
       // Write the end marker.
       // Close the connection when the whole content is written out.
       ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
   }
   
   protected void sendNotModified(ChannelHandlerContext ctx) {
       FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_MODIFIED);

       Calendar time = new GregorianCalendar();
       response.headers().set(HttpHeaderNames.DATE, XDateFormatter.from(time));

       // Close the connection as soon as the error message is sent.
       ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
   }

   protected void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
       // Date header
       Calendar time = new GregorianCalendar();
       response.headers().set(HttpHeaderNames.DATE, XDateFormatter.from(time));

       // Add cache headers
       time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
       response.headers().set(HttpHeaderNames.EXPIRES, XDateFormatter.from(time));
       response.headers().set(HttpHeaderNames.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
       response.headers().set(HttpHeaderNames.LAST_MODIFIED, XDateFormatter.from(fileToCache.lastModified()));
   }
   
   @Override
   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
       ctx.close();
   }


}
