package server.filter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptocurrencyUpdateMFTQPT {
    @JsonProperty("M")
    private String m;      // giełda

    @JsonProperty("FSYM")
    private String fsym;   // symbol bazowy

    @JsonProperty("TSYM")
    private String tsym;   // symbol kwotowany

    @JsonProperty("Q")
    private BigDecimal q;  // ilość

    @JsonProperty("P")
    private BigDecimal p;  // cena

    @JsonProperty("TOTAL")
    private BigDecimal total; // łączna wartość

    @JsonProperty("TS")
    private Long ts;       // timestamp transakcji
}
