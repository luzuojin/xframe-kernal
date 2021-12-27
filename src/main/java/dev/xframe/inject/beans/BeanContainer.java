package dev.xframe.inject.beans;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * 数据结构为Array,为了访问时比Map性能更好/占用内存更小 (所以需要Index结构)
 * 根据BeanIndexes中所有的BeanBinder创建实例Bean(同一个BeanBinder只有一个实例)
 * 完成Bean的初始化/注入过程
 * @author luzj
 */
public class BeanContainer implements BeanFetcher {
	
	protected BeanIndexes indexes;
	
	private Object[] beans;
	private BitSet flags;
	
	public BeanContainer(BeanIndexes indexes) {
		int cap = indexes.length();
		this.indexes = indexes;
		this.beans = new Object[cap];
		this.flags = new BitSet(cap);
	}
	
	synchronized void ensureCap(int cap) {
		if(cap > beans.length) {
			beans = Arrays.copyOf(beans, cap);
			flags.ensureCap(cap);
		}
	}
	
	public void setBean(int index, Object bean) {
		int aIndex = indexes.arrayIndex(index);
		ensureCap(aIndex+1);
		beans[aIndex] = bean;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getBean(int index) {
		int aIndex = indexes.arrayIndex(index);
		if(aIndex < 0 || aIndex >= beans.length) {
			return null;
		}
		return (T) beans[aIndex];
	}
	
	protected boolean getFlag(int index) {
		return flags.get(indexes.arrayIndex(index));
	}
	protected void setFlag(int index, boolean flag) {
		flags.set(indexes.arrayIndex(index), flag);
	}
	
	private Object getOrNew(BeanBinder binder) {
		Object bean = getBean(binder.getIndex());
		return bean == null ? newAndCache(binder) : bean;
	}
	
    private Object newAndCache(BeanBinder binder) {
        Object bean = binder.newInstance();
        setBean(binder.getIndex(), bean);
        return bean;
    }
	
	private void loadBean(BeanBinder binder, Object bean) {
		int index = binder.getIndex();
        if(!this.getFlag(index)) {
			loadBeanExec(binder, bean);
			this.setFlag(index, true);
		}
	}
	
	//完成Bean初始化过程
	protected void loadBeanExec(BeanBinder binder, Object bean) {
		binder.integrate(bean, this);
		BeanLoader.doLoad0(bean);
	}

	public void integrate() {
	    Iterator<BeanBinder> it = indexes.iterator();
	    while(it.hasNext()) {
	        integrate(it.next());
	    }
	}

	public synchronized void integrate(BeanBinder binder) {
		if(binder.getIndex() != -1) {
			this.loadBean(binder, getOrNew(binder));
		}
	}
	
	public BeanIndexes indexes() {
	    return this.indexes;
	}
	
	public List<BeanBinder> binders() {
		return indexes.binders();
	}
	
	@Override
	public synchronized Object fetch(int index) {
		Object bean = getBean(index);
		if(bean == null) {
			BeanBinder binder = indexes.getBinder(index);
			if(binder != null) {//仅创建对象, 需要调用integrate完成注入/Loadable
				bean = newAndCache(binder);
			}
		}
		return bean;
	}
	
}
