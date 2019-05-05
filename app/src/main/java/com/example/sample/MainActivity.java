package com.example.sample;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG ="MainActivity";

    BluetoothAdapter mBluetoothAdapter;
    Button unpair;
    public ArrayList<BluetoothDevice> mBtDevices = new ArrayList<>();
    public device_list mdevice_list;
    ListView list;
    BluetoothConnectionService mBluetoothConnection;
    Button btnStart;
    Button btnSend;
    EditText etSend;
    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e8-ac64-0B00200c9a66");

    BluetoothDevice mBTDevice;

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceive1 : STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, " mBroadcastReceive1 : STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceive1 : STATE TURNING ON");
                        break;
                }
            }

        }
    };

    private final BroadcastReceiver mBroadcastReceiver2= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action =intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){
                int mode=intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,BluetoothAdapter.ERROR);

                switch (mode){
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG,"Discoverabiltiy enabled");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG , " Connectable");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG," Not able to receive connection");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG," Connecting ...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG," Connected");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver3=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action= intent.getAction();
            Log.d(" onReceive"," Action Found");

            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBtDevices.add(device);
                Log.d(" onReceive",device.getName()+":"+device.getAddress());
                mdevice_list= new device_list(context,R.layout.device_list_view,mBtDevices);
                list.setAdapter(mdevice_list);
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver4=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action=intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice= intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mDevice.getBondState()== BluetoothDevice.BOND_BONDED) {
                    Log.d("BroadcastReceiver", " BOND_BONDED");
                    mBTDevice=mDevice;
                }
                if (mDevice.getBondState()==BluetoothDevice.BOND_BONDING){
                    Log.d("BroadcastReceiver"," BOND_BONDING");
                    }
                if (mDevice.getBondState()==BluetoothDevice.BOND_NONE){
                    Log.d(" BroadcastReceiver","BOND_NONE ");
                }

            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver4);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button blue = (Button) findViewById(R.id.blue);
        Button disc = (Button) findViewById(R.id.disc);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick: enabling/disabling ");
                enableDisableBT();
            }
        });
        disc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick:disc/nodisc");
                discnodisc();
            }
        });
        list= (ListView) findViewById(R.id.list);
        mBtDevices = new ArrayList<>();
        list.setOnItemClickListener(MainActivity.this);
        IntentFilter filter=new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4,filter);
        btnStart=(Button)findViewById(R.id.btnStart);
        btnSend=(Button)findViewById(R.id.btnSend);
        etSend=(EditText)findViewById(R.id.editText);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConnection();

            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            byte[] bytes=etSend.getText().toString().getBytes(Charset.defaultCharset());
            mBluetoothConnection.write(bytes);
            }
        });
    }

    public void startConnection(){
        startBtConnection(mBTDevice,MY_UUID_INSECURE);
    }
    public void startBtConnection(BluetoothDevice device,UUID uuid){
        Log.d(TAG," startBTConnection");
        mBluetoothConnection.startClient(device,uuid);
    }
    public void enableDisableBT(){
            if(mBluetoothAdapter==null){
                Log.d(TAG , "enableDisableBT: Does not have BT capabilities");
                }
            if(!mBluetoothAdapter.isEnabled()){
                Log.d(TAG," enableDisableBT: enabling BT");
                Intent enableBTIntent= new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBTIntent);

                IntentFilter BTIntent=new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
                registerReceiver(mBroadcastReceiver1,BTIntent);
                }
            if(mBluetoothAdapter.isEnabled()){
                Log.d(TAG , " enableDisableBT: disabling BT");
                mBluetoothAdapter.disable();
                IntentFilter BTIntent = new IntentFilter((BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED));

                registerReceiver(mBroadcastReceiver1,BTIntent);
                }
        }
    public void discnodisc(){
            Log.d(TAG," discnodisc: Making device discovarable");
            Intent discoverableIntent= new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
            startActivity(discoverableIntent);

            IntentFilter intentFilter= new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            registerReceiver(mBroadcastReceiver2,intentFilter);
    }
    public void btnDiscover (View view){
        Log.d("btnDiscover ","Looking for unpaired devices");
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG," btnDiscover: Cancelling Discovery");
            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3,discoverDevicesIntent);
        }
        if (!mBluetoothAdapter.isDiscovering()){
            checkBTPermission();

            mBluetoothAdapter.startDiscovery();

            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3,discoverDevicesIntent);
        }
    }
    private void checkBTPermission(){
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP) {
            int permissioncheck = this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            permissioncheck += this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permissioncheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},1001);
            }
        }   else{
                    Log.d("checkBTPermission"," No need to check version");
                }


        }
    @Override
    public void onItemClick(AdapterView<?> adapterView,View view ,int i, long l){
        mBluetoothAdapter.cancelDiscovery();

        Log.d(" onItemClick"," You clicked on a device");
       String deviceName= mBtDevices.get(i).getName();
       String deviceAddress= mBtDevices.get(i).getAddress();

       Log.d(" onItemClick"," = "+ deviceName);
       Log.d(" onItemClick","= "+ deviceAddress);

       if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
           Log.d(TAG,"Tyring to pair with device"+deviceName);
           mBtDevices.get(i).createBond();

           mBTDevice=mBtDevices.get(i);
           mBluetoothConnection=new BluetoothConnectionService(MainActivity.this);
       }
    }
    }

