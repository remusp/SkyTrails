package com.nasaappchalange.sonicbits.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author dragos.nutu
 */
public class TrackInfoDTO {

    private List<TrackEntryDTO> trackEntries;
    private String fromCode;
    private String toCode;
    private String flightCode;

    private Date departureDate;
    private Date arrivalDate;

    public TrackInfoDTO() {
        trackEntries = new ArrayList<TrackEntryDTO>();
    }

    public String getFlightCode() {
        return flightCode;
    }

    public void setFlightCode(String flightCode) {
        this.flightCode = flightCode;
    }

    public String getFromCode() {
        return fromCode;
    }

    public void setFromCode(String fromCode) {
        this.fromCode = fromCode;
    }

    public String getToCode() {
        return toCode;
    }

    public void setToCode(String toCode) {
        this.toCode = toCode;
    }

    public List<TrackEntryDTO> getTrackEntries() {
        return trackEntries;
    }

    public void setTrackEntries(List<TrackEntryDTO> trackEntries) {
        this.trackEntries = trackEntries;
    }

    public Date getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }

    public Date getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    @Override
    public String toString() {
        return "TrackInfoDTO{" +
                "trackEntries.size=" + trackEntries.size() +
                ", fromCode='" + fromCode + '\'' +
                ", toCode='" + toCode + '\'' +
                ", departureDate=" + departureDate +
                ", arrivalDate=" + arrivalDate +
                '}';
    }
}
