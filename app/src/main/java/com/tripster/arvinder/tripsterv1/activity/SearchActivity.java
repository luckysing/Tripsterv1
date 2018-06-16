package com.tripster.arvinder.tripsterv1.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.tripster.arvinder.tripsterv1.R;
import com.tripster.arvinder.tripsterv1.model.api.direction.PlaceAutocomplete;
import com.tripster.arvinder.tripsterv1.model.api.direction.Step;
import com.tripster.arvinder.tripsterv1.search.PlaceArrayAdapter;
import com.tripster.arvinder.tripsterv1.search.SearchPresenter;
import com.tripster.arvinder.tripsterv1.search.SearchView;

import java.io.Serializable;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, SearchView {
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private static final String TAG = "SearchActivity";

    private LatLng srcLatLng;
    private LatLng destLatLng;
    private PlaceArrayAdapter sourceArrayAdapter;
    private PlaceArrayAdapter destinationArrayAdapter;
    private AutoCompleteTextView sourceAddress;
    private AutoCompleteTextView destinationAddress;
    private GoogleApiClient apiClient;
    private SearchPresenter searchPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        apiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();

        //hotelButton.setCompoundDrawablesWithIntrinsicBounds(null, getStateListDrawable(), null, null);

        sourceAddress = findViewById(R.id.source_address);
        sourceAddress.setThreshold(3);
        sourceAddress.setOnItemClickListener(sourceAutocompleteClickListener);
        sourceArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1,
                null, null);
        sourceAddress.setAdapter(sourceArrayAdapter);

        destinationAddress = findViewById(R.id.destination_address);
        destinationAddress.setThreshold(3);
        destinationAddress.setOnItemClickListener(destinationAutocompleteClickListener);
        destinationArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1,
                null, null);
        destinationAddress.setAdapter(destinationArrayAdapter);
        searchPresenter = new SearchPresenter(this);
    }

    public void onSearch(View view) {
        searchPresenter.onSearch(srcLatLng, destLatLng);
    }

    public void onCafeClicked(View view) {
        searchPresenter.poiSelected(MapsActivity.Poi.CAFE);
        disableSelection(view);
    }

    public void onHotelClicked(View view) {
        searchPresenter.poiSelected(MapsActivity.Poi.HOTEL);
        disableSelection(view);
    }

    public void onGasClicked(View view) {
        searchPresenter.poiSelected(MapsActivity.Poi.GAS);
        disableSelection(view);

    }

    public void onFoodClicked(View view) {
        searchPresenter.poiSelected(MapsActivity.Poi.FOOD);
        disableSelection(view);
    }

    private void disableSelection(View view) {
        ((Button) view).setTextColor(getResources().getColor(R.color.colorDisabledText));
        view.setEnabled(false);
    }

    private AdapterView.OnItemClickListener sourceAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final PlaceAutocomplete item = sourceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(TAG, "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(apiClient, placeId);
            placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                @Override
                public void onResult(@NonNull PlaceBuffer places) {
                    if (!places.getStatus().isSuccess()) {
                        Log.e(TAG, "Place query did not complete. Error: " +
                                places.getStatus().toString());
                        return;
                    }
                    // Selecting the first object buffer.
                    final Place place = places.get(0);
                    srcLatLng = place.getLatLng();
                }
            });
            Log.i(TAG, "Fetching details for ID: " + item.placeId);
        }
    };

    private AdapterView.OnItemClickListener destinationAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final PlaceAutocomplete item = destinationArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(TAG, "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(apiClient, placeId);
            placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                @Override
                public void onResult(@NonNull PlaceBuffer places) {
                    if (!places.getStatus().isSuccess()) {
                        Log.e(TAG, "Place query did not complete. Error: " +
                                places.getStatus().toString());
                        return;
                    }
                    // Selecting the first object buffer.
                    final Place place = places.get(0);
                    destLatLng = place.getLatLng();
                }
            });
            Log.i(TAG, "Fetching details for ID: " + item.placeId);
        }
    };


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        sourceArrayAdapter.setGoogleApiClient(apiClient);
        destinationArrayAdapter.setGoogleApiClient(apiClient);
        Log.i(TAG, "Google Places API connected.");

    }

    @Override
    public void onConnectionSuspended(int i) {
        sourceArrayAdapter.setGoogleApiClient(null);
        destinationArrayAdapter.setGoogleApiClient(null);
        Log.e(TAG, "Google Places API connection suspended.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.e(TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onStop() {
        if (apiClient != null && apiClient.isConnected()) {
            sourceArrayAdapter.setGoogleApiClient(null);
            destinationArrayAdapter.setGoogleApiClient(null);
            apiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void showError(String errorMessage) {
        Toast.makeText(SearchActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToMap(List<Step> steps, List<MapsActivity.Poi> poiList) {
        Log.d(TAG, "successful");
        Intent mapIntent = new Intent(this, MapsActivity.class);
        mapIntent.putExtra("steps", (Serializable) steps); // make parcelable
        mapIntent.putExtra("src", srcLatLng);
        mapIntent.putExtra("dest", destLatLng);
        mapIntent.putExtra("poiList", (Serializable) poiList);
        startActivity(mapIntent);
    }
}
