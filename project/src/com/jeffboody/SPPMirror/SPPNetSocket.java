/*
 * Copyright (c) 2012 Jeff Boody
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
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

	public void write(byte[] buffer, int offset, int count)
	{
		try
		{
			mOutputStream.write(buffer, offset, count);
		}
		catch (Exception e)
		{
			mLock.lock();
			try
			{
				if(mIsConnected && (mIsError == false))
				{
					Log.e(TAG, "write: " + e);
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

	public int read(byte[] buffer, int offset, int length)
	{
		int b = 0;
		try
		{
			b = mInputStream.read(buffer, offset, length);
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
					Log.e(TAG, "read: " + e);
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
