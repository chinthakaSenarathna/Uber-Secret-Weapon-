package com.rideshare.machingservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * Event consumed from kafka topic: ride.requested
 * Published by Ride Service when a rider requests a ride
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
