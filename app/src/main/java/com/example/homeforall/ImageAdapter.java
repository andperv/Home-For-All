package com.example.homeforall;

import android.net.Uri;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

//Adapter for AdCreation Activity Recycle view item
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private ArrayList<Uri> imageList;
    private boolean existAd;
    private OnDeleteButtonListener deleteButtonListener;
    private DbImageCount imageNumInDb;

    //Constructor
    public ImageAdapter(ArrayList<Uri> imageList,OnDeleteButtonListener deleteButtonListener,DbImageCount imageNumInDb, boolean existAd) {
        this.imageList = imageList;
        this.deleteButtonListener=deleteButtonListener;
        this.imageNumInDb=imageNumInDb;
        this.existAd=existAd;

    }

    //Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(view,deleteButtonListener );
    }

    //Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
                Glide.with(holder.animalImage.getContext()).load(imageList.get(position)).into(holder.animalImage);

    }


    //Override
    public int getItemCount() {
        return imageList.size();
    }

    //Inner class Hold Ad View
    public class ImageViewHolder extends  RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView animalImage;
        private OnDeleteButtonListener deleteButtonListener;


        public ImageViewHolder(@NonNull View itemView,OnDeleteButtonListener listener) {
            super(itemView);
            animalImage=itemView.findViewById(R.id.animalImageViewStorage);
            Button deleteImageBut = itemView.findViewById(R.id.deleteImageButton);
            deleteButtonListener=listener;
            deleteImageBut.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
         int position=getAdapterPosition();
         //Image isn't in DB
         if(!existAd ){
             imageList.remove(position);
             notifyItemRemoved(position);
             notifyDataSetChanged();
             return;
         }
            int imageInDb=imageNumInDb.getImageNumInDB();
         //Add a image without loading to DataBase,no new Images
            if(position>imageInDb){
                imageList.remove(position);
                notifyItemRemoved(position);
                notifyDataSetChanged();

            }
            //New images or Exist images
         else {
             deleteButtonListener.onDeleteButtonClick(position, imageList.get(position).toString());
         }
        }
    }

    public interface OnDeleteButtonListener{
        void onDeleteButtonClick(int position,String uriPath);
    }

    public interface DbImageCount{
        int getImageNumInDB();
    }
}
