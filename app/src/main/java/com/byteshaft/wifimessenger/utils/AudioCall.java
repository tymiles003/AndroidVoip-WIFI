package com.byteshaft.wifimessenger.utils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class AudioCall {

    private static final String LOG_TAG = "AudioCall";
    private static final int SAMPLE_RATE = 8000; // Hertz
    private static final int SAMPLE_INTERVAL = 20; // Milliseconds
    private static final int SAMPLE_SIZE = 2; // Bytes
    private static final int BUF_SIZE = SAMPLE_INTERVAL * SAMPLE_INTERVAL * SAMPLE_SIZE * 2; //Bytes
    private InetAddress address; // Address to call
    private int port = 50000; // Port the packets are addressed to
    private boolean mic = false; // Enable mic?
    private boolean speakers = false; // Enable speakers?

    private static AudioCall sInstance;
    //constructor where ip address is set
    private AudioCall(InetAddress address) {
        this.address = address;
    }
    //getinstance checks if audicall's instance is already present,if present it returns object
//containing details of ipaddress else it creates new object and instantiates ip address with passed in parameter
    public static AudioCall getInstance(InetAddress address) {
        if (sInstance == null) {
            sInstance = new AudioCall(address);
        }
        return sInstance;
    }
    //this method calls start mic n speaker to basically hav them in working state
    public void startCall() {
        startMic();
        startSpeakers();
    }
    //endcall stops mic n speakers
    public void endCall() {

        Log.i(LOG_TAG, "Ending call!");
        muteMic();
        muteSpeakers();
    }

    public void muteMic() {

        mic = false;
    }

    public void muteSpeakers() {

        speakers = false;
    }
    //this method starts mic
    public void startMic() {
        // Creates the thread for capturing and transmitting audio
        mic = true;
        //thread is created to handle communication continuosly
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                // Create an instance of the AudioRecord class
                Log.i(LOG_TAG, "Send thread started. Thread id: " + Thread.currentThread().getId());
                //learn more about audiorecord library
                AudioRecord audioRecorder = new AudioRecord (MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)*10);
                int bytes_read = 0;
                int bytes_sent = 0;
                byte[] buf = new byte[BUF_SIZE];
                try {
                    // Create a socket and start recording
                    Log.i(LOG_TAG, "Packet destination: " + address.toString());
                    //here code is  creating a socket ,defining the port n address to which packets need to be transmitted
                    DatagramSocket socket = new DatagramSocket();
                    audioRecorder.startRecording();
                    //mic is a boolean value set to true
                    while(mic) {
                        // Capture audio from the mic and transmit it
                        //reads data means captures audio upto buf_size n stores in 'buf' variable
                        bytes_read = audioRecorder.read(buf, 0, BUF_SIZE);
                        //second constructor of datagram ; learn the constructors of datagrampacket
                        DatagramPacket packet = new DatagramPacket(buf, bytes_read, address, port);
                        //send the constructed packet containin data thorugh the socket,learn more about sockets n
                        //how they fucntion just for better understanding in networking
                        socket.send(packet);
                        //keep incrementing bytes_sent
                        bytes_sent += bytes_read;
                        Log.i(LOG_TAG, "Total bytes sent: " + bytes_sent);
                        Thread.sleep(SAMPLE_INTERVAL, 0);
                    }
                    // Stop recording and release resources
                    audioRecorder.stop();
                    audioRecorder.release();
                    socket.disconnect();
                    socket.close();
                    mic = false;
                }
                catch(InterruptedException e) {

                    Log.e(LOG_TAG, "InterruptedException: " + e.toString());
                    mic = false;
                }
                catch(SocketException e) {

                    Log.e(LOG_TAG, "SocketException: " + e.toString());
                    mic = false;
                }
                catch(UnknownHostException e) {

                    Log.e(LOG_TAG, "UnknownHostException: " + e.toString());
                    mic = false;
                }
                catch(IOException e) {

                    Log.e(LOG_TAG, "IOException: " + e.toString());
                    mic = false;
                }
            }
        });
        //this is to call thread lifecycle method
        thread.start();
    }

    public void startSpeakers() {
        // Creates the thread for receiving and playing back audio
        if(!speakers) {

            speakers = true;
            Thread receiveThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    // Create an instance of AudioTrack, used for playing back audio
                    Log.i(LOG_TAG, "Receive thread started. Thread id: " + Thread.currentThread().getId());
                    AudioTrack track = new AudioTrack(AudioManager.STREAM_VOICE_CALL, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT, BUF_SIZE, AudioTrack.MODE_STREAM);
                    track.play();
                    try {
                        // Define a socket to receive the audio
                        DatagramSocket socket = new DatagramSocket(port);
                        byte[] buf = new byte[BUF_SIZE];
                        while(speakers) {
                            // Play back the audio received from packets
                            DatagramPacket packet = new DatagramPacket(buf, BUF_SIZE);
                            socket.receive(packet);
                            Log.i(LOG_TAG, "Packet received: " + packet.getLength());
                            track.write(packet.getData(), 0, BUF_SIZE);
                        }
                        // Stop playing back and release resources
                        socket.disconnect();
                        socket.close();
                        track.stop();
                        track.flush();
                        track.release();
                        speakers = false;
                    }
                    catch(SocketException e) {

                        Log.e(LOG_TAG, "SocketException: " + e.toString());
                        speakers = false;
                    }
                    catch(IOException e) {

                        Log.e(LOG_TAG, "IOException: " + e.toString());
                        speakers = false;
                    }
                }
            });
            receiveThread.start();
        }
    }
}
