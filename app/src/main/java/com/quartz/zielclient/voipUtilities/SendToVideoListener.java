package com.quartz.zielclient.voipUtilities;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.quartz.zielclient.R;
import com.quartz.zielclient.activities.common.VideoActivity;

public class SendToVideoListener implements View.OnClickListener{

  private Context context;
  private String channelId;
  public SendToVideoListener(Context context, String channelId){
    this.channelId = channelId;
    this.context = context;
  }
  @Override
  public void onClick(View v) {
    Intent intentToVideo = new Intent(context, VideoActivity.class);
    intentToVideo.putExtra(context.getResources().getString(R.string.channel_key),channelId);
    context.startActivity(intentToVideo);

  }
}