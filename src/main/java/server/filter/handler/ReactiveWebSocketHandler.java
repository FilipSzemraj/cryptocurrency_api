package server.filter.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import server.filter.dto.*;
import server.filter.service.FilterService;

import java.io.IOException;

@Slf4j
@Component
public class ReactiveWebSocketHandler implements WebSocketHandler {
    private final FilterService filterService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserPreferencesStore preferencesStore = new UserPreferencesStore();

    public ReactiveWebSocketHandler(FilterService filterService) {
        this.filterService = filterService;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Mono<Void> inputHandler = session.receive()
                .map(message -> {
                    log.info("Received message from user: {}", message.getPayloadAsText());
                    return message.getPayloadAsText();
                })
                .doOnNext(message -> handleClientMessage(session, message))
                .doOnError(error -> cleanupSession(session, "Connection error: " + error.getMessage()))
                .doOnTerminate(() -> cleanupSession(session, "Session closed by the client"))
                .then();

        Flux<WebSocketMessage> dataStream = getFilteredDataStream(session);

        return Mono.when(inputHandler, session.send(dataStream))
                .doFinally(signalType -> {
                    System.out.println("Session ended");
                });
    }

    @PreDestroy
    public void preDestroy(){
        preferencesStore.clearAllPreferences();
    }

    private void handleClientMessage(WebSocketSession session, String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String action = jsonNode.get("action").asText();

            if ("updatePreferences".equals(action)) {
                String filterTypeStr = jsonNode.get("filterType").asText();
                int frequency = jsonNode.get("frequency").asInt();

                // Konwersja String na FilterType
                FilterType filterType = FilterType.valueOf(filterTypeStr.toUpperCase());

                ClientPreferences preferences = new ClientPreferences(filterType, frequency);
                preferencesStore.updatePreferences(session.getId(), preferences);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid filter type provided: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error processing client message: " + e.getMessage());
        }
    }

    private Flux<WebSocketMessage> getFilteredDataStream(WebSocketSession session) {
        return filterService.getDataStreamString()
                .flatMap(data -> {
                    try {
                        ClientPreferences preferences = preferencesStore.getPreferences(session.getId());
                        FilterType filterType = preferences.getFilterType();
                        Object parsedObject;

                        switch (filterType) {
                            case QF:
                                parsedObject = objectMapper.readValue(data, CryptocurrencyUpdateQF.class);
                                break;
                            case PQFT:
                                parsedObject = objectMapper.readValue(data, CryptocurrencyUpdatePQFT.class);
                                break;
                            case MFTQPT:
                                parsedObject = objectMapper.readValue(data, CryptocurrencyUpdateMFTQPT.class);
                                break;
                            case FULL:
                                parsedObject = objectMapper.readValue(data, CryptocurrencyUpdate.class);
                                break;
                            default:
                                return Flux.empty();
                        }

                        return Flux.just(parsedObject);
                    } catch (IOException e) {
                        return Flux.empty();
                    }
                })
                .index()
                .filter(tuple -> {
                    ClientPreferences currentPrefs = preferencesStore.getPreferences(session.getId());
                    int currentFrequency = currentPrefs.getFrequency();
                    return tuple.getT1() % currentFrequency == 0;
                })
                .map(tuple -> serializeToJson(session, tuple.getT2()));
    }

    private WebSocketMessage serializeToJson(WebSocketSession session, Object update) {
        try {
            return session.textMessage(objectMapper.writeValueAsString(update));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON serialization error", e);
        }
    }

    private void cleanupSession(WebSocketSession session, String reason) {
        System.out.println("Closing session " + session.getId() + ": " + reason);
        preferencesStore.removePreferences(session.getId());
    }
}
