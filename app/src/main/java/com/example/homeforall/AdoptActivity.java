package com.example.homeforall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import static com.example.homeforall.AppConstants.*;

public class AdoptActivity extends AppCompatActivity implements AdAdoptAdapter.OnImageListener{

    private Spinner animalSpin;
    private Spinner animalTypeSpin;
    private Spinner animalGenderSpin;
    private Spinner locationSpin;
    private Button expandSearchBar;
    private TextView noAdsTxt;
    private RecyclerView recyclerView;
    private AdAdoptAdapter adapter;
    private ArrayList<Ad> adList;
    private int  animalIndex,typeIndex,locationIndex,genderIndex;
    private DatabaseReference adRef;
    private boolean findAd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adopt);
        searchBarInit();
        noAdsTxt=findViewById(R.id.no_ads_txt);
        findAd=false;
        //RecycleView initialization
        recyclerView=findViewById(R.id.adoptRecycleView);
        adList=new ArrayList<>();
        adapter=new AdAdoptAdapter(this,adList,getSupportFragmentManager(),this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(adapter);
        adRef= FirebaseDatabase.getInstance().getReference().child("ads");


    }

    private void searchBarInit(){
        animalSpin=findViewById(R.id.animalSpinnerAdoptActivity);
        animalSpin.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,ANIMALS));
        animalTypeSpin=findViewById(R.id.animalTypeSpinnerAdoptActivity);
        animalGenderSpin=findViewById(R.id.animalGenderSpinnerAdoptActivity);
        animalGenderSpin.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,GENDER));
        locationSpin=findViewById(R.id.locationSpinnerAdoptActivity);
        locationSpin.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,LOCATION_ALL));
        Button searchButton;
        searchButton=findViewById(R.id.searchButton);
        expandSearchBar=findViewById(R.id.expand_search_bar);
        //Animal type spinner initialization depends on animal selection
        animalSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayAdapter<String> breedAdapter;
                switch(parent.getItemAtPosition(position).toString()){

                    case("חתולים"):
                        breedAdapter=new ArrayAdapter<String>(AdoptActivity.this,android.R.layout.simple_spinner_item,CAT_BREED_ALL);
                        animalTypeSpin.setAdapter(breedAdapter);
                        break;
                    case("ציפורים"):
                        breedAdapter=new ArrayAdapter<String>(AdoptActivity.this,android.R.layout.simple_spinner_item,BIRD_BREED_ALL);
                        animalTypeSpin.setAdapter(breedAdapter);
                        break;
                    case("דגים"):
                        breedAdapter=new ArrayAdapter<String>(AdoptActivity.this,android.R.layout.simple_spinner_item,FISH_BREED_ALL);
                        animalTypeSpin.setAdapter(breedAdapter);
                        break;
                    case("זוחלים"):
                        breedAdapter=new ArrayAdapter<String>(AdoptActivity.this,android.R.layout.simple_spinner_item,REPTILE_BREED_ALL);
                        animalTypeSpin.setAdapter(breedAdapter);
                        break;
                    case("מכרסמים"):
                        breedAdapter=new ArrayAdapter<String>(AdoptActivity.this,android.R.layout.simple_spinner_item,RODENT_BREED_ALL);
                        animalTypeSpin.setAdapter(breedAdapter);
                        break;
                    case("חיות משק"):
                        breedAdapter=new ArrayAdapter<String>(AdoptActivity.this,android.R.layout.simple_spinner_item,FARM_ANIMALS_BREED_ALL);
                        animalTypeSpin.setAdapter(breedAdapter);
                        break;
                    default:
                        breedAdapter=new ArrayAdapter<String>(AdoptActivity.this,android.R.layout.simple_spinner_item,DOG_BREED_ALL);
                        animalTypeSpin.setAdapter(breedAdapter);
                }

            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        })                                                                                                                                                                                                     ;

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final RelativeLayout searchBar=findViewById(R.id.search_bar_layout);
                findAd=false;
                animalIndex= animalSpin.getSelectedItemPosition();
                locationIndex=locationSpin.getSelectedItemPosition();
                typeIndex=animalTypeSpin.getSelectedItemPosition();
                genderIndex=animalGenderSpin.getSelectedItemPosition();
                //Translate from hebrew to DB reference
                String animal= ANIMAL_DB_ADAPTER[animalIndex];
                DatabaseReference animalChoiceRef=adRef.child(animal);
                //New search case
                adList.clear();
                animalChoiceRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot adSnapshot:dataSnapshot.getChildren()) {
                            Ad ad = adSnapshot.getValue(Ad.class);
                            if (properAd(ad)) {
                                adList.add(0, ad);
                                adapter.notifyDataSetChanged();
                                //This block run only once for first ad
                                if (!findAd) {
                                    findAd = true;
                                    searchBar.setVisibility(View.GONE);
                                    expandSearchBar.setVisibility(View.VISIBLE);
                                    if (!recyclerView.isShown())
                                        recyclerView.setVisibility(View.VISIBLE);
                                    if (noAdsTxt.isShown())
                                        noAdsTxt.setVisibility(View.GONE);
                                }

                            }
                        }
                        //No ads for current search hide recycle view set no ads text view
                        if(!findAd) {
                            if(recyclerView.isShown())
                                recyclerView.setVisibility(View.GONE);
                            noAdsTxt.setVisibility(View.VISIBLE);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
        //Hide a search properties button open search bar layout
        expandSearchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandSearchBar.setVisibility(View.GONE);
                RelativeLayout searchBar=findViewById(R.id.search_bar_layout);
                searchBar.setVisibility(View.VISIBLE);
            }
        });

        }

        //Method check if search properties is matching to specific ad
        private boolean properAd(Ad ad){
        if(typeIndex==0 || ad.getBreedIndex()+1==typeIndex){
            if(locationIndex==0 || ad.getLocationIndex()+1==locationIndex){
                return genderIndex == 0 || GENDER[genderIndex].equals(ad.getGender());
            }
        }
        return false;

        }


    // AdAdoptAdapter.OnImageListener interface method Override
    // Calling to a method when user click on image in RecycleView item
    public void onImageClick(int position) {
        //Fragment creation
        ViewPagerFragment viewPagerFragment=new ViewPagerFragment();
        Bundle args=new Bundle();
       // Sending array list of images
        args.putStringArrayList("imageList",adList.get(position).getImageUrlList());
        viewPagerFragment.setArguments(args);
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.adoptActivity,viewPagerFragment);
        fragmentTransaction.commit();
    }
}
