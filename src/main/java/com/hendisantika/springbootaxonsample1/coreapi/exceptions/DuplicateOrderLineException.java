package com.hendisantika.springbootaxonsample1.coreapi.exceptions;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-axon-sample1
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 16/11/21
 * Time: 06.13
 */
public class DuplicateOrderLineException extends IllegalStateException {

    public DuplicateOrderLineException(String productId) {
        super("Cannot duplicate order line for product identifier [" + productId + "]");
    }
}
