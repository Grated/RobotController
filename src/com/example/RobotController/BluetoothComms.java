package com.example.RobotController;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: sgreenman
 * Date: 4/29/13
 * Time: 9:57 PM
 *
 * Code based on the Bluetooth Chat sample provided with the Android SDK.
 */
public class BluetoothComms extends Service
{
   // Used for tagging debug messages
   private static final String TAG = "BluetoothComms";

   // Name for the SDP record when creating server socket
   private static final String NAME_SECURE = "RobotContoller";

   // Unique UUID for this application
   private static final UUID MY_UUID_SECURE =
      UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

   // Member fields
   private final BluetoothAdapter mAdapter;
   private final Handler mHandler;
   private BluetoothAcceptThread mSecureAcceptThread;
   private BluetoothConnectThread mConnectThread;
   private BluetoothConnectedThread mConnectedThread;
   private State mState;

   // Constants that indicate the current connection state
   public enum State
   {
      NONE,       // we're doing nothing
      LISTEN,     // now listening for incoming connections
      CONNECTING, // now initiating an outgoing connection
      CONNECTED   // now connected to a remote device
   }

   // Binder given to clients (part of being a service)
   private final Binder binder = new LocalBinder();

   // List of activities who care about connection events.
   private final ArrayList<BluetoothCommsListener> listeners = new ArrayList<BluetoothCommsListener>();

   /**
    * Prepares a new bluetooth connection session.
    * @param context The UI activity context.
    * @param handler A handler to pass messages back to the UI activity.
    */
   public BluetoothComms(Context context, Handler handler)
   {
      this.mAdapter = BluetoothAdapter.getDefaultAdapter();
      this.mState = State.NONE;
      this.mHandler = handler;
   }

   /**
    * Set the current state of the bluetooth connection.
    * @param state An integer defining the current state.
    */
   private synchronized void setState(State state)
   {
      Log.i(TAG, "setState() " + mState + " --> "+ state);
      mState = state;
      mHandler.obtainMessage(RobotControlActivity.MESSAGE_STATE_CHANGE, -1, -1, state).sendToTarget();
   }

   /**
    *
    * @return The current connection state
    */
   public synchronized State getState()
   {
      return mState;
   }

   /**
    * Start the bluetooth connection.
    * Starts a BluetoothAcceptThread in server mode.  Called by the activity
    * onResume()
    */
   public synchronized void start()
   {
      Log.i(TAG, "BluetoothComms start");

      // Cancel any current connections or connection attempts.
      if (mConnectThread != null)
      {
         mConnectThread.cancel();
         mConnectThread = null;
      }

      if (mConnectedThread != null)
      {
         mConnectedThread.cancel();
         mConnectedThread = null;
      }


      // Start the thread to listen as a server
      if (mSecureAcceptThread == null)
      {
         mSecureAcceptThread = new BluetoothAcceptThread();
         mSecureAcceptThread.start();
      }
      this.setState(State.LISTEN);

      // TODO Insecure accept thread?
   }

   /**
    * Called by the BluetoothAcceptThread when a connection has been accepted.
    * Starts a BluetoothConnectThread to initiate a connection.
    * @param device Device to connect.
    */
   public synchronized void connect(BluetoothDevice device)
   {
      Log.i(TAG, "Connecting to: " + device);

      // Cancel any current connection attempts.
      if(mState == State.CONNECTING)
      {
         if(mConnectThread != null)
         {
            mConnectThread.cancel();
            mConnectThread = null;
         }
      }

      if(mConnectedThread != null)
      {
         mConnectedThread.cancel();
         mConnectedThread = null;
      }

      // Start the thread to connect to the target device
      mConnectThread = new BluetoothConnectThread(device);
      mConnectThread.start();
      setState(State.CONNECTING);
   }

   /**
    * Starts a BluetoothConnectedThread to begin managing a bluetooth connection.
    * @param socket The BluetoothSocket on which the connection was made.
    * @param device The BluetoothDevice that has been connected.
    */
   public synchronized void connected(BluetoothSocket socket, BluetoothDevice device)
   {
      Log.i(TAG, "Connected");

      // Cancel the thread that completed the connection
      if (mConnectThread != null)
      {
         mConnectThread.cancel();
         mConnectThread = null;
      }

      // Cancel any threads running a current connection
      if(mConnectedThread != null)
      {
         mConnectedThread.cancel();
         mConnectedThread = null;
      }

      // Cancel the accept thread because we only want to connect to one device.
      if (mSecureAcceptThread != null)
      {
         mSecureAcceptThread.cancel();
         mSecureAcceptThread = null;
      }

      // Start the thread to manage the connection and perform transmissions
      mConnectedThread = new BluetoothConnectedThread(socket);
      mConnectedThread.start();

      // Send the name of the connected device back to the UI Activity
      Message msg = mHandler.obtainMessage(RobotControlActivity.MESSAGE_DEVICE_NAME);
      Bundle bundle = new Bundle();
      bundle.putString(RobotControlActivity.DEVICE_NAME, device.getName());
      msg.setData(bundle);
      mHandler.sendMessage(msg);

      setState(State.CONNECTED);
   }

