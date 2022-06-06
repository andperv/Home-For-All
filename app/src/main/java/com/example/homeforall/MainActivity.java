package com.example.homeforall;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

//Main Activity is a launch activity
public class MainActivity extends AppCompatActivity {
    private Button adoptButton;
    private Button deliverToAdoptButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adoptButton=findViewById(R.id.adoptButton);
        deliverToAdoptButton=findViewById(R.id.deliverToAdoptButton);
        //Click on this button will transfer you to Login Activity there you can create account or login one that exist
        deliverToAdoptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });
        //Click on this button will transfer you to Adopt Activity there we can search for a animals
        adoptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,AdoptActivity.class);
                startActivity(intent);
            }
        });

    }
}
