package com.skyskew.sampledeliveryapp;

import android.location.Location;

import java.io.Serializable;

/**
 * Created by Chiffon on 22/06/17.
 */

public class ConsigneeDetail implements Serializable {


    private String name ;
    private  double lat;
    private  double lon;
    private  float distance;
    private boolean completed ;


    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }



    ConsigneeDetail()
    {

    }
    ConsigneeDetail(String name,float dist,double lat,double lon)
    {
        this.name = name;
        distance= dist;
        this.lat = lat;
        this.lon = lon;
        completed = false;

    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }




    public void setName(String name)
    {
        this.name = name;

    }
    public void setDistance(float distance)
    {
        this.distance = distance;
    }
    public String getName()
    {
        return name;
    }
    public float getDistance()
    {
        return distance;
    }

    public void calculateDistance(double latitude,double longtitude)
    {
        float results[]=new float[5];
        Location.distanceBetween(
                latitude,
                longtitude,
                this.lat,
                this.lon,
                results);
        distance = results[0];


    }


}
