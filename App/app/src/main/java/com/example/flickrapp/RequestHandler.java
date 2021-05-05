package com.example.flickrapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

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
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.net.ssl.HttpsURLConnection;

public class RequestHandler {
    private int pageNr;
    private BiConsumer<List<String>, Integer> callback;


    private class SearchPicturesTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            try {
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
            int maxPageNr = -1;
            if (jsonObject == null) {
                imageUrlStrings.add("Error while accessing the API. Please check your network connection");
                callback.accept(imageUrlStrings, -1);
                return;
            }
            try {

                if (jsonObject.getString("stat").equals("fail")) {//cancel if error in response
                    imageUrlStrings.add(jsonObject.getString("message"));
                    callback.accept(imageUrlStrings, -1);
                    return;
                }

                JSONObject photoPage = jsonObject.getJSONObject("photos");
                maxPageNr = photoPage.getInt("pages");
                if (maxPageNr == 0) {//check if answer contains result
                    callback.accept(imageUrlStrings, maxPageNr);
                    return;
                }
                //fill list with url strings for each image in the response
                JSONArray photos = photoPage.getJSONArray("photo");
                for (int i = 0; i < photos.length(); i++) {
                    JSONObject photo = photos.optJSONObject(i);
                    String urlString = "https://farm" + photo.getInt("farm") + ".static.flickr.com/" + photo.getString("server") + "/" + photo.getString("id") + "_" + photo.getString("secret") + ".jpg";
                    imageUrlStrings.add(urlString);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (imageUrlStrings.size() == 0) {//response was empty maybe an empty page was requested
                callback.accept(imageUrlStrings, 0);
                return;
            }
            callback.accept(imageUrlStrings, maxPageNr);
        }
    }

    /**
     * Sends the search request to the Flickr API and returns the answer
     *
     * @param query  the search term
     * @param pageNr the page number that will be requested
     * @return the unfiltered answer from the Flickr API
     * @throws IOException
     * @throws JSONException
     */
    private JSONObject requestPictures(String query, int pageNr) throws IOException, JSONException {
        //Put together the request URL and establish the connection
        String urlString = "https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=37ad288835e4c64f" +
                "c0cb8af3f3a1a65d&format=json&nojsoncallback=1&page=" + pageNr + "&safe_search=1&text=" + query;
        URL url = new URL(urlString);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        String jsonString = null;
        try {
            //Read API response into String
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }
            jsonString = responseStrBuilder.toString();

        } finally {
            urlConnection.disconnect();
        }
        JSONObject json = new JSONObject(jsonString);
        return json;
    }

    /**
     * Searches for images corresponding to the query through the Flickr API
     * on the specified page number
     *
     * @param query
     * @param pageNr the page number of the result page
     */
    public void search(String query, int pageNr, BiConsumer<List<String>, Integer> callback) {
        this.pageNr = pageNr;
        this.callback = callback;
        new SearchPicturesTask().execute(query);
    }

}
