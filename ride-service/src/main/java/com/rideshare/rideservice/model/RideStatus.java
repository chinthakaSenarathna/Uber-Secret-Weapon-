package com.rideshare.rideservice.model;

/**
 * Flow:
 * Requested -> Matching -> Accepted -> Driver_Arriving
 *           -> Ride_Started -> Completed
 *           -> Canceled (can happen at multiple stages)
 *
 * */

public enum RideStatus {
    REQUESTED,
    MATCHING,
    ACCEPTED,
    DRIVING_ARRIVING,
    RIDE_STARTED,
    COMPLETED,
    CANCELLED
}
