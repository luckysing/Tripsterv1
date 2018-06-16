package com.tripster.arvinder.tripsterv1.model.api.direction;

import java.io.Serializable;

public class PlaceAutocomplete implements Serializable {

    public CharSequence placeId;
    public CharSequence description;

    public PlaceAutocomplete(CharSequence placeId, CharSequence description) {
        this.placeId = placeId;
        this.description = description;
    }

    @Override
    public String toString() {
        return description.toString();
    }
}