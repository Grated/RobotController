package com.example.RobotController;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;

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
      BluetoothAdapter btooth = BluetoothAdapter.getDefaultAdapter();
      if (btooth != null)
      {
         if (btooth.isEnabled())
         {
            // LETS DO THIS
            // Look for some devices
         }
         else
         {
            // Bluetooth isn't enabled...
            Intent enableBtoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // Eventually we will want to kindly ask the user to turn bluetooth on.
            //startActivityForResult(enableBtoothIntent, REQUEST_ENABLE_BT);
         }
      }
      else
      {
         // Device does not support bluetooth.
      }
   }

}
