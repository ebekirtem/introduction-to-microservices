package com.introms.service;

import com.introms.dto.ResourceCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceEventPublisher {
    private final EventProducer eventProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleResourceCreated(ResourceCreatedEvent event) {
        log.info("Transaction committed. Publishing ResourceCreatedEvent for resource ID: {}", 
            event.resourceId());
        try {
            eventProducer.publishResourceCreatedEvent(event);
            log.info("Published ResourceCreatedEvent after transaction commit for resource ID: {}", 
                event.resourceId());
        } catch (Exception e) {
            log.error("Failed to publish ResourceCreatedEvent after transaction commit for resource ID: {}. " +
                "Resource is saved but event was not published. Error: {}", 
                event.resourceId(), e.getMessage(), e);
            // Don't throw - resource is already saved
        }
    }
}
