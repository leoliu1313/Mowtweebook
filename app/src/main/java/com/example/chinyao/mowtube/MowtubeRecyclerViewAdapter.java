package com.example.chinyao.mowtube;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Created by chinyao on 7/23/2016.
 */
public class MowtubeRecyclerViewAdapter
        extends RecyclerView.Adapter<MowtubeRecyclerViewAdapter.ViewHolder> {

    // private final TypedValue mTypedValue = new TypedValue();
    // private int mBackground;
    private List<MowtubeMovie> mMovies;
    private RecyclerView mRecyclerView;


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mowtube_list_item, parent, false);
        // view.setBackgroundResource(mBackground);
        return new ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;
        // public String mBoundString;

        @BindView(R.id.r_movie_image) ImageView mImageView;
        @BindView(R.id.r_title) TextView mTextViewTitle;
        @BindView(R.id.r_sub_title) TextView mTextView2;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            ButterKnife.bind(this, view);
        }
    }

    public MowtubeRecyclerViewAdapter(Context context, List<MowtubeMovie> items, RecyclerView rv) {
        // context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        // mBackground = mTypedValue.resourceId;
        mMovies = items;
        mRecyclerView = rv;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        MowtubeMovie theMowtubeMovie = mMovies.get(position);
        if (MowtubeListFragment.ContentMode == 1) {
            // holder.mBoundString = mMovies.get(position).title;
            holder.mTextViewTitle.setText(theMowtubeMovie.title);
            Glide.with(holder.mImageView.getContext())
                    .load(MowActivity.getRandomDrawable())
                    .fitCenter()
                    .into(holder.mImageView);
        }
        else if (MowtubeListFragment.ContentMode == 2) {
            holder.mTextViewTitle.setText(theMowtubeMovie.title);
            holder.mTextView2.setText(theMowtubeMovie.release_date);
            if (MowtubeListFragment.ImageLoadingMode == 1) {
                // Glide supports gif only in load()
                // speed up gif by diskCacheStrategy(DiskCacheStrategy.SOURCE)
                Glide.with(holder.mImageView.getContext())
                        .load("http://image.tmdb.org/t/p/w500" + theMowtubeMovie.backdrop_path)
                        .centerCrop()
                        .placeholder(R.drawable.blobb)
                        .error(R.drawable.tmdb)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(holder.mImageView);

            }
            else if (MowtubeListFragment.ImageLoadingMode == 2) {
                if (theMowtubeMovie.backdrop_path.equals("null")) {
                    Glide.with(holder.mImageView.getContext())
                            .load(R.drawable.blobb)
                            .centerCrop()
                            .placeholder(R.drawable.blobb)
                            .error(R.drawable.tmdb)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .into(holder.mImageView);
                }
                else {
                    // Picasso does not support gif
                    Picasso.with(holder.mImageView.getContext())
                            .load("http://image.tmdb.org/t/p/w500" + theMowtubeMovie.backdrop_path)
                            .transform(new RoundedCornersTransformation(10, 10))
                            .fit().centerCrop()
                            .placeholder(R.drawable.blobb)
                            .error(R.drawable.tmdb)
                            .into(holder.mImageView);
                }
            }
            // https://github.com/rtheunissen/md-preloader
            // https://thomas.vanhoutte.be/miniblog/android-lollipop-animated-loading-gif/
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRecyclerView == null) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, MowActivity.class);
                    // intent.putExtra(MowActivity.EXTRA_NAME, holder.mBoundString);
                    context.startActivity(intent);
                }
                else {
                    int position = mRecyclerView.getChildAdapterPosition(v);
                    final int id = mMovies.get(position).id;
                    final MowtubeActivity activity = (MowtubeActivity) v.getContext();

                    AsyncHttpClient client = new AsyncHttpClient();
                    // Turn off Debug Log
                    client.setLoggingEnabled(false);
                    String url = "https://api.themoviedb.org/3/movie/" + id + "/trailers";
                    RequestParams params = new RequestParams();
                    params.put("api_key", MowtubeActivity.TMDB_API_KEY);
                    Log.d("RecyclerViewAdapter", url);
                    client.get(url, params, new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                    try {
                                        JSONArray moviesJson = response.getJSONArray("youtube");
                                        for (int i=0; i < moviesJson.length(); i++) {
                                            response = moviesJson.getJSONObject(i);
                                            if (response.getString("type").equals("Trailer")) {
                                                activity.loadBottom(response.getString("source"), id);
                                                return;
                                            }
                                        }
                                        if (moviesJson.length() > 0) {
                                            // https://api.themoviedb.org/3/movie/316727/trailers?api_key=a07e22bc18f5cb106bfe4cc1f83ad8ed
                                            // Some movies only have Featurette instead of Trailer
                                            response = moviesJson.getJSONObject(0);
                                            activity.loadBottom(response.getString("source"), id);
                                            return;
                                        }
                                        activity.loadBottom(MowtubeActivity.YOUTUBE_DEFAULT_LINK, id);
                                        Toast.makeText(activity,
                                                "No Trailer...",
                                                Toast.LENGTH_LONG)
                                                .show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                                }
                            }
                    );
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }
}