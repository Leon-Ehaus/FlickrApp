package com.example.flickrapp;

import android.app.SearchManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;

import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

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

import javax.net.ssl.HttpsURLConnection;

public class ScrollingActivity extends AppCompatActivity {
    private int testID = 0;
    private int pageNr = 1;
    private LinearLayout scrollablePictures;

    private class SearchPicturesTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            try {
                return requestPictures(strings[0]);
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
            try {
                JSONObject photoPage = jsonObject.getJSONObject("photos");
                JSONArray photos = photoPage.getJSONArray("photo");
                for (int i = 0; i < photos.length(); i++) {
                    JSONObject photo = photos.optJSONObject(i);
                    String urlString = "https://farm" + photo.getInt("farm") + ".static.flickr.com/" + photo.getString("server") + "/" + photo.getString("id") + "_" + photo.getString("secret") + ".jpg";
                    addPicture(photo.getString("title"), urlString);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    //TODO:Pageup button, search History, endless scrolling with recycler
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        scrollablePictures = findViewById(R.id.ScrollablePic);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                new SearchPicturesTask().execute("Heidelberg");
            }
        });
    }

    public JSONObject requestPictures(String query) throws IOException, JSONException {
        String urlString = "https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=37ad288835e4c64f" +
                "c0cb8af3f3a1a65d&format=json&nojsoncallback=1&page=" + 1 + "&safe_search=1&text=" + query;
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

    private void addPicture(String title, String url) {
        //TODO: remove stackoverflow link
        //https://stackoverflow.com/questions/5776851/load-image-from-url
        View tmpView = getLayoutInflater().inflate(R.layout.picture_view, scrollablePictures, false);
        TextView text = (TextView) tmpView.findViewById(R.id.textView2);
        text.setText(title);
        scrollablePictures.addView(tmpView);
        ImageView img = (ImageView) tmpView.findViewById(R.id.imageView6);
        Picasso.get().load(url).into(img);
    }

    private void newSearch(String query){
        new SearchPicturesTask().execute(query);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here
                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_search) {
            Log.d("Scrolling", "Searchbutton");
        }
        return super.onOptionsItemSelected(item);
    }
}