package com.example.homeforall;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

//Adapter for ViewPagerFragment View item
public class ImagePageAdapter extends PagerAdapter {
    private Context context;
    private ArrayList<String> imageList;

    //Constructor
    public ImagePageAdapter(Context context, ArrayList<String> imageList ){
        this.context=context;
        this.imageList=imageList;

    }
    //Override
    public int getCount() {
        return imageList.size();
    }

    //Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view= LayoutInflater.from(context).inflate(R.layout.pager_item,null);
        ImageView imageView=view.findViewById(R.id.pager_image);
        Glide.with(imageView.getContext()).load(imageList.get(position)).into(imageView);
        container.addView(view);
        return view;
    }

    //Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
       container.removeView((View)object);
    }

    //Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }
}
