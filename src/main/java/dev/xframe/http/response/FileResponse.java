package dev.xframe.http.response;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import dev.xframe.http.Request;
import dev.xframe.utils.XDateFormatter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;

public abstract class FileResponse extends WriterResponse {
	
	int cacheTime = 3600;//s (1h)
	
	public abstract long lastModified();

	public FileResponse setCacheTime(int cacheTime) {
		this.cacheTime = cacheTime;
		return this;
	}
	
	protected void writeNotModified(Channel channel) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_MODIFIED);
		Calendar time = new GregorianCalendar();
		response.headers().set(HttpHeaderNames.DATE, XDateFormatter.from(time));
		// Close the connection as soon as the error message is sent.
		channel.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	@Override
	public void write(Channel channel, Request origin) {
		long lastModified = lastModified();
		// Cache Validation
		String ifModifiedSince = origin.headers().get(HttpHeaderNames.IF_MODIFIED_SINCE);
		if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
			Date ifModifiedSinceDate = XDateFormatter.toDate(ifModifiedSince);

			// Only compare up to the second because the datetime format we send to the client
			// does not have milliseconds
			long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
			long fileLastModifiedSeconds = lastModified / 1000;
			if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
				writeNotModified(channel);
				return;
			}
		}
		
		try {
			write0(channel, origin);
		} catch (IOException e) {
			e.printStackTrace();
			Responses.BAD_REQUEST.writeTo(channel, origin);
		}
	}

	protected void setDateAndCacheHeaders(HttpResponse response) {
		// Date header
		Calendar time = new GregorianCalendar();
		response.headers().set(HttpHeaderNames.DATE, XDateFormatter.from(time));
		// Add cache headers
		time.add(Calendar.SECOND, cacheTime);
		response.headers().set(HttpHeaderNames.EXPIRES, XDateFormatter.from(time));
		response.headers().set(HttpHeaderNames.CACHE_CONTROL, "private, max-age=" + cacheTime);
		response.headers().set(HttpHeaderNames.LAST_MODIFIED, XDateFormatter.from(lastModified()));
	}
	
	protected abstract void write0(Channel channel, Request origin) throws IOException;
	
	public static class Sys extends FileResponse {
		File file;
		public Sys(File file) {
			assert file.exists() && !file.isHidden();
			this.file = file;
			this.set(new ContentType(Mimetypes.get(file.getName())));
		}
		public long lastModified() {
			return file.lastModified();
		}
		
		@Override
		@SuppressWarnings("resource")
		protected void write0(Channel channel, Request origin) throws IOException {
			RandomAccessFile raf;
			try {
				raf = new RandomAccessFile(file, "r");
			} catch (FileNotFoundException ignore) {
				Responses.NOT_FOUND.writeTo(channel, origin);
				return;
			}
			long fileLength = raf.length();

			HttpResponse response = newHttpResp();

			setBasisHeaders(response, fileLength);

			setDateAndCacheHeaders(response);
			// Write the initial line and the header.
			channel.write(response);

			if(!HttpMethod.HEAD.equals(origin.method())) {
				// Write the content.
				channel.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), channel.newProgressivePromise());
			}
			// Write the end marker.
			// Close the connection when the whole content is written out.
			channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
		}
	}
	
	//classpath jar
	public static class ClassPath extends FileResponse {
		String file;
		long lastModified;
		
		volatile ByteBuf data;
		
		public ClassPath(String file) {
			this.file = file;
			this.set(new ContentType(Mimetypes.get(file)));
			this.lastModified = System.currentTimeMillis();
			this.setCacheTime(Integer.MAX_VALUE);
		}
		
		public long lastModified() {
			return System.currentTimeMillis();
		}
		
		private byte[] readBytes(InputStream input) throws IOException {
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        int b;
	        while((b = input.read()) != -1) {
	            out.write(b);
	        }
	        return out.toByteArray();
	    }
		
		public ByteBuf data() throws IOException  {
			if(data != null) {
				return data.retain();
			}
			synchronized (this) {
				if(data == null) {
					InputStream input = this.getClass().getClassLoader().getResourceAsStream(file);
					byte[] bytes = readBytes(input);
					data = Unpooled.copiedBuffer(bytes);
				}
			}
			return data.retain();
		}
		
		@Override
		protected void write0(Channel channel, Request origin) throws IOException {
			ByteBuf content = data();
			long length = content.readableBytes();
			
			HttpResponse response = HttpMethod.HEAD.equals(origin.method()) ? newHttpResp() : newHttpResp(content);

			setBasisHeaders(response, length);
			setDateAndCacheHeaders(response);

			channel.write(response);
			channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
		}
	}

}
