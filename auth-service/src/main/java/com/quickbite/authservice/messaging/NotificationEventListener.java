package com.quickbite.authservice.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "quickbite.rabbitmq", name = "enabled", havingValue = "true")
public class NotificationEventListener {
    @RabbitListener(queues = AuthRabbitConfig.NOTIFICATION_QUEUE)
    public void onNotification(NotificationEvent event) {
    }
}
