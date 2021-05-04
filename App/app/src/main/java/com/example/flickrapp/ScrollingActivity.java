package com.example.flickrapp;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

public class ScrollingActivity extends AppCompatActivity {

    private int pageNr = 1;
    private int maxPageNr;
    private String curSearchTerm;
    private RecyclerView.Adapter recyclerViewAdapter;

    private boolean imageDisplayActive;

    private boolean loading = false;
    int pastVisiblesItems, visibleItemCount, totalItemCount;


    private class SearchPicturesTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            try {
                Log.d("Scrolling", "requesting: " + strings[0] + " on page: " + pageNr);
                return requestPictures(strings[0], pageNr);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            List<String> imageUrlStrings = new ArrayList<>();
            try {
                JSONObject photoPage = jsonObject.getJSONObject("photos");
                maxPageNr = photoPage.getInt("pages");
                if (maxPageNr == 0){
                    failedSearch();
                    loading = false;
                    return;
                }
                JSONArray photos = photoPage.getJSONArray("photo");
                for (int i = 0; i < photos.length(); i++) {
                    JSONObject photo = photos.optJSONObject(i);
                    String urlString = "https://farm" + photo.getInt("farm") + ".static.flickr.com/" + photo.getString("server") + "/" + photo.getString("id") + "_" + photo.getString("secret") + ".jpg";
                    imageUrlStrings.add(urlString);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ((ImageRecyclerViewAdapter) recyclerViewAdapter).addData(imageUrlStrings);
            loading = false;
        }
    }

    //TOD:Change toast background
    private void failedSearch(){
        Toast toast = Toast.makeText(this,getString(R.string.no_images),Toast.LENGTH_LONG);
        toast.show();
    }


    //TODO:Cleanup code
    //TODO:prevent empty response from api
    //TODO:edgecases
    //TODO:Sizing in the button_item.xml
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        initImageDisplay();
    }


    private void initImageDisplay() {
        RecyclerView recyclerView = findViewById(R.id.ScrollablePic);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerViewAdapter = new ImageRecyclerViewAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(recyclerViewAdapter);

        FloatingActionButton topScrollButton = (FloatingActionButton) findViewById(R.id.top_Sscroll_button);
        topScrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.smoothScrollToPosition(0);
                topScrollButton.setVisibility(View.GONE);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) { //check for scroll down
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();

                    if (!loading) { //check if currently loading more images
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) { //check if on the end of the current list

                            Log.d("Scrolling", "End reached");
                            if (pageNr <= maxPageNr - 1) { //check if more pages are available
                                loading = true;
                                pageNr++;
                                new SearchPicturesTask().execute(curSearchTerm);
                            } else {
                                ((ImageRecyclerViewAdapter) recyclerViewAdapter).setLooping(true);
                            }
                        }
                    }
                } else if (dy < 0) { // for scroll up
                    topScrollButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //disable  scroll button after delay
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            topScrollButton.setVisibility(View.GONE);
                        }
                    }, 2000);
                }
            }
        });
        imageDisplayActive = true;
    }


    private void initHistory() {
        imageDisplayActive = false;
        RecyclerView recyclerView = findViewById(R.id.ScrollablePic);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        SharedPreferences sharedPreferences = getSharedPreferences("search_history", 0);
        Set<String> querySet = sharedPreferences.getStringSet("queries", new HashSet<String>());
        List<String> queryList = new ArrayList<>(querySet);

        recyclerViewAdapter = new ButtonRecyclerViewAdapter(this, queryList);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(layoutManager);
    }


    public JSONObject requestPictures(String query, int pageNr) throws IOException, JSONException {
        String urlString = "https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=37ad288835e4c64f" +
                "c0cb8af3f3a1a65d&format=json&nojsoncallback=1&page=" + pageNr + "&safe_search=1&text=" + query;
        URL url = new URL(urlString);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        String jsonString = null;
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);
            jsonString = responseStrBuilder.toString();

        } finally {
            urlConnection.disconnect();
        }
        JSONObject json = new JSONObject(jsonString);
        Log.d("Scrolling", json.toString());
        return json;
    }

    /**
     *
     * @param query
     */
    public void newSearch(String query) {
        loading = true;
        pageNr = 1;
        maxPageNr = -1;
        if (recyclerViewAdapter instanceof ImageRecyclerViewAdapter) {
            ((ImageRecyclerViewAdapter) recyclerViewAdapter).setLooping(false);
            ((ImageRecyclerViewAdapter) recyclerViewAdapter).clear();
        } else {
            initImageDisplay();
            newSearch(query);
            return;
        }
        curSearchTerm = query;
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        collapsingToolbarLayout.setTitle("\"" + query + "\"");

        SharedPreferences sharedPreferences = getSharedPreferences("search_history", 0);
        Set<String> querySet = new HashSet<>(
                sharedPreferences.getStringSet("queries", new HashSet<String>()));
        querySet.add(query);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("queries", querySet).apply();

        new SearchPicturesTask().execute(query);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!imageDisplayActive) {
                    initImageDisplay();
                }
                newSearch(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_history) {
            initHistory(); //load history into the recycler View
        }
        return true;
    }
}