/**
* Notification receiver thread
*/

package org.cas.heartcor.sip_proxy; //you might change this after your package name

import java.util.HashMap;
import java.util.Map;

import webphone.webphone;


public class SIPNotifications extends Thread
{
   boolean terminated = false;
   webphone webphoneobj = null;
   
   Map<Integer, SIPMediaStream> streams = new HashMap<>();

   /**
   * ctor
   */
   public SIPNotifications(webphone webphoneobj_in)
   {
       webphoneobj = webphoneobj_in;
   }

   /**
   *Start thread
   */
   public boolean Start()
   {

       try{
           this.start();
           System.out.println("sip notifications started");
           return true;
       }catch(Exception e) {System.out.println("Exception at SIPNotifications Start: "+e.getMessage()+"\r\n"+e.getStackTrace()); }
       return false;
   }


   /**
   * signal terminate thread
   */
   public void Stop()
   {
       try{
           terminated = true;
       }catch(Exception e) { }
   }

   //

   /**
   * blocking read in this thread
   */
   public void run()
   {
        try{
           String sipnotifications = "";
           String[] notarray = null;

           while (!terminated)
           {
                  //get notifications from the SIP stack
                  sipnotifications = webphoneobj.API_GetNotificationsSync();

                  if (sipnotifications != null && sipnotifications.length() > 0)
                  {
                      //split by line
                      System.out.println("\tREC FROM JVOIP: " + sipnotifications);
                      notarray = sipnotifications.split("\r\n");

                      if (notarray == null || notarray.length < 1)
                      {
                         if(!terminated) Thread.sleep(1); //some error occured. sleep a bit just to be sure to avoid busy loop
                      }
                      else
                      {
                        for (int i = 0; i < notarray.length; i++)
                        {
                            if (notarray[i] != null && notarray[i].length() > 0)
                            {
                                if(notarray[i].indexOf("WPNOTIFICATION,") == 0) notarray[i] = notarray[i].substring(15); //remove the WPNOTIFICATION, prefix
                                processNotification(notarray[i]);
                            }
                        }
                      }
                  }
                  else
                  {
                      if(!terminated) Thread.sleep(1);  //some error occured. sleep a bit just to be sure to avoid busy loop
                  }
           }

        }catch(Exception e)
        {
           if(!terminated) System.out.println("Exception at SIPNotifications run: "+e.getMessage()+"\r\n"+e.getStackTrace());
        }

    }


    /**
    * all messages from JVoIP will be routed to this function.
    * parse and process them after your needs regarding to your application logic
    */

    public void processNotification(String msg)
    {
        try{
            //frame.jTextArea1.append(msg);

            //TODO: process notifications here (change your user interface or business logic depending on the sipstack state / call state by parsing the strings receiver here).
            //See the "Notifications" chapter in the documentation for the expecteddetails.

            //example code:
            String[] parameters = msg.split(",");
            if(parameters.length >= 2 && parameters[0].equals("STATUS") && parameters[2].equals("Registered."))
            {
                System.out.println("SIP client registered.");
            }
            
            // <3Cor
            if(parameters.length >= 2 && parameters[0].equals("STATUS") && parameters[2].equals("Ringing"))
            {
            	int line = Integer.parseInt(parameters[1]);
            	webphoneobj.API_Accept(line);
            	SIPMediaStream sipmediastream = new SIPMediaStream(webphoneobj, 22001); // start media stream listener
    			sipmediastream.Start();
    			streams.put(line, sipmediastream);
            }
            if(parameters.length >= 2 && parameters[0].equals("STATUS") && parameters[2].equals("Finished"))
            {
            	System.out.println("<3 We stop the stream");
            	int line = Integer.parseInt(parameters[1]);
    			streams.get(line).Stop();
            }

        }catch(Exception e) { System.out.println("Exception at SIPNotifications ProcessNotifications: "+e.getMessage()+"\r\n"+e.getStackTrace()); }
    }
}

