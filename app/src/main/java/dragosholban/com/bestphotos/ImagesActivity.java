package dragosholban.com.bestphotos;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ImagesActivity extends AppCompatActivity {
    private static final String TAG = ImagesActivity.class.getName();

    private ArrayList<FacebookImage> images = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        GraphRequest.Callback callback = new GraphRequest.Callback() {

            @Override
            public void onCompleted(GraphResponse response) {
                JSONObject json = response.getJSONObject();
                Log.d(TAG, "Photos: " + json.toString());

                Gson gson = new Gson();
                FacebookPhotos fbPhotos =  gson.fromJson(json.toString(), FacebookPhotos.class);
                for (FacebookPhotos.Datum datum : fbPhotos.data) {
                    Log.d(TAG, "Image URL: " + datum.picture);

                    FacebookImage image = new FacebookImage();
                    image.fbId = datum.id;
                    image.reactions = datum.reactions.summary.total_count;
                    image.link = datum.link;
                    image.url = datum.picture;

                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+SSSS");
                    Date date = new Date();
                    try {
                        date = format.parse(datum.created_time);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    image.createdAt = date.getTime();

                    // find the closest image to 300x300px
                    FacebookPhotos.Datum.Image imageSource = null;
                    for (FacebookPhotos.Datum.Image im : datum.images) {
                        if (imageSource == null) {
                            imageSource = im;
                        } else {
                            if (imageSource.width < imageSource.height) {
                                if (imageSource.width <= 300 && imageSource.width < im.width || imageSource.width > 300 && im.width > 300 && imageSource.width > im.width) {
                                    imageSource = im;
                                }
                            } else {
                                if (imageSource.height <= 300 && imageSource.height < im.height || imageSource.height > 300 && im.height > 300 && imageSource.height > im.height) {
                                    imageSource = im;
                                }
                            }
                        }
                    }
                    if (imageSource != null) {
                        image.url = imageSource.source;
                    }

                    images.add(image);
                }

                if (fbPhotos.paging != null && fbPhotos.paging.cursors.after != null) {
                    GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(), "me/photos?fields=picture,reactions.limit(1).summary(true),link,images,created_time&type=uploaded&limit=500&after=" + fbPhotos.paging.cursors.after, null, HttpMethod.GET, this);
                    request.executeAsync();
                }
            }
        };

        GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(), "me/photos?fields=picture,reactions.limit(1).summary(true),link,images,created_time&type=uploaded&limit=500", null, HttpMethod.GET, callback);
        request.executeAsync();
    }
}
