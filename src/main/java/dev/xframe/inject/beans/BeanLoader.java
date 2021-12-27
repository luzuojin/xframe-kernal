package dev.xframe.inject.beans;

import dev.xframe.inject.Loadable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanLoader {
    
    static final Logger logger = LoggerFactory.getLogger(BeanLoader.class);

    /**
     * for BeanHelper without used time log
     * @param bean
     */
    static void doLoad(Object bean) {
        if(bean instanceof Loadable) {
            ((Loadable) bean).load();
        }
    }

    /**
     * for bean integrate
     * @param bean
     */
    static void doLoad0(Object bean) {
        if(bean instanceof Loadable) {
            long start = System.currentTimeMillis();
            ((Loadable) bean).load();
            long used = System.currentTimeMillis() - start;
            logger.info("Load completed {} used {}ms", getClsName(bean), used);
        }
    }

    private static String getClsName(Object bean) {//@Reloadable ...etc
        return (BeanHelper.isProxy(bean) ? BeanHelper.getProxyDelegate(bean) : bean).getClass().getName();
    }

}
