package server.filter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptocurrencyUpdateQF {
    @JsonProperty("Q")
    private BigDecimal q;  // ilość

    @JsonProperty("FSYM")
    private String fsym;   // symbol bazowy
}
