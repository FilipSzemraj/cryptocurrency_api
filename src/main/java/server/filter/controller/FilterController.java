package server.filter.controller;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import server.filter.dto.CryptocurrencyUpdateMFTQPT;
import server.filter.dto.CryptocurrencyUpdatePQFT;
import server.filter.dto.CryptocurrencyUpdateQF;
import server.filter.service.FilterService;

@RestController
public class FilterController {
    private final FilterService filterService;

    public FilterController(FilterService filterService) {
        this.filterService = filterService;
    }

    //@GetMapping(value = "/stream-data", produces = MediaType.APPLICATION_NDJSON_VALUE)
    @GetMapping(value = "/stream-data", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamData() {
        return filterService.getDataStreamString();
    }
    /*@GetMapping(value = "/stream-pqft", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<CryptocurrencyUpdatePQFT> streamDataPQFT() {
        return filterService.getDataStreamPQFT();
    }

    @GetMapping(value = "/stream-qf", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<CryptocurrencyUpdateQF> streamDataQF() {
        return filterService.getDataStreamQF();
    }

    @GetMapping(value = "/stream-mftqpt", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<CryptocurrencyUpdateMFTQPT> streamDataMFTQPT() {
        return filterService.getDataStreamMFTQPT();
    }*/
}
