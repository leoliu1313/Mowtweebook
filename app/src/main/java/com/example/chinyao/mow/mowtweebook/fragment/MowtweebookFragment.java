package com.example.chinyao.mow.mowtweebook.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.chinyao.mow.R;
import com.example.chinyao.mow.mowdigest.EndlessRecyclerViewScrollListener;
import com.example.chinyao.mow.mowtweebook.adapter.MowtweebookRecyclerAdapter;
import com.example.chinyao.mow.mowtweebook.model.MowtweebookPersistentTweet;
import com.example.chinyao.mow.mowtweebook.model.MowtweebookTweet;
import com.example.chinyao.mow.mowtweebook.utility.MowtweebookRestApplication;
import com.example.chinyao.mow.mowtweebook.utility.MowtweebookRestClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

/**
 * Created by chinyao on 7/29/2016.
 */
public class MowtweebookFragment extends Fragment {
    // ButterKnife
    // http://guides.codepath.com/android/Reducing-View-Boilerplate-with-Butterknife
    @BindView(R.id.mowtube_swipe_refresh_layout)
    SwipeRefreshLayout theSwipeRefreshLayout;
    @BindView(R.id.mowtube_recycler_view)
    RecyclerView theRecyclerView;

    public List<MowtweebookTweet> tweets = null;
    public MowtweebookRecyclerAdapter tweetsAdapter = null;
    public int mode = 1;

    private MowtweebookRestClient client = null;

    private boolean lock = false;
    private String query = null;
    private boolean first_time = true;

    public static MowtweebookFragment newInstance(int mode) {
        MowtweebookFragment theFragment = new MowtweebookFragment();

        theFragment.mode = mode;
        theFragment.client = MowtweebookRestApplication.getRestClient();

        theFragment.tweets = new ArrayList<>(); // avoid java.lang.NullPointerException at getItemCount()

        return theFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        // Note that this can have more than RecyclerView
        view = inflater.inflate(R.layout.mowtube_stream_fragment, container, false);

        // orientation issue
        // http://stackoverflow.com/questions/9727173/support-fragmentpageradapter-holds-reference-to-old-fragments
        // http://stackoverflow.com/questions/32478968/android-viewpager-orientation-change
        // https://medium.com/@roideuniverse/android-viewpager-fragmentpageradapter-and-orientation-changes-256c23bee035#.ufb2ywv33
        // http://stackoverflow.com/questions/28982512/handling-orientation-change-with-viewpager-fragmentpageradapter
        // https://github.com/codepath/android_guides/wiki/ViewPager-with-FragmentPagerAdapter
        // http://guides.codepath.com/android/Handling-Configuration-Changes
        // http://guides.codepath.com/android/Understanding-App-Resources
        // http://android-er.blogspot.com/2013/05/how-setretaininstancetrue-affect.html
        setRetainInstance(true);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ButterKnife
        // http://stackoverflow.com/questions/27002200/butterknife-fragment-rotation-giving-nullpointer
        ButterKnife.bind(this, view);

        setupRecyclerView();

        setupSwipeRefreshLayout();
    }

