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
