package com.rideshare.rideservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * Event consumed from kafka topic: ride.matched
 * Published by Matching Service to update ride with assigned driver
 *
 * */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideMatchedEvent {

    private String rideId;

    private String riderId;

    private String driverId;

    private double driverLatitude;

    private double driverLongitude;

    private double distanceToPickupKm;

}
