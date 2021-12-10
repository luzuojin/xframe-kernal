package dev.xframe.inject.code;

import dev.xframe.utils.XPaths;
import dev.xframe.utils.XStrings;
import dev.xframe.utils.XUnsafe;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
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
public class Scanner {

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

    @SuppressWarnings("unchecked")
    private static Set<String> getClassPathes0(ClassLoader loader) throws Exception {
        Field ucpField = getUcpField(loader.getClass());
        Object ucpObj = XUnsafe.getObject(loader, XUnsafe.objectFieldOffset(ucpField));
        //URLClassPath.path:List<URL>
        Field pathField = ucpObj.getClass().getDeclaredField("path");
        List<URL> urls = (List<URL>) XUnsafe.getObject(ucpObj, XUnsafe.objectFieldOffset(pathField));
        return urls.stream().map(XPaths::toPath).collect(Collectors.toSet());
    }
    //URLClassLoader/BuiltinClassLoader.ucp:URLClassPath
    private static Field getUcpField(Class<?> loaderCls) {
        try {
            return loaderCls.getDeclaredField("ucp");
        } catch (NoSuchFieldException | SecurityException e) {
            return ClassLoader.class.isAssignableFrom(loaderCls) ? getUcpField(loaderCls.getSuperclass()) : null;
        }
    }
    /**
     * 获取项目的所有classpath ，包括 APP_CLASS_PATH 和所有的jar文件
     */
    private static Set<String> getClassPathes() throws Exception {
        Set<String> set = getClassPathes0(Thread.currentThread().getContextClassLoader());
        for(String cp : set.stream().filter(path->isJarFile(path)&&!isInsidePath(path)).collect(Collectors.toList())) {
            try(JarFile jarFile = new JarFile(new File(cp))) {
                if(jarFile.getManifest() == null)
                    continue;
                String manfest = jarFile.getManifest().getMainAttributes().getValue(MANFEST_CLASS_PATH);
                if(XStrings.isEmpty(manfest))
                    continue;
                for (String c : manfest.split("\\s+")) {
                    if(c.contains(":")) {
                        set.add(XPaths.toPath(new URL(c)));
                    } else {
                        set.add(XPaths.toFile(c).getAbsolutePath());
                    }
                }
            }
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
        Set<File> files = new LinkedHashSet<>();
        if (!file.isDirectory()) {
            files.add(file);
        } else {
            File[] subFiles = file.listFiles();
            if(subFiles != null) {
                for (File f : subFiles) {
                    files.addAll(getFiles(f));
                }
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
        Set<File> classes = new LinkedHashSet<>();
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
                    Set<ClassEntry> classes = new LinkedHashSet<>();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.startsWith(_path[1]) && isClassFile(name)) {
                            String className = name.substring(_path[1].length(), entry.getName().indexOf(CLASS_FILE_EXT)).replace(FILE_SEPARATOR, PACKAGE_SEPARATOR);
                            classes.add(new ClassEntry(className, entry.getSize(), entry.getTime()));
                        }
                    }
                    return classes;
                }
            }
        }
        return new LinkedHashSet<>();
    }

    private static Set<ClassEntry> getFromJarStream(InputStream input) throws Exception {
        Set<ClassEntry> classes = new LinkedHashSet<>();
        try(JarInputStream jarInput = new JarInputStream(input)) {
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
        Set<ClassEntry> classes = new LinkedHashSet<>();
        for (File f : files) {
            classes.add(new ClassEntry(file, f));
        }
        return classes;
    }

    /**
     * 获取jar文件里的所有class文件名
     */
    private static Set<ClassEntry> getFromJar(File file) throws Exception {
        try (JarFile jarFile = new JarFile(file)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            Set<ClassEntry> classes = new LinkedHashSet<>();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
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
        List<ClassEntry> ret = new ArrayList<>();
        try {
            for (String path : getClassPathes(matcher)) {
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
            return getFromJar(new File(path));
        }
        return getFromDir(new File(path));
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
        private final SingleMatcher includes;
        private final SingleMatcher excludes;
        public ScanMatcher(String includes, String excludes) {
            this.includes = new SingleMatcher(includes, true);
            this.excludes = new SingleMatcher(excludes, false);
        }
        public boolean match(String path) {
            return includes.slack(path) && !excludes.strict(path);
        }
        public ScanMatcher merge(ScanMatcher matcher) {
            includes.merge(matcher.includes);
            excludes.merge(matcher.excludes);
            return this;
        }
    }

    private static class SingleMatcher {
        SimpleMatcher jarMatcher;
        SimpleMatcher clsMatcher;
        public SingleMatcher(String res, boolean matchJarWhenEmpty) {
            List<Pattern> jarPatterns = new ArrayList<>(3);
            List<Pattern> clsPatterns = new ArrayList<>(3);
            if(!XStrings.isEmpty(res)) {
                for (String reg : res.split(";")) {
                    List<Pattern> list = reg.endsWith(".jar") ? jarPatterns : clsPatterns;
                    list.add(Pattern.compile(quote(reg)));
                }
            }
            if(matchJarWhenEmpty && jarPatterns.isEmpty()) {
                jarPatterns.add(Pattern.compile(quote("*.jar")));
            }
            jarMatcher = new SimpleMatcher(jarPatterns.toArray(new Pattern[0]));
            clsMatcher = new SimpleMatcher(clsPatterns.toArray(new Pattern[0]));
        }
        private String quote(String reg) {
            reg = reg.endsWith("*") ? reg : reg + "$";
            reg = reg.replace(".", "\\.");
            reg = reg.replace("*", ".*");
            return reg;
        }
        public boolean strict(String path) {
            return (isJarFile(path) ? jarMatcher : clsMatcher).match(path, false);
        }
        public boolean slack(String path) {
            return (isJarFile(path) ? jarMatcher : clsMatcher).match(path, true);
        }
        public void merge(SingleMatcher matcher) {
            jarMatcher.merge(matcher.jarMatcher);
            clsMatcher.merge(matcher.clsMatcher);
        }
    }

    private static class SimpleMatcher {
        private Pattern[] patterns;
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
        public void merge(SimpleMatcher n) {
            int oLen = patterns.length;
            int cLen = n.patterns.length;
            Pattern[] t = Arrays.copyOf(patterns, oLen + cLen);
            System.arraycopy(n.patterns, 0, t, oLen, cLen);
            patterns = t;
        }
    }

}
