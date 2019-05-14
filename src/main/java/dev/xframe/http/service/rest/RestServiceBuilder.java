package dev.xframe.http.service.rest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import dev.xframe.http.service.Http;
import dev.xframe.http.service.Request;
import dev.xframe.http.service.Service;
import dev.xframe.http.service.ServiceBuilder;
import dev.xframe.http.service.ServiceContext;
import dev.xframe.injection.Bean;
import dev.xframe.injection.Inject;
import dev.xframe.injection.Injection;
import dev.xframe.tools.CtHelper;
import dev.xframe.tools.XStrings;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;

@Bean
public class RestServiceBuilder implements ServiceBuilder {
	
	@Inject
	private RestConfig config;
	@Inject(lazy=true)
	private ServiceContext serviceContext;
	
    public void setConfiguration(RestConfig config) {
        this.config = config;
    }

    public void setServiceContext(ServiceContext serviceContext) {
		this.serviceContext = serviceContext;
	}

	@Override
    public Service build(Class<?> clazz) throws Exception {
        if(RestService.class.isAssignableFrom(clazz)) {
            Service dynamic = buildDynamic(clazz);
            if(dynamic != null) return dynamic;
        }
        return (Service) clazz.newInstance();
    }

	public Service buildDynamic(Class<?> clazz) {
		try {
			RestService service = (RestService) Injection.makeInstanceAndInject(clazz);
			buildSubResourceService(clazz, service);
			return buildAdapter(buildDynamicService(service, findMethods(clazz, false)));
		} catch (Throwable e) {
			//ignore
			throw new IllegalArgumentException(e);
		}
	}

	private void buildSubResourceService(Class<?> clazz, RestService service) throws Exception {
		AtomicInteger index = new AtomicInteger(0);
        Arrays.stream(findMethods(clazz, true)).collect(Collectors.groupingBy(cm->findSubresPath(clazz, cm))).forEach((k, v) -> {
            //check repeated http methods
            serviceContext.registService(k, buildAdapter(buildDynamicService(service, v.toArray(new Method[0]), "_res" + index.incrementAndGet())));
        });
	}

    private String findSubresPath(Class<?> clazz, Method m) {
        try {
            return XStrings.trim(clazz.getAnnotation(Http.class).value(), '/') + '/' + XStrings.trim(findMethodResPath(clazz, m), '/');
        } catch (Throwable e) {
            throw new IllegalArgumentException(e);
        }
    }

	private ServiceAdapter buildAdapter(RestService service) {
		return new ServiceAdapter(service, config.getRespEncoder());
	}
	
	private RestService buildDynamicService(RestService origin, Method[] httpMethods) throws Exception {
		return buildDynamicService(origin, httpMethods, "");
	}
	
	@SuppressWarnings("unchecked")
	private RestService buildDynamicService(RestService origin, Method[] httpMethods, String suffix) {
		try {
            ClassPool pool = ClassPool.getDefault();
            CtClass p = pool.get(RestService.class.getName());
            Class<?> clazz = origin.getClass();
            CtClass c = pool.makeClass(clazz.getName()+"$$dynamic"+suffix);
            c.addInterface(p);
            
            c.addField(CtField.make(BodyDecoder.class.getName() + " _bodyDecoder;", c));
            c.addField(CtField.make(clazz.getName() + " _origin;", c));
            
            CtConstructor cc = CtNewConstructor.make(new CtClass[]{pool.get(BodyDecoder.class.getName()),pool.get(clazz.getName())}, new CtClass[0], c);
            cc.setBody("{this._bodyDecoder=$1;this._origin=$2;}");
            c.addConstructor(cc);
            
            for (Method httpMethod : httpMethods) {
                CtMethod cm = p.getDeclaredMethod(findMethodName(httpMethod));
                StringBuilder body = new StringBuilder().append("{");
                StringBuilder argsStr = new StringBuilder();
                Argument[] args = findArgs(httpMethod);
                for (int i = 0; i < args.length; i++) {
                    Argument arg = args[i];
                    body.append(arg.type.getName()).append(" ").append("_").append(i).append(" = ").append(toDecodeStr(arg)).append(";");
                    argsStr.append(i == 0 ? "" : ",").append("_").append(i);
                }
                body.append("return this._origin.").append(httpMethod.getName()).append("(").append(argsStr).append(");").append("}");
                
                c.addMethod(CtHelper.copy(cm, body.toString(), c));
            }

            c.addMethod(CtNewMethod.make("public String toString() {return _origin.toString();}", c));
            return (RestService) c.toClass().getConstructor(BodyDecoder.class, clazz).newInstance(config.getBodyDecoder(), origin);
        } catch (Throwable e) {
            throw new IllegalArgumentException(e);
        }
	}
	
	private String findMethodName(Method httpMethod) {
	    if(httpMethod.isAnnotationPresent(HttpMethods.POST.class)) {
	        return "post";
	    }
	    if(httpMethod.isAnnotationPresent(HttpMethods.PUT.class)) {
	        return "put";
	    }
	    if(httpMethod.isAnnotationPresent(HttpMethods.DELETE.class)) {
	        return "delete";
	    }
	    if(httpMethod.isAnnotationPresent(HttpMethods.GET.class)) {
	        return "get";
	    }
	    if(httpMethod.isAnnotationPresent(HttpMethods.OPTIONS.class)) {
	        return "options";
	    }
        throw new IllegalArgumentException("can`t be here");
    }

