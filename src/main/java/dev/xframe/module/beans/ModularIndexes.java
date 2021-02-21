package dev.xframe.module.beans;

import java.util.List;
import java.util.stream.Collectors;

import dev.xframe.inject.beans.BeanBinder;
import dev.xframe.inject.beans.BeanIndexes;
import dev.xframe.inject.beans.BeanIndexing;
import dev.xframe.module.ModularHelper;
import dev.xframe.module.ModularScope;

@ModularScope
public class ModularIndexes extends BeanIndexes {
	
	public static final int OFFSET = 8000;
	
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
		list.forEach(b->b.makeComplete(this));
	}
	
	@Override
	public BeanBinder indexOf0(Object keyword) {
		if(keyword instanceof Class<?>) {
			BeanBinder binder = super.indexOf0(keyword);
			if(binder != null || ModularHelper.isModularClass((Class<?>) keyword)) {
				return binder;
			}
		}
		return gIndexing.indexOf0(keyword);
	}
	
}
