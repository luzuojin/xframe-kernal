package dev.xframe.tools;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * class文件扫描
 * 
 * @author luzj
 */
public class XScanner {
	
    private static final String MANFEST_CLASS_PATH = "Class-Path";
    
    private static final String WIN_FILE_SEPARATOR = "\\";
    // 文件分隔符"\"
    private static final String FILE_SEPARATOR = "/";
    // package扩展名分隔符
    private static final String PACKAGE_SEPARATOR = ".";
    // java类文件的扩展名
    private static final String CLASS_FILE_EXT = ".class";
    // jar类文件的扩展名
    private static final String JAR_FILE_EXT = ".jar";
    // jar包内路径分隔符
    private static final String INSIDE_SEPARATOR = "!/";
    
	public static String fromURIPath(String path) {
		String p = path;
		// "/foo!/" --> "/foo"
		if (p.length() > 2 && p.endsWith("!/")) {//jar path
			p = p.substring(0, p.length() - 2);
		}
		if ((p.length() > 2) && (p.charAt(2) == ':')) {//win path
			// "/c:/foo" --> "c:/foo"
			p = p.substring(1);
			// "c:/foo/" --> "c:/foo", but "c:/" --> "c:/"
			if ((p.length() > 3) && p.endsWith("/"))
				p = p.substring(0, p.length() - 1);
		} else if ((p.length() > 1) && p.endsWith("/")) {//unix path
			// "/foo/" --> "/foo"
			p = p.substring(0, p.length() - 1);
		}
		return p;
	}
	
	private static String urlToPath(URL url) {
		return fromURIPath(url.getFile());
	}
    
    private static File newFile(String path) throws UnsupportedEncodingException {
		return new File(URLDecoder.decode(path, "utf-8"));
	}

    /**
     * 获取项目的所有classpath ，包括 APP_CLASS_PATH 和所有的jar文件
     */
    private static Set<String> getClassPathes() throws Exception {
        Set<String> set = new LinkedHashSet<String>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        while(set.isEmpty() && loader != null) {
            if(loader instanceof URLClassLoader) {
                Arrays.stream(((URLClassLoader)loader).getURLs()).map(XScanner::urlToPath).forEach(set::add);
            }
            loader = loader.getParent();
        }
        
        for(String cp : set.stream().filter(path->isJarFile(path)&&!isInsidePath(path)).collect(Collectors.toList())) {
            JarFile jarFile = new JarFile(newFile(cp));
            String manfest = (String) jarFile.getManifest().getMainAttributes().getValue(MANFEST_CLASS_PATH);
            if(!XStrings.isEmpty(manfest)) {
                for (String c : manfest.split("\\s+")) {
                    if(c.contains(":"))
                    	set.add(urlToPath(new URL(c)));
                }
            }
            jarFile.close();
        }
        return set;
    }
    
    private static boolean isClassFile(String name) {
        return name.endsWith(CLASS_FILE_EXT);
    }
    
    private static boolean isJarFile(String path) {
        return path.endsWith(JAR_FILE_EXT);
    }
    
    private static boolean isInsidePath(String path) {
    	return path.contains(INSIDE_SEPARATOR); //@see JarURLConnection
    }

    /**
     * 获取文件下的所有文件(递归)
     */
    private static Set<File> getFiles(File file) {
        Set<File> files = new LinkedHashSet<File>();
        if (!file.isDirectory()) {
            files.add(file);
        } else {
            File[] subFiles = file.listFiles();
            for (File f : subFiles) {
                files.addAll(getFiles(f));
            }
        }
        return files;
    }

    /**
     * 获取文件下的所有.class文件
     */
    private static Set<File> getClassFiles(File file) {
        // 获取所有文件
        Set<File> files = getFiles(file);
        Set<File> classes = new LinkedHashSet<File>();
        // 只保留.class 文件
        for (File f : files) {
            if (isClassFile(f.getName())) {
                classes.add(f);
            }
        }
        return classes;
    }
    
    //jar:file:{jarpath}!/{jarentry}!/
  	private static Set<ClassEntry> getFromJarInsidePath(String path) throws Exception {
      	String[] _path = path.split(INSIDE_SEPARATOR);
      	if(_path.length > 1) {
      		try(JarFile jarFile = new JarFile(Paths.get(new URI(_path[0])).toFile())) {
      			if(isJarFile(_path[1])) {//jar
      				JarEntry entry = jarFile.getJarEntry(_path[1]);
      				return getFromJarStream(jarFile.getInputStream(entry));
      			} else {//folder
      				Enumeration<JarEntry> entries = jarFile.entries();
      				Set<ClassEntry> classes = new LinkedHashSet<ClassEntry>();
      				while (entries.hasMoreElements()) {
      					JarEntry entry = (JarEntry) entries.nextElement();
      					String name = entry.getName();
      					if (name.startsWith(_path[1]) && isClassFile(name)) {
      						String className = name.substring(_path[1].length(), entry.getName().indexOf(CLASS_FILE_EXT)).replace(FILE_SEPARATOR, PACKAGE_SEPARATOR);
      						classes.add(new ClassEntry(className, entry.getSize(), entry.getTime()));
      					}
      				}
      			}
      		}
      	}
      	return new LinkedHashSet<ClassEntry>();
  	}
  	
  	private static Set<ClassEntry> getFromJarStream(InputStream input) throws Exception {
      	Set<ClassEntry> classes = new LinkedHashSet<ClassEntry>();
  		try(JarInputStream jarInput = new JarInputStream(input);) {
  			JarEntry next;
  			while((next = jarInput.getNextJarEntry()) != null) {
  				if(isClassFile(next.getName())) {
  					classes.add(new ClassEntry(next));
  				}
  			}
  		}
  		return classes;
  	}

