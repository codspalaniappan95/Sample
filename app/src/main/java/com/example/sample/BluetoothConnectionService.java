package com.example.sample;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectionService {

    private static final String TAG ="BluetoothConnectionServ";

    private static final String appname= "Sample";

    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e8-ac64-0B00200c9a66");

    private final BluetoothAdapter mBluetoothAdapter;

    private AcceptThread mInsecureAcceptThread ;
    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;
    Context mContext;

    private ConnectedThread mconnectedThread;
    public BluetoothConnectionService(Context mContext) {
        this.mContext = mContext;
        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        start();
    }

    private class AcceptThread extends Thread{
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread()
        {
            BluetoothServerSocket tmp=null;
            try{
                tmp= mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appname,MY_UUID_INSECURE);

                Log.d(TAG," AcceptThread:Setting up Server settings"+MY_UUID_INSECURE);
            }catch(IOException e){
            Log.e(TAG," Accept thread IOException :"+ e.getMessage());
            }
            mmServerSocket=tmp;
        }
        public void run(){
            Log.d(TAG," run:AcceptThread Running");

            BluetoothSocket socket = null;
            try{

                Log.d(TAG," run:RFCOM server socket start....");
                socket= mmServerSocket.accept();
                Log.d(TAG," run:RFCOM server socket accepted connection");
            }catch(IOException e){
                Log.e(TAG," Accept thread IOException :"+ e.getMessage());
            }
            if (socket!=null){
                connected(socket,mmDevice);
            }
            Log.i(TAG," END mAcceptThread");
        }
        public void cancel(){
            Log.d(TAG," cancel accepting thread");
            try{
                mmServerSocket.close();
            }catch(IOException e){
                Log.e(TAG," cancel: Close of accepted thread server socket failed"+ e.getMessage());
            }
        }
    }


        private class ConnectThread extends Thread {
            private BluetoothSocket mmSocket;

            public ConnectThread(BluetoothDevice device, UUID uuid) {
                Log.d(TAG, " ConnectThread:started");
                mmDevice = device;
                deviceUUID = uuid;
            }

            public void run() {
                Log.i(TAG, " run:mConnectedThread ");

                BluetoothSocket tmp = null;
                try {

                    Log.d(TAG, " ConnectThread: Tyring to create InsecureRfcommnSocket using UUID" + MY_UUID_INSECURE);
                    tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(deviceUUID);
                    Log.d(TAG, " run:RFCOM server socket accepted connection");
                } catch (IOException e) {
                    Log.e(TAG, " ConnectThread : Could not create InsecureRfCommSocket" + e.getMessage());
                }
                mmSocket = tmp;
                mBluetoothAdapter.cancelDiscovery();
                try {
                    mmSocket.connect();
                    Log.d(TAG, "ConnectThread connected");


                } catch (IOException e) {
                    try {
                        mmSocket.close();
                        Log.d(TAG, "run:Socket Closed");
                    } catch (IOException e1) {
                        Log.e(TAG, "Unable to close connection");

                    }
                    Log.d(TAG, "Could not connect to UUID");
                }
                connected(mmSocket, mmDevice);
            }

            public void cancel() {
                try {
                    Log.d(TAG, " cancel:Closing socket");
                    mmSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, " cancel:close()");

                }
            }
        }

            public synchronized void start(){
                Log.d(TAG,"start");

                if(mConnectThread!=null){
                    mConnectThread.cancel();
                    mConnectThread=null;
                }
                if(mInsecureAcceptThread==null){
                    mInsecureAcceptThread=new AcceptThread();
                    mInsecureAcceptThread.start();
                }
            }
            public void startClient(BluetoothDevice device,UUID uuid){
            Log.d(TAG,"startclient");
            mProgressDialog= ProgressDialog.show(mContext,"Connecting Bluetooth","Please Wait",true);
            mConnectThread=new ConnectThread(device,uuid);
            mConnectThread.start();
            }

            private class ConnectedThread extends Thread{
            private final BluetoothSocket mSocket;
            private final InputStream mmInStream;
            private final OutputStream mmOutStream;

            public ConnectedThread(BluetoothSocket socket){
                Log.d(TAG," Connected thread starting");
                mSocket = socket;
                InputStream tmpIn=null;
                OutputStream tmpOut=null;

                try {
                    mProgressDialog.dismiss();
                }catch (NullPointerException e){
                    e.printStackTrace();
                }

                try{
                    tmpIn=mSocket.getInputStream();
                    tmpOut=mSocket.getOutputStream();
                }catch(IOException e){
                    e.printStackTrace();
                }
                mmInStream=tmpIn;
                mmOutStream=tmpOut;
            }
            public void run(){
                byte[] buffer =new byte[1024];
                int bytes;
                while(true){
                    try{
                        bytes=mmInStream.read(buffer);
                        String incomingMessage=new String(buffer,0,bytes);
                        Log.d(TAG," Input stream "+incomingMessage);

                    }catch(IOException e){
                        Log.e(TAG," error writing to input stream");
                        break;
                    }
                }
            }

            public void write(byte[] bytes){
                String text=new String(bytes, Charset.defaultCharset());
                Log.d(TAG," writing"+text);
                try {
                    mmOutStream.write(bytes);

                }catch(IOException e){
                    Log.e(TAG," Error writing ");
                }
            }
        public void cancel(){
                try{
                    mSocket.close();
                }catch(IOException e){

            }


        }
    }
    private void connected(BluetoothSocket mSocket,BluetoothDevice mmDevice ){
        Log.d(TAG," connected:Starting");
        mconnectedThread=new ConnectedThread(mSocket);
        mConnectThread.start();
    }

    public void write(byte[] out){
        ConnectedThread r;
        int d = Log.d(TAG, " write:Write called");
        mconnectedThread.write(out);

    }
}





