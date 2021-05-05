package com.example.flickrapp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;


public class RequestHandlerTest {


    private CountDownLatch lock = new CountDownLatch(1);


    private List<String> responseUrls;
    private int responseMaxPage;
    private RequestHandler requestHandler;

    @Before
    public void setUp() {
        requestHandler = new RequestHandler();
        responseUrls = null;
        responseMaxPage = Integer.MAX_VALUE;
    }

    @After
    public void tearDown() {
    }

    @Test
    public void correctQueryAndValidPageNr() {

        requestHandler.search("Leon Ehaus", 1, (x, y) -> {
            responseUrls = x;
            responseMaxPage = y;
            lock.countDown();
        });

        try {
            lock.await(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<String> correctResponse = new ArrayList<String>();
        correctResponse.add("https://farm4.static.flickr.com/3849/14780946851_d915bf02c2.jpg");
        assertEquals(responseUrls,correctResponse);
        assertEquals(responseMaxPage,1);
    }

    @Test
    public void correctQueryAndPageNrTooHigh() {

        requestHandler.search("Leon Ehaus", 2, (x, y) -> {
            responseUrls = x;
            responseMaxPage = y;
            lock.countDown();
        });

        try {
            lock.await(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(responseMaxPage,0);
    }

    @Test
    public void correctQueryAndInValidPageNr() {

        requestHandler.search("Leon Ehaus", 1, (x, y) -> {
            responseUrls = x;
            responseMaxPage = y;
            lock.countDown();
        });

        try {
            lock.await(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<String> correctResponse = new ArrayList<String>();
        correctResponse.add("https://farm4.static.flickr.com/3849/14780946851_d915bf02c2.jpg");
        assertEquals(responseUrls,correctResponse);
        assertEquals(responseMaxPage,1);
    }

    @Test
    public void emptyQueryAndInValidPageNr() {

        requestHandler.search("", 1, (x, y) -> {
            responseUrls = x;
            responseMaxPage = y;
            lock.countDown();
        });

        try {
            lock.await(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<String> correctResponse = new ArrayList<String>();
        correctResponse.add("https://farm4.static.flickr.com/3849/14780946851_d915bf02c2.jpg");
        assertEquals(responseMaxPage,-1);
    }
}