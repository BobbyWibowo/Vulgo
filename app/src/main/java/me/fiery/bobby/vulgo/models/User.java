package me.fiery.bobby.vulgo.models;

import android.support.annotation.Nullable;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by bobby on 12/19/17.
 */

@IgnoreExtraProperties
public class User
{
    public String displayName;
    public String email;
    public String photoUri;

    public User()
    {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String email, @Nullable String displayName, @Nullable String photoUri)
    {
        this.displayName = displayName;
        this.email = email;
        this.photoUri = photoUri;
    }
}