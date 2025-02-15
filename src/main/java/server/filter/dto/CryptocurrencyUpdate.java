package server.filter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptocurrencyUpdate {
    @JsonProperty("TYPE")
    private String type;  // będzie "0"

    @JsonProperty("M")
    private String m;     // giełda, np. "Coinbase"

    @JsonProperty("FSYM")
    private String fsym;  // symbol bazowy, np. "BTC"

    @JsonProperty("TSYM")
    private String tsym;  // symbol kwotowany, np. "USD"

    @JsonProperty("P")
    private BigDecimal p; // cena

    @JsonProperty("Q")
    private BigDecimal q; // ilość

    @JsonProperty("TOTAL")
    private BigDecimal total; // łączna wartość

    @JsonProperty("TS")
    private Long ts;      // timestamp transakcji
}
