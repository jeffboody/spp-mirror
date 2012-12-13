/*
 * Copyright (c) 2012 Jeff Boody
 */

package com.jeffboody.SPPMirror;

import android.util.Log;
import android.os.Binder;
import android.os.IBinder;
import android.content.Context;
import android.content.Intent;
import android.app.Service;

public class SPPMirrorService extends Service
{
	private static final String TAG = "SPPMirrorService";

	private SPPMirrorServiceBinder mBinder;

	public SPPMirrorService()
	{
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.i(TAG, "onCreate");
		mBinder = new SPPMirrorServiceBinder(this);
	}

	@Override
	public void onDestroy()
	{
		mBinder = null;
		Log.i(TAG, "onDestroy");
		super.onDestroy();
	}

	/*
	 * binder implementation
	 */

	public class SPPMirrorServiceBinder extends Binder
	{
		private SPPMirrorService mService;

		SPPMirrorServiceBinder(SPPMirrorService service)
		{
			mService = service;
		}

		public void onSync()
		{
			mService.onSync();
		}

		public void onConnectLink(String addr, int port)
		{
		}

		public void onDisconnectLink()
		{
		}
	}

	public void onSync()
	{
		Log.i(TAG, "onSync");

		Intent intent = new Intent("com.jeffboody.SPPMirror.action.STATUS");
		intent.putExtra("status", "SPP: stopped\nNET: stopped\nRX: 0B\nTX: 0B");
		sendBroadcast(intent);
	}

	/*
	 * service implementation
	 */

	@Override
	public IBinder onBind(Intent intent)
	{
		Log.i(TAG, "onBind");
		return mBinder;
	}
}
