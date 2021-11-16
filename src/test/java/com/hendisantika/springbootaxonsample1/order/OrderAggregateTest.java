package com.hendisantika.springbootaxonsample1.order;

import com.hendisantika.springbootaxonsample1.coreapi.command.AddProductCommand;
import com.hendisantika.springbootaxonsample1.coreapi.command.ConfirmOrderCommand;
import com.hendisantika.springbootaxonsample1.coreapi.command.CreateOrderCommand;
import com.hendisantika.springbootaxonsample1.coreapi.command.DecrementProductCountCommand;
import com.hendisantika.springbootaxonsample1.coreapi.command.IncrementProductCountCommand;
import com.hendisantika.springbootaxonsample1.coreapi.events.OrderConfirmedEvent;
import com.hendisantika.springbootaxonsample1.coreapi.events.OrderCreatedEvent;
import com.hendisantika.springbootaxonsample1.coreapi.events.ProductAddedEvent;
import com.hendisantika.springbootaxonsample1.coreapi.events.ProductCountDecrementedEvent;
import com.hendisantika.springbootaxonsample1.coreapi.events.ProductCountIncrementedEvent;
import com.hendisantika.springbootaxonsample1.coreapi.events.ProductRemovedEvent;
import com.hendisantika.springbootaxonsample1.coreapi.exceptions.DuplicateOrderLineException;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.axonframework.test.matchers.Matchers;
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

    @Test
    void givenOrderCreatedEventAndProductAddedEvent_whenAddProductCommandForSameProductId_thenShouldThrowDuplicateOrderLineException() {
        fixture.given(new OrderCreatedEvent(ORDER_ID), new ProductAddedEvent(ORDER_ID, PRODUCT_ID))
                .when(new AddProductCommand(ORDER_ID, PRODUCT_ID))
                .expectException(DuplicateOrderLineException.class)
                .expectExceptionMessage(Matchers.predicate(message -> ((String) message).contains(PRODUCT_ID)));
    }

    @Test
    void givenOrderCreatedEventAndProductAddedEvent_whenIncrementProductCountCommand_thenShouldPublishProductCountIncrementedEvent() {
        fixture.given(new OrderCreatedEvent(ORDER_ID), new ProductAddedEvent(ORDER_ID, PRODUCT_ID))
                .when(new IncrementProductCountCommand(ORDER_ID, PRODUCT_ID))
                .expectEvents(new ProductCountIncrementedEvent(ORDER_ID, PRODUCT_ID));
    }

    @Test
    void givenOrderCreatedEventProductAddedEventAndProductCountIncrementedEvent_whenDecrementProductCountCommand_thenShouldPublishProductCountDecrementedEvent() {
        fixture.given(new OrderCreatedEvent(ORDER_ID),
                        new ProductAddedEvent(ORDER_ID, PRODUCT_ID),
                        new ProductCountIncrementedEvent(ORDER_ID, PRODUCT_ID))
                .when(new DecrementProductCountCommand(ORDER_ID, PRODUCT_ID))
                .expectEvents(new ProductCountDecrementedEvent(ORDER_ID, PRODUCT_ID));
    }

    @Test
    void givenOrderCreatedEventAndProductAddedEvent_whenDecrementProductCountCommand_thenShouldPublishProductRemovedEvent() {
        fixture.given(new OrderCreatedEvent(ORDER_ID), new ProductAddedEvent(ORDER_ID, PRODUCT_ID))
                .when(new DecrementProductCountCommand(ORDER_ID, PRODUCT_ID))
                .expectEvents(new ProductRemovedEvent(ORDER_ID, PRODUCT_ID));
    }

    @Test
    void givenOrderCreatedEvent_whenConfirmOrderCommand_thenShouldPublishOrderConfirmedEvent() {
        fixture.given(new OrderCreatedEvent(ORDER_ID))
                .when(new ConfirmOrderCommand(ORDER_ID))
                .expectEvents(new OrderConfirmedEvent(ORDER_ID));
    }

    @Test
    void givenOrderCreatedEventAndOrderConfirmedEvent_whenConfirmOrderCommand_thenExpectNoEvents() {
        fixture.given(new OrderCreatedEvent(ORDER_ID), new OrderConfirmedEvent(ORDER_ID))
                .when(new ConfirmOrderCommand(ORDER_ID))
                .expectNoEvents();
    }
}
