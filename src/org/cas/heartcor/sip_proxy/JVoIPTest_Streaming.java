package org.cas.heartcor.sip_proxy;
/**
* This is a simple test application for JVoIP demonstrating capturing it's media streams.
* See the "How to get the audio stream" and "Work with audio streams" FAQ point in the JVoIP documentation for the details.
*
* In this simple example we just capture the audio stream received from the remote peer and save it in a stream.wav file.
*
* (Of course, the same could be done using the JVoIP voice recording functionality (voicerecording parameter / API_VoiceRecord function), but
* this is just a demonstration and instead of just saving the stream to file, you might process it in realm time after your needs, for example using speech to text for translation or other purposes)
*
* Make sure to copy/include the JVoIP.jar file to your project required libraries list!
* (It is also recommended to copy the mediaench libraries near your jar or class files: https://www.mizu-voip.com/Portals/0/Files/mediaench.zip)
*/



import webphone.*; //import JVoIP. You will have an error here if you haven't added the JVoIP.jar to your project


public class JVoIPTest_Streaming {

    webphone webphoneobj = null;
    SIPNotifications sipnotifications = null;
    SIPMediaStream sipmediastream = null;

    /**
    * Construct and show the application.
    */
    public JVoIPTest_Streaming() {
        Go();
    }

    /**
    * Application entry point.
    */
    public static void main(String[] args) {
        try {
            new JVoIPTest_Streaming();
        }catch(Exception e) {System.out.println("Exception at main: "+e.getMessage()+"\r\n"+e.getStackTrace()); }

    }

    /**
    * This is the important function where all the real work is done.
    * In this example we start the JVoIP (a webphone instance), set parameters, configure streaming and start our SIPMediaStream listent and make an outbound call.
    */

    void Go()
    {
        try{
            System.out.println("init...");

            //create a JVoIP instance
            webphoneobj = new webphone();

            //create the SIPNotifications object to catch the events from JVoIP
            sipnotifications = new SIPNotifications(webphoneobj);
            //start receiving the SIP notifications
            
            sipnotifications.Start();
            //note: not recommended but it is also possible to receive the notifications via UDP packets instead of API_GetNotifications polling. For that just use the depreacted SIPNotificationsUDP class instead of SIPNotifications class

            //Thread.sleep(100); //you might wait a bit for the JVoIP to construct itself

            //set parameters
            webphoneobj.API_SetParameter("loglevel", "1"); //for development you should set the loglevel to 5. for production you should set the loglevel to 1
            webphoneobj.API_SetParameter("logtoconsole", "true"); //if the loglevel is set to 5 then a log window will appear automatically. it can be disabled with this setting
            webphoneobj.API_SetParameter("polling", "3"); //we will use the API_GetNotifications from our notifications thread, so we can safely disable socket/webphonetojs with this setting
            webphoneobj.API_SetParameter("startsipstack", "1"); //auto start the sipstack
            webphoneobj.API_SetParameter("register", "1"); //auto register (set to 0 if you don't need to register or if you wish to call the API_Register explicitely later or set to 2 if must register)
            //webphoneobj.API_SetParameter("proxyaddress", "xxx");  //set this if you have a (outbound) proxy
            //webphoneobj.API_SetParameter("transport", "0");  //the default transport is UDP. Set to 1 if you need TCP or to 2 if you need TLS
            //webphoneobj.API_SetParameter("realm", "xxx");  //your sip realm. it have to be set only if it is different from the serveraddress
            
            // SIP Provider settings??
            webphoneobj.API_SetParameter("serveraddress", "voip.mizu-voip.com"); //your sip server domain or IP:port (the port number must be set only if not the standard 5060)
            webphoneobj.API_SetParameter("username", "jvoiptest");
            webphoneobj.API_SetParameter("password", "jvoiptestpwd");
//             webphoneobj.API_SetParameter("serveraddress", "sipgate.de"); //your sip server domain or IP:port (the port number must be set only if not the standard 5060)
//             webphoneobj.API_SetParameter("username", "2913957e0");
//             webphoneobj.API_SetParameter("password", "AT9RRjf7Q4Bx");
            //we prefer a narrowband codec in this example (but the same works fine also with wideband such as OPUS)
            webphoneobj.API_SetParameter("use_pcmu", "3"); webphoneobj.API_SetParameter("use_pcma", "2"); webphoneobj.API_SetParameter("use_g729", "2"); webphoneobj.API_SetParameter("use_gsm", "2"); webphoneobj.API_SetParameter("use_speex", "1"); webphoneobj.API_SetParameter("use_speexwb", "1"); webphoneobj.API_SetParameter("use_speexuwb", "1"); webphoneobj.API_SetParameter("use_opus", "1"); webphoneobj.API_SetParameter("use_opuswb", "1"); webphoneobj.API_SetParameter("use_opusuwb", "1"); webphoneobj.API_SetParameter("use_opusswb", "1");  webphoneobj.API_SetParameter("use_ilbc", "1");

            //streaming related settings:
            webphoneobj.API_SetParameter("sendmedia_type", "2");  //force 8kHz narrowband for this example
            webphoneobj.API_SetParameter("sendmediain_to", "22001"); //we are interested in remote audio (peer speaking)
            webphoneobj.API_SetParameter("sendmediaout_to", 0); //we are not inerested in local audio (recorded from local microphone)
            webphoneobj.API_SetParameter("sendmediaout_line", "0"); //no need for line headers
            webphoneobj.API_SetParameter("sendmedia_marks", "1"); //enable EOF/BOF packets

            sipmediastream = new SIPMediaStream(webphoneobj,22001); //start media stream listener
            sipmediastream.Start();

            //you might set any other required parameters here for your use-case, for example proxyaddres, transport, others. See the parameter list in the documentation.

            //start the SIP stack
            System.out.println("start...");
            webphoneobj.API_Start();
            Thread.sleep(200); //you might wait a bit for the sip stack to fully initialize (to make this more correct and reduce the wait time, you might remove this sleep in your app and continue instead when you receive the "START,sip" noification)

            System.out.println("SIP stack initialized. Press enter to make a call");
            WaitForEnterKeyPress();

            //make an outbound call
            System.out.println("calling...");
            webphoneobj.API_Call( -1, "testivr3");
           // webphoneobj.Api_

            //normally your app logic might be continued elsewhere (handling user interactions such as disconnect button click) and you should process the notifications about the call state changes in the SIPNotification.java -> ProcessNotifications() function

            //wait for key press
            System.out.println("Call initiated. Press enter to stop");
            WaitForEnterKeyPress();

            //disconnect the call and stop the SIP stack
            System.out.println("closing...");
            webphoneobj.API_Hangup( -1);  //disconnect the call

            sipmediastream.Stop(); //stop media stream listener
            webphoneobj.API_Stop(); //stop the sip stack (this will also unregister)
            sipnotifications.Stop(); //stop the JVoIP notification listener
            System.out.println("Finished. Press enter to exit");

            WaitForEnterKeyPress();
            System.exit(0); //exit the JVM

        }catch(Exception e) {System.out.println("Exception at Go: "+e.getMessage()+"\r\n"+e.getStackTrace()); }

    }

    void WaitForEnterKeyPress()
    {
        try{
            //skip existing (old) input
            int avail = System.in.available();
            if(avail > 0) System.in.read(new byte[avail]);
        }catch(Exception e) {}

        try{
            //wait for enter press
            while (true)
            {
                int ch = System.in.read();
                if(ch == '\n') break;
            }
        }catch(Exception e) {}
    }
}

