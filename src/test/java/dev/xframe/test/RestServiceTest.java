package dev.xframe.test;

import java.util.function.Consumer;

import org.junit.Ignore;

import dev.xframe.http.decode.HttpBody;
import dev.xframe.http.service.Request;
import dev.xframe.http.service.Response;
import dev.xframe.http.service.Rest;
import dev.xframe.http.service.ServiceContext;
import dev.xframe.http.service.rest.BodyDecoder;
import dev.xframe.http.service.rest.HttpArgs;
import dev.xframe.http.service.rest.HttpMethods;
import dev.xframe.http.service.rest.RespEncoder;
import dev.xframe.http.service.rest.RestConfigSetter;
import dev.xframe.http.service.rest.RestServiceBuilder;
import dev.xframe.injection.ApplicationContext;
import dev.xframe.injection.Injection;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpMethod;

@Ignore
@Rest("something")
public class RestServiceTest {
    
    @HttpMethods.GET("/b")
	public String dosomething0(@HttpArgs.Param int a, @HttpArgs.Header long b, @HttpArgs.Body int x) {
    	long ret = (a + b) / x;
		return "dosomething0: (" + a + "+" + b + ")/"+x+"=" + ret;
	}
    
    @HttpMethods.GET("/{r}")
    public String dosomething1(@HttpArgs.Path int r, @HttpArgs.Param int a, @HttpArgs.Header long b, @HttpArgs.Body int x) {
    	long ret = (a + b) * r / x;
    	return "dosomething1: (" + a + "+" + b + ")*" + r + "/" + x + "=" + ret;
    }
    
    @HttpMethods.GET("/a/{v}")
    public String dosomething2(@HttpArgs.Path String v) {
        return v;
    }
    
    @HttpMethods.GET("/x")
    public String dosomething3(@HttpArgs.Param boolean x) {
        return String.valueOf(x);
    }
	
	public static void main(String[] args) throws Exception {
		RestConfigSetter configurer = new RestConfigSetter(){
            @Override
            public void setBodyDecoder(Consumer<BodyDecoder> setter) {
                setter.accept((t, body) -> Integer.parseInt(new String(body)));
            }
            @Override
            public void setRespEncoder(Consumer<RespEncoder> setter) {
                setter.accept(resp -> new Response(resp.toString()));
            }
		};
		configurer.load();
		ApplicationContext.registBean(configurer);
		ApplicationContext.registBean(Injection.makeInstanceAndInject(RestServiceBuilder.class));
		ApplicationContext.registBean(Injection.makeInstanceAndInject(ServiceContext.class));
		ApplicationContext.fetchBean(ServiceContext.class).registService(RestServiceTest.class);
		
		printResp(doService(new Request(null, "something/2?a=2", null, HttpMethod.GET){
			@Override
			public String getHeader(String name) {
				return name.equals("b") ? "1" : "0";
			}
			@Override
			public HttpBody body() {
				return of("3".getBytes());
			}
		}));
		printResp(doService(new Request(null, "something/b?a=2", null, HttpMethod.GET){
			@Override
			public String getHeader(String name) {
				return name.equals("b") ? "1" : "0";
			}
			@Override
			public HttpBody body() {
				return of("3".getBytes());
			}
		}));
		printResp(doService(new Request(null, "something/a/0.1-2?a=2", null, HttpMethod.GET){
        }));
	}

	private static Response doService(Request request) {
		return ApplicationContext.fetchBean(ServiceContext.class).get(request.path()).invoke(request);
	}

	private static void printResp(Response resp) {
		byte[] bytes = new byte[resp.content.readableBytes()];
		resp.content.readBytes(bytes);
		System.out.println(new String(bytes));
	}
	
	public static HttpBody of(byte[] bytes) {
	    HttpBody body = new HttpBody(null);
	    body.setByteBuf(Unpooled.copiedBuffer(bytes));
	    return body;
	}
	
}
