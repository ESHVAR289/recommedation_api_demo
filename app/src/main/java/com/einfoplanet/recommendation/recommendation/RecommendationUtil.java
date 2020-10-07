package com.einfoplanet.recommendation.recommendation;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.media.tv.TvContract;
import android.net.Uri;
import android.util.Log;

import androidx.tvprovider.media.tv.Channel;
import androidx.tvprovider.media.tv.ChannelLogoUtils;
import androidx.tvprovider.media.tv.PreviewProgram;
import androidx.tvprovider.media.tv.TvContractCompat;

import com.einfoplanet.recommendation.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages interactions with the TV Provider.
 */
public class RecommendationUtil {
    private static final String TAG = RecommendationUtil.class.getSimpleName();
    private static final String SCHEMA_URI_PREFIX = "tvrecommendation://app/";
    public static final String PLAYBACK = "playback";
    public static final String BROWSE = "browse";
    private static final String URI_PLAY = SCHEMA_URI_PREFIX + PLAYBACK;
    private static final String URI_VIEW = SCHEMA_URI_PREFIX + BROWSE;
    private static final String[] CHANNELS_PROJECTION = {
            TvContractCompat.Channels._ID,
            TvContract.Channels.COLUMN_DISPLAY_NAME,
            TvContractCompat.Channels.COLUMN_BROWSABLE
    };

    public static long createChannel(Context context, String channelName) {

        // Checks if our subscription has been added to the channels before.
        Cursor cursor =
                context.getContentResolver()
                        .query(
                                TvContractCompat.Channels.CONTENT_URI,
                                null,
                                null,
                                null,
                                null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Channel channel = Channel.fromCursor(cursor);
                if (channelName.equals(channel.getDisplayName())) {
                    Log.d(
                            TAG,
                            "Channel already exists. Returning channel "
                                    + channel.getId()
                                    + " from TV Provider.");
                    return channel.getId();
                }
            } while (cursor.moveToNext());
        }

        // Create the channel since it has not been added to the TV Provider.
        Uri appLinkIntentUri = Uri.parse(URI_VIEW).buildUpon().appendPath("Trending Videos").build();

        Channel.Builder builder = new Channel.Builder();
        builder.setType(TvContractCompat.Channels.TYPE_PREVIEW)
                .setDisplayName(channelName)
                .setAppLinkIntentUri(appLinkIntentUri);

        Log.d(TAG, "Creating channel: " + channelName);
        Uri channelUrl =
                context.getContentResolver()
                        .insert(
                                TvContractCompat.Channels.CONTENT_URI,
                                builder.build().toContentValues());

        Log.d(TAG, "channel insert at " + channelUrl);
        long channelId = ContentUris.parseId(channelUrl);
        Log.d(TAG, "channel id " + channelId);

        Bitmap bitmap = convertToBitmap(context, R.drawable.app_icon_your_company);
        ChannelLogoUtils.storeChannelLogo(context, channelId, bitmap);

        return channelId;
    }

    public static List<Movie> createPrograms(Context context, long channelId, List<Movie> movies) {

        List<Movie> moviesAdded = new ArrayList<>(movies.size());
        for (Movie movie : movies) {
            PreviewProgram previewProgram = buildProgram(channelId, movie);

            Uri programUri =
                    context.getContentResolver()
                            .insert(
                                    TvContractCompat.PreviewPrograms.CONTENT_URI,
                                    previewProgram.toContentValues());
            long programId = ContentUris.parseId(programUri);
            Log.d(TAG, "Inserted new program: " + programId);
            movie.setProgramId(programId);
            moviesAdded.add(movie);
        }

        return moviesAdded;
    }

    private static PreviewProgram buildProgram(long channelId, Movie movie) {
        Uri posterArtUri = Uri.parse(movie.getCardImageUrl());
        Uri appLinkUri = Uri.parse(URI_PLAY)
                .buildUpon()
                .appendPath(String.valueOf(channelId))
                .appendPath(String.valueOf(movie.getId()))
                .appendPath(String.valueOf(-1))
                .build();
        Uri previewVideoUri = Uri.parse(movie.getVideoUrl());

        PreviewProgram.Builder builder = new PreviewProgram.Builder();
        builder.setChannelId(channelId)
                .setType(TvContractCompat.PreviewProgramColumns.TYPE_CLIP)
                .setTitle(movie.getTitle())
                .setDescription(movie.getDescription())
                .setPosterArtUri(posterArtUri)
                .setPreviewVideoUri(previewVideoUri)
                .setIntentUri(appLinkUri);
        return builder.build();
    }

    /**
     * Converts a resource into a {@link Bitmap}. If the resource is a vector drawable, it will be
     * drawn into a new Bitmap. Otherwise the {@link BitmapFactory} will decode the resource.
     *
     * @param context    used for getting the drawable from resources.
     * @param resourceId of the drawable.
     * @return a bitmap of the resource.
     */
    public static Bitmap convertToBitmap(Context context, int resourceId) {
        Drawable drawable = context.getDrawable(resourceId);
        if (drawable instanceof VectorDrawable) {
            Bitmap bitmap =
                    Bitmap.createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }

        return BitmapFactory.decodeResource(context.getResources(), resourceId);
    }

}
