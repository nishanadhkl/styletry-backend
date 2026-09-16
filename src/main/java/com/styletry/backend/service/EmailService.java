package com.styletry.backend.service;

import com.styletry.backend.model.Order;
import com.styletry.backend.model.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Async
    public void sendOrderPlacedEmail(Order order) {
        String items = order.getOrderItems() == null ? "" : order.getOrderItems().stream()
                .map(this::formatItem)
                .collect(Collectors.joining("\n"));

        String body = "Dear " + order.getUser().getFullName() + ",\n\n"
                + "Your StyleTry order has been placed successfully.\n\n"
                + "Order Number: ORD-" + order.getId() + "\n"
                + "Status: " + order.getStatus() + "\n"
                + "Total Amount: Rs " + order.getTotalAmount() + "\n"
                + "Shipping Address: " + order.getShippingAddress() + "\n\n"
                + "Items:\n" + items + "\n\n"
                + "Thank you for shopping with StyleTry.";

        send(order.getUser().getEmail(), "StyleTry Order Confirmation - ORD-" + order.getId(), body);
    }

    @Async
    public void sendOrderStatusEmail(Order order) {
        String body = "Dear " + order.getUser().getFullName() + ",\n\n"
                + "Your StyleTry order status has been updated.\n\n"
                + "Order Number: ORD-" + order.getId() + "\n"
                + "New Status: " + order.getStatus() + "\n"
                + "Total Amount: Rs " + order.getTotalAmount() + "\n\n"
                + "Thank you for shopping with StyleTry.";

        send(order.getUser().getEmail(), "StyleTry Order Status Updated - ORD-" + order.getId(), body);
    }

    private String formatItem(OrderItem item) {
        String variantText = item.getVariant() == null
                ? ""
                : " (" + item.getVariant().getSize() + ", " + item.getVariant().getColor() + ")";
        return "- " + item.getProduct().getName() + variantText + " x " + item.getQuantity()
                + " = Rs " + item.getPrice();
    }

    private void send(String to, String subject, String body) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null || !StringUtils.hasText(fromEmail)) {
            System.out.println("StyleTry email skipped: configure MAIL_HOST, MAIL_USERNAME and MAIL_PASSWORD before starting backend.");
            return;
        }

        try {
            System.out.println("StyleTry email sending to " + to + " with subject: " + subject);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("StyleTry email sent to " + to);
        } catch (Exception ex) {
            System.out.println("StyleTry email send failed: " + ex.getMessage());
        }
    }
}
