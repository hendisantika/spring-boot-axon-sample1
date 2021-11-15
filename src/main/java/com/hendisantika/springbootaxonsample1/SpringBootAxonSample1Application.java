package com.hendisantika.springbootaxonsample1;

import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.Snapshotter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringBootAxonSample1Application {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootAxonSample1Application.class, args);
    }

    @Bean
    public SnapshotTriggerDefinition orderAggregateSnapshotTriggerDefinition(Snapshotter snapshotter,
                                                                             @Value("${axon.aggregate.order" +
                                                                                     ".snapshot-threshold:250}") int threshold) {
        return new EventCountSnapshotTriggerDefinition(snapshotter, threshold);
    }
}
