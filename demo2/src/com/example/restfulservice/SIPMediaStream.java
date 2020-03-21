/**
* UDP socket to receive the media stream
*/


package com.example.restfulservice; //you might change this after your package name

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import webphone.webphone;


public class SIPMediaStream extends Thread
{
   boolean terminated = false;
   byte[] buf = null;
   DatagramSocket socket = null;
   DatagramPacket packet = null;
   int listenport = 0;
   final static int MAXBUFFZIE = 1600;

   /**
   * ctor
   */
   public SIPMediaStream(webphone webphoneobj_in, int listenport_in)
   {
       listenport = listenport_in;
   }

   /**
   *Start listening for audio packets on listenport
   */
   public boolean Start()
   {

       try{
           //socket = new DatagramSocket(listenport);
           InetSocketAddress localaddress = new InetSocketAddress("127.0.0.1", listenport);
           //bind to 127.0.0.1:listenport
           socket = new DatagramSocket(localaddress);

           buf = new byte[MAXBUFFZIE];
           packet = new DatagramPacket(buf, buf.length);
           terminated = false;
           this.start();
           System.out.println("sip media stream listener started on port "+Integer.toString(listenport));
           return true;
       }catch(Exception e) {System.out.println("Exception at SIPMediaStream Start: "+e.getMessage()+"\r\n"+e.getStackTrace()); }
       return false;
   }


   /**
   * terminate
   */
   public void Stop()
   {
       Destroy();
   }

   /**
   * signal terminate and close the socket
   */

   public void Destroy()
    {
         if(terminated) return;
         terminated = true;
         if (socket != null) { try{ socket.close(); }catch(Exception e) { } socket = null; }
         WriteWaveFile(); //write wav file if there were packets collected
    }

   /**
   * blocking read in this thread
   */
   public void run()
   {
        try{
            while (!terminated) {
                packet.setData(buf, 0, buf.length);
                packet.setLength(buf.length);
                socket.receive(packet);
                if (packet != null && packet.getLength() > 0) {
                    ProcessAudioPacket(packet.getData(),packet.getLength());
                }
            }
        }catch(Exception e)
        {
           if(!terminated) System.out.println("Exception at SIPMediaStream run: "+e.getMessage()+"\r\n"+e.getStackTrace());
        }
        Destroy();
    }


   byte[] audiobuffer = null; //we will collect all received audio into this buffer
   int audiobuffersize = 1000000;  //def to ~1MB
   int audiobufferlen = 0; //written size

   /**
    * process audio packets here
    */

    public void ProcessAudioPacket(byte[] buff, int length)
    {
        try{
            if(buff == null || length < 1) return;

            if(length == 3 && buff[0] == 'B' && buff[1] == 'O' && buff[2] == 'F')  //handle BOF (begin of stream packet)
            {
                System.out.println("audio streaming BOF packet received");
                //audiobufferlen = 0;
                return;
            }
            else if(length == 3 && buff[0] == 'E' && buff[1] == 'O' && buff[2] == 'F')  //handle EOF (end of stream packet)
            {
                System.out.println("audio streaming EOF packet received");
                WriteWaveFile();
                return;
            }

            //note: instead of just collecting the packets to be written in a wav file, you can do any audio processing here (for example translate using some speech to text library or cloud service)

            //append received buff to audiobuffer
            if(audiobufferlen == 0)
            {
                System.out.println("first audio streaming packet received from JVoIP");
            }

            if(audiobuffer == null)
            {
                audiobuffer = new byte[audiobuffersize];
            }
            if(audiobufferlen + length >= audiobuffersize)
            {
                //expand buffer if not enough room
                byte[] tmpbuff = new byte[audiobufferlen];
                System.arraycopy(audiobuffer, 0, tmpbuff, 0, audiobufferlen);
                audiobuffersize *= 2;
                audiobuffer = new byte[audiobuffersize];
                System.arraycopy(tmpbuff, 0, audiobuffer, 0, audiobufferlen);
            }

            System.arraycopy(buff, 0, audiobuffer, audiobufferlen, length);
            audiobufferlen += length;

        }catch(Throwable e) { System.out.println("Exception at SIPMediaStream process audio packet: "+e.getMessage()+"\r\n"+e.getStackTrace()); }
    }


    /**
    * write collected media to wave file
    */

   void WriteWaveFile()
   {
       try{
           if(audiobuffer == null || audiobufferlen < 1) return;
           int framecount = audiobufferlen/2; //recording in 8kHz 16 bit mono, so two bytes means one frame
           System.out.println("storing audio to stream.wav ("+ Integer.toString(framecount)+" frames)");
           WavFile wavFile = WavFile.newWavFile(new java.io.File("stream.wav"), 1, framecount, 16, 8000, false);

           int[] framebuff = new int[2];

           for(int i = 0; i < framecount; i++)
           {
               framebuff[0] = bytesToInt16(audiobuffer, i * 2);
               wavFile.writeFrames(framebuff, 1);
           }

           wavFile.close();
       }catch(Throwable e) { System.out.println("Exception at SIPMediaStream WriteWaveFile: "+e.getMessage()+"\r\n"+e.getStackTrace()); }
       audiobufferlen = 0;
   }

   /**
    * helper function to convert byte buffer to audio frame
    */

    int bytesToInt16(byte[] buffer, int byteOffset)
    {
         return (buffer[byteOffset + 1] << 8) | (buffer[byteOffset] & 0xFF);
    }



}
