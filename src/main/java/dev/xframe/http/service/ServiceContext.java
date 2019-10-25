package dev.xframe.http.service;

import java.lang.reflect.Modifier;
import java.util.List;

import dev.xframe.http.service.config.ErrorHandler;
import dev.xframe.http.service.config.FileHandler;
import dev.xframe.http.service.config.RequestInteceptor;
import dev.xframe.http.service.config.ServiceConfig;
import dev.xframe.http.service.path.PathMap;
import dev.xframe.http.service.path.PathMatcher;
import dev.xframe.http.service.path.PathPattern;
import dev.xframe.http.service.path.PathTemplate;
import dev.xframe.injection.Bean;
import dev.xframe.injection.Eventual;
import dev.xframe.injection.Inject;
import dev.xframe.injection.code.Codes;

/**
 * @author luzj
 */
@Bean
public class ServiceContext implements Eventual {
    
    @Inject
    private ServiceConfig config;
    @Inject
    private ServiceBuilder builder;
    
    private PathMap<Pair> services;
    
    private ConflictHandler conflictHandler;
    
    public ServiceContext() {
        services = new PathMap<>();
        conflictHandler = this::conflict;
    }
    
    public void registService(String path, Service service) {
    	registService(path, service, conflictHandler);
    }
    
	public void registService(String path, Service service, ConflictHandler conflictHandler) {
    	PathTemplate temp = new PathTemplate(path);
    	PathPattern pattern = new PathPattern(temp);
    	Pair old = services.put(temp.mapping(), new Pair(pattern, service));
    	if(old != null) conflictHandler.handle(path, old.serivce, service);
    }
    
    public static class ServiceInvoker {
    	final PathMatcher matcher;
    	final Service service;
    	ServiceInvoker(PathMatcher matcher, Service service) {
    		this.matcher = matcher;
    		this.service = service;
		}
    	public Response invoke(Request req) {
    		return service.service(req, matcher);
    	}
    }
    
    public static class Pair {
    	PathPattern pattern;
    	Service serivce;
    	Pair(PathPattern pattern, Service service) {
    		this.pattern = pattern;
    		this.serivce = service;
		}
    }

    public ServiceInvoker get(String path) {
    	Pair pair = services.get(path);
    	if(pair != null) {
    		PathMatcher matcher = pair.pattern.compile(path);
    		if(matcher.find()) {
    			return new ServiceInvoker(matcher, pair.serivce);
    		}
    	}
    	return null;
    }

    public int size() {
        return services.size();
    }
    
    public FileHandler fileHandler() {
        return config.getFileHandler();
    }
    public ErrorHandler errorHandler() {
        return config.getErrorhandler();
    }
    public RequestInteceptor Interceptor() {
        return config.getInteceptor();
    }
    
    @Override
    public void eventuate() {
        registServices(Codes.getDeclaredClasses());
    }
    
    public void registServices(List<Class<?>> clazzes) {
    	registServices(clazzes, conflictHandler);
    }
    
    private void conflict(String path, Service s1, Service s2) {
		throw new IllegalArgumentException(String.format("conflict path %s in [%s:%s]", path, s1, s2));
	}

	public void registServices(List<Class<?>> clazzes, ConflictHandler conflictHandler) {
		for (Class<?> clazz : clazzes) registService(clazz, conflictHandler);
	}
	
	public void registService(Class<?> clazz) {
		registService(clazz, conflictHandler);
	}
	
    public void registService(Class<?> clazz, ConflictHandler conflictHandler) {
        String path = Service.findPath(clazz);
        if(path != null && !Modifier.isAbstract(clazz.getModifiers()) && !Modifier.isInterface(clazz.getModifiers())) {
            ConflictHandler osch = this.conflictHandler;
            this.conflictHandler = conflictHandler;
            registService(path, builder.build(clazz), conflictHandler);
            this.conflictHandler = osch;
        }
    }
    
    @FunctionalInterface
    public static interface ConflictHandler {
    	
    	public void handle(String path, Service s1, Service s2);

    }


}
