package com.example.csankaran.digitalsignin;

// imports
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;

import java.util.Calendar;
import java.util.jar.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    //variables
    private DrawerLayout nDrawerLayout;
    private ActionBarDrawerToggle nToggle;
    private Toolbar nToolbar;

    //firebase auth object
    private FirebaseAuth firebaseAuth;

    //view objects
    private TextView textViewUserEmail;
    private Button buttonLogout;

    private DatabaseReference databaseReference;

    private TextView editTextSignIn;
    private TextView editTextSignOut;

    Button btnShowLocation;
    GPSTracker gps;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // GPS Tracker Implementation (currently doesnt work)
       /*btnShowLocation = (Button) findViewById(R.id.btn_SignIn);

        btnShowLocation.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                gps = new GPSTracker(ProfileActivity.this);
                if(gps.canGetLocation()){
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    if(latitude == 0 && longitude == 0){
                        signIn(view);
                        Toast.makeText(getApplicationContext(), "GPS in range", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "GPS not in range", Toast.LENGTH_LONG).show();
                    }
                }else {
                    gps.showSettingsAlert();
                }
            }
        });*/

        nToolbar = (Toolbar) findViewById(R.id.nav_action);
        setSupportActionBar(nToolbar);

        nDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        nToggle = new ActionBarDrawerToggle(this, nDrawerLayout, R.string.open, R.string.close);

        nDrawerLayout.addDrawerListener(nToggle);
        nToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //initializing firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();

        //if the user is not logged in, then the current user will return null
        if (firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        //Updates the database on create so that the on data change function will run
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Status");
        myRef.setValue("Updating...");
        //All of the data that is put into the data base will be under a "Data" node
        databaseReference = FirebaseDatabase.getInstance().getReference("Data");

        //The times for sign in and sign out from the XML
        editTextSignIn = (TextView) findViewById(R.id.signInTime);
        editTextSignOut = (TextView) findViewById(R.id.signOutTime);

        //Gets the logged in user
        FirebaseUser user = firebaseAuth.getCurrentUser();

        //The email of the user is defined
        textViewUserEmail = (TextView) findViewById(R.id.textViewUserEmail);
        textViewUserEmail.setText(user.getEmail());

        // Used to set the time of the clock
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            public void run() {
                TextView time = (TextView) findViewById(R.id.clock_Time);
                Calendar c = Calendar.getInstance();
                int hours = c.get(Calendar.HOUR);
                int minutes = c.get(Calendar.MINUTE);
                int amOrPm = c.get(Calendar.AM_PM);
                if (hours == 0) {
                    hours = 12;
                }
                String output;
                if (amOrPm == 1) {
                    if (minutes < 10) {
                        if (hours == 0) {
                            output = 12 + ":0" + minutes + " PM";
                        } else {
                            output = hours + ":0" + minutes + " PM";
                        }
                    } else {
                        if (hours == 0) {
                            output = 12 + ":" + minutes + " PM";
                        } else {
                            output = hours + ":" + minutes + " PM";
                        }
                    }
                } else {
                    if (minutes < 10) {
                        if (hours == 0) {
                            output = 12 + ":0" + minutes + " AM";
                        } else {
                            output = hours + ":0" + minutes + " AM";
                        }
                    } else {
                        if (hours == 0) {
                            output = 12 + ":" + minutes + " AM";
                        } else {
                            output = hours + ":" + minutes + " AM";
                        }
                    }
                }
                time.setText(output);
                h.postDelayed(this, 100);
            }
        }, 100);

        // end of clock setting

        //Querying through the database to find the information that is needed

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                //Query through all of the data that is within the User ID and retrieving the time back
                Query queryRef = databaseReference.orderByChild(user.getUid().toString() + "/time");


                queryRef.addChildEventListener(new ChildEventListener() {
                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String previousChild) {
                        //Setting variables that will be needed to get the information back
                        Button sign = (Button) findViewById(R.id.btn_SignIn);
                        editTextSignIn = (TextView) findViewById(R.id.signInTime);
                        Calendar c = Calendar.getInstance();
                        int month = c.get(Calendar.MONTH) + 1;
                        int day = c.get(Calendar.DAY_OF_MONTH);
                        int year = c.get(Calendar.YEAR);
                        String date2 = month + "/" + day + "/" + year;
                        //Will take a snapshot of the database at this current time
                        UserInformation u = dataSnapshot.getValue(UserInformation.class);
                        /*If the current user and the saved user is the same,
                            today's date and the saved date is the same
                            the user has signed in
                            the user has signed out
                          Then set the sign in time to the saved time*/

                        if (user.getEmail().equals(u.getName()) && date2.equals(u.getDate()) && u.getSignin() == true && u.getSignout() == false) {
                            editTextSignIn.setText(u.getTime());
                            sign.setText("Sign Out");
                        }

                        /*If the current user and the saved user is the same,
                            today's date and the saved date is the same
                            the user has signed out
                        Then set the sign out time to the saved time
                        make the sign in/out button so that it cannot be clicked on*/

                        if (user.getEmail().equals(u.getName()) && date2.equals(u.getDate()) && u.getSignin() == false && u.getSignout() == true) {
                            editTextSignOut.setText(u.getTime());
                            sign.setBackgroundColor(Color.GRAY);
                            sign.setEnabled(false);
                            TextView sI = (TextView) findViewById(R.id.signInTime);
                            TextView sO = (TextView) findViewById(R.id.signOutTime);
                            TextView time = (TextView) findViewById(R.id.Time);
                            String start = (String) sI.getText();
                            String end = (String) sO.getText();
                            int sPos = start.indexOf(":");
                            int sHours = Integer.parseInt(start.substring(0, sPos));
                            int sMins = Integer.parseInt(start.substring(sPos + 1, sPos + 3));
                            String sAMPM = start.substring(start.length() - 2, start.length());
                            int ePos = end.indexOf(":");
                            int eHours = Integer.parseInt(end.substring(0, ePos));
                            int eMins = Integer.parseInt(end.substring(ePos + 1, ePos + 3));
                            String eAMPM = end.substring(end.length() - 2, end.length());
                            int tMinutes;
                            if (sAMPM.equals(eAMPM)) {
                                tMinutes = eHours * 60 - sHours * 60 + eMins - sMins;
                            } else {
                                if (eHours == 12) {
                                    tMinutes = eHours * 60 - sHours * 60 + eMins - sMins;
                                } else {
                                    tMinutes = 720 + eHours * 60 - sHours * 60 + eMins - sMins;
                                }
                            }
                            time.setText(tMinutes / 60 + " hours and " + tMinutes % 60 + " minutes");
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // For menu side bar
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation_menu, menu);
        return true;
    }

    // Opens different activities on menu item clicks
    public void onGroupItemClick (MenuItem item){
        System.out.println(item.getItemId());
        if (item.getItemId() == 2131558583) {
            startActivity(new Intent(this, ViewDatabaseActivity.class));
        }
        if (item.getItemId() == 2131558584) {
            firebaseAuth.signOut();
            Toast.makeText(this, "Signing Out...", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    // When menu group is clicked call method for menu item
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(nToggle.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //This will show what happens when the user clicks sign in or sign out
    // Sets time, and changes button text
    public void signIn(View view) {
        Button sign = (Button) findViewById(R.id.btn_SignIn);
        TextView sI = (TextView) findViewById(R.id.signInTime);
        TextView sO = (TextView) findViewById(R.id.signOutTime);
        TextView time = (TextView) findViewById(R.id.Time);
        Calendar c = Calendar.getInstance();
        int hours = c.get(Calendar.HOUR);
        int minutes = c.get(Calendar.MINUTE);
        int amOrPm = c.get(Calendar.AM_PM);
        if (sign.getText().equals("Sign In") && sI.getText().equals("Sign In:")) {
            String output;
            if (amOrPm == 1) {
                if (minutes < 10) {
                    if (hours == 0) {
                        output = 12 + ":0" + minutes + " PM";
                    } else {
                        output = hours + ":0" + minutes + " PM";
                    }
                } else {
                    if (hours == 0) {
                        output = 12 + ":" + minutes + " PM";
                    } else {
                        output = hours + ":" + minutes + " PM";
                    }
                }
            } else {
                if (minutes < 10) {
                    if (hours == 0) {
                        output = 12 + ":0" + minutes + " AM";
                    } else {
                        output = hours + ":0" + minutes + " AM";
                    }
                } else {
                    if (hours == 0) {
                        output = 12 + ":" + minutes + " AM";
                    } else {
                        output = hours + ":" + minutes + " AM";
                    }
                }
            }
            sI.setText(output);
            sign.setText("Sign Out");
        } else if (sign.getText().equals("Sign Out") && sO.getText().equals("Sign Out:")) {
            String output;
            if (amOrPm == 1) {
                if (minutes < 10) {
                    if (hours == 0) {
                        output = 12 + ":0" + minutes + " PM";
                    } else {
                        output = hours + ":0" + minutes + " PM";
                    }
                } else {
                    if (hours == 0) {
                        output = 12 + ":" + minutes + " PM";
                    } else {
                        output = hours + ":" + minutes + " PM";
                    }
                }
            } else {
                if (minutes < 10) {
                    if (hours == 0) {
                        output = 12 + ":0" + minutes + " AM";
                    } else {
                        output = hours + ":0" + minutes + " AM";
                    }
                } else {
                    if (hours == 0) {
                        output = 12 + ":" + minutes + " AM";
                    } else {
                        output = hours + ":" + minutes + " AM";
                    }
                }
            }
            sO.setText(output);
            String start = (String) sI.getText();
            String end = (String) sO.getText();
            int sPos = start.indexOf(":");
            int sHours = Integer.parseInt(start.substring(0, sPos));
            int sMins = Integer.parseInt(start.substring(sPos + 1, sPos + 3));
            String sAMPM = start.substring(start.length() - 2, start.length());
            int ePos = end.indexOf(":");
            int eHours = Integer.parseInt(end.substring(0, ePos));
            int eMins = Integer.parseInt(end.substring(ePos + 1, ePos + 3));
            String eAMPM = end.substring(end.length() - 2, end.length());
            int tMinutes;
            if (sAMPM.equals(eAMPM)) {
                tMinutes = eHours * 60 - sHours * 60 + eMins - sMins;
            } else {
                if(eHours == 12){
                    tMinutes = eHours * 60 - sHours * 60 + eMins - sMins;
                }else {
                    tMinutes = 720 + eHours * 60 - sHours * 60 + eMins - sMins;
                }
            }
            time.setText(tMinutes / 60 + " hours and " + tMinutes % 60 + " minutes");
            sign.setText("Sign In");
        }
        //It will then go to persisting the data
        saveUserInformation();
    }


    //This will persist the data into the Firebase Database
    private void saveUserInformation() {
        //Setting variables that will be needed to save the information into the databasae
        TextView time = (TextView) findViewById(R.id.clock_Time);
        String name = textViewUserEmail.getText().toString().trim();
        String Time = time.getText().toString();
        Calendar c = Calendar.getInstance();
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int year = c.get(Calendar.YEAR);
        String date = month + "/" + day + "/" + year;

        Button sign = (Button) findViewById(R.id.btn_SignIn);

        //set both sign in and sign out booleans to initially be false
        boolean SignIn = false;
        boolean SignOut = false;

        /*
        If the button text has been changed to sign out, then that must mean the user has already signed in
        Change booleans to match
         */
        if (sign.getText().equals("Sign Out")) {
            SignIn = true;
            SignOut = false;

            /*
        If the button text has been changed to sign in, then that must mean the user has already signed in
        and signed out
        Change booleans to match
        Give a toast to the user telling them they have successfully sign in and out for the day
         */
        } else if (sign.getText().equals("Sign In")) {
            SignIn = false;
            SignOut = true;
            Toast.makeText(this, "Time has been saved", Toast.LENGTH_LONG).show();
            sign.setBackgroundColor(Color.GRAY);
            sign.setEnabled(false);
            Toast.makeText(this, "You have signed out. Please wait till the next day.", Toast.LENGTH_LONG).show();

        }
        //Creates an object userInformation from the java class UserInformation with the correct parameters
        UserInformation userInformation = new UserInformation(name, Time, SignIn, SignOut, date);

        //Persists the UserInformaton data into Firebase under the proper node of "Data"
        databaseReference.push().setValue(userInformation);
    }
}
