package org.cas.heartcor.sip_proxy;
import java.util.Arrays;
import java.util.List;

import webphone.*;

public class HeartCorStreaming{
	  static webphone webphoneobj = null;
	  
	  static SIPNotifications sipnotifications = null;
	  static SIPMediaStream sipmediastream = null;
	  
		public static void main(String[] args) {
			try {
				new HeartCorStreaming();
				initialize();
			} catch (Exception e) {
				System.out.println("Exception at main: " + e.getMessage() + "\r\n" + e.getStackTrace());
			}
		}
	  
		public static class HeartCorPolling extends Thread {
			public void run() {
				String notificationsResponse = webphoneobj.API_GetNotifications();
				List<String> notifications = Arrays.asList(notificationsResponse.split("\r\n"));

				int i = 0;
				while (i < notifications.size()) {
					String message = notifications.get(i);
					if (message.startsWith("STATUS")) {
						System.out.println(message);
//						List<String> messageParameters = Arrays.asList(message.split(","));
//						if (messageParameters.get(2).contains("Ringing") && messageParameters.get(5).equals("2")) {
//							System.exit(1);
//							// return from thread
//						}
					}
					i++;
				}
			}
		}
	  
	  public static void initialize() {
		  // Registry Settings
		  // 
		  
		  webphoneobj = new webphone();

          //create the SIPNotifications object to catch the events from JVoIP
          sipnotifications = new SIPNotifications(webphoneobj);
          
          
		  sipnotifications.Start();
		  //note: not recommended but it is also possible to receive the notifications via UDP packets instead of API_GetNotifications polling. For that just use the depreacted SIPNotificationsUDP class instead of SIPNotifications class
		  webphoneobj.API_SetParameter("loglevel", "1"); //for development you should set the loglevel to 5. for production you should set the loglevel to 1
		  webphoneobj.API_SetParameter("logtoconsole", "true"); //if the loglevel is set to 5 then a log window will appear automatically. it can be disabled with this setting
		  webphoneobj.API_SetParameter("polling", "3"); //we will use the API_GetNotifications from our notifications thread, so we can safely disable socket/webphonetojs with this setting
		  webphoneobj.API_SetParameter("startsipstack", "1"); //auto start the sipstack
		  webphoneobj.API_SetParameter("register", "1"); //auto register (set to 0 if you don't need to register or if you wish to call the API_Register explicitely later or set to 2 if must register)
		  //webphoneobj.API_SetParameter("proxyaddress", "xxx");  //set this if you have a (outbound) proxy
		  //webphoneobj.API_SetParameter("transport", "0");  //the default transport is UDP. Set to 1 if you need TCP or to 2 if you need TLS
		  //webphoneobj.API_SetParameter("realm", "xxx");  //your sip realm. it have to be set only if it is different from the serveraddress
		  
		   webphoneobj.API_SetParameter("serveraddress", "sipgate.de"); //your sip server domain or IP:port (the port number must be set only if not the standard 5060)
		   webphoneobj.API_SetParameter("username", "2913957e0");
		   webphoneobj.API_SetParameter("password", "AT9RRjf7Q4Bx");
		  //we prefer a narrowband codec in this example (but the same works fine also with wideband such as OPUS)
		  webphoneobj.API_SetParameter("use_pcmu", "3"); webphoneobj.API_SetParameter("use_pcma", "2"); webphoneobj.API_SetParameter("use_g729", "2"); webphoneobj.API_SetParameter("use_gsm", "2"); webphoneobj.API_SetParameter("use_speex", "1"); webphoneobj.API_SetParameter("use_speexwb", "1"); webphoneobj.API_SetParameter("use_speexuwb", "1"); webphoneobj.API_SetParameter("use_opus", "1"); webphoneobj.API_SetParameter("use_opuswb", "1"); webphoneobj.API_SetParameter("use_opusuwb", "1"); webphoneobj.API_SetParameter("use_opusswb", "1");  webphoneobj.API_SetParameter("use_ilbc", "1");
		
		  //streaming related settings:
		  webphoneobj.API_SetParameter("sendmedia_type", "2");  //force 8kHz narrowband for this example
		  webphoneobj.API_SetParameter("sendmediain_to", "22001"); //we are interested in remote audio (peer speaking)
		  webphoneobj.API_SetParameter("sendmediaout_to", 0); //we are not inerested in local audio (recorded from local microphone)
		  webphoneobj.API_SetParameter("sendmediaout_line", "0"); //no need for line headers
		  webphoneobj.API_SetParameter("sendmedia_marks", "1"); //enable EOF/BOF packets

			webphoneobj.API_Start();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		  // keep connection open & register listener
          
		  // save the stream 
		  
		  // forward to Alexa?
		  
		  // quit on Exception/ Event
		  }
		  
		  
	  }
	  

