package za.co.we_party.weparty.Adapters;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import za.co.we_party.weparty.Models.Club;
import za.co.we_party.weparty.R;
import za.co.we_party.weparty.Util.ClubUtil;

/**
 * RecyclerView adapter for a list of clubs.
 */
public class ClubAdapter extends FirestoreAdapter<ClubAdapter.ViewHolder> {

    public interface OnClubSelectedListener {

        void onClubSelected(DocumentSnapshot club);

    }

    private OnClubSelectedListener mListener;

    public ClubAdapter(Query query, OnClubSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_restaurant, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.restaurant_item_image)
        ImageView imageView;

        @BindView(R.id.restaurant_item_name)
        TextView nameView;

        @BindView(R.id.restaurant_item_rating)
        MaterialRatingBar ratingBar;

        @BindView(R.id.restaurant_item_num_ratings)
        TextView numRatingsView;

        @BindView(R.id.restaurant_item_price)
        TextView priceView;

        @BindView(R.id.restaurant_item_category)
        TextView categoryView;

        @BindView(R.id.restaurant_item_city)
        TextView cityView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnClubSelectedListener listener) {

            Club club = snapshot.toObject(Club.class);
            Resources resources = itemView.getResources();

            // Load image
            Glide.with(imageView.getContext())
                    .load(club.getPhoto())
                    .into(imageView);

            nameView.setText(club.getName());
            ratingBar.setRating((float) club.getAvgRating());
            cityView.setText(club.getCity());
            categoryView.setText(club.getCategory());
            numRatingsView.setText(resources.getString(R.string.fmt_num_ratings,
                    club.getNumRatings()));
            priceView.setText(ClubUtil.getPriceString(club));

            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onClubSelected(snapshot);
                    }
                }
            });
        }

    }
}
