package dev.xframe.test.game;

import org.junit.Assert;

import dev.xframe.inject.code.JavaBean;

@JavaBean
public class TInfo {

    protected int id;
    
    public TInfo() {
    }
    
    public TInfo(int id) {
    	this.id = id;
    }
    
    public int getId() {
        return this.id;
    }

    public void dosomething() throws Exception {
        Assert.assertNotNull(this.getClass().getDeclaredMethod("setId", new Class<?>[]{int.class}));
    }
    
}
