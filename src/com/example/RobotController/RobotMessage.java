package com.example.RobotController;

/**
 * Created with IntelliJ IDEA.
 * User: sgreenman
 * Date: 5/5/13
 * Time: 9:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class RobotMessage
{
   // Version number
   private final static int VERSION = 1;

   // Size of top and bottom messages
   private final static int MAX_LCD_LINE = 16;

   // Maximum left and right speed
   private final static int MAX_SPEED = 100;
   private final static int MIN_SPEED = -100;

   // 1 byte version
   // 2 bytes speed
   // 2*(MAX_STRING+1) bytes LCD messages plus nul terminator
   // 1 byte checksum
   private final static int MSG_SIZE = (1 + 2 + (2*(MAX_LCD_LINE+1)) + 1);
   private final static int VERSION_IDX = 0;
   private final static int LSPEED_IDX = 1;
   private final static int RSPEED_IDX = 2;
   private final static int TOP_START = 3;
   private final static int BOT_START = 20;
   private final static int CSUM_IDX = MSG_SIZE - 1;

   private final String lcdTop;
   private final String lcdBot;
   private int leftSpeed;
   private int rightSpeed;

   public RobotMessage(String top, String bot, int left, int right)
   {
      lcdTop = top;
      lcdBot = bot;
      leftSpeed = left;
      rightSpeed = right;
   }

   public byte[] packMessage()
   {
      byte[] msg = new byte[MSG_SIZE];

      byte checksum = 0;

      // Clamp the speed values
      if (leftSpeed > MAX_SPEED)
      {
         leftSpeed = MAX_SPEED;
      }
      else if (leftSpeed < MIN_SPEED)
      {
         leftSpeed = MIN_SPEED;
      }

      if (rightSpeed > MAX_SPEED)
      {
         rightSpeed = MAX_SPEED;
      }
      else if (rightSpeed < MIN_SPEED)
      {
         rightSpeed = MIN_SPEED;
      }

      // TODO This can be cleaner

      // Copy the version.
      msg[VERSION_IDX] = VERSION;

      // Copy the speed values
      msg[LSPEED_IDX] = (byte)leftSpeed;
      msg[RSPEED_IDX] = (byte)rightSpeed;

      // Loop iterator
      int i;

      // Copy the top message
      for (i = TOP_START; i < MAX_LCD_LINE && i < lcdTop.length(); i++)
      {
         msg[i] = (byte)lcdTop.charAt(i);
      }
      // Null terminate
      msg[i] = '\0';

      // Copy the bottom message
      for (i = BOT_START; i < MAX_LCD_LINE && i < lcdBot.length(); i++)
      {
         msg[i] = (byte)lcdBot.charAt(i);
      }
      // Null terminate
      msg[i] = '\0';

      // Calculate the checksum
      for (i = 0; i < MSG_SIZE - 1; i++)
      {
         checksum += msg[i];
      }

      msg[CSUM_IDX] = checksum;

      return msg;
   }
}
