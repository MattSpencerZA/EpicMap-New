package com.example.EpicMap;

import com.mapbox.api.directions.v5.DirectionsCriteria;

public class MapHelper {

    public String getTransportationMethod(String getTransport) {

        String transMethod = null;

        if(getTransport.equals("Car")) {
            transMethod = DirectionsCriteria.PROFILE_DRIVING_TRAFFIC;
        }
        if(getTransport.equals("Walking")){
            transMethod = DirectionsCriteria.PROFILE_WALKING;
        }
        if(getTransport.equals("Cycling")) {
            transMethod = DirectionsCriteria.PROFILE_CYCLING;
        }
        return transMethod;
    }

    public String getSystemMethod(String getSystem){

        String system = null;

        if(getSystem.equals("Metric")) {
            system = DirectionsCriteria.METRIC;
        }
        if(getSystem.equals("Imperial")) {
            system = DirectionsCriteria.IMPERIAL;
        }
        return system;
    }
}
