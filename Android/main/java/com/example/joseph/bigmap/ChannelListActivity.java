package com.example.joseph.bigmap;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;

public class ChannelListActivity
        extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static String TAG = "ChannelListActivity";
    ListView channelList;

    @Override
    public void onCreate(Bundle savedInstanceVariable) {
        super.onCreate(savedInstanceVariable);
        setContentView(R.layout.channel_list);

        // create list
        channelList = (ListView) findViewById(R.id.channel_listView);
        ArrayAdapter<String> adapter
                = new ArrayAdapter<String>(this,R.layout.channel_list_item,
                R.id.channel_list_item, APIHandler.channelsAsString());
        channelList.setAdapter(adapter);
        channelList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TextView textView = (TextView) view;
        int channelId = Integer.parseInt(textView.getText().toString());

        // open channel activity
        Intent channelActivity = new Intent(ChannelListActivity.this, ChannelActivity.class);
        channelActivity.putExtra("channelId", channelId);
        startActivity(channelActivity);
    }
}
