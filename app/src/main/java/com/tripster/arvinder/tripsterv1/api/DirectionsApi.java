package com.tripster.arvinder.tripsterv1.api;

import com.tripster.arvinder.tripsterv1.model.api.direction.Directions;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DirectionsApi {
    //https://maps.googleapis.com/maps/api/directions/json?origin=34.031657,-118.444594&destination=37.482708,-121.914868&sensor=false&mode=driving
    @GET("maps/api/directions/json?")
    Call<Directions> getDirections(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("sensor") boolean sensor,
            @Query("mode") String mode
    );
}
