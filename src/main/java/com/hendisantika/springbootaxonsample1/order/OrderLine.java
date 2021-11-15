package com.hendisantika.springbootaxonsample1.order;

import org.axonframework.modelling.command.EntityId;

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

}
