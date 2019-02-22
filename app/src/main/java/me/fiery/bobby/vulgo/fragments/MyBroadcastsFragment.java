package me.fiery.bobby.vulgo.fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * Created by bobby on 1/9/18.
 */

public class MyBroadcastsFragment extends BroadcastListFragment
{
    public MyBroadcastsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference)
    {
        return databaseReference.child("user-broadcasts")
                .child(getUid());
    }
}
