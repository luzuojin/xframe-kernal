package dev.xframe.module.beans;

import java.util.List;
import java.util.stream.Collectors;

import dev.xframe.inject.beans.BeanIndexes;
import dev.xframe.inject.beans.BeanIndexing;
import dev.xframe.module.ModularHelper;

public class ModularIndexes extends BeanIndexes implements BeanIndexing {
	
	static final int OFFSET = 8000;
	
	BeanIndexing gIndexing;
	
	ModularBinder[] residents;
	ModularBinder[] transients;
	
	public ModularIndexes(BeanIndexing gIndexing) {
		super(OFFSET);//指定偏移量
		this.gIndexing = gIndexing;
	}
	
	public void integrate() {
		List<ModularBinder> list = binders().stream().map(binder->((ModularBinder)binder)).collect(Collectors.toList());
		residents = list.stream().filter(ModularBinder::isResident).toArray(ModularBinder[]::new);
		transients = list.stream().filter(ModularBinder::isTransient).toArray(ModularBinder[]::new);
		list.forEach(b->b.buildInvoker(this));
	}
	
	@Override
	public int indexOf(Object keyword) {
		if(keyword instanceof Class<?>) {
			int index = getIndex(keyword);
			if(index != -1 || ModularHelper.isModularClass((Class<?>) keyword)) {
				return index;
			}
		}
		return gIndexing.indexOf(keyword);
	}
	
}
