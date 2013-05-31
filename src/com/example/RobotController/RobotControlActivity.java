package com.example.RobotController;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: sgreenman
 * Date: 5/6/13
 * Time: 7:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class RobotControlActivity extends Activity
{
   // Debugging
   private static final String TAG = "RobotControl";

   // Key names received from the BluetoothChatService Handler
   public static final String DEVICE_NAME = "device_name";
   public static final String TOAST = "toast";

   // Maintains bluetooth connection
   private BluetoothComms comm = null;

   /**
    * Called when the activity is first created.
    *
    * @param savedInstanceState
    */
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.control);

      // Create connection handler
      comm = new BluetoothComms(this, mHandler);

      // Get the message from the intent
      Intent intent = getIntent();
      String message = intent.getStringExtra(MainActivity.EXTRA_DEVICE);
      Log.i(TAG, "Received message: " + message);
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

   // The Handler that gets information back from the BluetoothChatService
   private final BtHelperHandler mHandler = new BtHelperHandler()
   {
      @Override
      public void handleMessage(Message msg)
      {
         BtHelperHandler.MessageType messageType =
               BtHelperHandler.MessageType.values()[msg.what];
         switch (messageType)
         {
            case STATE:
               stateChanged((BtSPPHelper.State) msg.obj);
               break;
            case WRITE:
               byte[] writeBuf = (byte[]) msg.obj;
               // construct a string from the buffer
               String writeMessage = new String(writeBuf);
               mConversationArrayAdapter.add("Me:  " + writeMessage);
               break;
            case READ:
               byte[] readBuf = (byte[]) msg.obj;
               // construct a string from the valid bytes in the buffer
               String readMessage;
               try
               {
                  readMessage = new String(readBuf, 0, msg.arg1, "UTF-16");
               } catch (UnsupportedEncodingException e)
               {
                  // Should complain
                  readMessage = "";
               }
               mConversationArrayAdapter.add(mConnectedDeviceName + ":  "
                     + readMessage);
               break;
            case DEVICE:
               // save the connected device's name
               mConnectedDeviceName = (String) msg.obj;
               Toast.makeText(getApplicationContext(),
                     "Connected to " + mConnectedDeviceName,
                     Toast.LENGTH_SHORT).show();
               break;
            case NOTIFY:
               Toast.makeText(getApplicationContext(), (String) msg.obj,
                     Toast.LENGTH_SHORT).show();
               break;
         }
      }

      /*
       * If the Handler got a state-changes message, process
       * the new state here. We indicate current state in the
       * title bar
       */
      private void stateChanged(BtSPPHelper.State state)
      {
         switch (state)
         {
            case CONNECTED:
               mTitle.setText(R.string.title_connected_to);
               mTitle.append(mConnectedDeviceName);
               mConversationArrayAdapter.clear();
               break;
            case CONNECTING:
               mTitle.setText(R.string.title_connecting);
               break;
            case LISTEN:
            case NONE:
               mTitle.setText(R.string.title_not_connected);
               break;
         }
      }
   };

}
