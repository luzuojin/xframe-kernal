package dev.xframe.http.service;

import dev.xframe.http.Request;
import dev.xframe.http.Response;
import dev.xframe.http.service.path.PathMap;
import dev.xframe.http.service.path.PathMatcher;
import dev.xframe.http.service.path.PathPattern;
import dev.xframe.http.service.path.PathTemplate;
import dev.xframe.inject.Bean;
import dev.xframe.inject.Eventual;
import dev.xframe.inject.Inject;
import dev.xframe.inject.code.Clazz;
import dev.xframe.inject.code.Codes;
import dev.xframe.utils.XReflection;

import java.util.List;

/**
 * @author luzj
 */
@Bean
public class ServiceContext implements Eventual {
    
    @Inject
    private ServiceBuilder builder;
    
    private PathMap<ServicePair> paths;
    
    private ConflictHandler conflictHandler;
    
    public ServiceContext() {
        paths = new PathMap<>();
        conflictHandler = this::conflict;
    }
    
    public void registService(String path, Service service) {
    	registService(path, service, conflictHandler);
    }
    
	public void registService(String path, Service service, ConflictHandler conflictHandler) {
    	PathTemplate temp = new PathTemplate(path);
    	PathPattern pattern = new PathPattern(temp);
    	ServicePair old = paths.put(temp.mapping(), new ServicePair(pattern, service));
    	if(old != null) conflictHandler.handle(path, old.service, service);
    }
    
    public static class ServicePair {
    	final PathPattern pattern;
    	final Service service;
    	ServicePair(PathPattern pattern, Service service) {
    		this.pattern = pattern;
    		this.service = service;
		}
    }
    
    public static class ServiceInvoker implements Service {
        final PathMatcher matcher;
        final Service internal;
        ServiceInvoker(PathMatcher matcher, Service service) {
            this.matcher = matcher;
            this.internal = service;
        }
        @Override
        public Response exec(Request req) throws Exception {
            return internal.exec(req, matcher);
        }
    }

    public Service get(String path) {
    	ServicePair sp = paths.get(path);
    	if(sp != null) {
    	    PathMatcher matcher = sp.pattern.compile(path);
            if(matcher.find()) {
                return new ServiceInvoker(matcher, sp.service);
            }
    	}
    	return null;
    }

    public int size() {
        return paths.size();
    }
    
    @Override
    public void eventuate() {
        defineServices(Codes.getScannedClasses(Clazz.filter(Http.class, Rest.class)));
    }
    
    public void defineServices(List<Class<?>> clazzes) {
    	defineServices(clazzes, conflictHandler);
    }
    
    private void conflict(String path, Service s1, Service s2) {
		throw new IllegalArgumentException(String.format("conflict path %s in [%s:%s]", path, s1, s2));
	}

	public void defineServices(List<Class<?>> clazzes, ConflictHandler conflictHandler) {
		for (Class<?> clazz : clazzes) {
		    defineService(clazz, conflictHandler);
		}
	}
	
	public void defineService(Class<?> clazz) {
		defineService(clazz, conflictHandler);
	}
	
    public void defineService(Class<?> clazz, ConflictHandler conflictHandler) {
        String path = Service.findPath(clazz);
        if(path != null && XReflection.isImplementation(clazz)) {
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
