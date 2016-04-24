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

import java.util.LinkedList;
import java.util.List;

public class CameraOverlayView extends View implements SensorEventListener, LocationListener {

	private static final String TAG = "CameraOverlayView";
	private static final long MIN_TIME_BW_UPDATES = 100;
	private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 100f;
	private Location lastLocation = staticLocation;
	float[] gravity;
	float[] geomag;
	private boolean canGetLocation;
	private double latitude;
	private double longitude;
	private Location location;

	private float verticalFOV;
	private float horizontalFOV;

	private List<PointDTO> pointsToDraw;

	private LinkedList<float[]> previousGravity = new LinkedList<float[]>();
	private LinkedList<float[]> previousGeomag = new LinkedList<float[]>();


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
		if (lastLocation == null || gravity == null || geomag == null) {
			return;
		}

		for (PointDTO point : pointsToDraw) {
			Location pointLocation = new Location("manual");
			pointLocation.setLongitude(point.getLon());
			pointLocation.setLatitude(point.getLat());
			pointLocation.setAltitude(point.getAltitude() * 0.3d);

			drawMyPoint(lastLocation, pointLocation, canvas);
		}
	}

	private void drawMyPoint(Location location1, Location location2, Canvas canvas) {
		float curBearingToMW = location1.bearingTo(location2);

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

				// use roll for screen rotation
				canvas.rotate((float) (0.0f - Math.toDegrees(orientation[2])));
				// Translate, but normalize for the FOV of the camera -- basically, pixels per degree, times degrees == pixels
				float dx = (float) ((canvas.getWidth() / horizontalFOV) * (Math.toDegrees(orientation[0]) - curBearingToMW));
				float dy = (float) ((canvas.getHeight() / verticalFOV) * Math.toDegrees(orientation[1]));

				// wait to translate the dx so the horizon doesn't get pushed off
				canvas.translate(0.0f, 0.0f - dy);

				Paint paint = new Paint();
				paint.setStrokeWidth(6.0f);
				paint.setColor(Color.RED);

				// make our line big enough to draw regardless of rotation and translation
				//canvas.drawLine(0f - canvas.getHeight(), canvas.getHeight() / 2, canvas.getWidth() + canvas.getHeight(), canvas.getHeight() / 2, paint);


				// now translate the dx
				canvas.translate(0.0f - dx, 0.0f);

				// This is MAGIC!!!
				float distanceToPoint = location1.distanceTo(location2);
				double angleForPoint = -Math.toDegrees(Math.atan2(location2.getAltitude(), (double) distanceToPoint));
				Log.d("ANGLE", String.valueOf(angleForPoint));

				// The REAL THING!!!
				float renderedHeight = (float) angleForPoint * ((float) canvas.getHeight() / verticalFOV) + canvas.getHeight() / 2;
				Log.d("ANGLE h", String.valueOf(renderedHeight));
				Log.d("ANGLE max h", String.valueOf(canvas.getHeight()));

				// draw our point -- we've rotated and translated this to the right spot already
				float circleRadius = 20f;
				if ((location1.distanceTo(location2) < 150000 * 0.3) && (location1.distanceTo(location2) > 80000 * 0.3)) {
					circleRadius -= (10 * location1.distanceTo(location2) / (150000 * 0.3));
				} else if ((location1.distanceTo(location2) <= 80000 * 0.3)) {
					circleRadius = 20f;
				} else {
					circleRadius = 10f;
				}


				canvas.drawCircle(canvas.getWidth() / 2, renderedHeight, circleRadius, paint);

				// Undo
				canvas.translate(0.0f + dx, 0.0f);
				canvas.translate(0.0f, 0.0f + dy);
				canvas.rotate((float) (0.0f + Math.toDegrees(orientation[2])));
			}
		}
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
//		if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
//			return;

		// Gets the value of the sensor that has been changed
		switch (event.sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
				previousGravity.add(event.values.clone());
				gravity = calculateGravity();
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				previousGeomag.add(event.values.clone());
				geomag = calculateGeomag();
				break;
		}

		this.invalidate();
	}

	private float[] calculateGravity() {
		if (previousGravity.size() > 10) {
			previousGravity.removeFirst();
		}

		float[] finalGravity = new float[3];
		for (float[] gravity : previousGravity) {
			finalGravity[0] += gravity[0];
			finalGravity[1] += gravity[1];
			finalGravity[2] += gravity[2];
		}

		finalGravity[0] /= previousGravity.size();
		finalGravity[1] /= previousGravity.size();
		finalGravity[2] /= previousGravity.size();

		return finalGravity;
	}

	private float[] calculateGeomag() {
		if (previousGeomag.size() > 10) {
			previousGeomag.removeFirst();
		}

		float[] finalGeomag = new float[3];
		for (float[] geomag : previousGeomag) {
			finalGeomag[0] += geomag[0];
			finalGeomag[1] += geomag[1];
			finalGeomag[2] += geomag[2];
		}

		finalGeomag[0] /= previousGeomag.size();
		finalGeomag[1] /= previousGeomag.size();
		finalGeomag[2] /= previousGeomag.size();

		return finalGeomag;
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

		try {
//            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

			// getting GPS status
			boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// getting network status
			boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
			} else {
				this.canGetLocation = true;
				if (isNetworkEnabled) {
					if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
						// TODO: Consider calling
						//    ActivityCompat#requestPermissions
						// here to request the missing permissions, and then overriding
						//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
						//                                          int[] grantResults)
						// to handle the case where the user grants the permission. See the documentation
						// for ActivityCompat#requestPermissions for more details.
//                        return TODO;

					}
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					Log.d("Network", "Network Enabled");
					location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					if (location != null) {
						latitude = location.getLatitude();
						longitude = location.getLongitude();
					}
				}
				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled) {
					if (location == null) {
						locationManager.requestLocationUpdates(
								LocationManager.GPS_PROVIDER,
								MIN_TIME_BW_UPDATES,
								MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						Log.d("GPS", "GPS Enabled");
						location = locationManager
								.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}

	private final static Location mountWashington = new Location("manual");

	static {
		mountWashington.setLatitude(46.777903d);
		mountWashington.setLongitude(23.596767d);
		mountWashington.setAltitude(1000d);
	}

	private final static Location mountWashington2 = new Location("manual2");

	static {
		mountWashington2.setLatitude(46.774750d);
		mountWashington2.setLongitude(23.612566d);
		mountWashington2.setAltitude(0d);
	}

	private final static Location staticLocation = new Location("initial");

	static {
		staticLocation.setLatitude(46.7617231d);
		staticLocation.setLongitude(23.5820956d);
		staticLocation.setAltitude(160.5d);
	}

	public void setPointsToDraw(List<PointDTO> points) {
		this.pointsToDraw = points;
	}

	public float getVerticalFOV() {
		return verticalFOV;
	}

	public void setVerticalFOV(float verticalFOV) {
		this.verticalFOV = verticalFOV;
	}

	public float getHorizontalFOV() {
		return horizontalFOV;
	}

	public void setHorizontalFOV(float horizontalFOV) {
		this.horizontalFOV = horizontalFOV;
	}
}
