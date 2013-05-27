package com.example.RobotController;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Created with IntelliJ IDEA.
 * User: sgreenman
 * Date: 5/6/13
 * Time: 7:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class RobotControlActivity extends Activity implements BluetoothCommsListener
{
   // Message types sent from the BluetoothComms Handler
   public static final int MESSAGE_STATE_CHANGE = 1;
   public static final int MESSAGE_READ = 2;
   public static final int MESSAGE_WRITE = 3;
   public static final int MESSAGE_DEVICE_NAME = 4;
   public static final int MESSAGE_TOAST = 5;

   // Key names received from the BluetoothChatService Handler
   public static final String DEVICE_NAME = "device_name";
   public static final String TOAST = "toast";

   /**
    * Called when the activity is first created.
    * @param savedInstanceState
    */
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.control);
   }

   @Override
   public void onStart()
   {
      super.onStart();

      // Initiate a bluetooth connection with the specified device.
   }

   @Override
   public void onStop()
   {
      super.onStop();

   }

   // The handler that gets information back from the BlutoothComms
   private final Handler mHandler = new Handler()
   {
      @Override
      public void handleMessage(Message msg)
      {
         switch(msg.what)
         {
            case MESSAGE_STATE_CHANGE:
               BluetoothComms.State state = (BluetoothComms.State)(msg.obj);
               switch(state)
               {
                  case CONNECTED:
                     // Woohoo! Connected. Now what?
                     break;
                  case CONNECTING:
                     // Something to do while connecting...
                     break;
                  case LISTEN:
                  case NONE:
                     // Something to do while not connected or waiting.
                     break;
               }
               break;
            case MESSAGE_WRITE:
               break;
            case MESSAGE_READ:
               break;
            case MESSAGE_DEVICE_NAME:
               break;
            case MESSAGE_TOAST:
               break;
         }
      }
   };

   @Override
   public void onConnect()
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   @Override
   public void onDisconnect()
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }
}
