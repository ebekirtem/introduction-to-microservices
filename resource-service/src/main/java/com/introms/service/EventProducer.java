package com.introms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventProducer {
    private final StreamBridge streamBridge;
    
    public void publishResourceCreatedEvent(Object event) {
        try {
            boolean sent = streamBridge.send("resourceCreated-out-0", event);
            if (sent) {
                log.info("ResourceCreatedEvent published successfully");
            } else {
                log.warn("Failed to publish ResourceCreatedEvent - StreamBridge returned false");
            }
        } catch (Exception e) {
            log.error("Error publishing ResourceCreatedEvent. Retry will be handled by Spring Cloud Stream. Error: {}", 
                e.getMessage(), e);
            throw e; // Re-throw to let Spring Cloud Stream retry mechanism handle it
        }
    }
}
