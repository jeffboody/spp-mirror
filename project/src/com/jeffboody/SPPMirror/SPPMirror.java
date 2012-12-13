/*
 * Copyright (c) 2012 Jeff Boody
 */

package com.jeffboody.SPPMirror;

import android.content.Context;
import android.os.IBinder;
import android.app.Activity;
import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import com.jeffboody.SPPMirror.SPPMirrorService.SPPMirrorServiceBinder;

public class SPPMirror extends Activity implements ServiceConnection
{
	private static final String TAG = "SPPMirror";

	// service state
	private Intent                     mIntent;
	private SPPMirrorServiceBinder     mBinder;
	private SPPMirrorBroadcastReceiver mBroadcastReceiver;
	private IntentFilter               mIntentFilter;

	// UI
	private TextView mTextViewStatus;
	private EditText mEditTextSPPMacAddr;
	private EditText mEditTextNetPort;

	public SPPMirror()
	{
		mIntent            = new Intent("com.jeffboody.SPPMirror.SPPMirrorService");
		mBinder            = null;
		mBroadcastReceiver = new SPPMirrorBroadcastReceiver(this);
		mIntentFilter      = new IntentFilter();

		mIntentFilter.addAction("com.jeffboody.SPPMirror.action.STATUS");
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		startService(mIntent);

		setContentView(R.layout.main);

		mTextViewStatus     = (TextView) findViewById(R.id.ID_STATUS);
		mEditTextSPPMacAddr = (EditText) findViewById(R.id.ID_SPP_MACADDR);
		mEditTextNetPort    = (EditText) findViewById(R.id.ID_NET_PORT);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		Log.i(TAG, "onResume");
		updateStatus("Waiting for sync");
		registerReceiver(mBroadcastReceiver, mIntentFilter);
		bindService(mIntent, this, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onPause()
	{
		Log.i(TAG, "onPause");

		try { unbindService(this); }
		catch(Exception e) { /* service may not be running */ }
		mBinder = null;

		try { unregisterReceiver(mBroadcastReceiver); }
		catch(Exception e) { }

		super.onPause();
	}

	@Override
	protected void onDestroy()
	{
		Log.i(TAG, "onDestroy");
		super.onDestroy();
	}

	/*
	 * service interface
	 */

	public void onServiceConnected(ComponentName name, IBinder service)
	{
		Log.i(TAG, "onServiceConnected");
		mBinder = (SPPMirrorServiceBinder) service;
		if(mBinder != null)
		{
			mBinder.onSync();
		}
		else
		{
			updateStatus("service connection failed");
		}
	}

	public void onServiceDisconnected(ComponentName name)
	{
		Log.i(TAG, "onServiceDisconnected");
		unbindService(this);
		mBinder = null;
	}

	/*
	 * buttons
	 */

	public void onConnectLink(View view)
	{
		Log.i(TAG, "onConnectLink");
		if(mBinder != null)
		{
			String addr = mEditTextSPPMacAddr.getText().toString();
			int    port = 6800;

			try { port = Integer.parseInt(mEditTextNetPort.getText().toString()); }
			catch(Exception e) { }

			mBinder.onConnectLink(addr, port);
		}
	}

	public void onDisconnectLink(View view)
	{
		Log.i(TAG, "onDisconnectLink");
		if(mBinder != null)
		{
			mBinder.onDisconnectLink();
		}
	}

	/*
	 * broadcast receiver implementation
	 */

	public class SPPMirrorBroadcastReceiver extends BroadcastReceiver
	{
		private SPPMirror mSPPMirror;

		public SPPMirrorBroadcastReceiver(SPPMirror spp_mirror)
		{
			mSPPMirror = spp_mirror;
		}

		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			Log.i(TAG, "onReceive " + action);
			if(action.equals("com.jeffboody.SPPMirror.action.STATUS"))
			{
				String status = intent.getStringExtra("status");
				mSPPMirror.updateStatus(status);
			}
		}
	}

	public void updateStatus(String status)
	{
		mTextViewStatus.setText(status);
	}
}
