package com.introms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ResourceEventSource {
    private final StreamBridge streamBridge;
    
    public ResourceEventSource(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }
    
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
