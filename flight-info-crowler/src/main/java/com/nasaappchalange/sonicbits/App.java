package com.nasaappchalange.sonicbits;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nasaappchalange.sonicbits.extractor.TrackHistoryExtractor;
import com.nasaappchalange.sonicbits.model.FlightBagDTO;
import com.nasaappchalange.sonicbits.model.TrackHistoryDTO;
import com.nasaappchalange.sonicbits.model.TrackInfoDTO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Hello world!
 */
public class App {

    private static TrackHistoryExtractor trackInfoExtractor;

    public static void main(String[] args) throws IOException, InterruptedException {

        trackInfoExtractor = new TrackHistoryExtractor();


        List<String> flightsFromCluj = Arrays.asList(
                "WZZ3303",
                "WZZ3371",
                "WZZ3411",
                "ROT648",
                "ROT650",
                "WZZ3301",
                "DLA1673",
                "WZZ3393",
                "WZZ3391",
                "WZZ3365",
                "WZZ3385",
                "WZZ3381",
                "DLA1669",
                "ROT644",
                "WZZ3401",
                "WZZ3405",
                "WZZ3303",
                "WZZ3331",
                "ROT648",
                "ROT658"
        );

        String baseFlightUrl = "https://flightaware.com/live/flight/";

        FlightBagDTO flightBagDTO = new FlightBagDTO();
        flightBagDTO.setAirportCode("LRCL");
        for (String flightCode : flightsFromCluj) {
            List<TrackInfoDTO> tracks = trackInfoExtractor.extract(baseFlightUrl, flightCode);
            if (tracks != null && !tracks.isEmpty()) {
                TrackHistoryDTO trackHistoryDTO = new TrackHistoryDTO();
                trackHistoryDTO.setInfos(tracks);
                flightBagDTO.addFlightHistory(trackHistoryDTO);
            }
            Thread.sleep(100);
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

//Object to JSON in file
        mapper.writeValue(new File("departs-from-cluj.json"), flightBagDTO);

    }
}
