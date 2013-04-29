package com.example.RobotController;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.Set;

public class MainActivity extends Activity
{
    /** Called when the activity is first created. */
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

      Spinner spin = (Spinner)this.findViewById(R.id.btooth_devices_spinner);

      String[] entries = new String[1];
      entries[0] = "No Devices";

      BluetoothAdapter btooth = BluetoothAdapter.getDefaultAdapter();

      if (btooth != null)
      {
         if (btooth.isEnabled())
         {
            // LETS DO THIS
            // See what devices are already paired
            Set<BluetoothDevice> pairedDevices = btooth.getBondedDevices();

            // If there are any paired devices
            if (pairedDevices.size() > 0)
            {
               entries = new String[pairedDevices.size()];

               // add the name and address to the drop down menu.
               int i = 0;
               for (BluetoothDevice dev : pairedDevices)
               {
                  entries[i++] = dev.getName() + " : " + dev.getAddress();
               }
            }
         }
         else
         {
            // Bluetooth isn't enabled...
            //Intent enableBtoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // Eventually we will want to kindly ask the user to turn bluetooth on.
            //startActivityForResult(enableBtoothIntent, REQUEST_ENABLE_BT);
         }
      }
      else
      {
         // Device does not support bluetooth.
      }

      ArrayAdapter<String> spinAdapter = new ArrayAdapter<String>(this,
            R.id.btooth_devices_spinner, entries);
      spin.setAdapter(spinAdapter);
   }

}
