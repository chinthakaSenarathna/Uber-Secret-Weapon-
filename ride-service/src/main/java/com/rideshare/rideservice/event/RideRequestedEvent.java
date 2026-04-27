package com.rideshare.rideservice.event;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * An event publish to kafka, when a ride is requested
 * Matching service consumes this event
 * Topic: ride.requested
 *
 * */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideRequestedEvent {

    private String rideId;
    private String riderId;

    // Pickup
    private double pickupLatitude;

    private double pickupLongitude;

    private String pickupAddress;

    // Drop
    private double dropLatitude;

    private double dropLongitude;

    private String dropAddress;

}
