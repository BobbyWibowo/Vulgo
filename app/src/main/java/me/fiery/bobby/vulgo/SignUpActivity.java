package me.fiery.bobby.vulgo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class SignUpActivity extends BaseActivity
{
    private static final String TAG = "SignUpActivity";

    private EditText mName;
    private EditText mEmail;
    private EditText mPassword;

    private Button mSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().setTitle(getString(R.string.sign_up_activity_title));

        mName = findViewById(R.id.et_name);
        mEmail = findViewById(R.id.et_email);
        mPassword = findViewById(R.id.et_password);

        mSignUp = findViewById(R.id.btn_sign_up);

        mSignUp.setOnClickListener(OnClickListener);
    }

    private void signUp()
    {
        Log.d(TAG, "signUp");

        if (!validateForm())
        {
            return;
        }

        showConfirmationAlert(
                null,
                getString(R.string.alert_sign_up_message),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        showProgressDialog();

                        final String name = mName.getText().toString();
                        final String email = mEmail.getText().toString();
                        final String password = mPassword.getText().toString();

                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task)
                            {
                                Log.d(TAG, "createUser:onComplete:" + task.isSuccessful());

                                hideProgressDialog();

                                if (task.isSuccessful())
                                {
                                    finishThisActivity(name);
                                }
                                else
                                {
                                    showToast(getString(R.string.sign_up_failed));
                                }
                            }
                        });
                    }
                }
        );
    }

    private void finishThisActivity(String name)
    {
        // Go to MainActivity
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_DISPLAY_NAME, name);
        startActivity(intent);
        finish();
    }

    private boolean validateForm()
    {
        boolean result = true;

        if (TextUtils.isEmpty(mName.getText().toString()))
        {
            mName.setError(getString(R.string.required));
            result = false;
        }
        else
        {
            mName.setError(null);
        }

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
        else if (mPassword.getText().toString().length() < 6)
        {
            mPassword.setError(getString(R.string.password_length_error));
            result = false;
        }
        else
        {
            mPassword.setError(null);
        }

        return result;
    }

    private View.OnClickListener OnClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            int id = view.getId();
            switch (id)
            {
                case R.id.btn_sign_up:
                    signUp();
                    break;
            }
        }
    };
}
