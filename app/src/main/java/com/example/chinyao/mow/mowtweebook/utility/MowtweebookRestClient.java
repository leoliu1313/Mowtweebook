package com.example.chinyao.mow.mowtweebook.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

import java.io.IOException;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class MowtweebookRestClient extends OAuthBaseClient {
	public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class; // Change this
	public static final String REST_URL = "https://api.twitter.com/1.1"; // Change this, base API URL
	public static final String REST_CONSUMER_KEY = "5j2STGNDc0tDKCmfnr0QHkQpQ";       // Change this
	public static final String REST_CONSUMER_SECRET = "5FeuOKadCVoQUjWOtHEe08hWc5GRdXRgKhSh48bNnHzqQggcnx"; // Change this
	public static final String REST_CALLBACK_URL = "oauth://cprest"; // Change this (here and in manifest)

	public static final boolean FAKE_NO_INTERNET = false; // TODO

	public MowtweebookRestClient(Context context) {
		super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
	}

	public boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager =
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
	}

	public boolean isOnline() {
		Runtime runtime = Runtime.getRuntime();
		try {
			Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
			int     exitValue = ipProcess.waitFor();
			return (exitValue == 0);
		}
		catch (IOException | InterruptedException e) { e.printStackTrace(); }
		return false;
	}

	public boolean hasNetwork() {
		if (FAKE_NO_INTERNET) {
			return false;
		}
		return isNetworkAvailable() && isOnline();
	}

	/* 1. Define the endpoint URL with getApiUrl and pass a relative path to the endpoint
	 * 	  i.e getApiUrl("statuses/home_timeline.json");
	 * 2. Define the parameters to pass to the request (query or body)
	 *    i.e RequestParams params = new RequestParams("foo", "bar");
	 * 3. Define the request method and make a call to the client
	 *    i.e client.get(apiUrl, params, handler);
	 *    i.e client.post(apiUrl, params, handler);
	 */

	public void getHomeTimeline(long max_id, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/home_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", 25);
		if (max_id > 0) {
			params.put("max_id", max_id);
		}
		client.get(apiUrl, params, handler);
	}

	public void getUserTimeline(long max_id, String user_id, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/user_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", 25);
		if (max_id > 0) {
			params.put("max_id", max_id);
		}
		if (user_id != null) {
			params.put("user_id", user_id);
		}
		client.get(apiUrl, params, handler);
	}

	public void getMentionsTimeline(long max_id, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/mentions_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", 25);
		if (max_id > 0) {
			params.put("max_id", max_id);
		}
		client.get(apiUrl, params, handler);
	}

	public void getSearchTweets(long max_id, String q, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("search/tweets.json");
		RequestParams params = new RequestParams();
		params.put("count", 25);
		if (max_id > 0) {
			params.put("max_id", max_id);
		}
		params.put("q", q);
		params.put("result_type", "popular");
		client.get(apiUrl, params, handler);
	}

	public void postUpdate(String status, String in_reply_to_status_id, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/update.json");
		RequestParams params = new RequestParams();
		params.put("status", status);
		if (in_reply_to_status_id != null) {
			params.put("in_reply_to_status_id", in_reply_to_status_id); // reply
		}
		client.post(apiUrl, params, handler);
	}

	public void postRetweet(String id, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/retweet/" + id + ".json");
		RequestParams params = new RequestParams();
		client.post(apiUrl, params, handler);
	}

	public void postLike(String id, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("favorites/create.json");
		RequestParams params = new RequestParams();
		params.put("id", id);
		client.post(apiUrl, params, handler);
	}
}