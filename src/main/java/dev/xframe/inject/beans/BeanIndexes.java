package dev.xframe.inject.beans;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 可以指定一个Index的偏移量, 方便直接使用Index区分自定义的BeanContainer
 * 存储BeanBinder列表
 * 生成BeanBinder的索引(同一个BeanBinder对应的所有可注入的接口,索引相同)
 * @author luzj
 */
public class BeanIndexes {
	
	final AtomicInteger seed;
	final int offset;
	
	private Map<Object, BeanBinder> indexes = new HashMap<>();
	private BeanBinder[] binders = new BeanBinder[0];
	
	public BeanIndexes() {
		this(0);
	}
	public BeanIndexes(int offset) {
		assert offset >= 0;
		this.offset = offset;
		this.seed = new AtomicInteger(offset);
	}
	
	synchronized void ensureCap(int cap) {
		if(cap > binders.length) {
			binders = Arrays.copyOf(binders, cap);
		}
	}
	
	public int nextIndex() {
		return seed.getAndIncrement();
	}
	
	public int maxIndex() {
		return seed.get();
	}
	
	public synchronized int regist(BeanBinder binder) {
		if(binder.index == -1) {
			binder.index = nextIndex();
			binder.getKeywords().forEach(k->{
				BeanBinder ex = indexes.get(k);
				BeanBinder cr = (ex == null) ? binder : ex.conflict(k, binder);
				indexes.put(k, cr);
			});
			setBinder(binder);
		}
		return binder.index;
	}
	
	public int getIndex(Object keyword) {
		BeanBinder binder = indexes.get(keyword);
		return binder == null ? -1 : binder.index;
	}
	
	public boolean isValidIndex(int binderIndex) {
		int aIndex = arrayIndex(binderIndex);
		return aIndex > -1 && aIndex < binders.length;
	}

	int arrayIndex(int binderIndex) {
		return binderIndex - offset;
	}
	
	public BeanBinder getBinder(int index) {
		int aIndex = arrayIndex(index);
		return aIndex == -1 ? null : binders[aIndex];
	}
	
	private void setBinder(BeanBinder binder) {
		int aIndex = arrayIndex(binder.index);
		ensureCap(aIndex + 1);
		binders[aIndex] = binder;
	}
	
	public List<BeanBinder> binders() {
		return Arrays.asList(binders);
	}
	
	public int length() {
		return binders.length;
	}

}
