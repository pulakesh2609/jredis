package com.project.jredis.command;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CommandRegistry {

    private final Map<String, Command> commands;

    public CommandRegistry(List<Command> availableCommands) {
        this.commands = availableCommands.stream()
                .collect(Collectors.toMap(Command::name, cmd -> cmd));
    }

    public Command find(String name) {
        return commands.get(name.toUpperCase());
    }
    public java.util.Set<String> allCommandNames() {
        return commands.keySet();}
}