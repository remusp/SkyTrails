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

		SensorManager sensors = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		Sensor accelSensor = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		Sensor compassSensor = sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		Sensor gyroSensor = sensors.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

		boolean isAccelAvailable = sensors.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
		boolean isCompassAvailable = sensors.registerListener(this, compassSensor, SensorManager.SENSOR_DELAY_NORMAL);
		boolean isGyroAvailable = sensors.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);

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

		lastLocation = getLastBestLocation(context, locationManager);
	}

	protected void onDraw(Canvas canvas) {
		drawMyStuff(canvas);

		if (lastLocation == null || gravity == null || geomag == null) {
			return;
		}

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

				float verticalFOV = 47.4366F;
				float horizontalFOV = 60.0848F;

				// use roll for screen rotation
				canvas.rotate((float) (0.0f - Math.toDegrees(orientation[2])));
				// Translate, but normalize for the FOV of the camera -- basically, pixels per degree, times degrees == pixels
				float dy = (float) ((canvas.getWidth() / horizontalFOV) * (Math.toDegrees(orientation[0]) - curBearingToMW));
				float dx = (float) ((canvas.getHeight() / verticalFOV) * Math.toDegrees(orientation[1]));

				// wait to translate the dx so the horizon doesn't get pushed off
				canvas.translate(0.0f, 0.0f - dy);

				Paint paint = new Paint();
				paint.setColor(Color.RED);

				// make our line big enough to draw regardless of rotation and translation
				canvas.drawLine(0f - canvas.getHeight(), canvas.getHeight() / 2, canvas.getWidth() + canvas.getHeight(), canvas.getHeight() / 2, paint);


				// now translate the dx
				canvas.translate(0.0f - dx, 0.0f);

				// draw our point -- we've rotated and translated this to the right spot already
				canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, 8.0f, paint);
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

	/**
	 * Get last location
	 *
	 * @return last location
	 */
	private Location getLastBestLocation(Context context, LocationManager locationManager) {

		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return null;
		}
		Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		long GPSLocationTime = 0;
		if (locationGPS != null) {
			GPSLocationTime = locationGPS.getTime();
		}

		long NetLocationTime = 0;

		if (locationNet != null) {
			NetLocationTime = locationNet.getTime();
		}

		if (GPSLocationTime - NetLocationTime > 0) {
			return locationGPS;
		} else {
			return locationNet;
		}
	}

	private final static Location mountWashington = new Location("manual");

	static {
		mountWashington.setLatitude(46.775472d);
		mountWashington.setLongitude(23.595470d);
		mountWashington.setAltitude(1916.5d);
	}
}
