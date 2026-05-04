package com.quickbite.authservice.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publish(NotificationEvent event) {
        try {
            rabbitTemplate.convertAndSend(AuthRabbitConfig.NOTIFICATION_EXCHANGE,
                    AuthRabbitConfig.NOTIFICATION_ROUTING_KEY, event);
        } catch (AmqpException ignored) {
        }
    }
}
