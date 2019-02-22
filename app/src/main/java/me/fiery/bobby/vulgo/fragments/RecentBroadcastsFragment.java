package me.fiery.bobby.vulgo.fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * Created by bobby on 12/19/17.
 */

public class RecentBroadcastsFragment extends BroadcastListFragment
{
    public RecentBroadcastsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference)
    {
        return databaseReference.child("broadcasts")
                .limitToLast(100);
    }
}