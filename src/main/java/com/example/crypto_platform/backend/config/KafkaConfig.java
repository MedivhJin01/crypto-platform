package com.example.crypto_platform.backend.config;

import com.example.crypto_platform.backend.dto.CsBatch;
import com.example.crypto_platform.contract.MarketEvent;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServer;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic csBatchTopic(@Value("${app.kafka.cs-batch-topic:cs-batch-raw}") String name) {
        return new NewTopic(name, 6, (short) 1);
    }

    @Bean
    public NewTopic marketEventTopic(@Value("${app.kafka.market-event-topic:market-event}") String name) {
        return new NewTopic(name, 3, (short) 1);
    }

    @Bean
    public ProducerFactory<String, CsBatch> csRawProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaProducerFactory<>(configProps);
    }
    @Bean
    public ProducerFactory<String, MarketEvent> marketEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean(name = "csRawKafkaTemplate")
    public KafkaTemplate<String, CsBatch> csRawKafkaTemplate() {
        return new KafkaTemplate<>(csRawProducerFactory());
    }

    @Bean(name = "marketEventKafkaTemplate")
    public KafkaTemplate<String, MarketEvent> marketEventKafkaTemplate() {
        return new KafkaTemplate<>(marketEventProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, CsBatch> csRawConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 64 * 1024);
        configProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 250);

        JsonDeserializer<CsBatch> jsonDeserializer = new JsonDeserializer<>(CsBatch.class);
        jsonDeserializer.setUseTypeHeaders(false);
        jsonDeserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), jsonDeserializer);
    }

    @Bean(name = "csBatchListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, CsBatch> csBatchListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CsBatch> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(csRawConsumerFactory());
        factory.setBatchListener(true);
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, MarketEvent> marketEventRawConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");


        JsonDeserializer<MarketEvent> jsonDeserializer = new JsonDeserializer<>(MarketEvent.class);
        jsonDeserializer.addTrustedPackages("*");
        jsonDeserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), jsonDeserializer);
    }

    @Bean(name = "marketEventKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, MarketEvent> marketEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, MarketEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(marketEventRawConsumerFactory());
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }

}
