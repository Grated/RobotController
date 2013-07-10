package com.example.RobotController;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created with IntelliJ IDEA.
 * User: sgreenman
 * Date: 4/20/13
 * Time: 11:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class JoystickView extends View implements View.OnTouchListener
{
   // Used for tagging debug messages
   private static final String TAG = "RobotController:JoystickView";

   // Our wonderful paint object
   private Paint paint = new Paint();

   // Inner and outer circle parameters
   // Radius
   private int m_out_radius = 0;
   private int m_in_radius = 0;

   // X and Y coordinates of the center of the outer circle.
   private float m_outer_x;
   private float m_outer_y;

   // Inner circle position is stored as a distance from the center of
   // the outer circle.  If the orientation of the phone changes this
   // may make the code simpler.
   private float m_inner_x;
   private float m_inner_y;

   // Connected device to send messages to.
   private RobotControlActivity remote;

//   public JoystickView(Context context, AttributeSet attrs, int defStyle)
//   {
//      super(context, attrs, defStyle);
//      initMembers();
//   }
//
//   public JoystickView(Context context, AttributeSet attrs)
//   {
//      super(context, attrs);
//      initMembers();
//   }
//
   public JoystickView(Context context, RobotControlActivity remote)
   {
      super(context);

      this.remote = remote;

      initMembers();
   }

   private void initMembers()
   {
      paint.setAntiAlias(true);
      this.setOnTouchListener(this);

      // Initialize inner_pos to 0
      m_inner_x = 0;
      m_inner_y = 0;
   }

   @Override
   protected void onSizeChanged(int newX, int newY, int oldX, int oldY)
   {
      Log.i(TAG, "Size changed");

      // Set the circle size
      if(newX > newY)
      {
         m_out_radius = (int)(0.8 * newY / 2);
         m_in_radius = (int)(0.2 * newY / 2);
      }
      else
      {
         m_out_radius = (int)(0.8 * newX / 2);
         m_in_radius = (int)(0.2 * newX / 2);
      }

      // Set the center position.
      m_outer_x = newX/2;
      m_outer_y = newY/2;
   }

   @Override
   public void onDraw(Canvas canvas)
   {
      super.onDraw(canvas);
      drawJoystick(canvas);
   }

   @Override
   public boolean onTouch(View view, MotionEvent motionEvent)
   {
      if (motionEvent.getActionMasked() == MotionEvent.ACTION_MOVE)
      {
         // Get the coordinates of this event
         float event_x = motionEvent.getX();
         float event_y = motionEvent.getY();

         // The point provided may be somewhere outside the joystick area.
         // If it is, map it to the nearest point within the area and
         // draw the joystick position.

         // Assume the point will be inside the area.
         // This is done to keep the code cleaner.
         m_inner_x = event_x - m_outer_x;
         m_inner_y = event_y - m_outer_y;

         // m_inner_x/y is now a vector.

         // ...get the length of the vector
         double length =
               Math.sqrt(Math.pow(m_inner_x, 2) + Math.pow(m_inner_y, 2));

         // If the length of the vector is zero, there's nothing to do.
         if (length > 0)
         {
            // Figure out if the new point is outside the joystick area.
            if (length > (m_out_radius - m_in_radius))
            {
               // Nuts, it's outside the area.

               // Normalize the vector.  If the length is zero then we aren't
               // moving anywhere.
               m_inner_x /= length;
               m_inner_y /= length;

               // Puts the joystick on the edge of the joystick area.
               m_inner_x *= (m_out_radius - m_in_radius);
               m_inner_y *= (m_out_radius - m_in_radius);
            }
            // Else it's already inside the area.

            // Tell the system to call onDraw()
            invalidate();

            // Send the new coordinates.
            int x_int = (int)(m_inner_x / m_in_radius);
            int y_int = (int)(m_inner_y / m_in_radius);
            sendNewPosition(x_int, y_int);
         }
      }
      else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP)
      {
         // User took their finger off the joystick, snap it back to
         // the middle.
         m_inner_x = 0;
         m_inner_y = 0;
         invalidate();
         sendNewPosition(0, 0);
      }

      return true;
   }

   private void drawJoystick(Canvas canvas)
   {
      // Draw the outer circle
      paint.setColor(Color.WHITE);
      canvas.drawCircle(m_outer_x, m_outer_y, m_out_radius, paint);

      // Draw the inner circle
      paint.setColor(Color.GREEN);
      canvas.drawCircle(m_inner_x + m_outer_x,
                        m_inner_y + m_outer_y,
                        m_in_radius, paint);

   }

   private boolean isInOuterCircle(Point point)
   {
      boolean retval = false;

      double x_diff_squared = Math.pow((double)(point.x - m_outer_x), 2.0);
      double y_diff_squared = Math.pow((double)(point.y - m_outer_y), 2.0);
      double radius_squared = Math.pow((double)(m_out_radius-m_in_radius), 2.0);

      if ((x_diff_squared + y_diff_squared) < radius_squared)
      {
         retval = true;
      }

      return retval;
   }

   private void sendNewPosition(int x, int y)
   {
      remote.updateSpeed(x, y);
   }

}
