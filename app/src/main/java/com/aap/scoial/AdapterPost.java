package com.aap.scoial;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterPost  extends RecyclerView.Adapter<AdapterPost.holderPost>  {
    private Context context;
    private ArrayList<uploadPost> userArrayList;

    public AdapterPost(Context context, ArrayList<uploadPost> userArrayList) {
        this.context = context;
        this.userArrayList = userArrayList;
    }

    @NonNull
    @Override
    public holderPost onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_post, parent, false);
        return new AdapterPost.holderPost(view);
    }

    @Override
    public void onBindViewHolder(@NonNull holderPost holder, int position) {

        uploadPost model = userArrayList.get(position);
        holder.txtName.setText(model.getName());
        holder.txtLocation.setText(model.getLocation());
        holder.txtCaption.setText(model.getCaption());
        holder.textTime.setText(model.getTime());

        String url = null;
        url =  model.getImageURL();
        Picasso.get().load(model.getImageURL()).into(holder.imgPost);


    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    class holderPost extends RecyclerView.ViewHolder{
        TextView txtName, txtLocation, txtCaption, textTime;
        ImageView imgPost;

        public holderPost(@NonNull View itemView) {
            super(itemView);
            imgPost = itemView.findViewById(R.id.imgPost);
            txtName = itemView.findViewById(R.id.textName);
            textTime = itemView.findViewById(R.id.textTime);
            txtLocation = itemView.findViewById(R.id.textLocation);
            txtCaption = itemView.findViewById(R.id.textCaption);
        }
    }

}
