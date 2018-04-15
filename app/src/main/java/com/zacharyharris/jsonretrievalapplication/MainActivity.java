package com.zacharyharris.jsonretrievalapplication;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.zacharyharris.jsonretrievalapplication.Object;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<Object> objectList;
    private int objectListSize;

    private static String url = "https://guidebook.com/service/v2/upcomingGuides/";
    private ProgressDialog pDialog;

    // adapter class
    public class mAdapter extends RecyclerView.Adapter {

        @Override
        public int getItemCount() {
            return objectList.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_object, parent, false);
            SimpleItemViewHolder pvh = new SimpleItemViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            SimpleItemViewHolder viewHolder = (SimpleItemViewHolder) holder;
            viewHolder.position = position;
            Object object = objectList.get(position);
            ((SimpleItemViewHolder) holder).name.setText(object.getName());
            ((SimpleItemViewHolder) holder).city.setText(object.getCity());
            ((SimpleItemViewHolder) holder).state.setText(object.getState());
            ((SimpleItemViewHolder) holder).endDate.setText(object.getEndDate());
            Glide.with(MainActivity.this).load(object.getPhotoURL()).into(viewHolder.image);
        }

        private class SimpleItemViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            TextView city;
            TextView state;
            TextView endDate;
            ImageView image;
            public int position;

            public SimpleItemViewHolder(View itemView) {
                super(itemView);
                name =  itemView.findViewById(R.id.name_textView);
                city =  itemView.findViewById(R.id.city_textView);
                state =  itemView.findViewById(R.id.state_textView);
                endDate = itemView.findViewById(R.id.endDate_textView);
                image = itemView.findViewById(R.id.imageView);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.main_recycler_view);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new mAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        /*nameTextView = findViewById(R.id.name_textView);
        cityTextView = findViewById(R.id.city_textView);
        stateTextView = findViewById(R.id.state_textView);
        endDateTextView = findViewById(R.id.endDate_textView);
        iconView = findViewById(R.id.imageView);*/

        objectList = new ArrayList<>();

        // call the json retrieval
        new getObjects().execute();
    }

    private class getObjects extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // show progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HTTPHandler sh = new HTTPHandler();

            // Making a request to url
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    // retrieve and store the json list
                    JSONObject resultJson = new JSONObject(jsonStr);
                    JSONArray jsonList = resultJson.getJSONArray("data");
                    objectListSize = jsonList.length();

                    // iterate through JSON list and add each object to the object list
                    for(int i = 0; i < jsonList.length(); i++){
                        JSONObject currObj = jsonList.getJSONObject(i);
                        /* Couldn't find any data within any of the venue keys */
                        JSONObject venueKey = currObj.getJSONObject("venue");
                        String name = currObj.getString("name");
                        /*String city = venueKey.getString("city");
                        String state = venueKey.getString("state");*/
                        String endDate = currObj.getString("endDate");
                        String photoURL = currObj.getString("icon");
                        Log.e(TAG, name);

                        Object object = new Object();
                        object.setName(name);
                        //object.setCity(city);
                        //object.setState(state);
                        object.setEndDate(endDate);
                        object.setPhotoURL(photoURL);

                        objectList.add(object);
                        Log.e(TAG, object.getName());
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            // update adapter
            mAdapter.notifyDataSetChanged();
        }
    }
}