    public String toDecodeStr(Argument arg) {
		if(arg.origin == Origin.REQUEST) return "$1";
		if(arg.origin == Origin.QUERY) return Primitives.castStr(arg, "$1.getParam(\""+ arg.name+"\")");
		if(arg.origin == Origin.HEDAER) return Primitives.castStr(arg, "$1.getHeader(\""+ arg.name+"\")");
		if(arg.origin == Origin.BODY) return Primitives.castStr(arg, "_bodyDecoder.decode("+arg.type.getName()+".class,$1.body())");
		if(arg.origin == Origin.PATH) return Primitives.castStr(arg, "$2.group(\""+arg.name+"\")");
		throw new IllegalArgumentException("can`t be here");
	}

	private Argument[] findArgs(Method m) throws Exception {
		Class<?>[] types = m.getParameterTypes();
		Object[][] annoss = m.getParameterAnnotations();
		Argument[] args = new Argument[types.length];
		for (int i = 0; i < args.length; i++) {
			Object[] annos = annoss[i];
			Class<?> type = types[i];
			
			Origin origin = null;
			String value = null;
			
			if(type.getName().equals(Request.class.getName())) {
                origin = Origin.REQUEST;
                value = "";
            } else {
                for (Object anno : annos) {
                    if(anno instanceof RequestParam) {
                        origin = Origin.QUERY;
                        value = ((RequestParam) anno).value();
                        break;
                    } else if(anno instanceof RequestHeader) {
                        origin = Origin.HEDAER;
                        value = ((RequestHeader) anno).value();
                        break;
                    } else if(anno instanceof RequestPath) {
                        origin = Origin.PATH;
                        value = ((RequestPath) anno).value();
                        break;
                    } else if(anno instanceof RequestBody) {
                        origin = Origin.BODY;
                        break;
                    } 
                }
                if(XStrings.isEmpty(value))
                	value = m.getParameters()[i].getName();
                
                if(XStrings.isEmpty(value))
                	throw new IllegalArgumentException("none usable name for method["+m.getName()+"] args[]" + i);
            }
			
            if (origin == null)
                throw new IllegalArgumentException("not support argument type [" + type.getName() + ":" + value + "]");
            
			args[i] = new Argument(origin, type, value);
		}
		return args;
	}

	private Method[] findMethods(Class<?> clazz, boolean subRes) throws Exception {
		List<Method> r = new ArrayList<>();
		Method[] ms = clazz.getDeclaredMethods();
		for (Method m : ms) {
		    String res = findMethodResPath(clazz, m);
		    if(res != null && (XStrings.isEmpty(res) != subRes)) {
		        r.add(m);
		    }
		}
		return r.toArray(new Method[0]);
	}
	
	private String findMethodResPath(Class<?> clazz, Method m) throws Exception {
		if(m.isAnnotationPresent(HttpMethods.GET.class)) {
            return m.isAnnotationPresent(Http.class) ? ((Http) m.getAnnotation(Http.class)).value() : ((HttpMethods.GET) m.getAnnotation(HttpMethods.GET.class)).value();
	    }
	    if(m.isAnnotationPresent(HttpMethods.POST.class)) {
	        return m.isAnnotationPresent(Http.class) ? ((Http) m.getAnnotation(Http.class)).value() : ((HttpMethods.POST) m.getAnnotation(HttpMethods.POST.class)).value();
	    }
	    if(m.isAnnotationPresent(HttpMethods.PUT.class)) {
	        return m.isAnnotationPresent(Http.class) ? ((Http) m.getAnnotation(Http.class)).value() : ((HttpMethods.PUT) m.getAnnotation(HttpMethods.PUT.class)).value();
	    }
	    if(m.isAnnotationPresent(HttpMethods.DELETE.class)) {
	        return m.isAnnotationPresent(Http.class) ? ((Http) m.getAnnotation(Http.class)).value() : ((HttpMethods.DELETE) m.getAnnotation(HttpMethods.DELETE.class)).value();
	    }
	    if(m.isAnnotationPresent(HttpMethods.OPTIONS.class)) {
	        return m.isAnnotationPresent(Http.class) ? ((Http) m.getAnnotation(Http.class)).value() : ((HttpMethods.OPTIONS) m.getAnnotation(HttpMethods.OPTIONS.class)).value();
	    }
	    return null;
	}
	
	public static class Argument {
		public final Origin origin;
		public final Class<?> type;
		public final String name;
		
		public Argument(Origin origin, Class<?> type, String name) {
			this.origin = origin;
			this.type = type;
			this.name = name;
		}
	}
	
	public static enum Origin {
		HEDAER, QUERY, PATH, BODY, REQUEST;
	}
	
	public static class Primitives {
		public static int toint(String val) {
			return Integer.parseInt(val);
		}
		public static int toint(Object val) {
			return toInteger(val).intValue();
		}
		
		public static Integer toInteger(String val) {
			return new Integer(val);
		}
		public static Integer toInteger(Object val) {
			return (Integer)val;
		}
		
		public static long tolong(String val) {
			return Long.parseLong(val);
		}
		public static long tolong(Object val) {
			return toLong(val).longValue();
		}
		
		public static Long toLong(String val) {
			return new Long(val);
		}
		public static Long toLong(Object val) {
			return (Long) val;
		}
		
		public static boolean toboolean(String val) {
		    return Boolean.parseBoolean(val);
		}
		public static boolean toboolean(Object val) {
		    return toBoolean(val).booleanValue();
		}
		
		public static Boolean toBoolean(String val) {
		    return new Boolean(val);
		}
		public static Boolean toBoolean(Object val) {
		    return (Boolean) val;
		}
		
		public static String castStr(Argument arg, String param) {
			if(boolean.class.equals(arg.type) || int.class.equals(arg.type) || long.class.equals(arg.type) || Boolean.class.equals(arg.type) || Integer.class.equals(arg.type) || Long.class.equals(arg.type)) {
				return Primitives.class.getName() + ".to" + arg.type.getSimpleName() + "(" + param + ")";
			}
			return "(" + arg.type.getName() + ")" + param;
		}
	}

}
