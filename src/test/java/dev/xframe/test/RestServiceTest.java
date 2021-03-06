//package dev.xframe.test;
//
//import java.util.function.Consumer;
//
//import org.junit.Ignore;
//
//import dev.xframe.http.Request;
//import dev.xframe.http.Response;
//import dev.xframe.http.request.HttpBody;
//import dev.xframe.http.response.SimpleResponse;
//import dev.xframe.http.service.Rest;
//import dev.xframe.http.service.ServiceBuilder;
//import dev.xframe.http.service.ServiceContext;
//import dev.xframe.http.service.config.BodyDecoder;
//import dev.xframe.http.service.config.RespEncoder;
//import dev.xframe.http.service.config.ServiceConfigSetter;
//import dev.xframe.http.service.rest.HttpArgs;
//import dev.xframe.http.service.rest.HttpMethods;
//import dev.xframe.http.service.rest.RestServiceBuilder;
//import dev.xframe.inject.ApplicationContext;
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.Unpooled;
//import io.netty.handler.codec.http.HttpMethod;
//
//@Ignore
//@Rest("something")
//public class RestServiceTest {
//    
//    @HttpMethods.GET("/b")
//	public String dosomething0(@HttpArgs.Param int a, @HttpArgs.Header long b, @HttpArgs.Body int x) {
//    	long ret = (a + b) / x;
//		return "dosomething0: (" + a + "+" + b + ")/"+x+"=" + ret;
//	}
//    
//    @HttpMethods.GET("/{r}")
//    public String dosomething1(@HttpArgs.Path int r, @HttpArgs.Param int a, @HttpArgs.Header long b, @HttpArgs.Body int x) {
//    	long ret = (a + b) * r / x;
//    	return "dosomething1: (" + a + "+" + b + ")*" + r + "/" + x + "=" + ret;
//    }
//    
//    @HttpMethods.GET("/a/{v}")
//    public String dosomething2(@HttpArgs.Path String v) {
//        return v;
//    }
//    
//    @HttpMethods.GET("/x")
//    public String dosomething3(@HttpArgs.Param boolean x) {
//        return String.valueOf(x);
//    }
//	
//	public static void main(String[] args) throws Throwable {
//		ServiceConfigSetter configurer = new ServiceConfigSetter(){
//            @Override
//            public void setBodyDecoder(Consumer<BodyDecoder> setter) {
//                setter.accept((t, body) -> Integer.parseInt(new String(body)));
//            }
//            @Override
//            public void setRespEncoder(Consumer<RespEncoder> setter) {
//                setter.accept(resp -> Response.of(resp.toString()));
//            }
//		};
//		configurer.load();
//		ApplicationContext.registBean(configurer);
//		ApplicationContext.registBean(Injection.makeInstanceAndInject(ServiceBuilder.class));
//		ApplicationContext.registBean(Injection.makeInstanceAndInject(RestServiceBuilder.class));
//		ApplicationContext.registBean(Injection.makeInstanceAndInject(ServiceContext.class));
//		ApplicationContext.fetchBean(ServiceContext.class).defineService(RestServiceTest.class);
//		
//		printResp(doService(new Request(null, "something/2?a=2", null, HttpMethod.GET){
//			@Override
//			public String getHeader(String name) {
//				return name.equals("b") ? "1" : "0";
//			}
//			@Override
//			public HttpBody body() {
//				return of("3".getBytes());
//			}
//		}));
//		printResp(doService(new Request(null, "something/b?a=2", null, HttpMethod.GET){
//			@Override
//			public String getHeader(String name) {
//				return name.equals("b") ? "1" : "0";
//			}
//			@Override
//			public HttpBody body() {
//				return of("3".getBytes());
//			}
//		}));
//		printResp(doService(new Request(null, "something/a/0.1-2?a=2", null, HttpMethod.GET){
//        }));
//	}
//
//	private static Response doService(Request request) throws Throwable {
//		return ApplicationContext.fetchBean(ServiceContext.class).get(request.path()).invoke(request);
//	}
//
//	private static void printResp(Response resp) {
//		ByteBuf content = ((SimpleResponse) resp).content();
//		byte[] bytes = new byte[content.readableBytes()];
//		content.readBytes(bytes);
//		System.out.println(new String(bytes));
//	}
//	
//	public static HttpBody of(byte[] bytes) {
//	    ByteBuf buf = Unpooled.copiedBuffer(bytes);
//	    HttpBody body = new HttpBody(null) {
//            protected ByteBuf content() {
//                return buf;
//            }
//	    };
//	    return body;
//	}
//	
//}
