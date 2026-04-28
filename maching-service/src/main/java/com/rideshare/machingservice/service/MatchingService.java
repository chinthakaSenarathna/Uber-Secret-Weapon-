package com.rideshare.machingservice.service;

import com.rideshare.machingservice.client.LocationServiceClient;
import com.rideshare.machingservice.dto.NearByDriverResponse;
import com.rideshare.machingservice.event.RideMatchedEvent;
import com.rideshare.machingservice.event.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchingService {

    private final LocationServiceClient locationServiceClient;
    private final KafkaTemplate<String, RideMatchedEvent> kafkaTemplate;

    private static final String RIDE_MATCHED_TOPIC = "ride.matched";
    private static final double DEFAULT_SEARCH_RADIUS_KM = 5.0;

    /**
     *
     * Main matching algorithm
     * called when RideRequestedEvent is consumed from kafka
     * @param event
     *
     * Steps:
     * 1. Ask Location Service for nearby drivers
     * 2. Score each driver and pick the best one
     *
     * */

    public void matchDriverForRide(RideRequestedEvent event){

        // Ask Location Service for nearby drivers
        List<NearByDriverResponse> nearByDrivers = locationServiceClient.getNearByDrivers(
                event.getPickupLatitude(),
                event.getPickupLongitude(),
                DEFAULT_SEARCH_RADIUS_KM
        );

        if(nearByDrivers.isEmpty()){
            log.warn("No nearby drivers found for ride: {}", event.getRideId());
            return;
        }

        // Score each driver and pick the best one
        Optional<NearByDriverResponse> bestDriver = findBestDriver(nearByDrivers);

        if(bestDriver.isEmpty()){
            log.warn("Could not find suitable driver for ride: {}", event.getRideId());
            return;
        }

        NearByDriverResponse assignedDriver = bestDriver.get();

        // Publish RideMatchedEvent to kafka
        RideMatchedEvent matchedEvent = new RideMatchedEvent();
        matchedEvent.setRideId(event.getRideId());
        matchedEvent.setRiderId(event.getRiderId());
        matchedEvent.setDriverId(assignedDriver.getDriverId());
        matchedEvent.setDriverLatitude(assignedDriver.getLatitude());
        matchedEvent.setDriverLongitude(assignedDriver.getLongitude());
        matchedEvent.setDistanceToPickupKm(assignedDriver.getDistanceInKm());

        kafkaTemplate.send(RIDE_MATCHED_TOPIC, matchedEvent.getRideId(), matchedEvent);
        log.info("RideMatcheddEvent publish on kafka from ride {}", matchedEvent.getRideId());

    }

    /**
     *
     * Driver Scoring Algorithm
     *
     * Distance: 70%
     * Rating: 30%
     *
     * Score = (1 / distance) * distanceWeight + rating * ratingWeight
     *
     * */
    private Optional<NearByDriverResponse> findBestDriver(List<NearByDriverResponse> nearByDrivers) {

        double distanceWeight = 0.7;
        double ratingWeight = 0.3;

        return nearByDrivers.stream()
                .max(Comparator.comparingDouble(driver -> {
                    // Distance score: closer = higher score
                    // Add 0.1 to avoid division by Zero
                    double distanceScore = 1 / (driver.getDistanceInKm() + 0.1);

                    // Simulated rating between 4.0 and 5.0
                    // In production fetch from Driver Service
                    double ratingScore = 4.0 + Math.random();

                    // Final weighted score
                    return (distanceScore * distanceWeight + ratingScore * ratingWeight);

                }));

    }

}
