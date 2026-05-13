package com.quickbite.orderservice.service;

import com.quickbite.orderservice.model.FoodOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.from:${spring.mail.username:}}")
    private String from;

    public void sendOrderConfirmation(FoodOrder order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(order.getCustomerEmail());
        if (from != null && !from.isBlank()) {
            message.setFrom(from);
        }
        message.setSubject("Your QuickBite order #" + order.getId() + " is confirmed");
        StringBuilder body = new StringBuilder();
        body.append("Hi,\n\n");
        body.append("Thanks for ordering from QuickBite.\n");
        body.append("Order ID: ").append(order.getId()).append("\n");
        body.append("Status: ").append(order.getOrderStatus()).append("\n");
        body.append("Payment: ").append(order.getPaymentStatus()).append("\n");
        body.append("Total: Rs ").append(order.getTotalAmount()).append("\n\n");
        body.append("Items:\n");
        order.getItems().forEach(item ->
                body.append("- ").append(item.getItemName())
                        .append(" x").append(item.getQuantity())
                        .append(" = Rs ").append(item.getUnitPrice() * item.getQuantity())
                        .append("\n"));
        body.append("\nWe'll keep you updated as your order moves forward.");
        message.setText(body.toString());
        mailSender.send(message);
    }
}
