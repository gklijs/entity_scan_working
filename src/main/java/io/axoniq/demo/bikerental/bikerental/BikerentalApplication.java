package io.axoniq.demo.bikerental.bikerental;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.config.EventProcessingConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@EntityScan(basePackages = {
        "io.axoniq.demo.bikerental.bikerental",
        "org.axonframework.eventhandling.tokenstore",
        "org.axonframework.eventhandling.deadletter.jpa",
        "org.axonframework.modelling.saga.repository.jpa"
        })
@SpringBootApplication
public class BikerentalApplication {

    public static void main(String[] args) {
        SpringApplication.run(BikerentalApplication.class, args);
    }

    @Autowired
    public void configure(ObjectMapper objectMapper) {
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(),
                                           ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS);
    }

    @Autowired
    public void eventProcessingModule(EventProcessingConfigurer configurer) {
        configurer.usingPooledStreamingEventProcessors()
                  .registerPooledStreamingEventProcessorConfiguration((c, processor) ->
                                                                              processor.workerExecutor(workerExecutor()));
    }

    @Qualifier("workerExecutor")
    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService workerExecutor() {
        return Executors.newScheduledThreadPool(2);
    }
}
