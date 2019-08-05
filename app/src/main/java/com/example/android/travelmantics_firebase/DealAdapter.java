package com.example.android.travelmantics_firebase;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.travelmantics_firebase.models.TravelDeal;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.ViewHolder> {


    private static final String LOG_TAG = "LOG_TAG";
//    private List<TravelDeal> mDeals;
    private FirebaseDealsDBListener mFirebaseDealsDBListener;


    DealAdapter(FirebaseDealsDBListener firebaseDealsDBListener) {
        mFirebaseDealsDBListener = firebaseDealsDBListener;

        DatabaseReference databaseReference = FirebaseUtil.sDatabaseReference;
//        mDeals = FirebaseUtil.sDeals;

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(LOG_TAG, "ListenerForSingleValueEvent: onDataChange()");

                if (dataSnapshot.exists()) {
                    Log.d(LOG_TAG, "ListenerForSingleValueEvent: data exists");
                    mFirebaseDealsDBListener.onContainsData();
                } else {
                    Log.d(LOG_TAG, "ListenerForSingleValueEvent: No data exists"); /// To be check if it's working
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(LOG_TAG, "ListenerForSingleValueEvent: onCancelled()");
            }
        });


        // get the auto-assigned ID from firebase for that deal
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(LOG_TAG, "ChildEventListener: onChildAdded()");
                TravelDeal travelDeal = dataSnapshot.getValue(TravelDeal.class);
//                Log.d("Tag", "Deal: " + travelDeal.getTitle());
                if (travelDeal != null) {
                    Log.d(LOG_TAG, "adding deal");
                    FirebaseUtil.sDeals.add(travelDeal);
                    travelDeal.setId(dataSnapshot.getKey()); // get the auto-assigned ID from firebase for that deal
                }
                notifyItemInserted(FirebaseUtil.sDeals.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(LOG_TAG, "ChildEventListener: onChildChanged()");
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(LOG_TAG, "ChildEventListener: onChildRemoved()");
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(LOG_TAG, "ChildEventListener: onChildMoved()");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(LOG_TAG, "ChildEventListener: onCancelled()");
            }
        };


        databaseReference.addChildEventListener(childEventListener);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.deal_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        TravelDeal deal = FirebaseUtil.sDeals.get(position);
        holder.bind(deal);
    }

    @Override
    public int getItemCount() {
        return FirebaseUtil.sDeals.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvTitle;
        TextView tvDescription;
        TextView tvPrice;
        ImageView mImageView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            mImageView = itemView.findViewById(R.id.imageView);

            itemView.setOnClickListener(this);
        }

        void bind(TravelDeal deal) {
            tvTitle.setText(deal.getTitle());
            tvDescription.setText(deal.getDescription());
            tvPrice.setText(deal.getPrice());
            Picasso.get()
                    .load(deal.getImageUrl())
//                    .resize(50, 50)
//                    .centerCrop()
                    .placeholder(R.drawable.ic_image_black_24dp)
                    .into(mImageView);

        }

        @Override
        public void onClick(View view) {
//            Log.d("LOG_TAG", "Position: " + getAdapterPosition());
            Intent intent = new Intent(view.getContext(), DealActivity.class);

            FirebaseImageUploadListener imageUploadListener = new FirebaseImageUploadListener() {
                @Override
                public void onImageChanged(int position) {
                    Log.d(LOG_TAG, "Image Uploaded to firebase");
                    notifyItemChanged(position);
                }
            };

            intent.putExtra("position", getAdapterPosition());
            view.getContext().startActivity(intent);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