   /**
    * Stops all threads.
    */
   public synchronized void stop()
   {
      Log.i(TAG, "Stop");

      if (mConnectThread != null)
      {
         mConnectThread.cancel();
         mConnectThread = null;
      }

      if(mConnectedThread != null)
      {
         mConnectedThread.cancel();
         mConnectedThread = null;
      }

      if (mSecureAcceptThread != null)
      {
         mSecureAcceptThread.cancel();
         mSecureAcceptThread = null;
      }

      setState(State.NONE);
   }

   public void write(RobotMessage message)
   {
      // Create temporary object
      BluetoothConnectedThread r;
      synchronized (this)
      {
         if (mState == State.CONNECTED)
         {
            r = mConnectedThread;
         }
         else
         {
            return;
         }
      }

      // Perform write unsynchronized.
      r.write(message);
   }

   /**
    * Indicate that the connection attempt failed and notify the UI Activity.
    */
   private void connectionFailed() {
      // Send a failure message back to the Activity
      Message msg = mHandler.obtainMessage(RobotControlActivity.MESSAGE_TOAST);
      Bundle bundle = new Bundle();
      bundle.putString(RobotControlActivity.TOAST, "Unable to connect device");
      msg.setData(bundle);
      mHandler.sendMessage(msg);

      // Start the service over to restart listening mode
      BluetoothComms.this.start();
   }

   /**
    * Indicate that the connection was lost and notify the UI Activity.
    */
   private void connectionLost() {
      // Send a failure message back to the Activity
      Message msg = mHandler.obtainMessage(RobotControlActivity.MESSAGE_TOAST);
      Bundle bundle = new Bundle();
      bundle.putString(RobotControlActivity.TOAST, "Device connection was lost");
      msg.setData(bundle);
      mHandler.sendMessage(msg);

      // Start the service over to restart listening mode
      BluetoothComms.this.start();
   }

   @Override
   public IBinder onBind(Intent intent)
   {
      return this.binder;
   }

   @Override
   public void onDestroy()
   {
      // Do some clean up
      stop();
   }

   /**
    * This thread runs while listening for incoming connections.
    * It behaves like a server-side client and runs until a connection is
    * accepted or cancel() is called.
    */
   private class BluetoothAcceptThread extends Thread
   {
      // Local access to the device and socket
      private final BluetoothServerSocket mmServerSocket;

      public BluetoothAcceptThread()
      {
         BluetoothServerSocket serverSocket = null;

         // Create a new listening server socket
         try
         {
            serverSocket = mAdapter.listenUsingRfcommWithServiceRecord(
                  NAME_SECURE, MY_UUID_SECURE);
         }
         catch (IOException e)
         {
            Log.e(TAG, "Secure socket listen() failed", e);
         }

         mmServerSocket = serverSocket;
      }

      public void run()
      {
         Log.i(TAG, "Begin BluetoothAcceptThead");
         setName("BluetoothAcceptThread");

         BluetoothSocket socket = null;

         // Listen to the server socket if we're not connected
         while (mState != BluetoothComms.State.CONNECTED)
         {
            try
            {
               // Blocking call
               socket = mmServerSocket.accept();
            }
            catch (IOException e)
            {
               // Failed to connect
               Log.e(TAG, "accept() failed", e);
               break;
            }

            // If a connection was accepted
            if (socket != null)
            {
               // TODO Why a lock here?
               synchronized (BluetoothComms.this)
               {
                  switch (mState)
                  {
                     case LISTEN:
                     case CONNECTING:
                        // Start the connected thread.
                        // This is the normal execution path.
                        connected(socket, socket.getRemoteDevice());
                        break;

                     case NONE:
                     case CONNECTED:
                        // Either not ready or already connected.
                        // Terminate new socket
                        try
                        {
                           socket.close();
                        }
                        catch (IOException e)
                        {
                           Log.e(TAG, "Could not close unwanted socket", e);
                        }
                        break;

                  } // End switch
               } // End synchronized
            } // end socket != null
         } // end while

         Log.i(TAG, "End accept thread");

      }

