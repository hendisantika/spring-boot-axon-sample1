package com.hendisantika.springbootaxonsample1.gui;

import com.hendisantika.springbootaxonsample1.coreapi.command.AddProductCommand;
import com.hendisantika.springbootaxonsample1.coreapi.command.ConfirmOrderCommand;
import com.hendisantika.springbootaxonsample1.coreapi.command.CreateOrderCommand;
import com.hendisantika.springbootaxonsample1.coreapi.command.DecrementProductCountCommand;
import com.hendisantika.springbootaxonsample1.coreapi.command.IncrementProductCountCommand;
import com.hendisantika.springbootaxonsample1.coreapi.command.ShipOrderCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-axon-sample1
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 16/11/21
 * Time: 06.17
 */
@RestController
public class OrderRestEndpoint {

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;

    public OrderRestEndpoint(CommandGateway commandGateway, QueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

    @PostMapping("/ship-order")
    public CompletableFuture<Void> shipOrder() {
        String orderId = UUID.randomUUID().toString();
        return commandGateway.send(new CreateOrderCommand(orderId))
                .thenCompose(result -> commandGateway.send(new AddProductCommand(orderId, "Deluxe Chair")))
                .thenCompose(result -> commandGateway.send(new ConfirmOrderCommand(orderId)))
                .thenCompose(result -> commandGateway.send(new ShipOrderCommand(orderId)));
    }

    @PostMapping("/ship-unconfirmed-order")
    public CompletableFuture<Void> shipUnconfirmedOrder() {
        String orderId = UUID.randomUUID().toString();
        return commandGateway.send(new CreateOrderCommand(orderId))
                .thenCompose(result -> commandGateway.send(new AddProductCommand(orderId, "Deluxe Chair")))
                // This throws an exception, as an Order cannot be shipped if it has not been confirmed yet.
                .thenCompose(result -> commandGateway.send(new ShipOrderCommand(orderId)));
    }

    @PostMapping("/order")
    public CompletableFuture<String> createOrder() {
        return createOrder(UUID.randomUUID().toString());
    }

    @PostMapping("/order/{order-id}")
    public CompletableFuture<String> createOrder(@PathVariable("order-id") String orderId) {
        return commandGateway.send(new CreateOrderCommand(orderId));
    }

    @PostMapping("/order/{order-id}/product/{product-id}")
    public CompletableFuture<Void> addProduct(@PathVariable("order-id") String orderId,
                                              @PathVariable("product-id") String productId) {
        return commandGateway.send(new AddProductCommand(orderId, productId));
    }

    @PostMapping("/order/{order-id}/product/{product-id}/increment")
    public CompletableFuture<Void> incrementProduct(@PathVariable("order-id") String orderId,
                                                    @PathVariable("product-id") String productId) {
        return commandGateway.send(new IncrementProductCountCommand(orderId, productId));
    }

    @PostMapping("/order/{order-id}/product/{product-id}/decrement")
    public CompletableFuture<Void> decrementProduct(@PathVariable("order-id") String orderId,
                                                    @PathVariable("product-id") String productId) {
        return commandGateway.send(new DecrementProductCountCommand(orderId, productId));
    }
}
