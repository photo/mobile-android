
package me.openphoto.android.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * The activity that gets presented to the user in case the user is not logged
 * in to a server. - setup screen
 * 
 * @author Patrick Boos
 */
public class SetupActivity extends Activity implements OnClickListener {
    public static final String TAG = SetupActivity.class.getSimpleName();

    private static final int REQUEST_LOGIN = 0;

    /**
     * Called when Setup Activity is first loaded
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        ((Button) findViewById(R.id.button_login)).setOnClickListener(this);
        ((Button) findViewById(R.id.button_create_account)).setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LOGIN:
                if (resultCode == RESULT_OK) {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.button_login:
                String server = ((EditText) findViewById(R.id.edit_server)).getText().toString();
                Preferences.setServer(this, server);
                startActivityForResult(new Intent(this, OAuthActivity.class), REQUEST_LOGIN);
                break;
            case R.id.button_create_account:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://openphoto.me/"));
                startActivity(browserIntent);
                break;
        }

    }
}
