package dev.xframe.inject.beans;

/**
 * 根据@Inject的Field对应的Keyword(class/name)获取BeanIndexes中对应的索引值
 * 获取索引值的过程只在真实需要时获取(避免特殊情况下构建Injector时BeanIndexes没有初始化完成 比如@Prototype)
 * @author luzj
 */
public interface BeanIndexing {
	
	//每一个实例都对应一个index, 方便快速访问到对应的实例(Array 替代 Map)
	default int indexOf(Object keyword) {
		BeanBinder binder = indexOf0(keyword);
		return binder == null ? -1 : binder.index;
	}
	
	BeanBinder indexOf0(Object keyword);

}
