package com.nasaappchalange.sonicbits.extractor;

import com.nasaappchalange.sonicbits.model.TrackInfoDTO;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author dragos.nutu
 */
public class TrackHistoryExtractor {
    private TrackInfoExtractor trackInfoExtractor;

    public TrackHistoryExtractor() {
        trackInfoExtractor = new TrackInfoExtractor();
    }

    public List<TrackInfoDTO> extract(String historyBrowser, String flightCode) throws IOException, InterruptedException {

        String finalUrl = historyBrowser + flightCode;
        System.out.println("Extracting flight for flightCode=[" + flightCode + "] from url=[" + finalUrl + "]");
        ArrayList<TrackInfoDTO> tracks = new ArrayList<TrackInfoDTO>();
        Connection connect = Jsoup.connect(finalUrl);
        Document document = connect.get();
//        Elements alLinks = document.select(".prettyTable td.nowrap a");
        Elements allTrs = document.select(".prettyTable tr");
        for (Element tr : allTrs) {
            Elements link = tr.select("td.nowrap a");


            if (link.size() == 1) {
                String href = link.attr("href");
                Thread.sleep(100);
                TrackInfoDTO trackInfoDTO = trackInfoExtractor.extract(href);
                if (trackInfoDTO != null) {
                    trackInfoDTO.setFlightCode(flightCode);

                    String date = tr.select("td:nth-child(1)").text();
                    String departure = tr.select("td:nth-child(5)").text();
                    String arrival = tr.select("td:nth-child(6)").text();
                    trackInfoDTO.setDepartureDate(extractDate(date, departure));
                    trackInfoDTO.setArrivalDate(extractDate(date, arrival));
                    tracks.add(trackInfoDTO);
                }
            }


        }

        return tracks;
    }

    private Date extractDate(String date, String departure) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyyhh:mmaZ");
            char wiredChar = 160;
            String finalDate = date.replaceAll("\\s+", "") + departure.replace(String.valueOf(wiredChar), "");
            return format.parse(finalDate);
        } catch (Exception ex) {
            // ignored
        }
        return null;
    }
}
