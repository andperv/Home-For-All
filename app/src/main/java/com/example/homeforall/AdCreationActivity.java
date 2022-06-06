package com.example.homeforall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.ArrayList;
import static com.example.homeforall.AppConstants.*;


//  Ad Creation Activity activity has 2 uses
//  First is to create a new ad
//  Second to edit user ad from a profile activity(edit mode)
public class AdCreationActivity extends AppCompatActivity implements ImageAdapter.OnDeleteButtonListener,ImageAdapter.DbImageCount {
    private static final int PIC_IMAGE_FROM_LOCAL_STORAGE = 100;
    private ArrayList<Uri> imageUrlList;
    //Adapter List
    private ArrayList<String> imageList;
    private Ad editAd;
    private Ad newAd;
    private Spinner animalSpin;
    private Spinner animalBreedSpin;
    private Spinner locationSpin;
    private Button addPhotoBut;
    private Button createEditAdBut;
    private EditText descriptionEditTxt;
    private RadioGroup genderRadioGroup;
    private ProgressBar progressBar;
    private RecyclerView imageRecycleView;
    private ImageAdapter imageAdapter;
    private DatabaseReference adsDbRef;
    private DatabaseReference userDbRef;
    private StorageReference imageReference;
    private int numOfUploadImg;
    private Runnable adUploading;
    private final Object monitor = new Object();
    private Thread uploadThread;
    private int initPictureNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advert);
        imageRecycleView = (RecyclerView) findViewById(R.id.photoRecycleView);
        animalSpin = (Spinner) findViewById(R.id.animalSpinner);
        animalBreedSpin = (Spinner) findViewById(R.id.animalBreedSpinner);
        locationSpin = (Spinner) findViewById(R.id.locationSpinner);
        addPhotoBut = findViewById(R.id.addPhotoButton);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        descriptionEditTxt = findViewById(R.id.animalDescriptionEditTxt);
        progressBar = findViewById(R.id.progressBarAdCreate);
        createEditAdBut = findViewById(R.id.createAdButton);
        Intent intent = getIntent();
        editAd = (Ad) intent.getSerializableExtra("Ad");
        //Spinner Adapters
        ArrayAdapter<String> animalAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ANIMALS);
        animalSpin.setAdapter(animalAdapter);
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, LOCATION);
        locationSpin.setAdapter(locationAdapter);
        //Recycle View
        imageUrlList = new ArrayList<>();
        imageAdapter = new ImageAdapter(imageUrlList, this, this, editAd != null);
        imageList = new ArrayList<>();
        imageRecycleView.setAdapter(imageAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        imageRecycleView.setLayoutManager(layoutManager);
        imageRecycleView.smoothScrollToPosition(imageAdapter.getItemCount());
        // Firebase
        FirebaseAuth auth = FirebaseAuth.getInstance();
        imageReference = FirebaseStorage.getInstance().getReference().child("Images");
        adsDbRef = FirebaseDatabase.getInstance().getReference().child("ads");
        userDbRef = FirebaseDatabase.getInstance().getReference().child("users").child(auth.getUid());
        //Listeners
        animalSpinnerListener();
        addPhotoListener();
        //New Ad mode
        if (editAd == null) {
            newAd = new Ad();
            getOwnerData(userDbRef);
        }
        //Edit Ad mode
        else {
            createEditAdBut.setText(R.string.edit_ad);
            insertData();
        }
        editTextSetOnKeyListener();
        createAdListener();
        adUploading = new Runnable() {
            @Override
            public void run() {
                uploadImageToStore();
                //New ad
                if (editAd == null) {
                    newAd.setAnimalIndex(animalSpin.getSelectedItemPosition());
                    newAd.setBreedIndex(animalBreedSpin.getSelectedItemPosition());
                    newAd.setLocationIndex(locationSpin.getSelectedItemPosition());
                    newAd.setGender(genderResult());
                    newAd.setDescription(descriptionEditTxt.getText().toString().trim().replaceAll("//s",""));
                    newAd.setDate();
                }
                //Edit Ad
                else {
                    editAd.setBreedIndex(animalBreedSpin.getSelectedItemPosition());
                    editAd.setLocationIndex(locationSpin.getSelectedItemPosition());
                    editAd.setGender(genderResult());
                    editAd.setDescription(descriptionEditTxt.getText().toString().trim().replaceAll("//s",""));
                    editAd.setDate();
                }
                //Insert Images to DB slows part of the code
                //numOfUploadImg!=0
                if (imageUrlList.size()>0){
                    try {

                        Thread.sleep(10000);

                    } catch (InterruptedException e) {
                        Log.d("Thread wake up", "Successfully wake up upload  thread");
                    }
               }
                synchronized (monitor) {
                    //New Ad
                    if (editAd == null) {
                        newAd.setImageUrlList(imageList);
                        DatabaseReference currentAdRef = adsDbRef.child(ANIMAL_DB_ADAPTER[animalSpin.getSelectedItemPosition()]).push();
                        newAd.setAdId(currentAdRef.getKey());
                        //Create ad in ads folder
                        currentAdRef.setValue(newAd);
                        //Firebase
                        //Create ad in user folder
                        userDbRef.child("user_ads").child(currentAdRef.getKey()).setValue(ANIMAL_DB_ADAPTER[animalSpin.getSelectedItemPosition()]);
                    }
                    //Exist Ad
                    else {
                        editAd.setImageUrlList(imageList);
                        //Replace a old add
                        adsDbRef.child(ANIMAL_DB_ADAPTER[editAd.getAnimalIndex()]).child(editAd.getAdId()).setValue(editAd);
                    }
                    Intent intent = new Intent(AdCreationActivity.this, ProfileActivity.class);
                    startActivity(intent);
                }
            }
        };
    }


    private void animalSpinnerListener() {
        animalSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            //Animal type spinner initialization depends on animal selection
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayAdapter<String> breedAdapter;
                switch (parent.getItemAtPosition(position).toString()) {

                    case ("חתולים"):
                        breedAdapter = new ArrayAdapter<String>(AdCreationActivity.this, android.R.layout.simple_spinner_item, CAT_BREED);
                        animalBreedSpin.setAdapter(breedAdapter);
                        break;
                    case ("ציפורים"):
                        breedAdapter = new ArrayAdapter<String>(AdCreationActivity.this, android.R.layout.simple_spinner_item, BIRD_BREED);
                        animalBreedSpin.setAdapter(breedAdapter);
                        break;
                    case ("דגים"):
                        breedAdapter = new ArrayAdapter<String>(AdCreationActivity.this, android.R.layout.simple_spinner_item, FISH_BREED);
                        animalBreedSpin.setAdapter(breedAdapter);
                        break;
                    case ("זוחלים"):
                        breedAdapter = new ArrayAdapter<String>(AdCreationActivity.this, android.R.layout.simple_spinner_item, REPTILE_BREED);
                        animalBreedSpin.setAdapter(breedAdapter);
                        break;
                    case ("מכרסמים"):
                        breedAdapter = new ArrayAdapter<String>(AdCreationActivity.this, android.R.layout.simple_spinner_item, RODENT_BREED);
                        animalBreedSpin.setAdapter(breedAdapter);
                        break;
                    case ("חיות משק"):
                        breedAdapter = new ArrayAdapter<String>(AdCreationActivity.this, android.R.layout.simple_spinner_item, FARM_ANIMALS_BREED);
                        animalBreedSpin.setAdapter(breedAdapter);
                        break;
                    default:
                        breedAdapter = new ArrayAdapter<String>(AdCreationActivity.this, android.R.layout.simple_spinner_item, DOG_BREED);
                        animalBreedSpin.setAdapter(breedAdapter);
                }
                //Edit mode
                if (editAd != null)
                    animalBreedSpin.setSelection(editAd.getBreedIndex());
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    //Add image button listener
    private void addPhotoListener() {
        addPhotoBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, PIC_IMAGE_FROM_LOCAL_STORAGE);
            }
        });

    }
    //Create Ad Or Edit Ad button listener
    private void createAdListener() {
        createEditAdBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //New ad
                disableChildren();
                progressBar.setVisibility(View.VISIBLE);
                uploadThread = new Thread(adUploading);
                uploadThread.start();

            }
        });

    }


    //Method return string answer from radio buttons
    private String genderResult() {
        int buttonID = genderRadioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) genderRadioGroup.findViewById(buttonID);
        String gender = (String) radioButton.getText();
        return gender;
    }

   //Override method to get images from the local storage
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PIC_IMAGE_FROM_LOCAL_STORAGE && resultCode == RESULT_OK) {
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri image = clipData.getItemAt(i).getUri();
                    if (duplicateValidation(image)) {
                        imageUrlList.add(image);
                        imageAdapter.notifyDataSetChanged();
                        //Scroll to the last one item
                        imageRecycleView.smoothScrollToPosition(imageAdapter.getItemCount());
                    }
                }
            }
            //User add only one image
            else {
                //Try to add same images next attempt(works only in same session)
                if (duplicateValidation(data.getData())) {
                    imageUrlList.add(data.getData());
                    imageAdapter.notifyDataSetChanged();
                    imageRecycleView.smoothScrollToPosition(imageAdapter.getItemCount());
                }
            }

        }


    }

    private void uploadImageToStore() {
        //New Ad
        if (editAd == null) {
            //Uploading a new Images
            numOfUploadImg = imageUrlList.size();
            for (Uri uri : imageUrlList) {
                final StorageReference imageRef = imageReference.child(uri.getLastPathSegment());
                UploadTask uploadTask = imageRef.putFile(uri);
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return imageRef.getDownloadUrl();

                    }
                });
                //Wakeup a sleeping upload thread
                urlTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        synchronized (monitor) {
                            imageList.add(uri.toString());
                            numOfUploadImg--;
                            if (numOfUploadImg == 0)

                                uploadThread.interrupt();

                        }
                    }
                });

            }
        }
        //Exist Ad with new added images
        else {
            //Number Of new Images user want to add
            numOfUploadImg = imageUrlList.size() - initPictureNum;
            for (int i = 0; i < imageUrlList.size(); i++) {
                if (i < initPictureNum) {
                    imageList.add(imageUrlList.get(i).toString());
                    continue;
                }
                //Add image to Storage
                Uri uri = imageUrlList.get(i);
                final StorageReference imageRef = imageReference.child(uri.getLastPathSegment());
                UploadTask uploadTask = imageRef.putFile(uri);
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return imageRef.getDownloadUrl();

                    }
                });
                //Upload thread is sleeping wake up a thread after finishing to upload a images
                urlTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        synchronized (monitor) {
                            imageList.add(uri.toString());
                            numOfUploadImg--;
                            if (numOfUploadImg == 0)
                                uploadThread.interrupt();

                        }
                    }
                });

            }

        }


    }
    //Edit ad mode insert data to fields
    private void insertData() {
        animalSpin.setSelection(editAd.getAnimalIndex());
        animalSpin.setEnabled(false);
        locationSpin.setSelection(editAd.getLocationIndex());
        descriptionEditTxt.setText(editAd.getDescription());
        ArrayList<String> tempImageList = editAd.getImageUrlList();
        if(tempImageList!=null) {
            for (String imagePath : tempImageList) {
                imageUrlList.add(Uri.parse(imagePath));
                imageAdapter.notifyDataSetChanged();
            }
            initPictureNum = tempImageList.size();
        }
        //Default value is MALE
        if (editAd.getGender().equals(R.string.female)) {
            RadioButton radioButton = findViewById(R.id.femaleRadioButton);
            radioButton.toggle();
        }

    }
    //Get owner connection data
    private void getOwnerData(DatabaseReference user) {
        user.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);
                String phoneNumber = currentUser.getPhone1() + "\n" + currentUser.getPhone2();
                newAd.setPhoneNumber(phoneNumber);
                newAd.setAdOwner(currentUser.getName());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    //Method check if user already added specific image
    private boolean duplicateValidation(Uri newImage) {
        if (imageUrlList.size() == 0)
            return true;
        for (Uri existImage : imageUrlList) {
            if (existImage.equals(newImage))
                return false;
        }
        return true;
    }
    //Method control a animal description length,max is 15 rows
    private void editTextSetOnKeyListener(){
        descriptionEditTxt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // if enter is pressed start calculating
                if (keyCode == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_UP) {

                    // get text from EditText
                    String description = ((EditText) v).getText().toString();

                    // find how many rows description contains
                    int editTextRowCount = description.split("\\n").length;

                    // user has input more than 15 rows
                    if (editTextRowCount > 14) {
                        // find the last break
                        int lastNewLineIndex = description.lastIndexOf("\n");
                        String newText = description.substring(0, lastNewLineIndex);
                        ((EditText) v).setText("");
                        ((EditText) v).append(newText);
                        Toast.makeText(AdCreationActivity.this,R.string.max_row_number_message,Toast.LENGTH_LONG).show();
                    }
                }
                return false;
            }
        });
    }


    // ImageAdapter.OnDeleteButtonListener interface method Override
    // Calling to a method when user click on close button in RecycleView item
    public void onDeleteButtonClick(int position, String uriPath) {
        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(uriPath);
        imageRef.delete();
        imageUrlList.remove(position);
        imageAdapter.notifyItemRemoved(position);
        imageAdapter.notifyDataSetChanged();
        initPictureNum--;
    }


    // ImageAdapter.DbImageCount interface method Override
    // return initial image num in Storage
    public int getImageNumInDB() {
        return initPictureNum;
    }

    //Disable UI to protect a app during access to DB
    private void disableChildren() {
        animalSpin.setEnabled(false);
        animalBreedSpin.setEnabled(false);
        locationSpin.setEnabled(false);
        addPhotoBut.setEnabled(false);
        createEditAdBut.setEnabled(false);
        descriptionEditTxt.setEnabled(false);
        imageRecycleView.setEnabled(false);
        genderRadioGroup.setEnabled(false);
        LinearLayout mainLayout=findViewById(R.id.mainLayoutAdCreation);
        mainLayout.setAlpha(0.5f);

    }
}
