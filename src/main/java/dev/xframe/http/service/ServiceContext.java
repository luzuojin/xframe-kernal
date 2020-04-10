package dev.xframe.http.service;

import java.lang.reflect.Modifier;
import java.util.List;

import dev.xframe.http.service.path.PathMap;
import dev.xframe.http.service.path.PathMatcher;
import dev.xframe.http.service.path.PathPattern;
import dev.xframe.http.service.path.PathTemplate;
import dev.xframe.inject.Bean;
import dev.xframe.inject.Eventual;
import dev.xframe.inject.Inject;
import dev.xframe.inject.code.Codes;

/**
 * @author luzj
 */
@Bean
public class ServiceContext implements Eventual {
    
    @Inject
    private ServiceBuilder builder;
    
    private PathMap<Pair> paths;
    
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
    	Pair old = paths.put(temp.mapping(), new Pair(pattern, service));
    	if(old != null) conflictHandler.handle(path, old.serivce, service);
    }
    
    public static class Pair {
    	PathPattern pattern;
    	Service serivce;
    	Pair(PathPattern pattern, Service service) {
    		this.pattern = pattern;
    		this.serivce = service;
		}
    }

    public ServicePair get(String path) {
    	Pair pair = paths.get(path);
    	if(pair != null) {
    		PathMatcher matcher = pair.pattern.compile(path);
    		if(matcher.find()) {
    			return new ServicePair(matcher, pair.serivce);
    		}
    	}
    	return null;
    }

    public int size() {
        return paths.size();
    }
    
    @Override
    public void eventuate() {
        defineServices(Codes.getDeclaredClasses());
    }
    
    public void defineServices(List<Class<?>> clazzes) {
    	defineServices(clazzes, conflictHandler);
    }
    
    private void conflict(String path, Service s1, Service s2) {
		throw new IllegalArgumentException(String.format("conflict path %s in [%s:%s]", path, s1, s2));
	}

	public void defineServices(List<Class<?>> clazzes, ConflictHandler conflictHandler) {
		for (Class<?> clazz : clazzes) defineService(clazz, conflictHandler);
	}
	
	public void defineService(Class<?> clazz) {
		defineService(clazz, conflictHandler);
	}
	
    public void defineService(Class<?> clazz, ConflictHandler conflictHandler) {
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
