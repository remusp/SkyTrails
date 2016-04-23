package org.nasaappchallenge.skytrails;

/**
 * Created by Vlad on 23.04.2016.
 */

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraSurfacePreview extends SurfaceView implements SurfaceHolder.Callback {
	public CameraSurfacePreview(Context context) {
		super(context);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}
}
