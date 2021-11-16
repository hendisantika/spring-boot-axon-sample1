package com.hendisantika.springbootaxonsample1.order;

import com.hendisantika.springbootaxonsample1.coreapi.command.AddProductCommand;
import com.hendisantika.springbootaxonsample1.coreapi.command.CreateOrderCommand;
import com.hendisantika.springbootaxonsample1.coreapi.events.OrderCreatedEvent;
import com.hendisantika.springbootaxonsample1.coreapi.events.ProductAddedEvent;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-axon-sample1
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 17/11/21
 * Time: 05.58
 */
class OrderAggregateTest {
    private static final String ORDER_ID = UUID.randomUUID().toString();
    private static final String PRODUCT_ID = UUID.randomUUID().toString();

    private FixtureConfiguration<OrderAggregate> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(OrderAggregate.class);
    }

    @Test
    void giveNoPriorActivity_whenCreateOrderCommand_thenShouldPublishOrderCreatedEvent() {
        fixture.givenNoPriorActivity()
                .when(new CreateOrderCommand(ORDER_ID))
                .expectEvents(new OrderCreatedEvent(ORDER_ID));
    }

    @Test
    void givenOrderCreatedEvent_whenAddProductCommand_thenShouldPublishProductAddedEvent() {
        fixture.given(new OrderCreatedEvent(ORDER_ID))
                .when(new AddProductCommand(ORDER_ID, PRODUCT_ID))
                .expectEvents(new ProductAddedEvent(ORDER_ID, PRODUCT_ID));
    }
}
