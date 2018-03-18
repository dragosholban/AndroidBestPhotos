package dragosholban.com.bestphotos;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class ImagesActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = ImagesActivity.class.getName();

    private ArrayList<FacebookImage> images = new ArrayList<>();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mRefreshlayout;
    private GraphRequest.Callback callback;
    private Spinner yearsSpinner;
    private ArrayList<String> filterYears = new ArrayList<>(Arrays.asList("All"));
    private ArrayAdapter<CharSequence> yearsAdapter;
    private int selectedYearPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        yearsAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_dropdown_item);

        mRefreshlayout = findViewById(R.id.swiperefresh);
        mRefreshlayout.setOnRefreshListener(this);

        recyclerView = this.findViewById(R.id.recyclerView);
        final ImagesActivity activity = this;

        callback = new GraphRequest.Callback() {

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

                    // add years to filter by
                    String year = String.valueOf(date.getYear() + 1900);
                    if (!filterYears.contains(year)) {
                        filterYears.add(year);
                    }

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
                } else {
                    mRefreshlayout.setRefreshing(false);
                    yearsAdapter.clear();
                    yearsAdapter.addAll(filterYears);
                    yearsAdapter.notifyDataSetChanged();
                    sortImages(images);
                    filterFacebookPhotos();
                }
            }
        };

        loadImages();
    }

    private void sortImages(ArrayList<FacebookImage> images) {
        Collections.sort(images, new Comparator<FacebookImage>() {
            @Override
            public int compare(FacebookImage i1, FacebookImage i2) {
                return ((Integer) i1.reactions).compareTo(i2.reactions);
            }
        });
        Collections.reverse(images);
    }

    public void onImageClick(View view) {
        String link = (String) view.getTag();
        if (link != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }

    @Override
    public void onRefresh() {
        loadImages();
    }

    private void loadImages() {
        images.clear();
        mRefreshlayout.setRefreshing(true);
        GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(), "me/photos?fields=picture,reactions.limit(1).summary(true),link,images,created_time&type=uploaded&limit=500", null, HttpMethod.GET, callback);
        request.executeAsync();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.android_action_bar_spinner_menu, menu);

        MenuItem item = menu.findItem(R.id.spinner);
        yearsSpinner = (Spinner) MenuItemCompat.getActionView(item);
        yearsSpinner.setPadding(0, 0, 0, 0);
        yearsSpinner.setAdapter(yearsAdapter);

        yearsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedYearPosition = i;
                filterFacebookPhotos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return true;
    }

    private void filterFacebookPhotos() {
        ArrayList<FacebookImage> filteredImages = new ArrayList<>();

        if (selectedYearPosition == 0) {
            // all images
            filteredImages = images;
        } else {
            String year = filterYears.get(selectedYearPosition);
            for (FacebookImage image : images) {
                Date imDate = new Date(image.createdAt);
                if (imDate.getYear() + 1900 == Integer.valueOf(year)) {
                    filteredImages.add(image);
                }
            }
        }

        ImageRecyclerViewAdapter adapter = (ImageRecyclerViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.updateData(filteredImages);
        } else {
            recyclerView.setAdapter(new ImageRecyclerViewAdapter(this, filteredImages));
        }
    }
}
