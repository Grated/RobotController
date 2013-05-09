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
   // 2*MAX_STRING bytes LCD messages
   // 1 byte checksum
   private final static int MSG_SIZE = (1 + 2 + (2*MAX_LCD_LINE) + 1);

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

      // Copy the speed values
      msg[0] = (byte)leftSpeed;
      msg[1] = (byte)rightSpeed;

      // Copy the top message
      for (int i = 0; i < MAX_LCD_LINE && i < lcdTop.length(); i++)
      {
         msg[2 + i] = (byte)lcdTop.charAt(i);
      }

      // Copy the bottom message
      for (int i = 0; i < MAX_LCD_LINE && i < lcdBot.length(); i++)
      {
         msg[2 + MAX_LCD_LINE + i] = (byte)lcdBot.charAt(i);
      }

      // Calculate the checksum
      for (int i = 0; i < MSG_SIZE - 1; i++)
      {
         checksum += msg[i];
      }

      msg[MSG_SIZE-1] = checksum;

      return msg;
   }
}
