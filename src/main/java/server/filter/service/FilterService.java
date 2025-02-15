package server.filter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import server.filter.dto.*;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class FilterService {
    private final ObjectMapper objectMapper;
    private final List<CryptocurrencyUpdate> updates = new CopyOnWriteArrayList<>();
    private final Sinks.Many<String> sinkString;
    private final Sinks.Many<CryptocurrencyUpdate> sink;



    public FilterService(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
        this.sinkString = Sinks.many().replay().limit(1);
        this.sink = Sinks.many().replay().limit(1);

    }



    public void processData(String json) {
        try {
            BaseResponse base = objectMapper.readValue(json, BaseResponse.class);
            //log.info("Checking message type: {}", base.getType());
            if ("0".equals(base.getType())) {

                //log.info("Processing data: {}", json);
                Sinks.EmitResult result = sinkString.tryEmitNext(json);
                if (result.isFailure()) {
                    log.error("Failed to emit data: {}", result);
                }

                CryptocurrencyUpdate update = objectMapper.readValue(json, CryptocurrencyUpdate.class);
                Sinks.EmitResult resultUpdate = sink.tryEmitNext(update);
                if (resultUpdate.isFailure()) {
                    log.error("Failed to emit data: {}", resultUpdate);
                }



            } else {
                log.debug("Received a message of a different type: {} => {}", base.getType(), base.getMessage());
            }
        } catch (JsonProcessingException e) {
            log.error("JSON parsing error: {}", e.getMessage(), e);
        }
    }
    public Flux<String> getDataStreamString() {
        return sinkString.asFlux();
    }
    public Flux<CryptocurrencyUpdate> getDataStream() {
        return sink.asFlux();
    }

    public Flux<CryptocurrencyUpdate> getFilteredDataStream(int n) {
        return sink.asFlux()
                .index()
                .filter(tuple -> tuple.getT1() % n == 0)
                .map(Tuple2::getT2);
    }



    /*public Flux<CryptocurrencyUpdatePQFT> getDataStreamPQFT() {
        return sinkPQFT.asFlux();
    }

    public Flux<CryptocurrencyUpdateQF> getDataStreamQF() {
        return sinkQF.asFlux();
    }

    public Flux<CryptocurrencyUpdateMFTQPT> getDataStreamMFTQPT() {
        return sinkMFTQPT.asFlux();
    }*/

    /*private final Sinks.Many<CryptocurrencyUpdatePQFT> sinkPQFT;
    private final Sinks.Many<CryptocurrencyUpdateQF> sinkQF;
    private final Sinks.Many<CryptocurrencyUpdateMFTQPT> sinkMFTQPT;*/

    /*this.sinkPQFT = Sinks.many().replay().limit(1);
        this.sinkQF = Sinks.many().replay().limit(1);
        this.sinkMFTQPT = Sinks.many().replay().limit(1);*/

        /*public void processData(String rawData) {
        log.info("Processing data: {}", rawData);
        Sinks.EmitResult result = sink.tryEmitNext(rawData);
        if (result.isFailure()) {
            log.error("Failed to emit data: {}", result);
        }
    }*/

    /*CryptocurrencyUpdatePQFT updatePQFT = objectMapper.readValue(json, CryptocurrencyUpdatePQFT.class);
                sinkPQFT.tryEmitNext(updatePQFT);

                CryptocurrencyUpdateQF updateQF = objectMapper.readValue(json, CryptocurrencyUpdateQF.class);
                sinkQF.tryEmitNext(updateQF);

                CryptocurrencyUpdateMFTQPT updateMFTQPT = objectMapper.readValue(json, CryptocurrencyUpdateMFTQPT.class);
                sinkMFTQPT.tryEmitNext(updateMFTQPT);*/
}
