package dev.xframe.module;

import java.util.concurrent.TimeUnit;


/**
 * 
 * 模块类型
 * 暂时只需要,长驻/非长驻内存类型
 * @author luzj
 *
 */
public enum ModuleType {
	
	RESIDENT (1, TimeUnit.MINUTES.toMillis(60)),
	
	TRANSIENT (2, TimeUnit.MINUTES.toMillis(15));
	
	public final int code;
	public final long unloadIdleTime;
	
    private ModuleType(int code, long time) {
    	this.code = code;
        this.unloadIdleTime = time;
    }	
	
}
