package me.fiery.bobby.vulgo;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Locale;

public class SignInActivity extends BaseActivity
{
    private static final String TAG = "SignInActivity";

    final static String EXTRA_IS_SIGN_OUT = "extra_is_sign_out";

    private final int RC_SIGN_IN_GOOGLE = 100;

    private EditText mEmail;
    private EditText mPassword;

    private Button mSignIn;
    private SignInButton mSignInGoogle;
    private TextView mSignUp;

    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // This will be set to TRUE by MainActivity when the logout button is clicked.
        // Auth will be signed out from this activity instead of MainActivity to handle
        // "permission denied" errors being thrown out in the split second after auth
        // was signed out to MainActivity being finished.
        boolean isSignOut = getIntent().getBooleanExtra(EXTRA_IS_SIGN_OUT, false);
        if (isSignOut)
        {
            FirebaseAuth.getInstance().signOut();
            showToast(getString(R.string.logout_toast));
        }

        // Set title in action bar.
        getSupportActionBar().setTitle(getString(R.string.sign_in_activity_title));

        // Get views.
        mEmail = findViewById(R.id.et_email);
        mPassword = findViewById(R.id.et_password);
        mSignIn = findViewById(R.id.btn_sign_in);
        mSignInGoogle = findViewById(R.id.btn_sign_in_google);
        mSignUp = findViewById(R.id.tv_sign_up);

        // Button onClick listeners.
        mSignIn.setOnClickListener(OnClickListener);
        mSignInGoogle.setOnClickListener(OnClickListener);
        mSignUp.setOnClickListener(OnClickListener);

        // Force locale to Indonesian since we do not have multilingual strings.
        forceLocale(new Locale("in"));
    }

    private void forceLocale(Locale locale)
    {
        Configuration resourceConfiguration = getBaseContext().getResources().getConfiguration();
        resourceConfiguration.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(resourceConfiguration, getResources().getDisplayMetrics());

        Configuration systemConfiguration = Resources.getSystem().getConfiguration();
        systemConfiguration.setLocale(locale);
        Resources.getSystem().updateConfiguration(systemConfiguration, getResources().getDisplayMetrics());

        Locale.setDefault(locale);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        int googlePlayServicesAvailability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        // Maybe handle other ConnectionResult such as ConnectionResult.SERVICE_DISABLED, etc.?
        boolean isGooglePlayServicesAvailable = googlePlayServicesAvailability == ConnectionResult.SUCCESS;

        // Toggle form whenever the activity starts (this also assume cases
        // after user comes back from sorting out their Google Play Services).
        toggleForm(isGooglePlayServicesAvailable);

        // Pretty much everything in this app requires Google API.
        if (isGooglePlayServicesAvailable)
        {
            // Check Firebase auth.
            // Finish activity if the user had already logged in, of course.
            FirebaseUser firebaseUser = mAuth.getCurrentUser();
            if (firebaseUser != null)
            {
                finishThisActivity();
            }
        }
        else
        {
            // Show a non-dismissable alert dialog.
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.alert_missing_play_services_message))
                    .show();
        }
    }

    private void toggleForm(boolean state)
    {
        mEmail.setEnabled(state);
        mPassword.setEnabled(state);
        mSignIn.setEnabled(state);
        mSignInGoogle.setEnabled(state);
        mSignUp.setEnabled(state);

        // Temporary measure since I'm a bit confused as to how to make color
        // that automatically changes whenever the view is enabled/disabled.
        if (state)
        {
            mSignUp.setTextColor(getResources().getColor(R.color.sign_up_textview));
        }
        else
        {
            mSignUp.setTextColor(getResources().getColor(R.color.sign_up_textview_disabled));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                case RC_SIGN_IN_GOOGLE:
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try
                    {
                        // Google Sign In was successful, then authenticate with Firebase.
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account);
                    }
                    catch (ApiException e)
                    {
                        Log.w(TAG, "Google sign in failed", e);
                        showToast(getString(R.string.auth_failed_google));
                    }

                    break;
            }
        }
        else
        {
            Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode);
        }
    }

    private void signIn()
    {
        Log.d(TAG, "signIn");

        if (!validateForm())
        {
            return;
        }

        showProgressDialog();
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                hideProgressDialog();

                if (task.isSuccessful())
                {
                    Log.d(TAG, "signIn:success:" + task.isSuccessful());
                    finishThisActivity();
                }
                else
                {
                    Log.d(TAG, "signIn:failure:" + task.isSuccessful());
                    showToast(getString(R.string.auth_failed));
                }
            }
        });
    }

    private boolean validateForm()
    {
        boolean result = true;

        if (TextUtils.isEmpty(mEmail.getText().toString()))
        {
            mEmail.setError(getString(R.string.required));
            result = false;
        }
        else
        {
            mEmail.setError(null);
        }

        if (TextUtils.isEmpty(mPassword.getText().toString()))
        {
            mPassword.setError(getString(R.string.required));
            result = false;
        }
        else
        {
            mPassword.setError(null);
        }

        return result;
    }

    private void signInGoogle()
    {
        Log.d(TAG, "signInGoogle");

        // Init options for Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE);
            }
        });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct)
    {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if (task.isSuccessful())
                {
                    Log.d(TAG, "signInWithCredential:success");

                    hideProgressDialog();
                    finishThisActivity();
                }
                else
                {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.getException());

                    hideProgressDialog();
                    showToast(getString(R.string.auth_failed_google));
                }
            }
        });

    }

    private void finishThisActivity()
    {
        // Go to MainActivity
        startActivity(new Intent(SignInActivity.this, MainActivity.class));
        finish();
    }

    private View.OnClickListener OnClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            int id = view.getId();
            switch (id)
            {
                case R.id.btn_sign_in:
                    signIn();
                    break;
                case R.id.btn_sign_in_google:
                    signInGoogle();
                    break;
                case R.id.tv_sign_up:
                    startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
                    break;
            }
        }
    };
}