package rs.elfak.jajac.geowarfare;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    private EditText mUsername;
    private EditText mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_login);

        mUsername = (EditText) findViewById(R.id.login_email_text);
        mPassword = (EditText) findViewById(R.id.login_password_text);
    }

    public void onNoAccountClick(View v) {
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
    }

}
