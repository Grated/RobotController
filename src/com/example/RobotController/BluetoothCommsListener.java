package com.example.RobotController;

/**
 * Created with IntelliJ IDEA.
 * User: sgreenman
 * Date: 5/8/13
 * Time: 7:39 AM
 * To change this template use File | Settings | File Templates.
 */
public interface BluetoothCommsListener
{
   public enum MessageType
   {
      STATE,
      READ,
      WRITE,
      DEVICE,
      NOTIFY
   }

   public void onConnect();
   public void onDisconnect();
   public void handleEvent(MessageType type, Object msg);
}
