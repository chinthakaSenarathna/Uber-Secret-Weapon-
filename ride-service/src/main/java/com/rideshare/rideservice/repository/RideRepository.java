package com.rideshare.rideservice.repository;

import com.rideshare.rideservice.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

@EnableJpaRepositories
public interface RideRepository extends JpaRepository<Ride, String> {

    List<Ride> findByRiderIdOrderByCreatedAtDesc(String riderId);

}
