package com.example.RobotController;

import android.os.Handler;
import android.os.Message;

/**
 * Created with IntelliJ IDEA.
 * User: sgreenman
 * Date: 5/27/13
 * Time: 5:28 PM
 *
 * This class defines constants and provides wrapper code that makes
 * messaged-related methods cleaner.
 */
public class BluetoothHelperHandler extends Handler
{
   public enum MessageType
   {
      STATE,
      READ,
      WRITE,
      DEVICE,
      NOTIFY
   }

   public Message obtainMessage(MessageType msg, int count, Object obj)
   {
      return obtainMessage(msg.ordinal(), count, -1, obj);
   }

   public MessageType getMessageType(int ordinal)
   {
      return MessageType.values()[ordinal];
   }

}
