package dev.xframe.test.code;

import dev.xframe.inject.code.Factory;

@Factory(value=FactoryAnno.class, singleton=true)
public interface FactoryInterface {
    
    public FactoryElement newElement(FactoryEnum fe);

}
