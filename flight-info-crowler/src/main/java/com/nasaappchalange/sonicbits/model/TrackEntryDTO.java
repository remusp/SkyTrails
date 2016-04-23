package com.nasaappchalange.sonicbits.model;

import java.util.Date;

/**
 * @author dragos.nutu
 */
public class TrackEntryDTO {

    private Date date;
    private Double lat;
    private Double lon;
    private Integer course;
    private String direction;
    private Integer knots;
    private Integer mph;
    private Integer feets;

    public TrackEntryDTO() {
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Integer getCourse() {
        return course;
    }

    public void setCourse(Integer course) {
        this.course = course;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setKnots(Integer knots) {
        this.knots = knots;
    }

    public Integer getKnots() {
        return knots;
    }

    public void setMph(Integer mph) {
        this.mph = mph;
    }

    public Integer getMph() {
        return mph;
    }

    public void setFeets(Integer feets) {
        this.feets = feets;
    }

    public Integer getFeets() {
        return feets;
    }
}
