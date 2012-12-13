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
import com.jeffboody.BlueSmirf.BlueSmirfSPP;

public class SPPMirrorService extends Service
{
	private static final String TAG = "SPPMirrorService";

	private SPPMirrorServiceBinder mBinder;

	private boolean      mIsConnected;
	private BlueSmirfSPP mSPP;
	private SPPNetSocket mNet;
	private int mRxCount;
	private int mTxCount;

	public SPPMirrorService()
	{
		mIsConnected = false;
		mBinder      = null;
		mSPP         = null;
		mNet         = null;
		mRxCount     = 0;
		mTxCount     = 0;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.i(TAG, "onCreate");
		mBinder = new SPPMirrorServiceBinder(this);
		mSPP    = new BlueSmirfSPP();
		mNet    = new SPPNetSocket();
	}

	@Override
	public void onDestroy()
	{
		mNet.disconnect();
		mNet    = null;
		mSPP.disconnect();
		mSPP    = null;
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
			mService.onConnectLink(addr, port);
		}

		public void onDisconnectLink()
		{
			mService.onDisconnectLink();
		}
	}

	public void onSync()
	{
		Log.i(TAG, "onSync");

		Intent intent = new Intent("com.jeffboody.SPPMirror.action.STATUS");
		if(mIsConnected)
		{
			intent.putExtra("status",
			                "connected\n" +
			                "RX: " + mRxCount + "B\n" +
			                "TX: " + mTxCount + "B");
		}
		else
		{
			intent.putExtra("status",
			                "disconnected\n" +
			                "RX: " + mRxCount + "B\n" +
			                "TX: " + mTxCount + "B");
		}
		sendBroadcast(intent);
	}

	public void onConnectLink(String addr, int port)
	{
		Log.i(TAG, "onConnectLink addr=" + addr + ", port=" + port);

		if(mIsConnected)
		{
			Log.e(TAG, "onConnectLink: already connected");
			return;
		}

		if((mSPP.connect(addr) == false) ||
		   (mNet.connect(port) == false))
		{
			mSPP.disconnect();
			mNet.disconnect();
			return;
		}

		// TODO - create TX/RX threads
		ThreadTX tx = new ThreadTX();
		ThreadRX rx = new ThreadRX();

		mIsConnected = true;
		onSync();
	}

	public void onDisconnectLink()
	{
		Log.i(TAG, "onDisconnectLink");

		// TODO - destroy TX/RX threads

		mNet.disconnect();
		mSPP.disconnect();

		mIsConnected = false;
		onSync();
	}

	/*
	 * thread implementation
	 */

	public class ThreadTX implements Runnable
	{
		ThreadTX()
		{
			Thread t = new Thread(this);
			t.start();
		}

		public void run()
		{
			Log.i(TAG, "TX starting");
			mTxCount = 0;
			while(mIsConnected &&
			      (mNet.isError() == false) &&
			      (mSPP.isError() == false))
			{
				int b = mNet.readByte();
				mSPP.writeByte(b);
				mSPP.flush();
				++mTxCount;
				onSync();
			}
			Log.i(TAG, "TX stopping");
		}
	}

	public class ThreadRX implements Runnable
	{
		ThreadRX()
		{
			Thread t = new Thread(this);
			t.start();
		}

		public void run()
		{
			Log.i(TAG, "RX starting");
			mRxCount = 0;
			while(mIsConnected &&
			      (mNet.isError() == false) &&
			      (mSPP.isError() == false))
			{
				int b = mSPP.readByte();
				mNet.writeByte(b);
				mNet.flush();
				++mRxCount;
				onSync();
			}
			Log.i(TAG, "RX stopping");
		}
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
