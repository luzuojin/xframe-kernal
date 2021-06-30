package dev.xframe.inject.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.inject.Loadable;
import dev.xframe.inject.code.ProxyBuilder.IProxy;

public class BeanLoader {
    
    static final Logger logger = LoggerFactory.getLogger(BeanLoader.class);;
    
    public static void doLoad(Object bean) {
        if(bean instanceof Loadable) {
            long start = System.currentTimeMillis();
            ((Loadable) bean).load();
            long used = System.currentTimeMillis() - start;
            //@Reloadable ...etc
            String clsName = (bean instanceof IProxy ? ((IProxy) bean)._getDelegate() : bean).getClass().getName();
            logger.info("Load completed {} used {}ms", clsName, used);
        }
    }

}
