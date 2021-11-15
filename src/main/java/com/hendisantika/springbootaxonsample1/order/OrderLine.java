package com.hendisantika.springbootaxonsample1.order;

import com.hendisantika.springbootaxonsample1.coreapi.command.DecrementProductCountCommand;
import com.hendisantika.springbootaxonsample1.coreapi.command.IncrementProductCountCommand;
import com.hendisantika.springbootaxonsample1.coreapi.events.OrderConfirmedEvent;
import com.hendisantika.springbootaxonsample1.coreapi.events.ProductCountDecrementedEvent;
import com.hendisantika.springbootaxonsample1.coreapi.events.ProductCountIncrementedEvent;
import com.hendisantika.springbootaxonsample1.coreapi.events.ProductRemovedEvent;
import com.hendisantika.springbootaxonsample1.coreapi.exceptions.OrderAlreadyConfirmedException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.EntityId;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-axon-sample1
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 16/11/21
 * Time: 06.04
 */
public class OrderLine {
    @EntityId
    private final String productId;
    private final Integer count;
    private boolean orderConfirmed;

    public OrderLine(String productId) {
        this.productId = productId;
        this.count = 1;
    }

    @CommandHandler
    public void handle(IncrementProductCountCommand command) {
        if (orderConfirmed) {
            throw new OrderAlreadyConfirmedException(command.getOrderId());
        }

        apply(new ProductCountIncrementedEvent(command.getOrderId(), productId));
    }

    @CommandHandler
    public void handle(DecrementProductCountCommand command) {
        if (orderConfirmed) {
            throw new OrderAlreadyConfirmedException(command.getOrderId());
        }

        if (count <= 1) {
            apply(new ProductRemovedEvent(command.getOrderId(), productId));
        } else {
            apply(new ProductCountDecrementedEvent(command.getOrderId(), productId));
        }
    }

    @EventSourcingHandler
    public void on(ProductCountIncrementedEvent event) {
        this.count++;
    }

    @EventSourcingHandler
    public void on(ProductCountDecrementedEvent event) {
        this.count--;
    }

    @EventSourcingHandler
    public void on(OrderConfirmedEvent event) {
        this.orderConfirmed = true;
    }

}