    private void setupRecyclerView() {
        // recyclerView.setHasFixedSize(true);
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        theRecyclerView.setLayoutManager(layoutManager);
        // Endless-Scrolling-with-AdapterViews-and-RecyclerView
        // http://guides.codepath.com/android/Endless-Scrolling-with-AdapterViews-and-RecyclerView#troubleshooting
        // https://gist.github.com/nesquena/d09dc68ff07e845cc622
        theRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                // customLoadMoreDataFromApi();
                if (!client.hasNetwork()) {
                    Toast.makeText(getContext(),
                            getResources().getString(R.string.no_internet),
                            Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (mode == 1) {
                    doSearch();
                }
                // TODO mode == 2
                // TODO mode == 3
            }
        });
        tweetsAdapter = new MowtweebookRecyclerAdapter(
                getActivity(),
                tweets
        );
        theRecyclerView.setAdapter(tweetsAdapter);
        // TODO
        // rotation orientation
        if (first_time) {
            // avoid reloading
            first_time = false;
            doSearch();
        }
    }

    public void notifyAdapter() {
        if (tweetsAdapter != null) {
            tweetsAdapter.notifyDataSetChanged();
        }
    }

    // pull-to-refresh
    // http://guides.codepath.com/android/Implementing-Pull-to-Refresh-Guide
    // theSwipeRefreshLayout.setRefreshing(false)
    private void setupSwipeRefreshLayout() {
        // Setup refresh listener which triggers new data loading
        theSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                clearAndrefreshAsync();
            }
        });
        // Configure the refreshing colors
        theSwipeRefreshLayout.setColorSchemeResources(
                R.color.mowtubeColorAccent,
                R.color.mowtubeColorAccentLightLight
        );
    }

    public void clearAndrefreshAsync() {
        tweets.clear();
        notifyAdapter();
        doSearch();
    }

    public void clearAndrefreshAsync(String theQuery) {
        tweets.clear();
        notifyAdapter();
        doSearch(theQuery);
    }

    public void doSearch() {
        doSearch(query);
    }

    public void doSearch(final String theQuery) {
        if (!lock) {
            lock = true;
            // perform query here
            if (theSwipeRefreshLayout != null) {
                theSwipeRefreshLayout.setRefreshing(true);
            }
            if (!client.hasNetwork()) {
                Toast.makeText(getContext(),
                        getResources().getString(R.string.no_internet),
                        Toast.LENGTH_SHORT)
                        .show();
                tweets.addAll(MowtweebookPersistentTweet.getAll(mode));
                notifyAdapter();
                if (theSwipeRefreshLayout != null) {
                    theSwipeRefreshLayout.setRefreshing(false);
                }
                lock = false;
                return;
            }
            if (mode == 1) {
                long max_id = -1;
                if (tweets.size() > 0) {
                    max_id = Long.parseLong(tweets.get(tweets.size() - 1).getId_str());
                }
                Log.d("getHomeTimeline", "max_id = " + max_id);
                client.getHomeTimeline(max_id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        Log.d("getHomeTimeline", response.toString());
                        try {
                            JSONObject theJSONObject;
                            for (int i = 0; i < response.length(); i++) {
                                theJSONObject = response.getJSONObject(i); // JSONException
                                tweets.add(MowtweebookTweet.parseJSON(mode, theJSONObject.toString()));
                            }
                            notifyAdapter();
                            // TODO
                            // query = theQuery;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (theSwipeRefreshLayout != null) {
                            theSwipeRefreshLayout.setRefreshing(false);
                        }
                        lock = false;
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                        Log.d("getHomeTimeline", errorResponse.toString());
                        if (theSwipeRefreshLayout != null) {
                            theSwipeRefreshLayout.setRefreshing(false);
                        }
                        lock = false;
                    }
                });
            }
            else if (mode == 2) {
                long max_id = -1;
                if (tweets.size() > 1) { // profile is index 0
                    max_id = Long.parseLong(tweets.get(tweets.size() - 1).getId_str());
                }
                client.getUserTimeline(max_id, theQuery,new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        Log.d("getUserTimeline", response.toString());
                        query = theQuery;
                        try {
                            boolean add_profile = false;
                            if (tweets.size() == 0) {
                                add_profile = true;
                            }
                            JSONObject theJSONObject;
                            for (int i = 0; i < response.length(); i++) {
                                theJSONObject = response.getJSONObject(i);
                                tweets.add(MowtweebookTweet.parseJSON(mode, theJSONObject.toString()));
                            }
                            if (add_profile && tweets.size() > 0) {
                                // profile is index 0
                                tweets.add(0, MowtweebookTweet.profile());
                            }
                            notifyAdapter();
                            // TODO
                            // query = theQuery;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (theSwipeRefreshLayout != null) {
                            theSwipeRefreshLayout.setRefreshing(false);
                        }
                        lock = false;
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                        Log.d("getUserTimeline", errorResponse.toString());
                        if (theSwipeRefreshLayout != null) {
                            theSwipeRefreshLayout.setRefreshing(false);
                        }
                        lock = false;
                    }
                });
            }
            else if (mode == 3) {
                long max_id = -1;
                if (tweets.size() > 0) {
                    max_id = Long.parseLong(tweets.get(tweets.size() - 1).getId_str());
                }
                Log.d("getMentionsTimeline", "max_id = " + max_id);
                client.getMentionsTimeline(max_id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        Log.d("getMentionsTimeline", response.toString());
                        try {
                            JSONObject theJSONObject;
                            for (int i = 0; i < response.length(); i++) {
                                theJSONObject = response.getJSONObject(i); // JSONException
                                tweets.add(MowtweebookTweet.parseJSON(mode, theJSONObject.toString()));
                            }
                            notifyAdapter();
                            // TODO
                            // query = theQuery;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (theSwipeRefreshLayout != null) {
                            theSwipeRefreshLayout.setRefreshing(false);
                        }
                        lock = false;
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                        Log.d("getMentionsTimeline", errorResponse.toString());
                        if (theSwipeRefreshLayout != null) {
                            theSwipeRefreshLayout.setRefreshing(false);
                        }
                        lock = false;
                    }
                });
            }
            else if (mode == 4) {
                long max_id = -1;
                if (tweets.size() > 0) {
                    max_id = Long.parseLong(tweets.get(tweets.size() - 1).getId_str());
                }
                Log.d("getSearchTweets", "max_id = " + max_id);
                client.getSearchTweets(max_id, theQuery, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.d("getSearchTweets", response.toString());
                        try {
                            JSONArray responseArray = response.getJSONArray("statuses");
                            JSONObject theJSONObject;
                            for (int i = 0; i < responseArray.length(); i++) {
                                theJSONObject = responseArray.getJSONObject(i); // JSONException
                                tweets.add(MowtweebookTweet.parseJSON(mode, theJSONObject.toString()));
                            }
                            notifyAdapter();
                            query = theQuery;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (theSwipeRefreshLayout != null) {
                            theSwipeRefreshLayout.setRefreshing(false);
                        }
                        lock = false;
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.d("getSearchTweets", errorResponse.toString());
                        if (theSwipeRefreshLayout != null) {
                            theSwipeRefreshLayout.setRefreshing(false);
                        }
                        lock = false;
                    }
                });
            }
        }
    }
}
