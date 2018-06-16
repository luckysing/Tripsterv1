package com.tripster.arvinder.tripsterv1.api;

import com.tripster.arvinder.tripsterv1.model.api.places.Places;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PlacesApi {
    //https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=33.8241470,-118.3412720&radius=50000&name=restaurant%20torrance&sensor=false&key=AIzaSyBzuKwz9f4K3dusgIocCzEYVoB6uiQDENk
    @GET("maps/api/place/nearbysearch/json?")
    Call<Places> getPlaces(
            @Query("location") String destination,
            @Query("radius") int radius,
            @Query("name") String name,
            @Query("sensor") boolean sensor,
            @Query("key") String mode
    );
}
