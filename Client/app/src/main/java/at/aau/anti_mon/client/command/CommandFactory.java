package at.aau.anti_mon.client.command;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;


public class CommandFactory {
    private final Map<String, Command> commandMap;

    public CommandFactory(Map<String, Command> commandMap) {
        this.commandMap = new HashMap<>(commandMap);
    }

    public Command getCommand(String commandType) {
        return commandMap.get(commandType);
    }
}

    /*
    public CommandFactory() {
        commandMap = new HashMap<>();
        commandMap.put("ANSWER", new AnswerCommand());
        commandMap.put("PIN", new PinCommand());
        commandMap.put("JOIN", new JoinGameCommand());
        commandMap.put("TEST", new TestCommand());
        commandMap.put("HEARTBEAT", new HeartBeatCommand());
        commandMap.put("CREATE_GAME", new CreateGameCommand());
        commandMap.put("NEW_USER", new JoinGameCommand());
        commandMap.put("INFO", new InfoCommand());
        commandMap.put("ERROR", new ErrorCommand());
        // weitere Commands hinzufügen
    }

     */