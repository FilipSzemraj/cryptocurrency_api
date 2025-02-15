package server.filter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseResponse {
    @JsonProperty("TYPE")
    private String type;

    @JsonProperty("MESSAGE")
    private String message;
}