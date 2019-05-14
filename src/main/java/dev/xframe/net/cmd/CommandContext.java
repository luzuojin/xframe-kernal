package dev.xframe.net.cmd;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.xframe.injection.Bean;
import dev.xframe.injection.Eventual;
import dev.xframe.injection.Inject;
import dev.xframe.injection.Injection;
import dev.xframe.injection.code.Codes;

/**
 * 命令集合 遍历所有文件,找出含有@cmd annotation的命令类
 * @author luzj
 */
@Bean
public class CommandContext implements Eventual {
    @Inject(nullable=true)
    private CommandBuilder builder;
    private Map<Integer, Command> cmds;
    
    public CommandContext() {
        cmds = new HashMap<>();
    }

    public void registCmd(int code, Command command) {
        this.cmds.put(code, command);
    }

    public Command get(int cmdCode) {
        return cmds.get(cmdCode);
    }

    public int size() {
        return cmds.size();
    }
    
    @Override
    public void eventuate() {
        registCommands(Codes.getDeclaredClasses());        
    }

	public void registCommands(List<Class<?>> clazzes) {
		for (Class<?> clazz : clazzes) {
			registCommand(clazz);
		}
	}
    
    public void registCommand(Class<?> clazz) {
        Cmd ann = clazz.getAnnotation(Cmd.class);
        if(ann != null && !Modifier.isAbstract(clazz.getModifiers()) && !Modifier.isInterface(clazz.getModifiers())) {
            registCmd(ann.value(), (Command) Injection.inject(newInstance(clazz)));
        }
    }

    private Command newInstance(Class<?> clazz) {
        try {
            return  (builder == null ? (Command) clazz.newInstance() : builder.build(clazz));
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(clazz.getName(), e);
        }
    }

}