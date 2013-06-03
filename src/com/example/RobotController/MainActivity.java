package com.example.RobotController;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements BluetoothCommsListener
{
   // Debugging
   private static final String TAG = "RobotController:MainActivity";

   // List of bluetooth devices
   List<BluetoothDevice> btoothDevices;

   // Bluetooth connection
   private BluetoothComms mBtoothComms;
   private boolean bound = false;

   /**
    * Called when the activity is first created.
    */
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
   }

   @Override
   public void onStart()
   {
      super.onStart();

      // Bind with the bluetooth connection service.
      Intent intent = new Intent(this, BluetoothComms.class);
      bindService(intent, connection, Context.BIND_AUTO_CREATE);

      Spinner spin = (Spinner) this.findViewById(R.id.btooth_devices_spinner);

      String[] entries = new String[1];
      entries[0] = "No Devices";

      BluetoothAdapter btooth = BluetoothAdapter.getDefaultAdapter();

      if (btooth != null)
      {
         if (btooth.isEnabled())
         {
            // LETS DO THIS
            // See what devices are already paired
            btoothDevices = new ArrayList<BluetoothDevice>();
            btoothDevices.addAll(btooth.getBondedDevices());

            // If there are any paired devices
            if (btoothDevices.size() > 0)
            {
               entries = new String[btoothDevices.size()];

               // add the name and address to the drop down menu.
               int i = 0;
               for (BluetoothDevice dev : btoothDevices)
               {
                  entries[i++] = dev.getName() + " : " + dev.getAddress();
               }
            }
         } else
         {
            // Bluetooth isn't enabled...
            //Intent enableBtoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // Eventually we will want to kindly ask the user to turn bluetooth on.
            //startActivityForResult(enableBtoothIntent, REQUEST_ENABLE_BT);
         }
      } else
      {
         // Device does not support bluetooth.
      }

      ArrayAdapter<String> spinAdapter = new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_item,
            entries);
      spin.setAdapter(spinAdapter);
   }

   @Override
   public void onStop()
   {
      super.onStop();
      if (bound)
      {
         unbindService(connection);
         bound = false;
      }
   }

   public void connectClicked(View view)
   {
      Log.i(TAG, "connectClicked");

      if (this.mBtoothComms != null)
      {
         // Get the BluetoothDevice object to start a connection with.
         Spinner spin = (Spinner) this.findViewById(R.id.btooth_devices_spinner);
         BluetoothDevice dev = this.btoothDevices.get(spin.getSelectedItemPosition());

         // Begin the connection process.
         this.mBtoothComms.connect(dev);
      }
      else
      {
         Log.i(TAG, "Error: mBtoothComms not initialized, bound?: " + bound);
      }
   }

   protected void onDestroy()
   {
      super.onDestroy();

      // Make sure we're not doing discovery anymore.
      // TODO: Cancel discovery
   }

   @Override
   public void onConnect()
   {
      // Switch to the remote control.
      Intent intent = new Intent(MainActivity.this, RobotControlActivity.class);
      MainActivity.this.startActivity(intent);
   }

   @Override
   public void onDisconnect()
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   @Override
   public void handleEvent(MessageType type, Object msg)
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   private ServiceConnection connection = new ServiceConnection()
   {
      @Override
      public void onServiceConnected(ComponentName componentName,
                                     IBinder iBinder)
      {
         Log.i(TAG, "Connected to BluetoothComms service");
         BluetoothComms.LocalBinder binder =
               (BluetoothComms.LocalBinder) iBinder;
         mBtoothComms = binder.getService();
         binder.registerListener(MainActivity.this);
         bound = true;
      }

      @Override
      public void onServiceDisconnected(ComponentName componentName)
      {
         Log.i(TAG, "Disconnected from BluetoothComms service");
         bound = false;
      }
   };

}
