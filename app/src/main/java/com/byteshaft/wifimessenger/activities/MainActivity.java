package com.byteshaft.wifimessenger.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.byteshaft.wifimessenger.R;
import com.byteshaft.wifimessenger.services.LongRunningService;
import com.byteshaft.wifimessenger.utils.AppGlobals;
import com.byteshaft.wifimessenger.utils.MessagingHelpers;
import com.byteshaft.wifimessenger.utils.ServiceHelpers;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements ListView.OnItemClickListener, View.OnClickListener,
        EditText.OnFocusChangeListener {
    //fields
    private LinearLayout layoutUsername;
    private RelativeLayout layoutMain;
    private LinearLayout layoutMainTwo;
    private EditText editTextUsername;
    private TextView showUsername;
    private ListView peerList;

    private static MainActivity sInstance;

    public static boolean isRunning() {
        return sInstance != null;
    }

    public static MainActivity getInstance() {
        return sInstance;
    }

    public void switchService(boolean on) {
//        serviceSwitch.setChecked(on);
        AppGlobals.setService (on);
    }

    // called when activity first starts up
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //on create is called,extends parent class
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);


        sInstance = this;
//       startActivity(new Intent(this, MessagesListActivity.class));
        layoutUsername = (LinearLayout) findViewById(R.id.layout_username);
        layoutMain = (RelativeLayout) findViewById(R.id.layout_main);
        layoutMainTwo = (LinearLayout) findViewById(R.id.layout_main_two);
        //check here
        showUsername = (TextView) findViewById(R.id.tv_username);
        peerList = (ListView) findViewById(R.id.lv_peer_list);
        peerList.setOnItemClickListener(this);

        
