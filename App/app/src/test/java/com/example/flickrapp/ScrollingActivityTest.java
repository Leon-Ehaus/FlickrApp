package com.example.flickrapp;

import android.util.Log;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ScrollingActivityTest extends TestCase {

    public void testRequestPictures() {
        ScrollingActivity scrollingActivity = new ScrollingActivity();
        JSONObject result = null;
        try {
            result = scrollingActivity.requestPictures("Heidelberg");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        assertNotNull(result);
    }
}