package com.example.RobotController;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: sgreenman
 * Date: 4/29/13
 * Time: 9:57 PM
 *
 * Code based on the Bluetooth Chat sample provided with the Android SDK.
 */
public class BluetoothComms
{
   // Used for tagging debug messages
   private static final String TAG = "BluetoothComms";

   /**
    * This thread runs while attempting to make a connection to a remote
    * device.
    */
   private class BlueToothConnectThread extends Thread
   {
      // Local access to the device and socket
      private final BluetoothSocket mSocket;
      private final BluetoothDevice mDevice;

      public BlueToothConnectThread(BluetoothDevice device)
      {
         // UUID for bluetooth SPP protocol.
         // It's on the internet brah.
         final UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

         mDevice = device;

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

         mSocket = tmpSock;
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
            mSocket.connect();
         } catch (IOException connectException)
         {
            // Nuts!
            Log.e(TAG, "Could not connect to remote device", connectException);

            try
            {
               mSocket.close();
            } catch (IOException closeException)
            {
               // Well, shit.
               Log.e(TAG, "Could not close socket", closeException);
            }

            // This place smells funny.
            return;
         }

         // Kick off a thread to manage the connection.
         // TODO Call parent "connected" method.
      }

      /**
       * Cancels an in-progress connection.
       */
      public void cancel()
      {
         try
         {
            mSocket.close();
         } catch (IOException e)
         {
            // doh
         }
      }
   }
}
