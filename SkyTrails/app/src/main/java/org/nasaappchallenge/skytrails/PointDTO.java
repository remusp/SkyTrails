package org.nasaappchallenge.skytrails;

/**
 * Created by dragos on 23.04.2016.
 */
public class PointDTO {
    private double lon;
    private double lat;
    private double altitude;

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }
}
