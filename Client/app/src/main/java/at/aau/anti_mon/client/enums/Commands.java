package at.aau.anti_mon.client.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;


// fixme remove the string parameter, see backend comments
/**
 * Enum that represents the different commands that can be sent to the server
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Commands {
    ANSWER("ANSWER"),
    PIN("PIN"),
    CREATE_GAME("CREATE_GAME"),
    LEAVE_GAME("LEAVE_GAME"),
    JOIN_GAME("JOIN"),
    HEARTBEAT("HEARTBEAT"),
    NEW_USER("NEW_USER"),
    INFO("INFO"),
    ERROR("ERROR"),
    DICENUMBER("DICENUMBER"),
    READY("READY"), START_GAME("START_GAME");
    // usw


    private final String command;

    Commands(String command) {
        this.command = command;
    }

    @JsonValue
    public String getCommand() {
        return command;
    }
}

