package server.filter.client;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Mono;
import server.filter.service.FilterService;

import java.net.URI;
import java.time.Duration;

@Slf4j
@Component
public class FilterClient {
    private final String wsUrl;
    private final ReactorNettyWebSocketClient client;
    private WebSocketSession session;
    private final FilterService filterService;

    public FilterClient(
            FilterService filterService,
            @Value("${CRYPTO_API_KEY}") String apiKey,
            @Value("${CRYPTO_WS_URL}") String wsUrlBase
    ) {
        this.filterService = filterService;
        this.wsUrl = wsUrlBase + "/?api_key=" + apiKey;
        this.client = new ReactorNettyWebSocketClient();
    }

    @PostConstruct
    public void start() {
        connect();
    }

    private void connect() {
        client.execute(URI.create(wsUrl), this::handleSession)
                .doOnError(error -> {
                    log.error("WebSocket connection failed, retrying in 5s...", error);
                    reconnect();
                })
                .subscribe();
    }

    private void closeSession() {
        if (session != null && session.isOpen()) {
            log.info("Closing WebSocket session...");
            session.close(CloseStatus.NORMAL).subscribe();
        }
    }

    private Mono<Void> handleSession(WebSocketSession session) {
        this.session = session;
        log.info("WebSocket connection established");

        String subscriptionMessage = """
        {
            "action": "SubAdd",
            "subs": ["5~CCCAGG~BTC~USD", "0~Coinbase~ETH~USD", "2~Binance~BTC~USDT"]
        }
        """;

        //log.info("Received WebSocket message: {}", msg);
        return session.send(Mono.just(session.textMessage(subscriptionMessage)))
                .thenMany(session.receive()
                        .map(WebSocketMessage::getPayloadAsText)
                        .doOnNext(filterService::processData)
                        .doOnError(error -> {
                            log.error("WebSocket error: {}", error.getMessage());
                            closeSession();
                            reconnect();
                        })
                        .doOnComplete(this::closeSession)
                )
                .then();
    }

    private void reconnect() {
        log.info("Attempting to reconnect...");
        Mono.delay(Duration.ofSeconds(5)).subscribe(x -> connect());
    }

    @PreDestroy
    public void stop() {
        if (session != null && session.isOpen()) {
            String unsubscribeMessage = """
            {
                "action": "SubRemove",
                "subs": ["5~CCCAGG~BTC~USD", "0~Coinbase~ETH~USD", "2~Binance~BTC~USDT"]
            }
            """;

            session.send(Mono.just(session.textMessage(unsubscribeMessage)))
                    .then(session.close(CloseStatus.NORMAL))
                    .subscribe();

            log.info("WebSocket session closed.");
        }
    }
}
