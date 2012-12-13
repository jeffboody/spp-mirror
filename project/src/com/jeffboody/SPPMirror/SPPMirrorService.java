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
	private int          mRxCount;
	private int          mTxCount;
	private String       mBluetoothAddress;
	private int          mNetPort;

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
		mBinder = new SPPMirrorServiceBinder(this);
		mSPP    = new BlueSmirfSPP();
		mNet    = new SPPNetSocket();
	}

	@Override
	public void onDestroy()
	{
		mNet.disconnect();
		mSPP.disconnect();
		mNet    = null;
		mSPP    = null;
		mBinder = null;
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
		Intent intent = new Intent("com.jeffboody.SPPMirror.action.STATUS");
		if(mIsConnected)
		{
			intent.putExtra("status",
			                "spp: " + (mSPP.isConnected() ? "connected\n" : "listening\n") +
			                "net: " + (mNet.isConnected() ? "connected\n" : "listening\n") +
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
		if(mIsConnected)
		{
			return;
		}

		// create TX/RX threads
		mBluetoothAddress = addr;
		mNetPort          = port;
		ThreadTX tx = new ThreadTX();
		mTxCount = 0;
		mRxCount = 0;
		mIsConnected = true;
		onSync();
	}

	public void onDisconnectLink()
	{
		mNet.disconnect();
		mSPP.disconnect();
		mBluetoothAddress = null;
		mNetPort          = 0;
		mIsConnected      = false;
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
			// connect to SPP and Net before starting rx thread
			if(mSPP.connect(mBluetoothAddress) == false)
			{
				onDisconnectLink();
			}
			onSync();
			if(mNet.connect(mNetPort) == false)
			{
				onDisconnectLink();
			}
			onSync();
			ThreadRX rx = new ThreadRX();

			while(mIsConnected &&
			      (mNet.isConnected()) &&
			      (mSPP.isConnected()) &&
			      (mNet.isError() == false) &&
			      (mSPP.isError() == false))
			{
				int b = mNet.readByte();
				mSPP.writeByte(b);
				mSPP.flush();
				++mTxCount;

				// throttle the updates
				if((mTxCount % 100) == 0)
				{
					onSync();
				}
			}

			// reached end-of-stream or error
			if(mIsConnected)
			{
				onDisconnectLink();
			}
			else
			{
				onSync();
			}
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
			while(mIsConnected &&
			      (mNet.isConnected()) &&
			      (mSPP.isConnected()) &&
			      (mNet.isError() == false) &&
			      (mSPP.isError() == false))
			{
				int b = mSPP.readByte();
				mNet.writeByte(b);
				mNet.flush();
				++mRxCount;

				// throttle the updates
				if((mRxCount % 100) == 0)
				{
					onSync();
				}
			}

			// reached end-of-stream or error
			if(mIsConnected)
			{
				onDisconnectLink();
			}
			else
			{
				onSync();
			}
		}
	}

	/*
	 * service implementation
	 */

	@Override
	public IBinder onBind(Intent intent)
	{
		return mBinder;
	}
}
