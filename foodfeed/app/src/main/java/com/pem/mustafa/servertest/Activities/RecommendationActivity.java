package com.pem.mustafa.servertest.Activities;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.pem.mustafa.servertest.Adapters.MainFeedAdapter;
import com.pem.mustafa.servertest.CustomObjects.DataObject;
import com.pem.mustafa.servertest.Other.LoginState;
import com.pem.mustafa.servertest.R;
import com.pnikosis.materialishprogress.ProgressWheel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RecommendationActivity extends AppCompatActivity {

    private static final String TAG = RecommendationActivity.class.getName();
    private static final String serverAddress = LoginState.getServerAdress();
    private static final String clientType = "1";
    private static Context applicationContext;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList results = new ArrayList<DataObject>();
    private int adapterState = 0;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayout progressWheel;
    private CoordinatorLayout recyclerLayout;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation);

        Toolbar toolbar = (Toolbar) findViewById(R.id.rcmmndtntoolbar);
        setSupportActionBar(toolbar);

        recyclerLayout = (CoordinatorLayout) findViewById(R.id.rcmmndtnrecyclerlayout);
        recyclerLayout.setVisibility(View.GONE);



        applicationContext = this.getApplicationContext();

        progressWheel = (LinearLayout) findViewById(R.id.rcmmndtnprogresswheel);
        progressWheel.setVisibility(View.VISIBLE);
        //wheel.setBarColor(Color.BLUE);

        mRecyclerView = (RecyclerView) findViewById(R.id.rcmmndtnRecyclerView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.rcmmndtnswiperefresh);

        mSwipeRefreshLayout.setColorSchemeColors(R.color.swipecolor1, R.color.swipecolor2);

        mRecyclerView.setHasFixedSize(true);
        mAdapter = new MainFeedAdapter(results, applicationContext);;
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        //getDataSet();

        //runnable.run();






        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                refreshRecyclerViewOnSwipe();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //runnable.run();

        progressWheel.setVisibility(View.VISIBLE);
        recyclerLayout.setVisibility(View.GONE);
        results.clear();
        adapterState = 0;
        mAdapter = new MainFeedAdapter(results, applicationContext);;
        mRecyclerView.setAdapter(mAdapter);
        getDataSet();

    }

    @Override
    protected void onPause() {

        super.onPause();
    }


    @Override
    protected void onStop() {
        super.onStop();
        //handler.removeCallbacks(runnable);
    }

    private void getDataSet() {


        //System.gc();
        String url = "http://" + serverAddress + "/followingrecommendation/?client_type="+clientType
                                                + "&username=" + LoginState.getUserInfo(applicationContext)
                                                + "&listType=following";

        JsonArrayRequest jsonRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray responseArray) {
                        // the response is already constructed as a JSONObject!
                        results.clear();
                        String foodName;
                        String price;
                        String location;
                        String username;
                        int hasPhotoFlag;
                        String url;
                        int userHasPhotoFlag;
                        String userImageUrl;
                        boolean hasPhotoBool = false;
                        boolean userHasPhotoBool = false;
                        int id;
                        //Log.d(TAG, "REFRESHING");
                        int index = 0;
                        for(int i=0; i < responseArray.length() ; i++)
                        {
                            try {
                                JSONObject tempObj = responseArray.getJSONObject(i);
                                foodName = tempObj.getString("name");
                                price = tempObj.getString("price");
                                location = tempObj.getString("meetingpoint");
                                id = tempObj.getInt("id");
                                hasPhotoFlag = tempObj.getInt("hasphoto");
                                url = tempObj.getString("photo_url");
                                JSONObject userObj = tempObj.getJSONObject("user");
                                userHasPhotoFlag = userObj.getInt("hasphoto");
                                userImageUrl = userObj.getString("photo_url");

                                if(hasPhotoFlag == 1)
                                {
                                    hasPhotoBool = true;
                                    url = "http://" + serverAddress + url;
                                }
                                else {
                                    url = "";
                                    hasPhotoBool = false;
                                }

                                if(userHasPhotoFlag == 1)
                                {
                                    userHasPhotoBool = true;
                                    userImageUrl = "http://" + serverAddress + userImageUrl;
                                }
                                else
                                {
                                    userHasPhotoBool = false;
                                    userImageUrl = null;
                                }

                                JSONObject tempUser = tempObj.getJSONObject("user");
                                JSONObject tempUser2 = tempUser.getJSONObject("user");
                                username = tempUser2.getString("username");

                                DataObject obj = new DataObject(foodName,price,location,username,"Rating "+i, url, hasPhotoBool, id, userHasPhotoBool, userImageUrl);
                                results.add(index, obj);
                                index++;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                        if(adapterState != results.size()) {
                            adapterState = results.size();
                            Log.d(TAG, "" + results.size());
                            ArrayList<DataObject> reversed = new ArrayList<>();
                            for (int i = results.size() - 1; i >= 0; i--)
                                reversed.add((DataObject) results.get(i));
                            mAdapter = new MainFeedAdapter(reversed, applicationContext);
                            mRecyclerView.setAdapter(mAdapter);
                        }
                        mSwipeRefreshLayout.setRefreshing(false);
                        progressWheel.setVisibility(View.GONE);
                        recyclerLayout.setVisibility(View.VISIBLE);


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        //responseTv.setText("Login Failed \n" + error.toString());
                        Toast.makeText(getApplicationContext(), "Couldn't get items(server)", Toast.LENGTH_SHORT).show();
                        mSwipeRefreshLayout.setRefreshing(false);
                        progressWheel.setVisibility(View.GONE);
                        recyclerLayout.setVisibility(View.VISIBLE);
                    }
                });

        Volley.newRequestQueue(RecommendationActivity.this).add(jsonRequest);
        //return results;
    }


    private void refreshRecyclerViewOnSwipe()
    {
        getDataSet();
        //mSwipeRefreshLayout.setRefreshing(false);
    }



}
