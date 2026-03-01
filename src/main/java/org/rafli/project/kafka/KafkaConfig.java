package org.rafli.project.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic marketDataTopic() {
        return TopicBuilder.name("market-data")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
