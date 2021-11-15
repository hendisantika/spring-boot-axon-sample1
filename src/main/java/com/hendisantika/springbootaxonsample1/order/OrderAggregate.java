package com.hendisantika.springbootaxonsample1.order;

import com.hendisantika.springbootaxonsample1.coreapi.command.AddProductCommand;
import com.hendisantika.springbootaxonsample1.coreapi.command.ConfirmOrderCommand;
import com.hendisantika.springbootaxonsample1.coreapi.command.CreateOrderCommand;
import com.hendisantika.springbootaxonsample1.coreapi.command.ShipOrderCommand;
import com.hendisantika.springbootaxonsample1.coreapi.events.OrderConfirmedEvent;
import com.hendisantika.springbootaxonsample1.coreapi.events.OrderCreatedEvent;
import com.hendisantika.springbootaxonsample1.coreapi.events.OrderShippedEvent;
import com.hendisantika.springbootaxonsample1.coreapi.events.ProductAddedEvent;
import com.hendisantika.springbootaxonsample1.coreapi.exceptions.DuplicateOrderLineException;
import com.hendisantika.springbootaxonsample1.coreapi.exceptions.OrderAlreadyConfirmedException;
import com.hendisantika.springbootaxonsample1.coreapi.exceptions.UnconfirmedOrderException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateMember;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.Map;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-axon-sample1
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 16/11/21
 * Time: 06.29
 */
@Aggregate(snapshotTriggerDefinition = "orderAggregateSnapshotTriggerDefinition")
public class OrderAggregate {
    @AggregateIdentifier
    private String orderId;
    private boolean orderConfirmed;

    @AggregateMember
    private Map<String, OrderLine> orderLines;

    @CommandHandler
    public OrderAggregate(CreateOrderCommand command) {
        apply(new OrderCreatedEvent(command.getOrderId()));
    }

    @CommandHandler
    public void handle(AddProductCommand command) {
        if (orderConfirmed) {
            throw new OrderAlreadyConfirmedException(orderId);
        }

        String productId = command.getProductId();
        if (orderLines.containsKey(productId)) {
            throw new DuplicateOrderLineException(productId);
        }
        apply(new ProductAddedEvent(orderId, productId));
    }

    @CommandHandler
    public void handle(ConfirmOrderCommand command) {
        if (orderConfirmed) {
            return;
        }

        apply(new OrderConfirmedEvent(orderId));
    }

    @CommandHandler
    public void handle(ShipOrderCommand command) {
        if (!orderConfirmed) {
            throw new UnconfirmedOrderException();
        }

        apply(new OrderShippedEvent(orderId));
    }
}
