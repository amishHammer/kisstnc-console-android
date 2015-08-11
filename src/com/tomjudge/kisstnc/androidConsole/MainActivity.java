package com.tomjudge.kisstnc.androidConsole;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getName();
    protected static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private static final int REQUEST_CONNECT_DEVICE = 1;
    protected static final int NEW_MESSAGE = 1;

    private BluetoothAdapter bluetoothAdapter = null;
    private TncConnection connection = null;
    private Handler handler;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogAdapter.reset(new LogAdapter());
        java.util.logging.Logger.getLogger("com.tomjudge.kisstnc").setLevel(Level.FINEST);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        createHandler();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        final EditText editText = (EditText) findViewById(R.id.edit_message);
        editText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage(null);
                    handled = true;

                }

                return handled;
            }

        });

    }

    private void createHandler() {
        // The Handler that gets information back from the BluetoothTncService
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case NEW_MESSAGE: {
                        String text = (String)msg.obj;
                        String date = dateFormat.format(new Date());
                        TextView log = (TextView) findViewById(R.id.log);
                        log.append(Html.fromHtml("<font color=\"gray\">"+date+" - </font>"));
                        log.append(Html.fromHtml("<font color=\"blue\">"+text+"</font><br/>"));
                        break;
                    }
                    default: {
                        
                    }
                }
            }
        };
    }

    public void sendMessage(View view) {
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String msg = editText.getText().toString();
        if (msg == null || "".equals(msg)) {
            return;
        }

        editText.setText("");
        TextView log = (TextView) findViewById(R.id.log);

        String date = dateFormat.format(new Date());
        log.append(Html.fromHtml("<font color=\"gray\">" + date + " - </font>"));
        log.append(Html.fromHtml("<font color=\"red\">" + msg + "</font><br/>"));
        connection.sendMessage(msg);
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(editText.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connect: {
                Log.d(TAG, "Starting connect activity");
                Intent i = new Intent(this, ConnectDialog.class);
                startActivityForResult(i, REQUEST_CONNECT_DEVICE);
                break;
            }
            default: {

            }
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE: {
                // When DeviceListActivity returns with a device to connect
                if (resultCode == RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(ConnectDialog.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    //mTncService.connect(device);
                    connection = new TncConnection(device, handler);
                    connection.start();
                }
                break;
            }
        }
    }
}
