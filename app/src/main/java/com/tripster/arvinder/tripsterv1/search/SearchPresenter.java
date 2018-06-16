package com.tripster.arvinder.tripsterv1.search;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.tripster.arvinder.tripsterv1.activity.MapsActivity;
import com.tripster.arvinder.tripsterv1.api.DirectionsApi;
import com.tripster.arvinder.tripsterv1.api.RetrofitClientInstance;
import com.tripster.arvinder.tripsterv1.model.api.direction.Directions;
import com.tripster.arvinder.tripsterv1.model.api.direction.Step;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchPresenter {
    private SearchView searchView;
    private List<MapsActivity.Poi> poiList = new ArrayList<>();

    public SearchPresenter(SearchView searchView) {
        this.searchView = searchView;
    }

    public void onSearch(LatLng srcLatLng, LatLng destLatLng) {
        if (srcLatLng == null || destLatLng == null) {
            return;
        }
        final DirectionsApi directionsApi = RetrofitClientInstance.getRetrofitInstance().create(DirectionsApi.class);

        Call<Directions> call = directionsApi.getDirections(srcLatLng.latitude + "," + srcLatLng.longitude, destLatLng.latitude + "," + destLatLng.longitude, false, "driving");
        call.enqueue(new Callback<Directions>() {

            @Override
            public void onResponse(Call<Directions> call, Response<Directions> response) {
                Log.d("fuck yeah", "i got a response");
                Directions directions = response.body();
                if (directions.getStatus().equals("OK")) {
                    //handle this gracefully
                    List<Step> steps = directions.getRoutes().get(0).getLegs().get(0).getSteps();
                    searchView.navigateToMap(steps, poiList);
                }
                Log.d("fuck yeah", "i got a response " + directions.getStatus());

            }

            @Override
            public void onFailure(Call<Directions> call, Throwable t) {
                searchView.showError("Something went wrong...Please try later!");
            }
        });
    }

    private LatLng getMidPoint(double lat1, double lon1, double lat2, double lon2) {

        double dLon = Math.toRadians(lon2 - lon1);

        //convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        lon1 = Math.toRadians(lon1);

        double Bx = Math.cos(lat2) * Math.cos(dLon);
        double By = Math.cos(lat2) * Math.sin(dLon);
        double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
        double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);

        return new LatLng(lat3, lon3);
    }

    public void poiSelected(MapsActivity.Poi poi) {
        poiList.add(poi);
    }
}
