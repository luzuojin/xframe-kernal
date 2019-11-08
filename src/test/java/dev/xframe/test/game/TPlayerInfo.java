package dev.xframe.test.game;

import org.junit.Assert;

import dev.xframe.inject.code.JavaBean;

@JavaBean
public class TPlayerInfo extends TInfo {
    
    private String name;

    public TPlayerInfo(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public TPlayerInfo() {
    }
    
    public String rename(String name) {
        String old = this.name;
        this.name = name;
        return old;
    }
    
    public void dosomething() throws Exception {
        Assert.assertNotNull(this.getClass().getDeclaredMethod("getName", new Class<?>[0]));
        Assert.assertNotNull(this.getClass().getDeclaredMethod("setName", new Class<?>[]{String.class}));
    }

}
