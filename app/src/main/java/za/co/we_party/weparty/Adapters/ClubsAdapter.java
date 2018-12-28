package za.co.we_party.weparty.Adapters;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import za.co.we_party.weparty.Models.ClubModel;
import za.co.we_party.weparty.R;

public class ClubsAdapter extends RecyclerView.Adapter<ClubsAdapter.ViewHolder> {

    private List<ClubModel> allNearbyClubLogos = new ArrayList<>();

    private int mLastAnimatedItemPosition = -1;

    private OnTouchListener touchListener;

    //when the switch is turned on, within schedule list item
    public interface OnTouchListener {
        void onAddStationNotification(boolean isChecked, int pos);
    }

    public void setItemsOnClickListener(OnTouchListener touchListener) {
        this.touchListener = touchListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final ImageView clubLogo;
        public final ImageView clubImg;

        public ViewHolder(View view) {
            super(view);

            clubLogo = view.findViewById(R.id.clublogo);
            clubImg = view.findViewById(R.id.bgClubImg);

        }
    }

    public void populateclubList(List<ClubModel> mNewDataSet) {
        allNearbyClubLogos = mNewDataSet;
        notifyDataSetChanged();//notifies the recycler view about any data changes
    }

    @Override
    public ClubsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.clubcardview, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")//https://stackoverflow.com/questions/46135249/custom-view-imagebutton-has-setontouchlistener-called-on-it-but-does-not-overr
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final ClubModel clubsList = allNearbyClubLogos.get(position);

        //Loads club logo
        Glide.with(holder.clubLogo.getContext())
                .load(clubsList.getLogoUrl())
                .into(holder.clubLogo);

        //Loads club img
        Glide.with(holder.clubImg.getContext())
                .load(clubsList.getBackgroungImgUrl())
                .into(holder.clubImg);

        if (mLastAnimatedItemPosition < position) {

            mLastAnimatedItemPosition = position;
        }

    }

    @Override
    public int getItemCount() {
        return allNearbyClubLogos.size();
    }
}