    /**
     * 得到文件夹下所有class的全包名
     */
    private static Set<ClassEntry> getFromDir(File file) {
        Set<File> files = getClassFiles(file);
        Set<ClassEntry> classes = new LinkedHashSet<ClassEntry>();
        for (File f : files) {
        	classes.add(new ClassEntry(file, f));
        }
        return classes;
    }

    /**
     * 获取jar文件里的所有class文件名
     */
    private static Set<ClassEntry> getFromJar(File file) throws Exception {
        try (JarFile jarFile = new JarFile(file);) {
        	Enumeration<JarEntry> entries = jarFile.entries();
        	Set<ClassEntry> classes = new LinkedHashSet<ClassEntry>();
        	while (entries.hasMoreElements()) {
        		JarEntry entry = (JarEntry) entries.nextElement();
        		if (isClassFile(entry.getName())) {
        			classes.add(new ClassEntry(entry));
        		}
        	}
        	return classes;
        }
    }
    
    public static List<String> getClassPathes(String includes, String excludes) {
        return getClassPathes(new ScanMatcher(includes, excludes));
    }

	public static List<String> getClassPathes(ScanMatcher matcher) {
		List<String> ret = new ArrayList<>();
        try {
            Set<String> classPathes = getClassPathes();
            for (String path : classPathes) {
                if (!isJarFile(path) || matcher.match(path))
                	ret.add(path);
            }
        } catch (Exception ex) {
            // ignore
        }
        return ret;
	}

    public static List<ClassEntry> scan(String includes, String excludes) {
        return scan(new ScanMatcher(includes, excludes));
    }

	public static List<ClassEntry> scan(ScanMatcher matcher) {
		List<ClassEntry> ret = new ArrayList<ClassEntry>();
        try {
            for (String path : getClassPathes()) {
                for (ClassEntry clazz : getFromPath(path)) {
                    if(matcher.match(clazz.name)) {
                        ret.add(clazz);
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            // ignore
        }
        return ret;
	}
    
    private static Set<ClassEntry> getFromPath(String path) throws Exception {
    	if(isInsidePath(path)) {//@see JarURLConnection
    		return getFromJarInsidePath(path);
    	}
    	if(isJarFile(path)) {
    		return getFromJar(newFile(path));
    	}
        return getFromDir(newFile(path));
    }

	public static class ClassEntry {
    	public final String name;
    	public final long size;
    	public final long modifyTime;
    	public ClassEntry(File root, File file) {
    		String fileName = root.toPath().relativize(file.toPath()).toString().replace(WIN_FILE_SEPARATOR, FILE_SEPARATOR);
            this.name = fileName.substring(0, fileName.indexOf(CLASS_FILE_EXT)).replace(FILE_SEPARATOR, PACKAGE_SEPARATOR);
            this.size = file.length();
            this.modifyTime = file.lastModified();
		}
    	public ClassEntry(JarEntry entry) {
    		this.name = entry.getName().substring(0, entry.getName().indexOf(CLASS_FILE_EXT)).replace(FILE_SEPARATOR, PACKAGE_SEPARATOR);
            this.size = entry.getSize();
            this.modifyTime = entry.getTime();
    	}
    	public ClassEntry(String name, long size, long modifyTime) {
    		this.name = name;
    		this.size = size;
    		this.modifyTime = modifyTime;
    	}
        @Override
        public String toString() {
            return name;
        }
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ClassEntry other = (ClassEntry) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
    }
	
	public static class ScanMatcher {
		final SingleMatcher includes;
		final SingleMatcher excludes;
		public ScanMatcher(String includes, String excludes) {
			this.includes = new SingleMatcher(includes);
			this.excludes = new SingleMatcher(excludes);
		}
		public boolean match(String path) {
			return includes.slack(path) && !excludes.strict(path);
		}
	}
    
    private static class SingleMatcher {
        SimpleMatcher jarMatcher;
        SimpleMatcher clsMatcher;
        public SingleMatcher(String res) {
            List<Pattern> jarPatterns = new ArrayList<>(3);
            List<Pattern> clsPatterns = new ArrayList<>(3);
            if(res != null && res.length() > 0) {
            	String[] regs = res.split(";");
            	for (int i = 0; i < regs.length; i++) {
            		String reg = regs[i];
            		List<Pattern> list = reg.endsWith(".jar") ? jarPatterns : clsPatterns;
            		reg = reg.endsWith("*") ? reg : reg + "$";
            		reg = reg.replace(".", "\\.");
            		reg = reg.replace("*", ".*");
            		list.add(Pattern.compile(reg));
            	}
            }
            jarMatcher = new SimpleMatcher(jarPatterns.toArray(new Pattern[0]));
            clsMatcher = new SimpleMatcher(clsPatterns.toArray(new Pattern[0]));
        }
        public boolean strict(String path) {
        	return (isJarFile(path) ? jarMatcher : clsMatcher).match(path, false);
        }
        public boolean slack(String path) {
        	return (isJarFile(path) ? jarMatcher : clsMatcher).match(path, true);
        }
    }
    
    private static class SimpleMatcher {
    	final Pattern[] patterns;
    	public SimpleMatcher(Pattern[] patterns) {
    		this.patterns = patterns == null ? new Pattern[0] : patterns;
		}
    	public boolean match(String val, boolean nilMatch) {
    		return (nilMatch && patterns.length == 0) || match(val);
    	}
    	private boolean match(String val) {
            for (Pattern p : patterns) {
                if(p.matcher(val).find()) return true;
            }
            return false;
        }
    }

}
