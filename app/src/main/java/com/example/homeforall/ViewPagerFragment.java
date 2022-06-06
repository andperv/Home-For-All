package com.example.homeforall;


import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import java.util.ArrayList;
import me.relex.circleindicator.CircleIndicator;

//Fragment class called from AdoptActivity and ProfileActivity
// Fragment receive image List
//Fragment function to show images on full screen
public class ViewPagerFragment extends Fragment {
    private ArrayList<String>imageList;

    //Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageList=getArguments().getStringArrayList("imageList");
    }

    //Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_view_pager, container, false);
        Button closeBut = view.findViewById(R.id.close_btn);
        ViewPager viewPager = view.findViewById(R.id.view_pager);
        CircleIndicator circleIndicator = view.findViewById(R.id.circleIndicator);
        ImagePageAdapter pageAdapter=new ImagePageAdapter(this.getContext(),imageList);
        viewPager.setAdapter(pageAdapter);
        circleIndicator.setViewPager(viewPager);
        closeBut.setOnClickListener(new View.OnClickListener() {
            //Override
            public void onClick(View v) {

                getActivity().getSupportFragmentManager().beginTransaction().remove(ViewPagerFragment.this).commit();
            }
        });
        return view;
    }
}
