package com.example.live_backend.domain.place.util;

import org.springframework.stereotype.Component;


@Component
public class PlaceDistanceCalculator {
    
    private static final double EARTH_RADIUS_METERS = 6371000;

    public Integer calculate(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return (int) Math.round(EARTH_RADIUS_METERS * c);
    }

}