package dragosholban.com.bestphotos;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.Arrays;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean loggedIn = AccessToken.getCurrentAccessToken() != null;
        if (loggedIn) {
            Intent intent = new Intent(this, ImagesActivity.class);
            this.startActivity(intent);

            return;
        }

        LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("user_photos"));

        final Activity activity = this;

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
                    Intent intent = new Intent(activity, ImagesActivity.class);
                    activity.startActivity(intent);
                } else {
                    Toast.makeText(activity, "Please allow us to access your Facebook photos in order to show you the best ones.", Toast.LENGTH_LONG).show();
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
