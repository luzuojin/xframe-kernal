package dev.xframe.test.game;

import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.Resource;

import dev.xframe.game.player.MCallerFactory;
import dev.xframe.game.player.ModularAdapter;
import dev.xframe.inject.Bean;
import dev.xframe.inject.Inject;
import dev.xframe.inject.Loadable;
import dev.xframe.inject.beans.BeanBinder;
import dev.xframe.inject.beans.BeanDiscovery;
import dev.xframe.inject.beans.BeanRegistrator;

@Bean
public class TCallerDiscovery implements BeanDiscovery, Loadable {
    
    @Inject
    private ModularAdapter adapter;
    
    private BeanBinder.LazyInstance cbb;
    private Class<?> cls;
    private Method cmm;

    @Override
    public void discover(List<Class<?>> scanned, BeanRegistrator reg) {
        Class<?> c = TPlayerInventory.class;
        for (Method m : c.getDeclaredMethods()) {
            if(m.isAnnotationPresent(Resource.class)) {
                cls = c;
                cbb = new BeanBinder.LazyInstance(TCaller.class);
                cmm = m;
                reg.regist(cbb);
                //测试代码仅一个实现类
                return;
            }
        }
    }

    @Override
    public void load() {
        cbb.setDelegate(MCallerFactory.make(TCaller.class, cls, adapter.indexOf(cls), cmm));
    }

}

