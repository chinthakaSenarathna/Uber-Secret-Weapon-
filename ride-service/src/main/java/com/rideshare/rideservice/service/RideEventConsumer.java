package com.rideshare.rideservice.service;

import com.rideshare.rideservice.event.RideMatchedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideEventConsumer {

    private final RideService rideService;

    /**
     *
     * Listen to ride.matched kafka topic
     * Triggered every time, Matching Service published a new ride request
     *
     * FLOW:
     * Matching Service -> kafka(ride.matched) -> This Consumer -> Ride Service
     *
     * */

    @KafkaListener(
            topics = "ride.matched",
            groupId = "ride-service-group"
    )
    public void consumeRideMatchedEvent(RideMatchedEvent event){

        try {
            rideService.updateRideWithDriver(event.getRideId(), event.getDriverId());
        }catch (Exception ex){
            log.error("Error processing match request: {} - {}", event.getRideId(),ex.getMessage(),ex);
        }

    }

}
