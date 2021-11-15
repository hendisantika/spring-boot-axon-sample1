package com.hendisantika.springbootaxonsample1.querymodel;

import com.hendisantika.springbootaxonsample1.coreapi.queries.Order;
import org.axonframework.config.ProcessingGroup;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-axon-sample1
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 16/11/21
 * Time: 06.22
 */
@Service
@ProcessingGroup("orders")
public class OrdersEventHandler {

    private final Map<String, Order> orders = new HashMap<>();

}
