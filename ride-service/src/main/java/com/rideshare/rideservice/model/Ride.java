package com.rideshare.rideservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "rides")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    // Who requested the ride
    @Column(nullable = false)
    private String riderId;

    // Who accepted the ride (null until match)
    @Column(nullable = false)
    private String driverId;

    @Column(nullable = false)
    private double pickupLatitude;

    @Column(nullable = false)
    private double pickupLongitude;

    @Column(nullable = false)
    private String pickupAddress;

    @Column(nullable = false)
    private double dropLatitude;

    @Column(nullable = false)
    private double dropLongitude;

    @Column(nullable = false)
    private String dropAddress;

    // Ride status - tracks the lifecycle
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus rideStatus;

    // Fare Details
    private double estimatedFare;

    private double actualFare;

    // Timestamps
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private  LocalDateTime updatedAt;

    private LocalDateTime startedTime;

    private LocalDateTime completeTime;

}
