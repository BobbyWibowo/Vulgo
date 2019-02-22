package me.fiery.bobby.vulgo.models;

import android.support.annotation.Nullable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bobby on 12/18/17.
 */

@IgnoreExtraProperties
public class Broadcast
{
    public String uid;
    public String author;
    public String title;
    public String description;
    public String contact;
    public String reward;
    public String latitude;
    public String longitude;
    public String photo;
    public long timestamp;

    public Broadcast()
    {
        // Default constructor required for calls to DataSnapshot.getValue(Broadcast.class)
    }

    public Broadcast(
            String uid,
            String displayName,
            String title,
            String description,
            String contact,
            @Nullable String reward,
            String latitude,
            String longitude,
            @Nullable  String photo,
            long timestamp)
    {
        this.uid = uid;
        this.author = displayName;
        this.title = title;
        this.description = description;
        this.contact = contact;
        this.reward = reward;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photo = photo;
        this.timestamp = timestamp;
    }

    @Exclude
    public Map<String, Object> toMap()
    {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("title", title);
        result.put("description", description);
        result.put("contact", contact);
        result.put("reward", reward);
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        result.put("photo", photo);
        result.put("timestamp", timestamp);

        return result;
    }
}
