package com.tripster.arvinder.tripsterv1.search;

import com.tripster.arvinder.tripsterv1.activity.MapsActivity;
import com.tripster.arvinder.tripsterv1.model.api.direction.Step;

import java.util.List;

public interface SearchView {
    void showError(String errorMessage);

    void navigateToMap(List<Step> steps, List<MapsActivity.Poi> poiList);
}
