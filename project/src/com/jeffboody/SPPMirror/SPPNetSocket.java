/*
 * Copyright (c) 2012 Jeff Boody
 */

package com.jeffboody.SPPMirror;

import android.util.Log;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SPPNetSocket
{
	private static final String TAG = "SPPNetSocket";

	// Internal state (protected by lock)
	private Lock    mLock;
	private boolean mIsConnected;
	private boolean mIsError;
	private int     mNetPort;

	// Net state
	private OutputStream mOutputStream;
	private InputStream  mInputStream;
	private ServerSocket mServerSocket;
	private Socket       mClientSocket;

	public SPPNetSocket()
	{
		mLock         = new ReentrantLock();
		mIsConnected  = false;
		mIsError      = false;
		mOutputStream = null;
		mInputStream  = null;
		mServerSocket = null;
		mClientSocket = null;
		mNetPort      = 6800;
	}

	public boolean connect(int port)
	{
		mLock.lock();
		try
		{
			if(mIsConnected)
			{
				Log.e(TAG, "connect: already connected");
				return false;
			}
			mNetPort = port;
		}
		finally
		{
			mLock.unlock();
		}

		try
		{
			mServerSocket = new ServerSocket(port);
			mClientSocket = mServerSocket.accept();
			mInputStream  = mClientSocket.getInputStream();
			mOutputStream = mClientSocket.getOutputStream();
			mServerSocket.close();
			mServerSocket = null;
		}
		catch(Exception e)
		{
			Log.e(TAG, "connect: ", e);
			disconnect();
			return false;
		}

		mLock.lock();
		try
		{
			mIsConnected = true;
			mIsError     = false;
		}
		finally
		{
			mLock.unlock();
		}
		return true;
	}

	public void disconnect()
	{
		mLock.lock();
		try
		{
			// don't log error when closing streams
			mIsConnected = false;

			try { mOutputStream.close(); } catch(Exception e) { }
			try { mInputStream.close();  } catch(Exception e) { }
			try { mClientSocket.close(); } catch(Exception e) { }
			try { mServerSocket.close(); } catch(Exception e) { }

			mOutputStream = null;
			mInputStream  = null;
			mClientSocket = null;
			mServerSocket = null;
			mIsError      = false;
		}
		finally
		{
			mLock.unlock();
		}
	}

	public boolean isConnected()
	{
		mLock.lock();
		try
		{
			return mIsConnected;
		}
		finally
		{
			mLock.unlock();
		}
	}

	public boolean isError()
	{
		mLock.lock();
		try
		{
			return mIsError;
		}
		finally
		{
			mLock.unlock();
		}
	}

	public int getNetPort()
	{
		mLock.lock();
		try
		{
			return mNetPort;
		}
		finally
		{
			mLock.unlock();
		}
	}

	public void writeByte(int b)
	{
		try
		{
			mOutputStream.write(b);
		}
		catch (Exception e)
		{
			mLock.lock();
			try
			{
				if(mIsConnected && (mIsError == false))
				{
					Log.e(TAG, "writeByte: " + e);
					mIsError = true;
				}
			}
			finally
			{
				mLock.unlock();
			}
		}
	}

	public int readByte()
	{
		int b = 0;
		try
		{
			b = mInputStream.read();
			if(b == -1)
			{
				disconnect();
			}
		}
		catch (Exception e)
		{
			mLock.lock();
			try
			{
				if(mIsConnected && (mIsError == false))
				{
					Log.e(TAG, "readByte: " + e);
					mIsError = true;
				}
			}
			finally
			{
				mLock.unlock();
			}
		}
		return b;
	}

	public void flush()
	{
		try
		{
			mOutputStream.flush();
		}
		catch (Exception e)
		{
			mLock.lock();
			try
			{
				if(mIsConnected && (mIsError == false))
				{
					Log.e(TAG, "flush: " + e);
					mIsError = true;
				}
			}
			finally
			{
				mLock.unlock();
			}
		}
	}
}