      public void cancel()
      {
         Log.i(TAG, "Accept thread cancel");
         try
         {
            mmServerSocket.close();
         }
         catch (IOException e)
         {
            Log.e(TAG, "Accept thread server socket close failed");
         }
      }

   }

   /**
    * This thread runs while attempting to make a connection to a remote
    * device.
    */
   private class BluetoothConnectThread extends Thread
   {
      // Local access to the device and socket
      private final BluetoothSocket mmSocket;
      private final BluetoothDevice mmDevice;

      public BluetoothConnectThread(BluetoothDevice device)
      {
         // UUID for bluetooth SPP protocol.
         // It's on the internet brah.
         final UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

         mmDevice = device;

         // Use a temp version of the socket because the member is final.
         BluetoothSocket tmpSock = null;

         // Try to connect the local socket to the bluetooth device
         try
         {
            tmpSock = device.createRfcommSocketToServiceRecord(SERIAL_UUID);
         } catch (IOException e)
         {
            // Sucks.
            Log.e(TAG, "Create failed: " + device.getName(), e);
         }

         mmSocket = tmpSock;
      }

      /**
       * Attemps to connect to the remote device.
       */
      public void run()
      {
         // Debuggery
         Log.i(TAG, "Starting connect thread");
         setName("Bluetooth connect thread");

         // Cancel discovery because it will slow down the connection.
         BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

         try
         {
            // Try connecting to the remote device.
            // This blocks until it succeeds or gives up.
            mmSocket.connect();
         } catch (IOException connectException)
         {
            // Nuts!
            Log.e(TAG, "Could not connect to remote device", connectException);

            try
            {
               mmSocket.close();
            } catch (IOException closeException)
            {
               // Well, shit.
               Log.e(TAG, "Could not close socket", closeException);
            }

            // This place smells funny.
            return;
         }

         // Kick off a thread to manage the connection.
         connected(mmSocket, mmDevice);
      }

      /**
       * Cancels an in-progress connection.
       */
      public void cancel()
      {
         try
         {
            mmSocket.close();
         } catch (IOException e)
         {
            // doh
         }
      }
   } // end class ConnectThread

   /**
    * This thread runs while there is an active connection with a remote
    * device.
    */
   private class BluetoothConnectedThread extends Thread
   {
      private final BluetoothSocket mmSocket;
      private final InputStream mmInStream;
      private final OutputStream mmOutStream;

      public BluetoothConnectedThread(BluetoothSocket socket)
      {
         mmSocket = socket;
         InputStream tmp_in = null;
         OutputStream tmp_out = null;

         try
         {
            tmp_in = socket.getInputStream();
            tmp_out = socket.getOutputStream();
         }
         catch (IOException e)
         {
            Log.e(TAG, "Failed to open input and output streams", e);
         }

         mmInStream = tmp_in;
         mmOutStream = tmp_out;
      }

      public void run()
      {
         Log.i(TAG, "Beginning background bluetooth comms");

         int num_bytes = 0;
         byte[] buffer = new byte[1024];

         // As long as we are still connected keep listening for messages
         // from the remote.
         while (true)
         {
            try
            {
               num_bytes = this.mmInStream.read(buffer);
               mHandler.obtainMessage(RobotControlActivity.MESSAGE_READ,
                     num_bytes, -1, buffer).sendToTarget();
            }
            catch (IOException e)
            {
               Log.e(TAG, "Disconnected", e);
               connectionLost();
               BluetoothComms.this.start();
               break;
            }
         }
      }

      /**
       * Writes data to the remote end if connected.  If no connection
       * exists data is written to the great bit bucket in the sky.
       */
      public void write(RobotMessage message)
      {
         try
         {
            byte[] msg = message.packMessage();
            mmOutStream.write(msg);
            mHandler.obtainMessage(RobotControlActivity.MESSAGE_WRITE, -1, -1, msg).sendToTarget();
         }
         catch (IOException e)
         {
            Log.e(TAG, "Exception during write", e);
         }
      }

      /**
       * Cancels the bluetooth connection.
       */
      public void cancel()
      {
         try
         {
            mmSocket.close();
         }
         catch (IOException e)
         {
            Log.e(TAG, "Failed to close connection in BluetoothConnectedThread", e);
         }
      }

   }

   /**
    * Client binder class
    */
   private class LocalBinder extends Binder
   {
      public BluetoothComms getService()
      {
         return BluetoothComms.this;
      }

      public void registerListener(BluetoothCommsListener listener)
      {
         listeners.add(listener);
      }

      public void unregisterListener(BluetoothCommsListener listener)
      {
         listeners.remove(listener);
      }
   }

}
