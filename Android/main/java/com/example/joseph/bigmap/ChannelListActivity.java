package com.example.joseph.bigmap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by Joseph on 5/21/2016.
 */
public class ChannelListActivity extends AppCompatActivity{

    @Override
    public void onCreate(Bundle savedInstanceVariable) {
        super.onCreate(savedInstanceVariable);
        setContentView(R.layout.channel_list);

        if (savedInstanceVariable == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.channel_list, new ChannelListFragment()).commit();
        }
    }

    public static class ChannelListFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.channel_list_fragment, container, false);
            ListView listView = (ListView) rootView.findViewById(R.id.channel_list);
            ArrayAdapter arrayAdapter = new ArrayAdapter<Integer>(
                    getActivity(),
                    R.layout.channel_list_fragment,
                    R.id.enter_channel_button);

            for (int i = 0; i < APIHandler.userChannels.size(); i++) {
                arrayAdapter.add(APIHandler.userChannels.get(i));
            }
            listView.setAdapter(arrayAdapter);
            return rootView;
        }
    }
}
