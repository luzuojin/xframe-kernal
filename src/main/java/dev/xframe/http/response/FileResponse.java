package dev.xframe.http.response;

import dev.xframe.http.Request;
import dev.xframe.http.Response;
import dev.xframe.utils.XDateFormatter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public abstract class FileResponse extends WriterResponse {
	int cacheTime = 3600;//s (1h)
	
	public FileResponse(ContentType type) {
		this.set(type);
	}

	public FileResponse forceDownload() {
		this.set(ContentType.FORCE_DOWNLOAD);
		return this;
	}
	public FileResponse setFileName(String fileName) {
		this.setHeader(HttpHeaderNames.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", fileName));
		return this;
	}
	
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
	public void writeTo(Channel channel, Request origin) {
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
			Response.BAD_REQUEST.getWriter().writeTo(channel, origin);
		}
	}

	protected void setCacheHeaders(HttpResponse response) {
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
			super(ContentType.mime(file.getName()));
			assert file.exists() && !file.isHidden();
			this.file = file;
		}

		public Sys setFileName() {
			this.setFileName(file.getName());
			return this;
		}

		public long lastModified() {
			return file.lastModified();
		}
		
		@Override
		@SuppressWarnings("resource")
		protected void write0(Channel channel, Request origin) throws IOException {
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			long fileLength = raf.length();

			HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status());//not full

			setBasisHeaders(response, fileLength);
			setCacheHeaders(response);
			
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
	
	public static class Binary extends FileResponse {
		long modified;
		byte[] data;
		Binary(ContentType type) {
			super(type);
			this.modified = System.currentTimeMillis();
			this.setCacheTime(Integer.MAX_VALUE);
		}
		public Binary(ContentType type, byte[] data) {
			this(type);
			this.data = data;
		}
		
		public long lastModified() {
			return modified;
		}
		
		public byte[] data() throws IOException  {
			return data;
		}
		public ByteBuf content() throws IOException {
			return Unpooled.wrappedBuffer(data());
		}
		@Override
		protected void write0(Channel channel, Request origin) throws IOException {
			ByteBuf content = content();
			long length = content.readableBytes();
			
			HttpResponse response = HttpMethod.HEAD.equals(origin.method()) ? newHttpResp(Unpooled.buffer(0)) : newHttpResp(content);

			setBasisHeaders(response, length);
			setCacheHeaders(response);

			channel.write(response);
			channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		}
	}
	
	public static class ClassPath extends Binary {
		String file;
		public ClassPath(String file) {
			super(ContentType.mime(file));
			this.file = file;
		}
		private byte[] readBytes(String file) throws IOException {
			InputStream input = getClass().getClassLoader().getResourceAsStream(file);
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        int b;
	        while((b = input.read()) != -1) {
	            out.write(b);
	        }
	        return out.toByteArray();
	    }
		
		public byte[] data() throws IOException  {
			return data == null ? setData() : data;
		}
		synchronized byte[] setData() throws IOException {
			return data == null ? (data = readBytes(file)) : data;
		}
	}

}
