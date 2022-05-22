package ru.isg.invest.helper.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Created by s.ivanov on 20.11.2021.
 */
@Configuration
@Slf4j
public class TinkoffWebSocketConfig {

    private final ObjectMapper objectMapper;

    {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

//    @Bean
//    public WebSocketSession getTinkoffWebSocketSession() throws ExecutionException, InterruptedException {
//
//        log.info("Try get tinkoff webSocket session");
//
//        WebSocketClient webSocketClient = new StandardWebSocketClient();
//
//        WebSocketHttpHeaders h = new WebSocketHttpHeaders();
//        h.add("Authorization",
//                "Bearer XXX");
//
//        ListenableFuture<WebSocketSession> sessionListenableFuture = webSocketClient.doHandshake(
//                new TextWebSocketHandler() {
//                    @Override
//                    public void handleTextMessage(WebSocketSession session, TextMessage message) {
//
//                        log.info("payload: {}", message.getPayload());
//
//                        try {
//                            MarketCandleEvent marketCandleEvent = objectMapper.readValue(message.getPayload(),
//                                    MarketCandleEvent.class);
//                            candlesCache.putCandle(marketCandleEvent.getPayload().getFigi(),
//                                    marketCandleEvent.getPayload());
//                        } catch (JsonProcessingException e) {
//                            log.error(e.getMessage(), e);
//                        }
//                    }
//
//                    @Override
//                    public void afterConnectionEstablished(WebSocketSession session) {
//                        log.info("Connection established!!!");
//                    }
//
//                    @Override
//                    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
//                        log.info("Transport error!!!");
//                    }
//                },
//                h,
//                URI.create("wss://api-invest.tinkoff.ru/openapi/md/v1/md-openapi/ws"));
//
//        return sessionListenableFuture.get();
//    }
}
