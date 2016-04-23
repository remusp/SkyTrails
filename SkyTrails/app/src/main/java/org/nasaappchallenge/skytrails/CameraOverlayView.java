package org.nasaappchallenge.skytrails;

/**
 * Created by Vlad on 23.04.2016.
 */

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class CameraOverlayView extends View implements SensorEventListener, LocationListener {

	private static final String TAG = "CameraOverlayView";
	private Location lastLocation = null;
	float[] gravity;
	float[] geomag;

	public CameraOverlayView(Context context, AttributeSet attrs) {
		super(context, attrs);

		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

		String best = locationManager.getBestProvider(criteria, true);

		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
				PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		locationManager.requestLocationUpdates(best, 100, 0, this);
	}

	protected void onDraw(Canvas canvas) {
		drawMyStuff(canvas);
		float curBearingToMW = lastLocation.bearingTo(mountWashington);

		// compute rotation matrix
		float rotation[] = new float[9];
		float identity[] = new float[9];
		boolean gotRotation = SensorManager.getRotationMatrix(rotation,
				identity, gravity, geomag);

		if (gotRotation) {
			float cameraRotation[] = new float[9];
			// remap such that the camera is pointing straight down the Y axis
			SensorManager.remapCoordinateSystem(rotation, SensorManager.AXIS_X,
					SensorManager.AXIS_Z, cameraRotation);

			// orientation vector
			float orientation[] = new float[3];
			SensorManager.getOrientation(cameraRotation, orientation);
			if (gotRotation) {
				cameraRotation = new float[9];
				// remap such that the camera is pointing along the positive direction of the Y axis
				SensorManager.remapCoordinateSystem(rotation, SensorManager.AXIS_X,
						SensorManager.AXIS_Z, cameraRotation);

				// orientation vector
				orientation = new float[3];
				SensorManager.getOrientation(cameraRotation, orientation);
			}
		}
	}

	private void drawMyStuff(final Canvas canvas) {
		Log.i(TAG, "Drawing...");
		Paint paint = new Paint();
		paint.setColor(Color.RED);
		canvas.drawLine(0, 0, 200, 200, paint);
		canvas.drawLine(20, 0, 0, 200, paint);
	}

	@Override
	public void onLocationChanged(Location location) {
		lastLocation = location;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// If the sensor data is unreliable return
		if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
			return;

		// Gets the value of the sensor that has been changed
		switch (event.sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
				gravity = event.values.clone();
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				geomag = event.values.clone();
				break;
		}

		this.invalidate();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	private final static Location mountWashington = new Location("manual");

	static {
		mountWashington.setLatitude(46.775472d);
		mountWashington.setLongitude(23.595470d);
		mountWashington.setAltitude(1916.5d);
	}
}
