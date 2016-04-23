package org.nasaappchallenge.skytrails;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dragos.nutu
 */
public class PointDaoImpl implements PointDao {
    @Override
    public List<PointDTO> createPoints(InputStream is) {
        ArrayList<PointDTO> points = new ArrayList<>();
        try {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            JSONObject jObject = new JSONObject(json);
            JSONArray trackEntries = jObject.getJSONArray("trackEntries");
            for (int i = 0; i < trackEntries.length(); i++) {
                JSONObject jsonObject = trackEntries.getJSONObject(i);
                PointDTO point = new PointDTO();
                point.setLat(jsonObject.getDouble("lat"));
                point.setLon(jsonObject.getDouble("lon"));
                points.add(point);
            }
        } catch (Exception ex) {
            // ignored
        }

        return points;
    }
}
