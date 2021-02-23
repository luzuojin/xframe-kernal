package dev.xframe.inject.beans;

/**
 * 根据Index获取对应的实例
 * 没有初始化的Bean新new实例但不调用初始化逻辑(解决循环依赖问题)
 * @author luzj
 */
public interface BeanFetcher {
	//仅实例, 不执行加载相关的逻辑
	Object fetch(int index);
	
}
