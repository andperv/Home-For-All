package com.example.homeforall;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

//Adapter for Adopt activity Recycle view item
public class AdAdoptAdapter extends RecyclerView.Adapter <AdAdoptAdapter.AdViewHolder> {

    private ArrayList<Ad> adList;
    private OnImageListener imageListener;

    //Constructor
    public AdAdoptAdapter (Context context,ArrayList<Ad> list,FragmentManager fragmentManager,OnImageListener listener)
    {
        adList=list;
        imageListener=listener;
    }

    //Override
    public AdAdoptAdapter.AdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.ad_item_adopt,parent,false);
        AdAdoptAdapter.AdViewHolder adHolder=new AdAdoptAdapter.AdViewHolder(view,imageListener);
        return adHolder;
    }

    //Override
    public void onBindViewHolder( AdAdoptAdapter.AdViewHolder holder, int position) {
        final Ad ad=adList.get(position);
        holder.typeRow.setText(ad.getBreed());
        holder.locationRow.setText(ad.getLocation());
        holder.descriptionRow.setText(miniCardDescription(ad.getDescription()));
        holder.genderRow.setText("מין "+ad.getGender());
        if(ad.getImageUrlList()!=null) {
            Glide.with(holder.imageRow.getContext()).load(ad.getImageUrlList().get(0)).into(holder.imageRow);
            holder.imageNum=ad.getImageUrlList().size();
        }
        else
            holder.imageRow.setImageResource(R.drawable.no_mage_80);

    }



    //Override
    public int getItemCount() {
        return adList.size();
    }


    //Inner class Hold Ad View
    public  class AdViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // Row Variables
        private ImageView imageRow;
        private TextView typeRow;
        private TextView  genderRow;
        private TextView  descriptionRow;
        private TextView  locationRow;
        private  LinearLayout row;
        //Card variables
        private ImageView imageCard;
        private TextView typeCard;
        private TextView  genderCard;
        private TextView  descriptionCard;
        private TextView  locationCard;
        private TextView  dateCard;
        private Button closeBtn;
        private CardView card;
        private Button showNumBtn;
        private TextView showNumTxt;
        private TextView showOwnerTxt;
        private TextView imageCount;
        private OnImageListener imageListener;
        private RelativeLayout imageCountLayout;
        //Other variable
        private int imageNum;




        public AdViewHolder( View itemView,OnImageListener imageListener) {
            super(itemView);
            //Row Item
            imageRow=itemView.findViewById(R.id.imageRecViewRow);
            typeRow=itemView.findViewById(R.id.animalTypeRecViewRow);
            genderRow=itemView.findViewById(R.id.animalGenderRecViewRow);
            descriptionRow=itemView.findViewById(R.id.animalDescriptionRecViewRow);
            locationRow=itemView.findViewById(R.id.locationRecViewRow);
            row=itemView.findViewById(R.id.adSmallCardAdoptActivity);
            //Card item
            imageCard=itemView.findViewById(R.id.animalImageRVCard);
            typeCard=itemView.findViewById(R.id.animalTypeRVCard);
            genderCard=itemView.findViewById(R.id.animalGenderRVCard);
            descriptionCard=itemView.findViewById(R.id.animalDescriptionRVCard);
            locationCard=itemView.findViewById(R.id.locationRVCard);
            dateCard=itemView.findViewById(R.id.dateRVCard);
            showNumBtn=itemView.findViewById(R.id.showNumberBtn);
            showNumTxt=itemView.findViewById(R.id.showNumberTxt);
            showOwnerTxt=itemView.findViewById(R.id.showOwnerTxt);
            imageCount=itemView.findViewById(R.id.imageCountTxt);
            imageCountLayout=itemView.findViewById(R.id.image_count_layout_adopt);
            card=itemView.findViewById(R.id.adFullCardAdoptActivity);
            closeBtn=itemView.findViewById(R.id.close_btn_fullCard);
            this.imageListener=imageListener;
            closeBtn.setOnClickListener(this);
            imageCard.setOnClickListener(this);
            showNumBtn.setOnClickListener(this);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            Ad ad=adList.get(getAdapterPosition());
            switch (v.getId()) {
                //Hide Full Ad
                case R.id.close_btn_fullCard:
                    row.setVisibility(View.VISIBLE);
                    card.setVisibility(View.GONE);
                    break;
                case R.id.animalImageRVCard:
                    if(imageNum>0)
                        imageListener.onImageClick(getAdapterPosition());
                    break;
                case R.id.showNumberBtn:
                     showNumTxt.setText(ad.getPhoneNumber());
                     showOwnerTxt.setText(ad.getAdOwner()+" ");
                     showNumBtn.setVisibility(View.GONE);
                     break;
                     //Expand a Full ad
                default:
                    typeCard.setText(ad.getBreed());
                    locationCard.setText(ad.getLocation());
                    descriptionCard.setText(ad.getDescription());
                    genderCard.setText(ad.getGender());
                    dateCard.setText(ad.getDate());
                    //
                    if(imageNum>0) {
                       Glide.with(imageCard.getContext()).load(ad.getImageUrlList().get(0)).into(imageCard);
                        if(imageNum>1) {
                            imageCountLayout.setVisibility(View.VISIBLE);
                            imageCount.setText("+" + (imageNum - 1));
                        }
                    }

                    //No images
                    else {
                        imageCard.setImageResource(R.drawable.no_mage_80);
                    }
                    row.setVisibility(View.GONE);
                    card.setVisibility(View.VISIBLE);


            }
        }


    }

    private String miniCardDescription(String description) {
        String[] descrLines = description.split("\\n");
        if (descrLines.length > 2) {
            String miniDescription= descrLines[0] + "\n" + descrLines[1] + "\n" + descrLines[2];
            //Long loaded description
            if(miniDescription.length()>40)
                return miniDescription.substring(0,40).concat("...");
            //Long description but not loaded
            else
                return miniDescription+ "\n" + "...";
        }
        //Short description
        else
            return description;

    }
    public interface OnImageListener{
        void onImageClick(int position);
    }
}
