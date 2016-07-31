package com.example.chinyao.mow.mowdigest.swipe;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chinyao.mow.R;
import com.example.chinyao.mow.mowdigest.MowdigestActivity;
import com.example.chinyao.mow.mowdigest.MowdigestFragment;
import com.example.chinyao.mow.mowdigest.model.MowdigestArticleSearch;
import com.example.chinyao.mow.mowdigest.model.MowdigestNews;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by chinyao on 7/23/2016.
 */
// TODO
// try to remove this fake class
// use NestedScrollView without background instead
public class MowdigestFakeAdapter
        extends RecyclerView.Adapter<MowdigestFakeAdapter.ViewHolder> {

    private List<String> items;
    private Context context;
    private List<MowdigestNews> newsDigest;
    private MowdigestFragment fragment;

    private ArrayList<MowdigestSwipe> theSwipes;
    private MowdigestSwipeAdapter theSwipeAdapter;

    private static final int HTTP_Mode = 2;
    // 1: android-async-http
    // 2: retrofit
    private static final int SwipeContentMode = 2;
    // 1: debug
    // 2: nytimes api
    private static final String link1 = "https://s-media-cache-ak0.pinimg.com/236x/e7/7b/29/e77b294d3dc6245ab4b517142e1f63b0.jpg";
    private static final String link2 = "https://s-media-cache-ak0.pinimg.com/236x/e7/7b/29/e77b294d3dc6245ab4b517142e1f63b0.jpg";
    private static final String link3 = "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcTqEJBhd92spKnkYretdXnn5Twbnoii1NgdjXLBuddq8bF1bfEA";

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final SwipeFlingAdapterView theSwipe;
        public ViewHolder(View view) {
            super(view);
            mView = view;
            theSwipe = (SwipeFlingAdapterView) view.findViewById(R.id.swipe_in_fake_recycler_item);
        }
    }

    public MowdigestFakeAdapter(Context theContext,
                                List<String> theItems,
                                List<MowdigestNews> theNewsDigest,
                                MowdigestFragment theFragment) {
        context = theContext;
        items = theItems;
        newsDigest = theNewsDigest;
        fragment = theFragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View theView = inflater.inflate(R.layout.mowdigest_fake_recycler_item, parent, false);
        ViewHolder output = new ViewHolder(theView);
        return output;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        setupSwipe(holder);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void setupSwipe(final ViewHolder holder) {
        theSwipes = new ArrayList<>();
        // theSwipeAdapter = new MowdigestSwipeAdapter(al, getContext());
        theSwipeAdapter = new MowdigestSwipeAdapter(theSwipes, context);
        theSwipeAdapter.setOnAsyncFinishedListener(fragment);
        Log.d("MowdigestFakeAdapter", "theSwipes is ready");

        if (SwipeContentMode == 1) {
            theSwipes.add(new MowdigestSwipe(link1, "link1"));
            theSwipes.add(new MowdigestSwipe(link2, "link2"));
            theSwipes.add(new MowdigestSwipe(link1, "link1"));
            theSwipes.add(new MowdigestSwipe(link2, "link2"));
            theSwipes.add(new MowdigestSwipe(link1, "link1"));
            theSwipes.add(new MowdigestSwipe(link2, "link2"));
            theSwipeAdapter.notifyDataSetChanged();
        }
        else if (SwipeContentMode == 2) {
            if (HTTP_Mode == 1) {
                AsyncHttpClient client = new AsyncHttpClient();
                // Turn off Debug Log
                // client.setLoggingEnabled(false);
                String url = MowdigestActivity.BASE_URL + MowdigestActivity.MOST_POPULAR + "/all-sections/1.json";
                RequestParams params = new RequestParams();
                params.put("api-key", MowdigestActivity.MOST_POPULAR_API_KEY);
                Log.d("MowdigestFragment", "url " + url);
                client.get(url, params, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                Log.d("MowdigestFragment", "onSuccess HTTP_Mode " + HTTP_Mode);
                                Log.d("MowdigestFragment",
                                        "statusCode " + statusCode);
                                MowdigestArticleSearch theSearch = MowdigestArticleSearch.parseJSON(response.toString());
                                Log.d("MowdigestFragment",
                                        "theSearch.getResults().size() " + theSearch.getResults().size());
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                                Log.d("MowdigestFragment", "HTTP_Mode " + HTTP_Mode);
                            }
                        }
                );
            }
            else if (HTTP_Mode == 2) {
                theSwipeAdapter.loadMore();
            }
        }

        holder.theSwipe.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // nothing
                // Log.d("MowdigestFakeAdapter", "removeFirstObjectInAdapter");
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                // TODO decrease the interest on the section
                // fix bug: no need to reload cards
                theSwipes.remove(0);
                theSwipeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                // TODO increase the interest on the section
                MowdigestNews theNews = theSwipes.get(0).getNews();
                newsDigest.add(theNews);
                // fix bug: no need to reload cards
                theSwipes.remove(0);
                theSwipeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                if (SwipeContentMode == 1) {
                    theSwipes.add(new MowdigestSwipe(link3, "More"));
                    theSwipeAdapter.notifyDataSetChanged();
                }
                else if (SwipeContentMode == 2) {
                    theSwipeAdapter.loadMore();
                }
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
                View view = holder.theSwipe.getSelectedView();
                if (view != null) {
                    MowdigestSwipeAdapter.ViewHolder viewHolder =
                            (MowdigestSwipeAdapter.ViewHolder) view.getTag();
                    // hide two background layouts
                    viewHolder.item_swipe_background.setAlpha(0);
                    // show right or left indicator
                    if (scrollProgressPercent < 0) {
                        // like
                        viewHolder.item_swipe_right_indicator.setAlpha(-scrollProgressPercent);
                    }
                    else if (scrollProgressPercent > 0) {
                        // nope
                        viewHolder.item_swipe_left_indicator.setAlpha(scrollProgressPercent);
                    }
                    else {
                        viewHolder.item_swipe_right_indicator.setAlpha(0);
                        viewHolder.item_swipe_left_indicator.setAlpha(0);
                    }
                }
            }
        });

        holder.theSwipe.setAdapter(theSwipeAdapter);
    }
}