# A Guide to the Axon Framework

In this repository, we'll be looking at Axon and how it helps us implement applications
with [CQRS](https://martinfowler.com/bliki/CQRS.html) (Command Query Responsibility Segregation)
and [Event Sourcing](https://martinfowler.com/eaaDev/EventSourcing.html) in mind.

During this guide, both Axon Framework and [Axon Server](https://axoniq.io/product-overview/axon-server) will be
utilized. The former will contain our implementation and the latter will be our dedicated Event Store and Message
Routing solution.

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