//  if appglobals is virgin, hides mainlayout, sets username visible,captures username
        // else notvirgin
        if (AppGlobals.isVirgin()) {
            //hides main layout
            layoutMain.setVisibility(View.GONE);
            //username is set as visible in second interface
            layoutUsername.setVisibility(View.VISIBLE);
            //checks xml file to get username and display it
            editTextUsername = (EditText) findViewById(R.id.editTextDisplayName);
            //sets listener from edittextdisplayname
            editTextUsername.setOnFocusChangeListener(this);
            // xml button, capture button from layout
            Button startButton = (Button) findViewById(R.id.buttonStart);
            //listener is set , when button is clicked, operation starts
            startButton.setOnClickListener(this);
        } else {
            notVirgin();
        }
    }

    //called when we override or create an own activity
    @Override
    protected void onPause() {
        super.onPause();
        ServiceHelpers.stopDiscover ();
    }

    @Override
    protected void onResume() {
        super.onResume ();
        if (!AppGlobals.isVirgin() && AppGlobals.isServiceOn() && !ServiceHelpers.DISCOVER &&
                LongRunningService.isRunning()) {

            ServiceHelpers.discover (MainActivity.this, peerList);
        }
    }


    //PENDING
    // GET LIST OF AVAILABLE PEERS connected to same Wifi network
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (!ServiceHelpers.isPeerListEmpty()) {
            ArrayList<HashMap> peers = ServiceHelpers.getPeersList();
            String name = parent.getItemAtPosition(position).toString();
            String ipAddress = (String) peers.get(position).get("ip");
            String userTable = (String) peers.get(position).get("user_table");
//            showActionsDialog(name, ipAddress, userTable);
            openChatActivity (name, ipAddress, userTable);
        }
    }


    //PENDING
    private void notVirgin() {
        layoutMain.setVisibility (View.VISIBLE);
        layoutUsername.setVisibility (View.GONE);
        AppGlobals.setVirgin (false);
        toggleUserName ();
        startDiscoverService ();
      //  showUsername.setText("Username: " + AppGlobals.getName());
     //   serviceSwitch.setChecked(AppGlobals.isServiceOn());
     //   if (AppGlobals.isServiceOn()) {
       //     layoutMainTwo.setVisibility(View.VISIBLE);
       //     showUsername.setTextColor(Color.parseColor("#4CAF50"));
       // } else {
       //     layoutMainTwo.setVisibility(View.GONE);
       //     showUsername.setTextColor(Color.parseColor("#F44336"));
      //  }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow (view.getWindowToken (), 0);
    }


    //This class is used to instantiate menu XML files into Menu objects.

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (AppGlobals.isVirgin ()){
            return false;
        }
        else {//populate menu if not virgin
            getMenuInflater().inflate (R.menu.menu_main, menu);
            
            if (AppGlobals.isServiceOn()) {
                //for the user
                menu.findItem(R.id.action_refresh).setVisible(true);
                menu.findItem (R.id.action_switchService).setChecked (true);
            } else {
                menu.findItem(R.id.action_refresh).setVisible(false);
                menu.findItem (R.id.action_switchService).setChecked (false);
            }

            if (TextUtils.isEmpty (showUsername.getText ())) {
                menu.findItem (R.id.action_toggleUserName).setChecked (false);
            }
            else {
                menu.findItem (R.id.action_toggleUserName).setChecked (true);
            }
            return true;
        }

    }

    private void toggleUserName(){
        if (TextUtils.isEmpty (showUsername.getText ())){
            if (AppGlobals.isServiceOn ()){
                showUsername.setTextColor (Color.parseColor("#4CAF50"));
            }
            else {
                showUsername.setTextColor (Color.parseColor ("#F44336"));
            }
            showUsername.setText ("Username: " + AppGlobals.getName ());
        }
        else {
            showUsername.setText ("");
        }
        invalidateOptionsMenu ();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // when settings is clicked, the user gets visible on mainlayout

        if (id == R.id.action_toggleUserName){
            Log.d ("debug", "toggle username clicked .........");
            toggleUserName ();
        }

        if (id == R.id.action_refresh) {
            peerList.setAdapter(null);
            if (!ServiceHelpers.DISCOVER) {
                ServiceHelpers.discover (MainActivity.this, peerList);

            }
            return true;
        }

        if (id == R.id.action_switchService){
            if (AppGlobals.isServiceOn ()){
                stopDiscoverService ();
            }
            else {
                startDiscoverService ();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
//on click, it reads data from text box, if empty, shows invalid notification,
    //first interface, we get an edit text view to enter a username
    //next, the button is to be pressed
    // once the button is pressed, it inputs the username
    //if the username length is less than 1, invalid username notification appears
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonStart:
                Log.i("wifiMessenger", "Start button pressed");
                String username = editTextUsername.getText().toString();
                if (username.trim().length() < 1) {
                    Toast.makeText(getApplicationContext(), "Invalid Username", Toast.LENGTH_SHORT).show();
                } else {
                    AppGlobals.putName(username);
                    notVirgin();
                }
                break;
        }
    }

//called when the focus state of a view has changed
    //called when user navigates onto or away from item
    //called when view gains or loses focus
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.editTextDisplayName:
                if (!hasFocus) {
                    hideKeyboard (v);
                }
                break;
        }
    }

    private void openChatActivity(String username, String ipAddress, String userTable){
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        //this is used to bring the chat activity to foreground
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

        intent.putExtra("CONTACT_NAME", username);
        intent.putExtra("IP_ADDRESS", ipAddress);
        intent.putExtra("user_table", userTable);
        startActivity(intent);
    }

    private void startDiscoverService(){
        startService (new Intent (getApplicationContext (), LongRunningService.class));
        layoutMainTwo.setVisibility (View.VISIBLE);
        AppGlobals.setService (true);
        showUsername.setTextColor (Color.parseColor ("#4CAF50"));
        ServiceHelpers.discover (MainActivity.this, peerList);
        invalidateOptionsMenu ();
        Toast.makeText (this, "Start discovering..", Toast.LENGTH_SHORT).show ();
    }

    private void stopDiscoverService(){
        stopService(new Intent(getApplicationContext(), LongRunningService.class));
        layoutMainTwo.setVisibility (View.GONE);
        AppGlobals.setService (false);
        showUsername.setTextColor (Color.parseColor ("#F44336"));
        ServiceHelpers.stopDiscover ();
        invalidateOptionsMenu ();
        Toast.makeText (this, "Stop discovering..", Toast.LENGTH_SHORT).show ();
    }

}
