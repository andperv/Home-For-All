package com.example.homeforall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import static com.example.homeforall.AppConstants.ANIMAL_DB_ADAPTER;
import java.util.ArrayList;


public class ProfileActivity extends AppCompatActivity implements AdAdapter.OnImageListener,AdAdapter.OnDeleteButListener,AdAdapter.OnEditButListener {

    private AdAdapter adAdapter;
    private ArrayList<Ad> adList;
    private DatabaseReference adRef, userAdRef, userRef;
    private FirebaseAuth auth;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Button adoptBut;
        adoptBut = findViewById(R.id.adoptButton);
        Button passBut;
        passBut = findViewById(R.id.deliverToAdoptButton);
        RecyclerView recyclerView;
        recyclerView = findViewById(R.id.profileRecycleView);
        adList = new ArrayList<Ad>();
        adAdapter = new AdAdapter(this,this,this, adList);
        RecyclerView.LayoutManager layoutManager;
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adAdapter);
        //Firebase initialization
        auth = FirebaseAuth.getInstance();
        String userID = auth.getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        userAdRef = userRef.child("user_ads");
        adRef = FirebaseDatabase.getInstance().getReference().child("ads");
         passBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, AdCreationActivity.class);
                startActivity(intent);
            }
        });
        adoptBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, AdoptActivity.class);
                startActivity(intent);
            }
        });
        addListener();
        getUser();
    }

    //Creating Menu for sign out and edit user data items
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Sign out item
            case R.id.logOut:
                auth.signOut();
                finishAffinity();
                startActivity(new Intent(this, LoginActivity.class));
                return true;
             //Edit user data item transfer to SignUp Activity
            case R.id.changeProfileData:
                Intent intent = new Intent(this, SignUpActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }

    }

    //Initialising a recycle view onCreate method
    private void addListener() {
        ChildEventListener adListener;
        adListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final String adId = dataSnapshot.getKey();
                final String animal = dataSnapshot.getValue(String.class);
                adRef.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get data from DB
                        Ad ad = dataSnapshot.child(animal).child(adId).getValue(Ad.class);
                        //Insert to list
                        adList.add(0, ad);
                        //Notify a Recycle View
                        adAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
              /*  String key = dataSnapshot.getKey();
                String animal = dataSnapshot.getValue(String.class);
                adRef.child(animal).child(key).removeValue();
                //
                int position = 0;
                for (Ad ad : adList) {
                    if (key.equals(ad.getAdId())) {
                        adList.remove(position);
                        adAdapter.notifyItemRemoved(position);
                        adAdapter.notifyItemRangeChanged(position, adList.size());
                        adAdapter.notifyDataSetChanged();
                        break;
                    }
                    position++;

                }*/
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }


        };
        userAdRef.addChildEventListener(adListener);
    }
    //Get user class from DB
    private void getUser() {

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // AdAdapter.OnImageListener interface method override
    // Calling to a method when user click on image in RecycleView item
    public void onImageClick(int position) {
        //Fragment creation a sending array list of images
        ViewPagerFragment viewPagerFragment=new ViewPagerFragment();
        Bundle args=new Bundle();
        args.putStringArrayList("imageList",adList.get(position).getImageUrlList());
        viewPagerFragment.setArguments(args);
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.profileActivity,viewPagerFragment);
        fragmentTransaction.commit();
    }

    // AdAdapter.OnDeleteButListener interface method override
    // Calling to a method when user click on delete image in RecycleView item
    public void onDeleteClick(final int position) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage("האם ברצונך למחוק את ההודעה?")

                .setPositiveButton("כן", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Ad ad=adList.get(position);
                        //Delete reference from users ads list
                        adList.remove(position);
                        adAdapter.notifyDataSetChanged();
                        userAdRef.child(ad.getAdId()).removeValue();
                        //Delete reference from ads list
                        adRef.child(ANIMAL_DB_ADAPTER[ad.getAnimalIndex()]).child(ad.getAdId()).removeValue();
                        //Delete images from storage
                        if(ad.getImageUrlList()!=null){
                            ArrayList<String> imageList=ad.getImageUrlList();
                            FirebaseStorage storage=FirebaseStorage.getInstance();
                            for(String url:imageList) {
                                StorageReference imageRef = storage.getReferenceFromUrl(url);
                                imageRef.delete();
                            }
                        }

                    }
                })
                .setNegativeButton("לא", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                })
                .show();
    }

    // AdAdapter.OnEditButListener interface method override
    // Calling to a method when user click on edit image in RecycleView item
    public void onEditClick(int position) {
       Ad ad =adList.get(position);
       //Transfer to sign up activity
       Intent intent=new Intent(ProfileActivity.this,AdCreationActivity.class);
       //Put a ad data
       intent.putExtra("Ad",ad);
       startActivity(intent);
    }
}




