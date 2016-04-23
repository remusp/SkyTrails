package com.nasaappchalange.sonicbits.extractor;

import com.nasaappchalange.sonicbits.model.TrackEntryDTO;
import com.nasaappchalange.sonicbits.model.TrackInfoDTO;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author dragos.nutu
 */
public class TrackInfoExtractor {

    public static final String FORMAT_PATTERN = "E hh:mm:ss a";

    public TrackInfoDTO extract(String href) throws IOException {
        String finalUrl = "https://flightaware.com" + href + "/tracklog";

        System.out.println("Extracting track for url : " + finalUrl);

        Connection connect = Jsoup.connect(finalUrl);
        Document document = connect.get();

        return populateTrackInfo(document);
    }

    private Date parseDateSimple(String dateAsString) {
        try {
            //20160422
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
            return dateFormat.parse(dateAsString);
        } catch (Exception ex) {
            //
        }
        return null;
    }

    private TrackInfoDTO populateTrackInfo(Document document) {
        Elements elements = document.select("#tracklogTable tr");
        if (elements.size() > 0) {
            TrackInfoDTO trackInfo = new TrackInfoDTO();
            List<TrackEntryDTO> trackEntries = new ArrayList<TrackEntryDTO>();
            trackInfo.setTrackEntries(trackEntries);
            Date landingDate = extractLandingDate(document);

            Element departure = elements.get(4);
            Element arrival = elements.get(elements.size() - 3);

            String departureCodeText = departure.select("th").text();
            String arrivalCodeText = arrival.select("th").text();

            trackInfo.setFromCode(extractCode(departureCodeText));
            trackInfo.setToCode(extractCode(arrivalCodeText));

//            trackInfo.setArrivalDate(landingDate);
//            trackInfo.setDepartureDate(landingDate);
            for (int i = 5; i < elements.size() - 3; i++) {
                Element currentTr = elements.get(i);

                String date = currentTr.select("td:nth-child(1)").text();
                String lat = currentTr.select("td:nth-child(2)").text();
                String lon = currentTr.select("td:nth-child(3)").text();
                String course = currentTr.select("td:nth-child(4)").text();
                String direction = currentTr.select("td:nth-child(5)").text();
                String knots = currentTr.select("td:nth-child(6)").text();
                String mph = currentTr.select("td:nth-child(7)").text();
                String feets = currentTr.select("td:nth-child(8)").text();

                TrackEntryDTO trackEntry = new TrackEntryDTO();
                trackEntries.add(trackEntry);

                Date dateForEntry = parseDate(date);

                trackEntry.setDate(dateForEntry);
                trackEntry.setLat(parseDouble(lat));
                trackEntry.setLon(parseDouble(lon));
                trackEntry.setCourse(extractCourse(course));
                trackEntry.setDirection(direction);
                trackEntry.setKnots(parseInt(knots));
                trackEntry.setMph(parseInt(mph));
                trackEntry.setFeets(parseInt(feets));
            }

            return trackInfo;
        }

        return null;
    }

    private Integer extractCourse(String course) {
        return parseInt(course);
    }

    private Integer parseInt(String string) {
        try {
            string = string.trim();
            string = string.replaceAll(",", "");
            string = string.replaceAll("\\.", "");
            string = string.replaceAll("°", "");
            return Integer.parseInt(string);

        } catch (Exception ex) {
            // ignored
        }
        return null;
    }


    private Date extractLandingDate(Document document) {
//        <meta property="og:url" content="http://flightaware.com/live/flight/DLA1673/history/20160422/0330Z/LRCL/EDDM/tracklog"/>
        Elements select = document.select("meta[property=og:url]");
        String url = select.get(0).attr("content");
        String[] tokens = url.split("/");
        String dayAsString = tokens[7];
        Date date = parseDateSimple(dayAsString);
        return date;
    }

    private String extractCode(String arrivalCodeText) {
        if (arrivalCodeText != null && arrivalCodeText.length() > 0) {

            try {
                int start = arrivalCodeText.indexOf("/ ");
                int end = arrivalCodeText.indexOf(")");

                return arrivalCodeText.substring(start + 1, end);
            } catch (Exception ex) {
                System.err.println("Could not parse arrivalCode : " + arrivalCodeText);
            }
        }

        return null;
    }


    private static Double parseDouble(String lat) {
        try {
            return Double.parseDouble(lat);
        } catch (Exception ex) {
            // ignored
        }

        return null;
    }

    private Date parseDate(String date) {

        try {

            //Thu 11:54:00 PM
            SimpleDateFormat format = new SimpleDateFormat(FORMAT_PATTERN, Locale.US);
            return format.parse(date);

        } catch (Exception ex) {
            // ignored
        }
        return null;
    }
}
