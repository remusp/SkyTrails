package com.nasaappchalange.sonicbits.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dragos.nutu
 */
public class FlightBagDTO {
    private List<TrackHistoryDTO> histories;
    private String airportCode;

    public FlightBagDTO() {
        histories = new ArrayList<TrackHistoryDTO>();
    }

    public String getAirportCode() {
        return airportCode;
    }

    public void setAirportCode(String airportCode) {
        this.airportCode = airportCode;
    }

    public List<TrackHistoryDTO> getHistories() {
        return histories;
    }

    public void setHistories(List<TrackHistoryDTO> histories) {
        this.histories = histories;
    }

    public void addFlightHistory(TrackHistoryDTO trackHistoryDTO) {
        histories.add(trackHistoryDTO);
    }
}
