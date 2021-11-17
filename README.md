# A Guide to the Axon Framework

In this repository, we'll be looking at Axon and how it helps us implement applications
with [CQRS](https://martinfowler.com/bliki/CQRS.html) (Command Query Responsibility Segregation)
and [Event Sourcing](https://martinfowler.com/eaaDev/EventSourcing.html) in mind.

During this guide, both Axon Framework and [Axon Server](https://axoniq.io/product-overview/axon-server) will be
utilized. The former will contain our implementation and the latter will be our dedicated Event Store and Message
Routing solution.

Or You can run Axon Server using docker by using this command:

```shell
docker run -d --name axonserver -p 8024:8024 -p 8124:8124 axoniq/axonserver
```

The sample application we'll be building focuses on an Order domain. For this, **we'll be leveraging the CQRS and Event
Sourcing building blocks Axon provides us.**

Note that a lot of the shared concepts come right out of [DDD](https://en.wikipedia.org/wiki/Domain-driven_design),
which is beyond the scope of this current article.

We will use [Axon Server](https://axoniq.io/product-overview/axon-server) to be
our [Event Store](https://en.wikipedia.org/wiki/Event_store) and our dedicated command, event and query routing
solution.

As an Event Store, it gives us the ideal characteristics required when storing
events. [This article](https://axoniq.io/blog-overview/eventstore) provides background on why this is desirable.

As a Message Routing solution, it gives us the option to connect several instances together without focusing on
configuring things like a RabbitMQ or a Kafka topic to share and dispatch messages.

Axon Server can be downloaded [here](https://download.axoniq.io/axonserver/AxonServer.zip). As it is a simple JAR file,
the following operation suffices to start it up:

> java -jar axonserver.jar

This will start a single Axon Server instance which is accessible through [localhost:8024](http://localhost:8024/). The
endpoint provides an overview of the connected applications and the messages they can handle, as well as a querying
mechanism towards the Event Store contained within Axon Server.

The default configuration of Axon Server together with the axon-spring-boot-starter dependency will ensure our Order
service will automatically connect to it.

### Order Service API – Commands

We'll set up our Order service with CQRS in mind. Therefore we'll emphasize the messages that flow through our
application.

**First, we'll define the Commands, meaning the expressions of intent.** The Order service is capable of handling three
different types of actions:

1. Creating a new order
2. Confirming an order
3. Shipping an order

Naturally, there will be three command messages that our domain can deal with — _CreateOrderCommand,
ConfirmOrderCommand, and ShipOrderCommand_:

```java
public class CreateOrderCommand {
 
    @TargetAggregateIdentifier
    private final String orderId;
    private final String productId;
 
    // constructor, getters, equals/hashCode and toString 
}
public class ConfirmOrderCommand {
 
    @TargetAggregateIdentifier
    private final String orderId;
    
    // constructor, getters, equals/hashCode and toString
}
public class ShipOrderCommand {
 
    @TargetAggregateIdentifier
    private final String orderId;
 
    // constructor, getters, equals/hashCode and toString
}
```

**
The [TargetAggregateIdentifier](https://apidocs.axoniq.io/4.0/org/axonframework/modelling/command/TargetAggregateIdentifier.html)
annotation tells Axon that the annotated field is an id of a given aggregate to which the command should be targeted.**
We'll briefly touch on aggregates later in this article.

Also, note that we marked the fields in the commands as _final_. **This is intentional, as it's a best practice for any
message implementation to be immutable.**

### Order Service API – Events

**Our aggregate will handle the commands**, as it's in charge of deciding if an Order can be created, confirmed, or
shipped.

It will notify the rest of the application of its decision by publishing an event. We'll have three types of events — _
OrderCreatedEvent, OrderConfirmedEvent, and OrderShippedEvent:
_

```java
public class OrderCreatedEvent {
 
    private final String orderId;
    private final String productId;
 
    // default constructor, getters, equals/hashCode and toString
}
public class OrderConfirmedEvent {
 
    private final String orderId;
 
    // default constructor, getters, equals/hashCode and toString
}
public class OrderShippedEvent { 

    private final String orderId; 

    // default constructor, getters, equals/hashCode and toString 
}
```

### The Command Model – Order Aggregate

Now that we've modeled our core API with respect to the commands and events, we can start creating the Command Model.

The [Aggregate](https://www.martinfowler.com/bliki/DDD_Aggregate.html) is a regular component within the Command Model
and stems from DDD. Other frameworks use the concept too, as is for example seen
in [this article](https://www.baeldung.com/spring-persisting-ddd-aggregates#introduction-to-aggregates) about persisting
DDD aggregates with Spring.

As our domain focuses on dealing with Orders, **we'll create an OrderAggregate as the centre of our Command Model.**

### Aggregate Class

Thus, let's create our basic aggregate class:

```java
@Aggregate
public class OrderAggregate {

    @AggregateIdentifier
    private String orderId;
    private boolean orderConfirmed;

    @CommandHandler
    public OrderAggregate(CreateOrderCommand command) {
        AggregateLifecycle.apply(new OrderCreatedEvent(command.getOrderId(), command.getProductId()));
    }

    @EventSourcingHandler
    public void on(OrderCreatedEvent event) {
        this.orderId = event.getOrderId();
        orderConfirmed = false;
    }

    protected OrderAggregate() { }
}
```

**The [Aggregate](https://apidocs.axoniq.io/4.0/org/axonframework/spring/stereotype/Aggregate.html) annotation is an
Axon Spring specific annotation marking this class as an aggregate.** It will notify the framework that the required
CQRS and Event Sourcing specific building blocks need to be instantiated for this _OrderAggregate_.

**As an aggregate will handle commands that are targeted to a specific aggregate instance, we need to specify the
identifier with
the [AggregateIdentifier](https://apidocs.axoniq.io/4.0/org/axonframework/modelling/command/AggregateIdentifier.html)
annotation.**

Our aggregate will commence its life cycle upon handling the _CreateOrderCommand_ in the _OrderAggregate_ ‘command
handling constructor'. **To tell the framework that the given function is able to handle commands, we'll add
the [CommandHandler](https://apidocs.axoniq.io/4.0/org/axonframework/commandhandling/CommandHandler.html) annotation.**

**When handling the CreateOrderCommand, it will notify the rest of the application that an order was created by
publishing the OrderCreatedEvent.** To publish an event from within an aggregate, we'll
use [AggregateLifecycle#apply(Object…).](https://apidocs.axoniq.io/4.0/org/axonframework/modelling/command/AggregateLifecycle.html)

From this point, we can actually start to incorporate Event Sourcing as the driving force to recreate an aggregate
instance from its stream of events.

We start this off with the ‘aggregate creation event', the _OrderCreatedEvent_, which is handled in
an [EventSourcingHandler](https://apidocs.axoniq.io/4.0/org/axonframework/eventsourcing/EventSourcingHandler.html)
annotated function to set the _orderId_ and _orderConfirmed_ state of the Order aggregate.

Also note that to be able to source an aggregate based on its events, Axon requires a default constructor.

### Aggregate Command Handlers

Now that we have our basic aggregate, we can start implementing the remaining command handlers:

```java
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

@EventSourcingHandler 
public void on(OrderConfirmedEvent event) { 
    orderConfirmed = true; 
}
```

The signature of our command and event sourcing handlers simply states _handle({the-command})_ and _on({the-event})_ to
maintain a concise format.

Additionally, we've defined that an Order can only be confirmed once and shipped if it has been confirmed. Thus, we'll
ignore the command in the former, and throw an _UnconfirmedOrderException_ if the latter isn't the case.

This exemplifies the need for the _OrderConfirmedEvent_ sourcing handler to update the _orderConfirmed_ state to true
for the Order aggregate.

### Testing the Command Model

First, we need to set up our test by creating
a [FixtureConfiguration](https://apidocs.axoniq.io/4.0/org/axonframework/test/aggregate/FixtureConfiguration.html) for
the _OrderAggregate_:

```java
private FixtureConfiguration<OrderAggregate> fixture;

@Before
public void setUp() {
    fixture = new AggregateTestFixture<>(OrderAggregate.class);
}
```

The first test case should cover the simplest situation. When the aggregate handles the _CreateOrderCommand_, it should
produce an _OrderCreatedEvent_:

```java
String orderId = UUID.randomUUID().toString();
String productId = "Deluxe Chair";
fixture.givenNoPriorActivity()
  .when(new CreateOrderCommand(orderId, productId))
  .expectEvents(new OrderCreatedEvent(orderId, productId));
```

Next, we can test the decision-making logic of only being able to ship an Order if it's been confirmed. Due to this, we
have two scenarios — one where we expect an exception, and one where we expect an _OrderShippedEvent_.

Let's take a look at the first scenario, where we expect an exception:

```java
String orderId = UUID.randomUUID().toString();
String productId = "Deluxe Chair";
fixture.given(new OrderCreatedEvent(orderId, productId))
  .when(new ShipOrderCommand(orderId))
  .expectException(UnconfirmedOrderException.class);
```

And now the second scenario, where we expect an _OrderShippedEvent:_

```java
String orderId = UUID.randomUUID().toString();
String productId = "Deluxe Chair";
fixture.given(new OrderCreatedEvent(orderId, productId), new OrderConfirmedEvent(orderId))
  .when(new ShipOrderCommand(orderId))
  .expectEvents(new OrderShippedEvent(orderId));
```

### The Query Model – Event Handlers

So far, we've established our core API with the commands and events, and we have the command model of our CQRS Order
service, the _OrderAggregate_, in place.

Next, **we can start thinking of one of the Query Models our application should service.**

One of these models is the _Order_:

```java
public class Order {

    private final String orderId;
    private final String productId;
    private OrderStatus orderStatus;

    public Order(String orderId, String productId) {
        this.orderId = orderId;
        this.productId = productId;
        orderStatus = OrderStatus.CREATED;
    }

    public void setOrderConfirmed() {
        this.orderStatus = OrderStatus.CONFIRMED;
    }

    public void setOrderShipped() {
        this.orderStatus = OrderStatus.SHIPPED;
    }

    // getters, equals/hashCode and toString functions
}
public enum OrderStatus {
    CREATED, CONFIRMED, SHIPPED
}
```

**We'll update this model based on the events propagating through our system.** A Spring Service bean to update our
model will do the trick:

```java
@Service
public class OrdersEventHandler {

    private final Map<String, Order> orders = new HashMap<>();

    @EventHandler
    public void on(OrderCreatedEvent event) {
        String orderId = event.getOrderId();
        orders.put(orderId, new Order(orderId, event.getProductId()));
    }

    // Event Handlers for OrderConfirmedEvent and OrderShippedEvent...
}
```

As we've used the axon-spring-boot-starter dependency to initiate our Axon application, the framework will automatically
scan all the beans for existing message-handling functions.

As the _OrdersEventHandler_ has [_
EventHandler_](https://apidocs.axoniq.io/4.0/org/axonframework/eventhandling/EventHandler.html) annotated functions to
store an Order and update it, this bean will be registered by the framework as a class that should receive events
without requiring any configuration on our part.

### The Query Model – Query Handlers

Next, to query this model to for example retrieve all the orders, we should first introduce a Query message to our core
API:

```java
public class FindAllOrderedProductsQuery { }
```

Second, we'll have to update the _OrdersEventHandler_ to be able to handle the _FindAllOrderedProductsQuery_:

```java
@QueryHandler
public List<Order> handle(FindAllOrderedProductsQuery query) {
    return new ArrayList<>(orders.values());
}
```

The [QueryHandler](https://apidocs.axoniq.io/4.0/org/axonframework/queryhandling/QueryHandler.html) annotated function
will handle the _FindAllOrderedProductsQuery_ and is set to return a _List<Order>_ regardless, similarly to any ‘find
all' query.

### Putting Everything Together

We've fleshed out our core API with commands, events, and queries, and set up our command and query model by having an _
OrderAggregate_ and _Order_ model.

Next is to tie up the loose ends of our infrastructure. As we're using the axon-spring-boot-starter, this sets a lot of
the required configuration automatically.

First, **as we want to leverage Event Sourcing for our Aggregate, we'll need
an [EventStore](https://apidocs.axoniq.io/4.0/org/axonframework/eventsourcing/eventstore/EventStore.html).** Axon Server
which we have started up in step three will fill this hole.

Secondly, we need a mechanism to store our Order query model. For this example, we can add h2 as an in-memory database
and spring-boot-starter-data-jpa for ease of use:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Setting up a REST Endpoint

Next, we need to be able to access our application, for which we'll be leveraging a REST endpoint by adding the
spring-boot-starter-web dependency:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

**From our REST endpoint, we can start dispatching commands and queries:**

```java
@RestController
public class OrderRestEndpoint {

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;

    // Autowiring constructor and POST/GET endpoints
}
```

**The [CommandGateway](https://apidocs.axoniq.io/4.0/org/axonframework/commandhandling/gateway/CommandGateway.html) is
used as the mechanism to send our command messages, and the QueryGateway, in turn, sends query messages.** The gateways
provide a simpler, more straightforward API, compared to
the [CommandBus](https://apidocs.axoniq.io/4.0/org/axonframework/commandhandling/CommandBus.html)
and [QueryBus](https://apidocs.axoniq.io/4.0/org/axonframework/queryhandling/QueryBus.html) that they connect with.

From here on, **our OrderRestEndpoint should have a POST endpoint to create, confirm, and ship an order:**

```java
@PostMapping("/ship-order")
public CompletableFuture<Void> shipOrder() {
    String orderId = UUID.randomUUID().toString();
    return commandGateway.send(new CreateOrderCommand(orderId, "Deluxe Chair"))
                         .thenCompose(result -> commandGateway.send(new ConfirmOrderCommand(orderId)))
                         .thenCompose(result -> commandGateway.send(new ShipOrderCommand(orderId)));
}

```

This rounds up the command side of our CQRS application. Note that a CompletableFuture is returned by the gateway,
enabling asynchronizity.

Now, all that's left is a GET endpoint to query all the Order:

```java
@GetMapping("/all-orders")
public CompletableFuture<List<Order>> findAllOrders() {
    return queryGateway.query(new FindAllOrderedProductsQuery(), ResponseTypes.multipleInstancesOf(Order.class));
}
```

**In the GET endpoint, we leverage the QueryGateway to dispatch a point-to-point query.** In doing so, we create a
default _FindAllOrderedProductsQuery_, but we also need to specify the expected return type.

As we expect multiple Order instances to be returned, we leverage the
static [ResponseTypes#multipleInstancesOf(Class)](https://apidocs.axoniq.io/4.0/org/axonframework/messaging/responsetypes/ResponseTypes.html)
function. With this, we have provided a basic entrance into the query side of our Order service.

We completed the setup, so now we can send some commands and queries through our REST Controller once we've started up
the OrderApplication.

POST-ing to endpoint _/ship-order_ will instantiate an _OrderAggregate_ that'll publish events, which, in turn, will _
save/update_ our _Orders_. GET-ing from the _/all-orders_ endpoint will publish a query message that'll be handled by
the OrdersEventHandler, which will return all the existing Orders.

### Conclusion

In this article, we introduced the Axon Framework as a powerful base for building an application leveraging the benefits
of CQRS and Event Sourcing.

We implemented a simple Order service using the framework to show how such an application should be structured in
practice.

Lastly, Axon Server posed as our Event Store and the message routing mechanism, greatly simplifying the infrastructure.

For any additional questions on this topic, also check out [Discuss AxonIQ](https://discuss.axoniq.io/).

