package com.manoj.fastserve.Service;

import com.manoj.fastserve.Entity.Order;
import com.manoj.fastserve.Entity.OrderStatus;
import com.manoj.fastserve.Entity.PaymentMode;
import com.manoj.fastserve.Entity.PaymentStatus;
import com.manoj.fastserve.Entity.User;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class EmailNotificationServiceTest {

    @Test
    void sendWelcome_sendsEmailWhenEnabled() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        ObjectProvider<JavaMailSender> provider = mailSenderProvider(mailSender);

        EmailNotificationService service = new EmailNotificationService(provider);
        ReflectionTestUtils.setField(service, "enabled", true);
        ReflectionTestUtils.setField(service, "fromAddress", "hello@fastserve.test");

        User user = new User();
        user.setName("Alex");
        user.setEmail("alex@example.com");

        service.sendWelcome(user);

        ArgumentCaptor<SimpleMailMessage> message =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(message.capture());
        assertEquals("hello@fastserve.test", message.getValue().getFrom());
        assertEquals("alex@example.com", message.getValue().getTo()[0]);
        assertEquals("Welcome to FastServe", message.getValue().getSubject());
    }

    @Test
    void sendOrderStatusChanged_includesStatusAndOrderWhenEnabled() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        ObjectProvider<JavaMailSender> provider = mailSenderProvider(mailSender);

        EmailNotificationService service = new EmailNotificationService(provider);
        ReflectionTestUtils.setField(service, "enabled", true);
        ReflectionTestUtils.setField(service, "fromAddress", "hello@fastserve.test");

        User user = new User();
        user.setName("Alex");
        user.setEmail("alex@example.com");
        Order order = new Order();
        ReflectionTestUtils.setField(order, "id", 42L);
        order.setUser(user);
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setPaymentMode(PaymentMode.CARD);
        order.setTotalPrice(250.0);

        service.sendOrderStatusChanged(order);

        ArgumentCaptor<SimpleMailMessage> message =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(message.capture());
        assertEquals("Order #42 is now OUT_FOR_DELIVERY", message.getValue().getSubject());
        assertTrue(message.getValue().getText().contains("OUT_FOR_DELIVERY"));
    }

    @Test
    void sendWelcome_doesNothingWhenDisabled() {
        ObjectProvider<JavaMailSender> provider = mailSenderProvider(null);
        EmailNotificationService service = new EmailNotificationService(provider);

        User user = new User();
        user.setEmail("alex@example.com");

        service.sendWelcome(user);

        verifyNoInteractions(provider);
    }

    @SuppressWarnings("unchecked")
    private ObjectProvider<JavaMailSender> mailSenderProvider(JavaMailSender sender) {
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        if (sender != null) {
            when(provider.getIfAvailable()).thenReturn(sender);
        }
        return provider;
    }
}
