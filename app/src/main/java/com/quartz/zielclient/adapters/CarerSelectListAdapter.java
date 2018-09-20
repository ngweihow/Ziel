package com.quartz.zielclient.adapters;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.quartz.zielclient.R;
import com.quartz.zielclient.activities.assisted.AssistedSelectCarerActivity;
import com.quartz.zielclient.activities.common.MapsActivity;
import com.quartz.zielclient.channel.ChannelController;
import com.quartz.zielclient.channel.ChannelData;
import com.quartz.zielclient.channel.ChannelListener;
import com.quartz.zielclient.channel.ChannelRequestController;
import com.quartz.zielclient.exceptions.AuthorisationException;
import com.quartz.zielclient.models.CarerSelectionItem;
import com.quartz.zielclient.user.User;
import com.quartz.zielclient.user.UserController;
import com.quartz.zielclient.user.UserFactory;

import java.util.List;

public class CarerSelectListAdapter
    extends RecyclerView.Adapter<CarerSelectListAdapter.TextViewHolder> {
  private List<CarerSelectionItem> listItems;
  private AssistedSelectCarerActivity context;

  // Constructor
  public CarerSelectListAdapter(
      List<CarerSelectionItem> listItems, AssistedSelectCarerActivity assistedSelectCarerActivity) {
    this.listItems = listItems;
    listItems.forEach(
        x -> {
          Log.d("ASDF", x.getPhoneNumber());
        });
    this.context = assistedSelectCarerActivity;
  }

  // Create ViewGroup whenever TextViewHolder gets instantiated
  @NonNull
  @Override
  public CarerSelectListAdapter.TextViewHolder onCreateViewHolder(
      @NonNull ViewGroup parent, int viewType) {
    // Get context of the view from respective XML
    View view =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.carer_select_list_item, parent, false);

    // Return view
    return new CarerSelectListAdapter.TextViewHolder(view);
  }

  // Binding the data to the ViewHolders
  @Override
  public void onBindViewHolder(
      @NonNull CarerSelectListAdapter.TextViewHolder textViewHolder, int i) {
    CarerSelectionItem carerSelectionItem = listItems.get(i);

    // Fetching the Names and number
    textViewHolder.textViewId.setText(carerSelectionItem.getCarerId());
  }

  // Returns size of list
  @Override
  public int getItemCount() {
    return listItems.size();
  }

  /** TextViewHolder class made for this CarerSelectListAdapter */
  class TextViewHolder extends RecyclerView.ViewHolder
      implements View.OnClickListener, ValueEventListener {

    // Defining TextViews of the Assisted List Objects
    private TextView textViewId;

    private String carerId;
    private User assisted;
    private Intent intentToMaps;
    private ChannelData channelData;

    TextViewHolder(@NonNull View itemView) {
      super(itemView);

      textViewId = itemView.findViewById(R.id.DisplayID);

      Button connectButton = itemView.findViewById(R.id.createChannelButton);
      connectButton.setOnClickListener(this);
    }

    // when user is selected make a channel request to them
    @Override
    public void onClick(View v) {
      channelData =
          ChannelController.createChannel(
              new ChannelListener() {
                @Override
                public void dataChanged() {}

                @Override
                public String getAssistedId() {
                  return FirebaseAuth.getInstance().getUid();
                }

                @Override
                public String getCarerId() {
                  return textViewId.getText().toString();
                }
              });
      // start intent to open maps
      intentToMaps = new Intent(context, MapsActivity.class);
      intentToMaps.putExtra(context.getString(R.string.channel_key), channelData.getChannelKey());
      Bundle bundle = context.getIntent().getExtras();

      // inject the destination which was established on the homepage
      if (bundle != null) {
        LatLng destination = bundle.getParcelable("destination");
        intentToMaps.putExtra("destination", destination);
      } // get the user
      try {
        UserController.fetchThisUser(this);
      } catch (AuthorisationException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
      assisted = UserFactory.getUser(dataSnapshot);
      carerId = textViewId.getText().toString();
      ChannelRequestController.createRequest(assisted, carerId, channelData.getChannelKey(), "");
      context.startActivity(intentToMaps);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {}
  }
}
