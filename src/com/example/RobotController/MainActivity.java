package com.example.RobotController;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements BluetoothCommsListener
{
   public static final int MESSAGE_DEVICE = 0;

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
      // Get the BluetoothDevice object to start a connection with.
      Spinner spin = (Spinner) this.findViewById(R.id.btooth_devices_spinner);
      BluetoothDevice dev = this.btoothDevices.get(spin.getSelectedItemPosition());

      // Begin the connection process.

   }

   @Override
   public void onConnect()
   {
      // Switch to the remote control activity.
      Intent intent = new Intent(this, RobotControlActivity.class);
      startActivity(intent);
   }

   @Override
   public void onDisconnect()
   {
   }
}
