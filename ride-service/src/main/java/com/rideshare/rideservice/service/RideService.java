package com.rideshare.rideservice.service;

import com.rideshare.rideservice.dto.RideRequest;
import com.rideshare.rideservice.dto.RideResponse;
import com.rideshare.rideservice.event.RideMatchedEvent;
import com.rideshare.rideservice.event.RideRequestedEvent;
import com.rideshare.rideservice.model.Ride;
import com.rideshare.rideservice.model.RideStatus;
import com.rideshare.rideservice.repository.RideRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

        // Step 2: Publish even on kafka
        // Matching service consume this to find nearest drivers
        RideRequestedEvent event = new RideRequestedEvent(
                savedRide.getId(),
                savedRide.getRiderId(),
                savedRide.getPickupLatitude(),
                savedRide.getPickupLongitude(),
                savedRide.getPickupAddress(),
                savedRide.getDropLatitude(),
                savedRide.getDropLongitude(),
                savedRide.getDropAddress()
        );

        kafkaTemplate.send(RIDE_REQUESTED_TOPIC, savedRide.getId(), event);
        log.info("RideRequestedEvent publish on kafka from ride {}", savedRide.getId());

        // Update status to Matching
        savedRide.setRideStatus(RideStatus.MATCHING);
        rideRepository.save(savedRide);

        return mapToResponse(savedRide);

    }

    public RideResponse getRideById(String rideId) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride with id " + rideId + " not found"));

        return mapToResponse(ride);

    }

    public List<RideResponse> getRidesByRider(String riderId) {

        return rideRepository.findByRiderIdOrderByCreatedAtDesc(riderId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

    }

    public RideResponse startRide(String rideId) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride with id " + rideId + " not found"));

        if(ride.getRideStatus() != RideStatus.ACCEPTED) {
            throw new RuntimeException("Ride cannot be started. Current status is " + ride.getRideStatus());
        }

        ride.setRideStatus(RideStatus.RIDE_STARTED);
        ride.setStartedTime(LocalDateTime.now());
        rideRepository.save(ride);

        return mapToResponse(ride);
    }

    public RideResponse completeRide(String rideId) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride with id " + rideId + " not found"));

        if(ride.getRideStatus() != RideStatus.RIDE_STARTED) {
            throw new RuntimeException("Ride cannot be completed. Current status is " + ride.getRideStatus());
        }

        ride.setRideStatus(RideStatus.COMPLETED);
        ride.setCompleteTime(LocalDateTime.now());
        ride.setActualFare(ride.getEstimatedFare());
        rideRepository.save(ride);

        return mapToResponse(ride);

    }

    public RideResponse cancelRide(String rideId) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride with id " + rideId + " not found"));

        ride.setRideStatus(RideStatus.CANCELLED);
        rideRepository.save(ride);

        return mapToResponse(ride);

    }

    // This service called by Matching service to update Ride Status ???
    public void updateRideWithDriver(String rideId, String driverId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride with id " + rideId + " not found"));

        ride.setDriverId(driverId);
        ride.setRideStatus(RideStatus.ACCEPTED);
        rideRepository.save(ride);

    }

    private RideResponse mapToResponse(Ride savedRide) {
        RideResponse response = new RideResponse();
        response.setId(savedRide.getId());
        response.setRiderId(savedRide.getRiderId());
        response.setDriverId(savedRide.getDriverId());
        response.setPickupLatitude(savedRide.getPickupLatitude());
        response.setPickupLongitude(savedRide.getPickupLongitude());
        response.setPickupAddress(savedRide.getPickupAddress());
        response.setDropLatitude(savedRide.getDropLatitude());
        response.setDropLongitude(savedRide.getDropLongitude());
        response.setDropAddress(savedRide.getDropAddress());
        response.setRideStatus(savedRide.getRideStatus());
        response.setEstimatedFare(savedRide.getEstimatedFare());
        response.setActualFare(savedRide.getActualFare());
        response.setCreatedAt(savedRide.getCreatedAt());
        response.setUpdatedAt(savedRide.getUpdatedAt());
        response.setStartedTime(savedRide.getStartedTime());
        response.setCompleteTime(savedRide.getCompleteTime());
        return response;
    }

    private double calculateEstimateFare(RideRequest rideRequest) {

        // Simplified Haversine distance calculation
        double lat1 = Math.toRadians(rideRequest.getPickupLatitude());
        double lat2 = Math.toRadians(rideRequest.getDropLatitude());

        double lon1 = Math.toRadians(rideRequest.getPickupLongitude());
        double lon2 = Math.toRadians(rideRequest.getDropLongitude());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                + Math.pow(Math.sin(dLon / 2), 2);

        double c = 2 * Math.asin(Math.sqrt(a));
        double distanceKm = 6371 * c;

        // Base fare: 50RS + 12RS. perKm
        double fare = 50 + (distanceKm * 12);
        return Math.round(fare * 100) / 100.0;

    }

}
