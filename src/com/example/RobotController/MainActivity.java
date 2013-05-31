package com.example.RobotController;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity
{
   // Debugging
   private static final String TAG = "MainActivity";

   // Intent extra
   public static String EXTRA_DEVICE = "device";

   // List of bluetooth devices
   List<BluetoothDevice> btoothDevices;

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

   public void connectClicked(View view)
   {
      Log.i(TAG, "connectClicked");

      // Get the BluetoothDevice object to start a connection with.
      Spinner spin = (Spinner) this.findViewById(R.id.btooth_devices_spinner);
      BluetoothDevice dev = this.btoothDevices.get(spin.getSelectedItemPosition());

      // Begin the connection process.
      Intent intent = new Intent(MainActivity.this, RobotControlActivity.class);
      intent.putExtra(EXTRA_DEVICE, dev.getAddress());
      MainActivity.this.startActivity(intent);

   }

   protected void onDestroy()
   {
      super.onDestroy();

      // Make sure we're not doing discovery anymore.
      // TODO: Cancel discovery
   }
}
