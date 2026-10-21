package com.manoj.fastserve.Service;

import com.manoj.fastserve.Entity.Order;
import com.manoj.fastserve.Entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;

@Service
public class EmailNotificationService {

    private static final Logger logger =
            LoggerFactory.getLogger(EmailNotificationService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${fastserve.email.enabled:false}")
    private boolean enabled;

    @Value("${fastserve.email.from:${spring.mail.username:}}")
    private String fromAddress;

    public EmailNotificationService(
            ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    public void sendWelcome(User user) {
        send(
                user.getEmail(),
                "Welcome to FastServe",
                "Hi " + user.getName() + ",\n\nWelcome to FastServe. "
                        + "We are glad to have you. You can now discover restaurants "
                        + "and place orders from your neighborhood."
        );
    }

    public void sendOrderPlaced(Order order) {
        send(
                order.getUser().getEmail(),
                "FastServe order #" + order.getId() + " received",
                "Hi " + order.getUser().getName() + ",\n\n"
                        + "We received your order #" + order.getId() + ".\n"
                        + "Total: " + formatPrice(order.getTotalPrice()) + "\n"
                        + "Payment method: " + order.getPaymentMode() + "\n"
                        + "Current status: " + order.getStatus() + "\n\n"
                        + "Thank you for ordering with FastServe."
        );
    }

    public void sendPaymentResult(Order order) {
        send(
                order.getUser().getEmail(),
                "Payment update for FastServe order #" + order.getId(),
                "Hi " + order.getUser().getName() + ",\n\n"
                        + "The payment status for order #" + order.getId()
                        + " is " + order.getPaymentStatus() + ".\n"
                        + "Total: " + formatPrice(order.getTotalPrice()) + "\n"
                        + "Payment method: " + order.getPaymentMode() + "\n"
                        + (order.getTransactionId() == null ? "" :
                        "Transaction reference: " + order.getTransactionId() + "\n")
        );
    }

    public void sendOrderStatusChanged(Order order) {
        send(
                order.getUser().getEmail(),
                "Order #" + order.getId() + " is now " + order.getStatus(),
                "Hi " + order.getUser().getName() + ",\n\n"
                        + "The status of your FastServe order #" + order.getId()
                        + " changed to " + order.getStatus() + ".\n"
                        + "Total: " + formatPrice(order.getTotalPrice()) + "\n"
                        + "Payment status: " + order.getPaymentStatus()
        );
    }

    private void send(String recipient, String subject, String body) {
        if (!enabled) {
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            logger.error("Email notifications are enabled, but no SMTP mail sender is configured");
            return;
        }
        if (fromAddress == null || fromAddress.isBlank()) {
            logger.error("Email notifications are enabled, but FASTSERVE_EMAIL_FROM or SPRING_MAIL_USERNAME is missing");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
        } catch (MailException exception) {
            logger.error("Could not send FastServe notification to {}", recipient, exception);
        }
    }

    private String formatPrice(Double amount) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
                .format(amount == null ? 0 : amount);
    }
}
