package com.calvo.ramon.tanquepepito;

import android.support.v7.app.AppCompatActivity;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.lang.Math;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.os.Handler;


public class MainActivity extends Activity {



    final long tick_every_ms = 80;
    // contains information:
    //      - lights are switched on or off?
    //      - laser is switched on or off?
    //      - fire
    byte tank_control;

    Handler bluetoothIn;

    final int handlerState = 0;             //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address = null;

    RelativeLayout layout_joystick_left;
    RelativeLayout layout_joystick_right;
    ImageView image_joystick, image_border;
    //TextView textView1, textView2;

    JoyStickClass js_left;
    JoyStickClass js_right;

    //ToggleButton LaserButton;
    //ToggleButton LightsButton;

    TextView debugText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //LaserButton = (ToggleButton) findViewById(R.id.LaserSwitch);
        //LightsButton = (ToggleButton) findViewById(R.id.LightsSwitch);
        debugText = (TextView) findViewById(R.id.textView);

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {          //if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);              //keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string

                        //TextView text = (TextView)findViewById(R.id.textView6);
                        //text.setText(dataInPrint);
                        recDataString.delete(0, recDataString.length());      //clear all string data
                        // strIncom =" ";
                        dataInPrint = " ";
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

        //textView1 = (TextView)findViewById(R.id.textView1);
        //textView2 = (TextView)findViewById(R.id.textView2);

        layout_joystick_left = (RelativeLayout)findViewById(R.id.layout_joystick_left);

        js_left = new JoyStickClass(getApplicationContext()
                , layout_joystick_left, R.drawable.image_button);
        js_left.setStickSize(150, 150);
        js_left.setLayoutSize(500, 500);
        js_left.setLayoutAlpha(150);
        js_left.setStickAlpha(100);
        js_left.setOffset(90);
        js_left.setMinimumDistance(50);
        
        layout_joystick_left.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                js_left.drawStick(arg1);
                //if(arg1.getAction() == MotionEvent.ACTION_DOWN
                 //       || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                    //int x = js_left.getX();
                    //x = x > 255 ? 255 : x < -255 ? -255 : x;
                    //int y = js_left.getY();
                    //y = y > 255 ? 255 : y < -255 ? -255 : y;
                    //textView1.setText("X : " + String.valueOf(x));
                    //textView2.setText("Y : " + String.valueOf(y));
                    //mConnectedThread.write("X: " + String.valueOf(x) + "\n" + "Y: " + String.valueOf(y) + "\n");
                //} //else if(arg1.getAction() == MotionEvent.ACTION_UP) {
                    //textView1.setText("X :");
                    //textView2.setText("Y :");
                //}
                return true;
            }
        });

        layout_joystick_right = (RelativeLayout)findViewById(R.id.layout_joystick_right);
        
        js_right = new JoyStickClass(getApplicationContext(), layout_joystick_right, R.drawable.image_button);
        js_right.setStickSize(150, 150);
        js_right.setLayoutSize(500, 500);
        js_right.setLayoutAlpha(150);
        js_right.setStickAlpha(100);
        js_right.setOffset(90);
        js_right.setMinimumDistance(50);
        
        layout_joystick_right.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                js_right.drawStick(event);
                //if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE){
                    //int x = js_right.getX();
                    //x = x > 255 ? 255 : x < -255 ? -255 : x;
                    //int y = js_right.getY();
                    //y = y > 255 ? 255 : y < -255 ? -255 : y;

                    //mConnectedThread.write("X': " + String.valueOf(x) + "\n" + "Y': " + String.valueOf(y) + "\n");
                //}
                return true;
            }
        });

        final Handler ha = new Handler();
        ha.postDelayed(new Runnable() {
            @Override
            public void run ()
            {
                //int x = js_right.getX();
                //x = x > 255 ? 255 : x < -255 ? -255 : x;
                //int y = js_right.getY();
                //y = y > 255 ? 255 : y < -255 ? -255 : y;

                //mConnectedThread.write("X': " + String.valueOf(x) + "\n" + "Y': " + String.valueOf(y) + "\n");
                SendJoystickPos();
                ha.postDelayed(this, tick_every_ms);
            }
        }, tick_every_ms);
    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        //Log.i("ramiro", "adress : " + address);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        //mConnectedThread.write("x");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);         //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                //Toast.makeText(getBaseContext(), "La conexión falló", Toast.LENGTH_LONG).show();
                finish();
            }
        }

        public void writeByteArray(byte[] input) {
            try {
                mmOutStream.write(input);
            } catch (IOException e) {
                //Toast.makeText(getBaseContext(), "La conexión falló", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    public void SendJoystickPos()
    {
        /////// -------------- Left Joystick ----------------------- /////////////
        // tranform the input pf the joystick to differential steering

        // Inputs
        int nJoyX = js_left.getX();                                 // get input
        nJoyX = nJoyX > 255 ? 255 : nJoyX < -255 ? -255 : nJoyX;    // clamp
        int nJoyY = js_left.getY();
        nJoyY = nJoyY > 255 ? 255 : nJoyY < -255 ? -255 : nJoyY;
        // (-128..+127)

        //nJoyX = Math.round((255 * ((-10/(Math.abs(nJoyX) + 10))+1) + 9.623f));
        //nJoyY = Math.round((255 * ((-10/(Math.abs(nJoyY) + 10))+1) + 9.623f));

        nJoyX /= 2;
        nJoyY /= 2;

        // OUTPUTS
        int mMotMixL;
        int mMotMixR;

        float fPivYLimit = 128.0f;

        // TEMP variables
        float mMotPremixL;
        float mMotPremixR;
        int nPivSpeed;
        float fPivScale;

        // Drive turn due to x axis
        if (nJoyY >= 0)
        {
            mMotPremixL = (nJoyX>=0.0f) ? 127.0f : (127.0f + nJoyX);
            mMotPremixR = (nJoyX>=0.0f) ? (127.0f - nJoyX) : 127.0f;
        }
        else
        {
            mMotPremixL = (nJoyX>=0.0f) ? (127 - nJoyX) : 127.0f;
            mMotPremixR = (nJoyX>=0.0f) ? 127.0f : (127.0f + nJoyX);
        }

        //Scale Drive output due to y axis
        mMotPremixL = mMotPremixL * nJoyY/128.0f;
        mMotPremixR = mMotPremixR * nJoyY/128.0f;

        // calculate the amount of pivot
        nPivSpeed = -nJoyX;
        fPivScale = (Math.abs(nJoyY)>fPivYLimit) ? 0.0f : (1.0f - Math.abs(nJoyY)/fPivYLimit);

        // calculate the final mix of drive and pivot
        mMotMixL = Math.round((1.0f - fPivScale)*mMotPremixL + fPivScale*(-nPivSpeed));
        mMotMixR = Math.round((1.0f - fPivScale)*mMotPremixR + fPivScale*( nPivSpeed));

        byte msg_leftMotor = (byte)mMotMixL;
        byte msg_rightMotor = (byte)mMotMixR;
        byte msg_other = 0;     // fire, lights, laser


        ///////////// ------------------- Right joystick ------------------------ //////////////

        int rJoyX = js_right.getX();
        rJoyX = rJoyX > 255 ? 255 : rJoyX < -255 ? -255 : rJoyX;
        int rJoyY = js_right.getY();
        rJoyY = rJoyY > 255 ? 255 : rJoyY < -255 ? -255 : rJoyY;

        byte msg_horizServo = (byte)(rJoyX / 2);
        byte msg_vertServo = (byte)(rJoyY / 2);

        byte[] byteArray = new byte[] {'@', msg_leftMotor, msg_rightMotor, msg_horizServo, msg_vertServo};
        String msg = new String(byteArray);
        debugText.setText("X: " + msg_leftMotor + ", Y: " + msg_rightMotor + "\n" + "X': " + msg_horizServo + ", Y': " + msg_vertServo);
        mConnectedThread.writeByteArray(byteArray);
    }

    public void Fire(View view)
    {
        mConnectedThread.writeByteArray(new byte[] {'F'});
    }

    public void ToggleLaser(View view)
    {
        boolean on = ((ToggleButton) view).isChecked();

        if (on)
            mConnectedThread.writeByteArray(new byte[] {'L'});
        else
            mConnectedThread.writeByteArray(new byte[] {'l'});
    }
    public void ToggleLights(View view)
    {
        boolean on = ((ToggleButton) view).isChecked();

        if (on)
            mConnectedThread.writeByteArray(new byte[] {'A'});
        else
            mConnectedThread.writeByteArray(new byte[] {'a'});
    }

}