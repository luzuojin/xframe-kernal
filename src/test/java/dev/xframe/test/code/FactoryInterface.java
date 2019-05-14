package dev.xframe.test.code;

import dev.xframe.injection.factory.Factory;

@Factory(FactoryAnno.class)
public interface FactoryInterface {
    
    public FactoryElement newElement(FactoryEnum fe);

}
