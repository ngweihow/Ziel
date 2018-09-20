package com.quartz.zielclient.activities.common;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.quartz.zielclient.R;
import com.quartz.zielclient.channel.ChannelController;
import com.quartz.zielclient.channel.ChannelData;
import com.quartz.zielclient.channel.ChannelListener;
import com.quartz.zielclient.messages.Message;
import com.quartz.zielclient.messages.MessageFactory;

import java.util.Objects;

/**
 * Chat activity allows users to communicate with eachother through messaging
 * This activity is currently unstyled.
 */
public class TextChatActivity extends AppCompatActivity implements ChannelListener, View.OnClickListener {


  // temporary for debugging will become a dynamic channel
  private ChannelData channel;
  private TextView chatOutput;
  private TextInputEditText chatInput;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    String channelKey = getIntent().getStringExtra(getApplicationContext().getString(R.string.channel_key));
    channel = ChannelController.retrieveChannel(channelKey, this);
    setContentView(R.layout.activity_text_chat);
    // initialize graphical elements
    chatOutput = findViewById(R.id.chatOutput);
    chatInput = findViewById(R.id.chatInput);
    Button sendButton = findViewById(R.id.sendButton);
    sendButton.setOnClickListener(this);
  }

  @Override
  public void dataChanged() {
    if (channel.getMessages() != null) {
      // this is completely unstylised representation of the messages
      chatOutput.setText(channel.getMessages().toString());
    }
  }

  @Override
  public String getAssistedId() {
    return null;
  }

  @Override
  public String getCarerId() {
    return null;
  }

  /**
   * Send message located in the input view
   *
   * @param view This is the view of the text chat
   */
  @Override
  public void onClick(View view) {
    Message messageToSend = MessageFactory.makeTextMessage(
        Objects.requireNonNull(chatInput.getText()).toString());
    channel.sendMessage(messageToSend);

  }
}
