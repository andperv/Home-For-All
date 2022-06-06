package com.example.homeforall;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.example.homeforall.AppConstants.*;

//Ad object User have arrayList of ads objects in firebase
public class Ad implements Serializable {
    private int animalIndex;
    private int breedIndex;
    private int locationIndex;
    private String adOwner;
    private String phoneNumber;
    private  String adId;
    private String gender;
    private String description;
    private ArrayList<String> imageUrlList;
    private String date;

    public Ad(){}

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

    public int getAnimalIndex(){return animalIndex;}

    public void setAnimalIndex(int animal) {
        this.animalIndex = animal;
    }

    public int getBreedIndex() {
        return breedIndex;
    }

    public String getBreed() {
        return BREED_MATRIX[animalIndex][breedIndex];
    }

    public void setBreedIndex(int breedIndex) {
        this.breedIndex = breedIndex;
    }

    public int getLocationIndex() {
        return locationIndex;
    }

    public String getLocation() {
        return LOCATION[locationIndex];
    }

    public void setLocationIndex(int location) {
        this.locationIndex = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setDate(){
        Calendar calendar=Calendar.getInstance();
        date=new SimpleDateFormat("dd/MM/yy" ).format(calendar.getTime());
    }

    public String getDate(){return date;}

    public ArrayList<String> getImageUrlList() {
        return imageUrlList;
    }

    public void setImageUrlList(ArrayList<String> imageUrlList) {
        this.imageUrlList = imageUrlList;
    }

    public String getAdOwner() {
        return adOwner;
    }

    public void setAdOwner(String adOwner) {
        this.adOwner = adOwner;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }



}
