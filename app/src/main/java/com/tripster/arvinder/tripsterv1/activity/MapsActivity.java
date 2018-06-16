package com.tripster.arvinder.tripsterv1.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.tripster.arvinder.tripsterv1.R;
import com.tripster.arvinder.tripsterv1.api.PlacesApi;
import com.tripster.arvinder.tripsterv1.api.RetrofitClientInstance;
import com.tripster.arvinder.tripsterv1.model.api.direction.EndLocation_;
import com.tripster.arvinder.tripsterv1.model.api.direction.StartLocation_;
import com.tripster.arvinder.tripsterv1.model.api.direction.Step;
import com.tripster.arvinder.tripsterv1.model.api.places.Places;
import com.tripster.arvinder.tripsterv1.model.api.places.Result;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "MapsActivity";
    private static final long DELAY_TIME = 1000;
    private static final int RADIUS = 1610; //1mile
    private GoogleMap mMap;
    private final Handler handler = new Handler();
    private Runnable loadPlacesMarkerRunnnable;

    public enum Poi {
        CAFE, HOTEL, FOOD, GAS
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(loadPlacesMarkerRunnnable);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final List<Step> steps = (List<Step>) getIntent().getSerializableExtra("steps");
        LatLng srcLatLong = getIntent().getParcelableExtra("src");
        LatLng destLatLong = getIntent().getParcelableExtra("dest");
        final List<Poi> poiList = (List<Poi>) getIntent().getSerializableExtra("poiList");
        animateRoute(srcLatLong, destLatLong, steps, poiList);
    }

    private void loadRoute(List<Step> steps) {
        for (Step step : steps) {
            List<LatLng> coordinates = PolyUtil.decode(step.getPolyline().getPoints());
            mMap.addPolyline(new PolylineOptions().color(Color.BLUE).addAll(coordinates));
        }
    }

    private void loadMarker(List<Step> steps, List<Poi> poiList) {
        final PlacesApi placesApi = RetrofitClientInstance.getRetrofitInstance().create(PlacesApi.class);
        for (Step step : steps) {
            for (Poi poi : poiList) {
                getPlacesApi(step.getStartLocation(), step.getEndLocation(), placesApi, poi);
            }
        }
    }

    private void animateRoute(LatLng srcLatLong, LatLng destLatLong, final List<Step> steps, final List<Poi> poiList) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(srcLatLong);
        builder.include(destLatLong);
        LatLngBounds bounds = builder.build();

        // begin new code:
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        mMap.animateCamera(cameraUpdate, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                loadRoute(steps);
                loadPlacesMarkerRunnnable = new Runnable() {
                    @Override
                    public void run() {
                        loadMarker(steps, poiList);
                    }
                };
                handler.postDelayed(loadPlacesMarkerRunnnable, DELAY_TIME);
            }

            @Override
            public void onCancel() {

            }
        });
    }

    private void getPlacesApi(StartLocation_ startLocation, EndLocation_ endLocation, PlacesApi placesApi, final Poi poiType) {
        Call<Places> callPlaces = placesApi.getPlaces(endLocation.getLat() + "," + endLocation.getLng(), RADIUS, getPoiType(poiType), false, getString(R.string.key_places));
        callPlaces.enqueue(new Callback<Places>() {
            @Override
            public void onResponse(Call<com.tripster.arvinder.tripsterv1.model.api.places.Places> call, Response<Places> response) {
                Log.d("fuck yeah", "i got a places response");
                com.tripster.arvinder.tripsterv1.model.api.places.Places places = response.body();
                int count = 0;
                for (Result result : places.getResults()) {
                    if (count < 5) {
                        mMap.addMarker(new MarkerOptions()
                                .position(result.getGeometry().getLatLng())
                                .title(result.getName())
                                .icon(bitmapDescriptorFromVector(MapsActivity.this, getIcon(poiType))));
                    }
                    count++;
                }
            }

            @Override
            public void onFailure(Call<com.tripster.arvinder.tripsterv1.model.api.places.Places> call, Throwable t) {
                Log.d("fuck no", "didnt get a places response");
            }
        });
    }

    private String getPoiType(Poi poiType) {
        switch (poiType) {
            case GAS:
                return "gas";
            case CAFE:
                return "cafe";
            case FOOD:
                return "restaurant";
            case HOTEL:
                return "hotel";
        }
        return "cafe";
    }


    private int getIcon(Poi poiType) {
        switch (poiType) {
            case GAS:
                return R.drawable.ic_gas_marker;
            case CAFE:
                return R.drawable.ic_cafe_marker;
            case FOOD:
                return R.drawable.ic_food_marker;
            case HOTEL:
                return R.drawable.ic_hotel_marker;
        }
        return R.drawable.ic_gas_marker;
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
