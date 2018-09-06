package com.example.csankaran.digitalsignin;

// Class for My Activity page

// imports
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ViewDatabaseActivity extends AppCompatActivity {

    // variables
    //firebase auth object
    private FirebaseAuth firebaseAuth;

    private ListView mListView;

    private ArrayList<String> mData = new ArrayList<>();

    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_database);

        mListView = (ListView) findViewById(R.id.listview);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mData);

        mListView.setAdapter(arrayAdapter);

        //initializing firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();

        //Updates the database on create so that the on data change function will run
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Status");
        myRef.setValue("Updating...");
        //All of the data that is put into the data base will be under a "Data" node
        databaseReference = FirebaseDatabase.getInstance().getReference("Data");


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
                        //Will take a snapshot of the database at this current time
                        UserInformation u = dataSnapshot.getValue(UserInformation.class);
                        /*If the current user and the saved user is the same,
                            Print all the data relevant to the user
                         */
                        if (user.getEmail().equals(u.getName())){
                            mData.add(u.getDate());
                            if(u.getSignin() == true) {
                                mData.add("     Signed In At: " + u.getTime());
                            }
                            else{
                                mData.add("     Signed Out At: " + u.getTime());
                            }
                        }


                        arrayAdapter.notifyDataSetChanged();

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
}