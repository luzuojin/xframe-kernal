package dev.xframe.modular.code;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import dev.xframe.modular.Component;
import dev.xframe.modular.ModularAgent;
import dev.xframe.modular.ModularDependence;
import dev.xframe.modular.ModularIgnore;
import dev.xframe.modular.ModularInject;
import dev.xframe.modular.ModularMethods;
import dev.xframe.modular.ModularShare;
import dev.xframe.modular.Module;
import dev.xframe.tools.XSorter;

/**
 * 分析各种annotation
 * 加载关系等
 * @author luzj
 */
public class ModularAnalyzer {

    public static List<ModularElement> analye(Class<?> assemble, List<Class<?>> clazzes) {
        return analyeDependence(assemble, clazzes.stream().filter(c->isNecessaryModularClass(c)).collect(Collectors.toList()));
    }

	private static boolean isNecessaryModularClass(Class<?> clazz) {
		return  isModule(clazz) || isComponent(clazz) || isAgent(clazz);
	}
	
	public static boolean isAgent(Class<?> clazz) {
        return clazz.isInterface() && clazz.isAnnotationPresent(ModularAgent.class);
    }
	
	public static Class<?> getSharableClass(Class<?> clazz) {
        return Arrays.stream(clazz.getInterfaces()).filter(c->c.isAnnotationPresent(ModularShare.class)).findAny().orElse(null);
    }

    public static boolean isComponent(Class<?> clazz) {
        return clazz.isAnnotationPresent(Component.class);
    }

    public static boolean isModule(Class<?> clazz) {
        return !Modifier.isAbstract(clazz.getModifiers()) && !clazz.isInterface() && clazz.isAnnotationPresent(Module.class) && !clazz.isAnnotationPresent(ModularIgnore.class);
    }

    private static List<ModularElement> analyeDependence(Class<?> assemble, List<Class<?>> modularClazzes) {
        XSorter.bubble(modularClazzes, new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> c1, Class<?> c2) {
                return c1.getSimpleName().compareTo(c2.getSimpleName());
            }
        });
        XSorter.bubble(modularClazzes, new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> c1, Class<?> c2) {
                return Boolean.compare(isComponent(c2), isComponent(c1));
            }
        });
        XSorter.bubble(modularClazzes, new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> c1, Class<?> c2) {
                return Boolean.compare(isAgent(c2), isAgent(c1));
            }
        });
        
        List<ModularElement> elements = new ArrayList<ModularElement>();
        Set<Class<?>> marked = new HashSet<>();
        for (Class<?> clazz : modularClazzes) {
            TreeNode root = new TreeNode(clazz);
            analyeDependence(assemble, root, clazz, marked, elements, modularClazzes);
        }
        
        XSorter.bubble(elements, new Comparator<ModularElement>() {
            @Override
            public int compare(ModularElement o1, ModularElement o2) {
                return o1.index - o2.index;
            }
        });
        return elements;
    }
    
    private static int index = 0;
    private static void analyeDependence(Class<?> assemble, TreeNode node, Class<?> clazz, Set<Class<?>> marked, List<ModularElement> modularEls, List<Class<?>> modularClazzes) {
        if(marked.contains(clazz)) { //已经加载完成
            return;
        }
        
        //有依赖 优先加载依赖
        Set<Class<?>> dependences = getDependences(assemble, clazz);
        for (Class<?> dependence : dependences) {
            dependence = analyeAssignedDependence(clazz, dependence, modularClazzes);
            analyeCircularDependence(node, dependence);
            TreeNode self = new TreeNode(node, dependence);
            node.children.add(self);
            analyeDependence(assemble, self, dependence, marked, modularEls, modularClazzes);
        }
        
        //依赖都已经加载完成
        marked.add(clazz);
        modularEls.add(new ModularElement(clazz, index));
        ++ index;
    }
    
    private static Set<Class<?>> getDependences(Class<?> assemble, Class<?> clazz) {
        Set<Class<?>> dependences = new HashSet<>();
        getAnnoDependences(clazz, dependences);
        getFieldsDependences(clazz, dependences, assemble);
        getMethodDependences(clazz, dependences, assemble);
        return dependences;
    }

    private static void getAnnoDependences(Class<?> clazz, Set<Class<?>> dependences) {
        if(clazz.isAnnotationPresent(ModularDependence.class)) {
            Arrays.stream(clazz.getAnnotation(ModularDependence.class).value()).forEach(dependences::add);
        }
    }

    private static void getFieldsDependences(Class<?> clazz, Set<Class<?>> dependences, Class<?> assemble) {
        Class<?> t = clazz;
        while(!Object.class.equals(t) && !t.isInterface()) {
            Field[] fields = t.getDeclaredFields();
            for (Field field : fields) {
                if(field.isAnnotationPresent(ModularInject.class) && !field.getAnnotation(ModularInject.class).lazy() && !field.getType().isAssignableFrom(assemble) && !isAgent(field.getType())) {
                    dependences.add(field.getType());
                }
            }
            t = t.getSuperclass();
        }
    }
    
    private static void getMethodDependences(Class<?> clazz, Set<Class<?>> dependences, Class<?> assemble) {
        Arrays.stream(clazz.getMethods()).filter(m->m.isAnnotationPresent(ModularMethods.Load.class)).forEach(m->{
            for (Class<?> type : m.getParameterTypes()) {
                if(!type.isAssignableFrom(assemble) && !isAgent(type)) {
                    dependences.add(type);
                }
            }
        });
    }

    private static Class<?> analyeAssignedDependence(Class<?> clazz, Class<?> dependence, List<Class<?>> modularClazzes) {
    	for (Class<?> modularClazz : modularClazzes) {
    		if(dependence.isAssignableFrom(modularClazz)) {
    			return modularClazz;
    		}
    	}
    	//can`t be here
		throw new IllegalArgumentException("None modular clazz assigned to [" + dependence.getName() + "] (" + clazz.getName() + ")");
    }

    private static void analyeCircularDependence(TreeNode node, Class<?> clazz) {
        TreeNode tmp = node;
        while(tmp.parent != null) {
            if(tmp.parent.clazz == clazz) {
                throw new IllegalArgumentException("circular dependency by [" + tmp.parent.clazz.getName() + "] and [" + clazz.getName() + "]");
            }
            tmp = tmp.parent;
        }
    }

    static class TreeNode {
        final TreeNode parent;
        final Class<?> clazz;
        final List<TreeNode> children;
        public TreeNode(Class<?> clazz) {
            this(null, clazz);
        }
        public TreeNode(TreeNode parent, Class<?> clazz) {
            this.parent = parent;
            this.clazz = clazz;
            this.children = new ArrayList<TreeNode>();
        }
    }
    
}
