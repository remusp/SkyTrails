package org.nasaappchallenge.skytrails;

import android.app.Activity;

import java.io.InputStream;
import java.util.List;

/**
 * @author dragos.nutu
 */
public interface PointDao {
    List<PointDTO> createPoints(InputStream is);
}
