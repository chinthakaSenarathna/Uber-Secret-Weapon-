package com.rideshare.locationservice.service;

import com.rideshare.locationservice.dto.DriverLocationRequest;
import com.rideshare.locationservice.dto.NearByDriverResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationService {

    private final RedisTemplate<String, String> redisTemplate;

    // Redis key for all drivers locations
    private static final String DRIVERS_GEO_KEY = "drivers:locations";

    /* Update driver location in Redis.
    *  Called every each 3 seconds by driver's phone
    *  Map to Redis GEOADD command
    * */
    public void updateDriverLocation(DriverLocationRequest driverLocationRequest) {
        log.info("Updating location for driver: {}", driverLocationRequest.getDriverId());

        // IMPORTANT : longitude FIRST, latitude SECOND - GeoSpatial Standard
        Point driverPoint = new Point(
                driverLocationRequest.getLongitude(),
                driverLocationRequest.getLatitude()
        );

        redisTemplate.opsForGeo().add(
                DRIVERS_GEO_KEY,
                driverPoint,
                driverLocationRequest.getDriverId()
        );

        log.info("Updated location for driver: {}", driverLocationRequest.getDriverId());
    }

    /* Find nearby drivers within given radius.
    *  called by matching service on ride request.
    *  Map to radius GEORADIUS command
    * */
    public List<NearByDriverResponse> findNearByDrivers(double latitude, double longitude, double radius) {
        log.info("Finding drivers near lat: {}, long: {} within {}Km", latitude, longitude,  radius);

        Circle searchArea = new Circle(
                new Point(longitude, latitude),
                new Distance(radius, Metrics.KILOMETERS)
        );

        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo().radius(
                        DRIVERS_GEO_KEY,
                        searchArea,
                        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                                .includeCoordinates()
                                .includeDistance()
                                .sortAscending()
                                .limit(10)
                );

        List<NearByDriverResponse> nearbyDrivers = new ArrayList<>();

        if(results != null) {
            results.getContent().forEach(result -> {
                RedisGeoCommands.GeoLocation<String> location = result.getContent();
                nearbyDrivers.add(new NearByDriverResponse(
                        location.getName(),
                        location.getPoint().getY(),
                        location.getPoint().getX(),
                        result.getDistance().getValue()
                ));
            });
        }

        log.info("Found {} drivers nearby", nearbyDrivers.size());

        return nearbyDrivers;
    }

    public void removeDriver(String driverId) {
    }
}
