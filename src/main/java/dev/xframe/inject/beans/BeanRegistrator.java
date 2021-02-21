package dev.xframe.inject.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanRegistrator {
    
    private BeanIndexes global;
    
    private Map<Class<?>, BeanIndexes> scopedIndexes = new HashMap<>();
    
    private List<RegHolder> waitingHolders = new ArrayList<>();
    
    public BeanRegistrator(BeanIndexes global) {
        this.global = global;
    }
    
    public void regist(BeanIndexes indexes) {
        Class<?> scopeClz = BeanBinder.Classic.scope(indexes.getClass());
        if(!isScopeClz(scopeClz)) {
            throw new IllegalArgumentException("Custom BeanIndexes must marked by Scoped annotation");
        }
        scopedIndexes.put(scopeClz, indexes);
        processWaitingHolders();
    }
    private void processWaitingHolders() {
        for (RegHolder holder : waitingHolders) {
            BeanIndexes indexes = getScopedInexes(holder);
            if(indexes != null) {
                holder.registTo(indexes);
            }
        }
    }
    //没有标识的认为是全局Bean
    private BeanIndexes getScopedInexes(RegHolder holder) {
        Class<?> scopeClz = holder.scope();
        return isScopeClz(scopeClz) ? 
                scopedIndexes.get(scopeClz) : global;
    }
    private boolean isScopeClz(Class<?> scopeClz) {
        return scopeClz != null && scopeClz.isAnnotationPresent(ScopeType.class);
    }
    public void regist(String key, Object bean) {
        regist(BeanBinder.named(key, bean));
    }
    public void regist(Object bean, Class<?>... keys) {
        regist(BeanBinder.instanced(bean, keys));
    }
    public void regist(Class<?> clz) {
        regist0(new ClassRegHolder(clz));
    }
    public void regist(BeanBinder binder) {
        regist0(new BinderRegHolder(binder));
    }
    private void regist0(RegHolder registrator) {
        BeanIndexes indexes = getScopedInexes(registrator);
        if(indexes != null) {
            registrator.registTo(indexes);
        } else {
            waitingHolders.add(registrator);
        }
    }
    
    static interface RegHolder {
        Class<?> scope();
        void registTo(BeanIndexes indexes);
    }
    static class ClassRegHolder implements RegHolder {
        final Class<?> clazz;
        ClassRegHolder(Class<?> clazz) {
            this.clazz = clazz;
        }
        public Class<?> scope() {
            return BeanBinder.Classic.scope(clazz);
        }
        public void registTo(BeanIndexes indexes) {
            indexes.regist(BeanBinder.classic(clazz, Injector.of(clazz, indexes)));
        }
    }
    static class BinderRegHolder implements RegHolder {
        final BeanBinder binder;
        BinderRegHolder(BeanBinder binder) {
            this.binder = binder;
        }
        public Class<?> scope() {
            return binder.scope();
        }
        public void registTo(BeanIndexes indexes) {
            indexes.regist(binder);
        }
    }
}
