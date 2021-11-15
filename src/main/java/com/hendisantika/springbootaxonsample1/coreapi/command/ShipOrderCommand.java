package com.hendisantika.springbootaxonsample1.coreapi.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-axon-sample1
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 16/11/21
 * Time: 06.09
 */
public class ShipOrderCommand {
    @TargetAggregateIdentifier
    private final String orderId;

    public ShipOrderCommand(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ShipOrderCommand other = (ShipOrderCommand) obj;
        return Objects.equals(this.orderId, other.orderId);
    }

    @Override
    public String toString() {
        return "ShipOrderCommand{" +
                "orderId='" + orderId + '\'' +
                '}';
    }
}
