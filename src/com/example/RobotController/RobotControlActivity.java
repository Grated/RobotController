package com.example.RobotController;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * This class contains the primary remove control functions.
 * User: sgreenman
 * Date: 5/6/13
 * Time: 7:06 PM
 */
public class RobotControlActivity extends Activity implements BluetoothCommsListener
{
   // Debugging
   private static final String TAG = "RobotController:RobotControl";

   // Bluetooth connection
   private BluetoothComms mBtoothComms;
   private boolean bound = false;

   /**
    * Called when the activity is first created.
    *
    * @param savedInstanceState
    */
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
   }

   @Override
   public void onStart()
   {
      super.onStart();

      Log.i(TAG, "RobotControlActivity started");

      // Bind with the BluetoothComms service
      Intent intent = new Intent(this, BluetoothComms.class);
      bindService(intent, connection, Context.BIND_AUTO_CREATE);
   }

   @Override
   public void onStop()
   {
      super.onStop();

      Log.i(TAG, "RobotControlActivity stopped");

      if (bound)
      {
         unbindService(connection);
         bound = false;
      }
   }

   private ServiceConnection connection = new ServiceConnection()
   {
      @Override
      public void onServiceConnected(ComponentName componentName,
                                     IBinder iBinder)
      {
         BluetoothComms.LocalBinder binder =
               (BluetoothComms.LocalBinder)iBinder;
         mBtoothComms = binder.getService();
         binder.registerListener(RobotControlActivity.this);
         bound = true;

         JoystickView view = new JoystickView(RobotControlActivity.this,
               RobotControlActivity.this);
         RobotControlActivity.this.setContentView(view);
         view.requestFocus();
      }

      @Override
      public void onServiceDisconnected(ComponentName componentName)
      {
         bound = false;
      }
   };

   @Override
   public void onConnect()
   {
      Log.i(TAG, "Connection made");
   }

   @Override
   public void onDisconnect()
   {
      Log.i(TAG, "Connection lost");

      // End this activity.
      finish();
   }

   @Override
   public void handleEvent(MessageType type, Object msg)
   {
      Log.i(TAG, "Event received: " + type);
   }

   public void updateSpeed(int x, int y)
   {
      Log.i(TAG, "Speed updated: " + x + ", " + y);

      RobotMessage message;

      message = new RobotMessage("From", "Android", x, y);

      this.mBtoothComms.write(message);
   }

}
