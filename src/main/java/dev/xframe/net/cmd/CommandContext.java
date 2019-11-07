package dev.xframe.net.cmd;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.xframe.injection.Bean;
import dev.xframe.injection.Eventual;
import dev.xframe.injection.Inject;
import dev.xframe.injection.code.Codes;

/**
 * 命令集合 遍历所有文件,找出含有@cmd annotation的命令类
 * @author luzj
 */
@Bean
public class CommandContext implements Eventual {
    @Inject
    private CommandBuilder builder;
    
    private Map<Integer, Command> cmds;
    
    public CommandContext() {
        cmds = new HashMap<>();
    }

    public void registCmd(int code, Command cmd) {
        this.cmds.put(code, cmd);
    }

    public Command get(int cmdCode) {
        return cmds.get(cmdCode);
    }

    public int size() {
        return cmds.size();
    }
    
    @Override
    public void eventuate() {
        defineCmds(Codes.getDeclaredClasses());        
    }

	public void defineCmds(List<Class<?>> clazzes) {
		for (Class<?> clazz : clazzes) {
			defineCmd(clazz);
		}
	}
    
    public void defineCmd(Class<?> clazz) {
        Cmd ann = clazz.getAnnotation(Cmd.class);
        if(ann != null && !Modifier.isAbstract(clazz.getModifiers()) && !Modifier.isInterface(clazz.getModifiers())) {
            registCmd(ann.value(), builder.build(clazz));
        }
    }

}