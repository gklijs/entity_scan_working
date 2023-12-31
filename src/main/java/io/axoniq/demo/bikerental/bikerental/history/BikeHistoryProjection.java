package io.axoniq.demo.bikerental.bikerental.history;

import io.axoniq.demo.bikerental.bikerental.coreapi.BikeRegisteredEvent;
import io.axoniq.demo.bikerental.bikerental.coreapi.BikeRentedEvent;
import io.axoniq.demo.bikerental.bikerental.coreapi.BikeReturnedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.Timestamp;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class BikeHistoryProjection {

    private final BikeHistoryRepository bikeHistoryRepository;
    private final QueryUpdateEmitter updateEmitter;

    public BikeHistoryProjection(BikeHistoryRepository bikeHistoryRepository, QueryUpdateEmitter updateEmitter) {
        this.bikeHistoryRepository = bikeHistoryRepository;
        this.updateEmitter = updateEmitter;
    }

    @EventHandler
    public void handle(BikeRegisteredEvent event, @Timestamp Instant timestamp) {
        bikeHistoryRepository.save(new BikeHistory(event.bikeId(), timestamp, "Bike registered in " + event.location()));
    }

    @EventHandler
    public void handle(BikeRentedEvent event, @Timestamp Instant timestamp) {
        BikeHistory newEntry = new BikeHistory(event.bikeId(), timestamp, "Bike rented out to " + event.renter());
        bikeHistoryRepository.save(newEntry);

        updateEmitter.emit(m -> "locationHistory".equals(m.getQueryName())
                                   && newEntry.getBikeId().equals(m.getPayload()),
                           newEntry);
    }

    @EventHandler
    public void handle(BikeReturnedEvent event, @Timestamp Instant timestamp) {
        BikeHistory newEntry = new BikeHistory(event.bikeId(), timestamp, "Bike returned in " + event.location());
        bikeHistoryRepository.save(newEntry);

        updateEmitter.emit(m -> "locationHistory".equals(m.getQueryName())
                                   && newEntry.getBikeId().equals(m.getPayload()),
                           newEntry);
    }

    @QueryHandler(queryName = "locationHistory")
    public List<BikeHistory> findMovements(String bikeId) {
        return bikeHistoryRepository.findByBikeIdOrderById(bikeId);
    }

}
