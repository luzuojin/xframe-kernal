package dev.xframe.inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Loadable {
	
	Logger logger = LoggerFactory.getLogger(Loadable.class);
    
    public void load();
    
    static void doLoad(Object bean) {
		if(bean instanceof Loadable) {
            long start = System.currentTimeMillis();
            ((Loadable) bean).load();
            long used = System.currentTimeMillis() - start;
            logger.info("Load completed {} used {}ms", bean.getClass().getName(), used);
        }
	}

}
