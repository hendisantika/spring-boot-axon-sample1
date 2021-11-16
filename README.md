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
