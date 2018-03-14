package dragosholban.com.bestphotos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("user_photos"));

        // Callback registration
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "Facebook login token: " + loginResult.getAccessToken().getToken());

                // Check permissions
                Boolean granted = false;
                Set<String> permissions = AccessToken.getCurrentAccessToken().getPermissions();
                for (String permission : permissions) {
                    if (permission.equals("user_photos")) {
                        granted = true;
                    }
                }

                if (granted) {
                    GraphRequest.Callback callback = new GraphRequest.Callback() {

                        @Override
                        public void onCompleted(GraphResponse response) {
                            JSONObject json = response.getJSONObject();
                            Log.d(TAG, "Photos: " + json.toString());
                        }
                    };

                    GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(), "me/photos?fields=picture,reactions.limit(1).summary(true),link,images,created_time&type=uploaded&limit=500", null, HttpMethod.GET, callback);
                    request.executeAsync();
                }
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "Facebook login canceled.");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "Facebook login error: " + error.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
