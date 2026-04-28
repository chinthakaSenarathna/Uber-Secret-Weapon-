package com.rideshare.machingservice.service;

import com.rideshare.machingservice.event.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideEventConsumer {

    private final MatchingService matchingService;

    /**
     *
     * Listen to ride.requested kafka topic
     * Triggered every time, Ride Service published a new ride request
     *
     * FLOW:
     * Ride Service -> kafka(ride.requested) -> This Consumer -> Matching Service
     *
     * */

    @KafkaListener(
            topics = "ride.requested",
            groupId = "matching-service-group"
    )
    public void consumeRideRequestedEvent(RideRequestedEvent event) {

        try {
            matchingService.matchDriverForRide(event);
        }catch (Exception ex){
            log.error("Error processing ride request: {} - {}", event.getRideId(),ex.getMessage(),ex);
        }

    }

}
