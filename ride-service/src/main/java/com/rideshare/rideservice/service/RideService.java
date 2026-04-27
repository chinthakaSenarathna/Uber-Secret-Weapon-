package com.rideshare.rideservice.service;

import com.rideshare.rideservice.dto.RideRequest;
import com.rideshare.rideservice.dto.RideResponse;
import com.rideshare.rideservice.event.RideRequestedEvent;
import com.rideshare.rideservice.model.Ride;
import com.rideshare.rideservice.model.RideStatus;
import com.rideshare.rideservice.repository.RideRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final KafkaTemplate<String, RideRequestedEvent> kafkaTemplate;

    private static final String RIDE_REQUESTED_TOPIC = "ride.requested";

    public RideResponse rideRequest(@Valid RideRequest rideRequest) {

        log.info("New ride request from rider: {}", rideRequest.getRiderId());

        // Step 1: save ride to database
        Ride newRide = new Ride();
        newRide.setRiderId(rideRequest.getRiderId());
        newRide.setPickupLatitude(rideRequest.getPickupLatitude());
        newRide.setPickupLongitude(rideRequest.getPickupLongitude());
        newRide.setPickupAddress(rideRequest.getPickupAddress());
        newRide.setDropLatitude(rideRequest.getDropLatitude());
        newRide.setDropLongitude(rideRequest.getDropLongitude());
        newRide.setDropAddress(rideRequest.getDropAddress());
        newRide.setRideStatus(RideStatus.REQUESTED);
        newRide.setEstimatedFare(calculateEstimateFare(rideRequest));

        Ride savedRide = rideRepository.save(newRide);



    }

    public RideResponse getRideById(String rideId) {
    }

    public List<RideResponse> getRidesByRider(String riderId) {
        return null;
    }

    public RideResponse startRide(String rideId) {
        return null;
    }

    public RideResponse completeRide(String rideId) {
    }

    public RideResponse cancelRide(String rideId) {
    }
}
