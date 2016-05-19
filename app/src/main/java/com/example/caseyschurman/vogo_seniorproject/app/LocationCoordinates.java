package com.example.caseyschurman.vogo_seniorproject.app;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by casey.schurman on 3/9/2016.
 */
public class LocationCoordinates {

    private static final String TAG = "LocationCoordinates";
    static LatLng result;

    public static LatLng getAddressFromLocation(final String locationAddress, final Context context) {

        Map<Double, Double> coordinateMap1 = new HashMap<Double, Double>();
        coordinateMap1.put(42.219737, -121.793993);
        Map<Double, Double> coordinateMap2 = new HashMap<Double, Double>();
        coordinateMap2.put(32.856086, -96.846023);
        Map<Double, Double> coordinateMap3 = new HashMap<Double, Double>();
        coordinateMap3.put(35.499303, -80.848685);
        Map<Double, Double> coordinateMap4 = new HashMap<Double, Double>();
        coordinateMap4.put(39.928413, -74.993656);
        Map<Double, Double> coordinateMap5 = new HashMap<Double, Double>();
        coordinateMap5.put(42.225572, -121.779294);
        Map<Double, Double> coordinateMap6 = new HashMap<Double, Double>();
        coordinateMap6.put(44.958649, -122.984840);

        Map<String, Map<Double, Double>> locationMap = new HashMap<String, Map<Double, Double>>();
        locationMap.put("707 Cypress Ave, Klamath Falls, OR 97601", coordinateMap1);
        locationMap.put("8609 Glencrest Ln, Dallas, TX 75209", coordinateMap2);
        locationMap.put("PO Box 605 Davidson, NC 28036", coordinateMap3);
        locationMap.put("375 Kings Highway North Cherry Hill, New Jersey 08034", coordinateMap4);
        locationMap.put("823 Walnut Ave, Klamath Falls, OR 97601", coordinateMap5);
        locationMap.put("3855 Wolverine St NE # 6, Salem, OR 97305", coordinateMap6);

        Map<Double, Double> longLat = locationMap.get(locationAddress);
        Set<Double> setLongitude = longLat.keySet();
        Iterator<Double> iter1 = setLongitude.iterator();
        Double longitude = iter1.next();

        Collection<Double> collDoubles = longLat.values();
        Iterator<Double> iter2 = collDoubles.iterator();
        Double latitude = iter2.next();

        result = new LatLng(longitude, latitude);

        return result;
    }
}
