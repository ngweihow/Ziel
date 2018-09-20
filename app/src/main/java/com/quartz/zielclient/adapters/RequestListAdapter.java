package com.quartz.zielclient.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.quartz.zielclient.R;
import com.quartz.zielclient.activities.carer.CarerChannel;
import com.quartz.zielclient.models.ChannelRequest;

import java.util.List;

/**
 * Adapter class for adapting data to integrate them into the ListViews
 *
 * @author wei how ng
 */
public class RequestListAdapter extends RecyclerView.Adapter<RequestListAdapter.TextViewHolder> {

  private List<ChannelRequest> listItems;
  private Context context;

  // Constructor
  public RequestListAdapter(List<ChannelRequest> listItems, Context context) {
    this.listItems = listItems;
    this.context = context;
  }

  // Create ViewGroup whenever TextViewHolder gets instantiated
  @NonNull
  @Override
  public TextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    // Get context of the view from respective XML
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.assisted_list_item, parent, false);

    // Return view
    return new TextViewHolder(view);
  }

  // Binding the data to the ViewHolders
  @Override
  public void onBindViewHolder(@NonNull TextViewHolder textViewHolder, int i) {
    ChannelRequest channelRequest = listItems.get(i);

    // Fetching the Names and Descriptions
    textViewHolder.textViewName.setText(channelRequest.getName());
    textViewHolder.textViewDesc.setText(channelRequest.getDescription());

    textViewHolder.setChannelId(channelRequest.getChannelId());
  }

  // Returns size of list
  @Override
  public int getItemCount() {
    return listItems.size();
  }

  /**
   * TextViewHolder class made for this RequestListAdapter
   */
  class TextViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    // Defining TextViews of the Assisted List Objects
    private TextView textViewName;
    private TextView textViewDesc;

    private String channelId;

    TextViewHolder(@NonNull View itemView) {
      super(itemView);

      textViewName = itemView.findViewById(R.id.assistedListItemName);
      textViewDesc = itemView.findViewById(R.id.assistedListItemDesc);

      Button connectButton = itemView.findViewById(R.id.connectToChannel);
      connectButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
      Intent intent = new Intent(context, CarerChannel.class);
      intent.putExtra(context.getResources().getString(R.string.channel_key), channelId);
      context.startActivity(intent);
    }

    void setChannelId(String channelId) {
      this.channelId = channelId;
    }
  }
}