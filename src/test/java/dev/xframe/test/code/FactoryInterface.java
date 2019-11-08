package dev.xframe.test.code;

import dev.xframe.inject.code.Factory;

@Factory(FactoryAnno.class)
public interface FactoryInterface {
    
    public FactoryElement newElement(FactoryEnum fe);

}
