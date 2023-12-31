package io.axoniq.demo.bikerental.bikerental.query;

import io.axoniq.demo.bikerental.bikerental.coreapi.BikeRegisteredEvent;
import io.axoniq.demo.bikerental.bikerental.coreapi.BikeRentedEvent;
import io.axoniq.demo.bikerental.bikerental.coreapi.BikeReturnedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
public class BikeStatusProjection {

    private final BikeStatusRepository bikeStatusRepository;

    public BikeStatusProjection(BikeStatusRepository bikeStatusRepository) {
        this.bikeStatusRepository = bikeStatusRepository;
    }

    @EventHandler
    public void on(BikeRegisteredEvent event) {
        bikeStatusRepository.save(new BikeStatus(event.bikeId(), event.location()));
    }

    @EventHandler
    public void on(BikeRentedEvent event) {
        bikeStatusRepository.findById(event.bikeId()).map(bs -> {
            bs.setRenter(event.renter());
            return bs;
        });
    }

    @EventHandler
    public void on(BikeReturnedEvent event) {
        bikeStatusRepository.findById(event.bikeId()).map(bs -> {
            bs.setRenter(null);
            bs.setLocation(event.location());
            return bs;
        });
    }

    @QueryHandler(queryName = "findAll")
    public Iterable<BikeStatus> findAll() {
        return bikeStatusRepository.findAll();
    }

    @QueryHandler(queryName = "findOne")
    public BikeStatus findOne(String bikeId) {
        return bikeStatusRepository.findById(bikeId).orElse(null);
    }
}
