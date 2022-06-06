package com.example.homeforall;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

//Adapter for Profile Recycle view item
public class AdAdapter extends RecyclerView.Adapter <AdAdapter.AdViewHolder>{
    private ArrayList<Ad> adList;
    private OnImageListener imageListener;
    private OnEditButListener editButListener;
    private OnDeleteButListener deleteButListener;

    //Constructor
    public AdAdapter (OnEditButListener editButListener,OnDeleteButListener deleteButListener,OnImageListener imageListener,ArrayList<Ad> list)
    {
        this.editButListener=editButListener;
        this.deleteButListener=deleteButListener;
        adList=list;
        this.imageListener=imageListener;
    }

    //Override
    public AdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.ad_item_profile,parent,false);
        return new AdViewHolder(view,imageListener,editButListener,deleteButListener);
    }

    //Override
    public void onBindViewHolder(@NonNull AdViewHolder holder,  int position) {
        Ad ad=adList.get(position);
        holder.species.setText(ad.getBreed());
        holder.location.setText(ad.getLocation());
        holder.description.setText(ad.getDescription());
        holder.date.setText(ad.getDate());
        holder.gender.setText("מין "+ad.getGender());
        if(ad.getImageUrlList()!=null) {
            Glide.with(holder.image.getContext()).load(ad.getImageUrlList().get(0)).into(holder.image);
            holder.imageNum = ad.getImageUrlList().size();
            if (holder.imageNum > 1) {
                holder.imageCountTxt.setText("+" + (holder.imageNum - 1));
            } else
                holder.imageCountLayout.setVisibility(View.GONE);
        }
        //No Images
        else {
            holder.image.setImageResource(R.drawable.no_mage_80);
            holder.imageCountLayout.setVisibility(View.GONE);
        }
    }



    //Override
    public int getItemCount() {
        return adList.size();
    }


    //Inner class Hold Ad View
    public static class AdViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView image;
        private TextView  species;
        private TextView  gender;
        private TextView  description;
        private TextView  location;
        private TextView  date;
        private TextView imageCountTxt;
        private RelativeLayout imageCountLayout;
        private OnImageListener imageListener;
        private OnEditButListener editButListener;
        private OnDeleteButListener deleteButListener;
        private int imageNum;


        public AdViewHolder( View itemView,OnImageListener imageListener,OnEditButListener editButListener,OnDeleteButListener deleteButListener) {
            super(itemView);
            image=itemView.findViewById(R.id.animalImageViewRV);
            species=itemView.findViewById(R.id.animalSpeciesTxt);
            gender=itemView.findViewById(R.id.genderTxt);
            description=itemView.findViewById(R.id.animalDescriptionTxt);
            location=itemView.findViewById(R.id.locationTxt);
            date=itemView.findViewById(R.id.dateTxt);
            Button delete=itemView.findViewById(R.id.deleteAdButton);
            Button edit=itemView.findViewById(R.id.editAdButton);
            imageCountTxt=itemView.findViewById(R.id.imageCountTxt);
            imageCountLayout=itemView.findViewById(R.id.image_count_layout_profile);
            edit.setOnClickListener(this);
            delete.setOnClickListener(this);
            image.setOnClickListener(this);
            this.imageListener = imageListener;
            this.editButListener=editButListener;
            this.deleteButListener=deleteButListener;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.deleteAdButton:
                     deleteButListener.onDeleteClick(getAdapterPosition());
                     break;

                case R.id.editAdButton:
                   editButListener.onEditClick(getAdapterPosition());
                   break;

                case R.id.animalImageViewRV:
                    if(imageNum!=0)
                       imageListener.onImageClick(getAdapterPosition());

            }
        }
    }

    public interface OnImageListener{
        void onImageClick(int position);
    }

    public interface OnDeleteButListener{
        void onDeleteClick(int position);
    }

    public interface OnEditButListener{
        void onEditClick(int position);
    }

}
