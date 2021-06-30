package dev.xframe.dev;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.Arrays;

import dev.xframe.http.Response;
import dev.xframe.http.service.Http;
import dev.xframe.http.service.Rest;
import dev.xframe.http.service.ServiceContext;
import dev.xframe.http.service.rest.HttpArgs;
import dev.xframe.http.service.rest.HttpMethods;
import dev.xframe.inject.ApplicationContext;
import dev.xframe.inject.Inject;
import dev.xframe.inject.Reloadable;
import dev.xframe.inject.code.Codes;
import dev.xframe.net.cmd.Cmd;
import dev.xframe.net.cmd.CommandContext;
import dev.xframe.utils.XStrings;

@Rest("dev/hotfix")
public class HotfixService {

    @Inject
    private ServiceContext serCtx;
    @Inject
    private CommandContext cmdCtx;

    /**
     * @param c (class file path)
     * @return
     */
    //替换整个类
    @HttpMethods.GET("swap")
    public Object swapByClassFile(@HttpArgs.Param String c) {
        assert c != null;
        try {
            Class<?> clazz = Codes.versioningClass(new File(c));
            if(clazz != null) {
                return swapClass(clazz);
            }
            return Response.of("nil");
        } catch (Throwable t) {
            return Response.of(XStrings.getStackTrace(t));
        }
    }

    @HttpMethods.GET("reswap")
    public Object reswap(@HttpArgs.Param String name, @HttpArgs.Param int version) {
        assert name != null;
        try {
            Class<?> clazz = Class.forName(Codes.naming(name, version));
            if(clazz != null) {
                return swapClass(clazz);
            }
            return Response.of("nil");
        } catch (Throwable t) {
            return Response.of(XStrings.getStackTrace(t));
        }
    }

    private Response swapClass(Class<?> clazz) {
        if(clazz.isAnnotationPresent(Rest.class) || clazz.isAnnotationPresent(Http.class)) {
            serCtx.defineService(clazz, (x,y,z)->{});
        } else if(clazz.isAnnotationPresent(Cmd.class)) {
            cmdCtx.defineCmd(clazz);
        } else if(clazz.isAnnotationPresent(Reloadable.class)){
            ApplicationContext.replace(Codes.rebaseClass(clazz), clazz);
        }
        return Response.of(clazz.getName());
    }

    //替换方法区
    @HttpMethods.GET("redefine")
    public Object redefineByClassFile(@HttpArgs.Param String c) {
        assert c != null;
        try {
            boolean r = Codes.redefineClass(new File(c));
            return Response.of(r ? "success" : "failed");
        } catch (Throwable t) {
            return Response.of(XStrings.getStackTrace(t));
        }
    }

    @HttpMethods.POST("swap")
    //[POST] (cat x.java | curl -X POST -d @_ http://127.0.0.1/dev/hotfix/swap)
    public Object swapByJavaContent(@HttpArgs.Body String source) {
        //暂未实现
        return Response.of("nil");
    }

    @SuppressWarnings("unchecked")
    @HttpMethods.GET("reload")
    public Object reload(@HttpArgs.Param String c) {
        String[] clsNames = XStrings.isEmpty(c) ? null : c.split(",");
        if(clsNames == null || clsNames.length == 0) {
            try {
                for (String clsName : clsNames) {
                    Class<?> cls = Class.forName(clsName);
                    if(cls.isAnnotation()) {
                        ApplicationContext.reload(clz->clz.isAnnotationPresent((Class<? extends Annotation>) cls));
                    } else {
                        ApplicationContext.reload(cls);
                    }
                }
                return Response.of(String.format("Load %s Finisned", Arrays.toString(clsNames)));
            } catch (ClassNotFoundException e) {
                //ignore
            }
        }
        return Response.of(String.format("Class %s not found", Arrays.toString(clsNames)));
    }

}
