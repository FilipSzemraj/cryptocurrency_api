package server.filter.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ErrorMessage {
    @JsonProperty("type")
    private final String type = "ERROR";
    private String message;

    public ErrorMessage(String message) {
        this.message = message;
    }

}