package me.fiery.bobby.vulgo.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import me.fiery.bobby.vulgo.BroadcastDetailActivity;
import me.fiery.bobby.vulgo.MainActivity;
import me.fiery.bobby.vulgo.R;
import me.fiery.bobby.vulgo.models.Broadcast;
import me.fiery.bobby.vulgo.viewholder.BroadcastViewHolder;

/**
 * Created by bobby on 12/19/17.
 */

public abstract class BroadcastListFragment extends Fragment
{
    private static final String TAG = "BroadcastListFragment";

    private DatabaseReference mDatabase;

    private FirebaseRecyclerAdapter<Broadcast, BroadcastViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    public BroadcastListFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_broadcasts, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mRecycler = rootView.findViewById(R.id.broadcasts_list);
        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = getQuery(mDatabase);

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Broadcast>()
                .setQuery(postsQuery, Broadcast.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<Broadcast, BroadcastViewHolder>(options)
        {
            @Override
            public BroadcastViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
            {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new BroadcastViewHolder(inflater.inflate(R.layout.item_broadcast, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(BroadcastViewHolder viewHolder, int position, final Broadcast model)
            {
                final DatabaseReference broadcastRef = getRef(position);

                // Set click listener for the whole broadcast view
                final String broadcastKey = broadcastRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        MainActivity mainActivity = (MainActivity) getActivity();

                        Intent intent = new Intent(getActivity(), BroadcastDetailActivity.class);

                        intent.putExtra(BroadcastDetailActivity.EXTRA_BROADCAST_KEY, broadcastKey);

                        if (mainActivity != null)
                        {
                            intent.putExtra(BroadcastDetailActivity.EXTRA_COARSE_LOCATION, mainActivity.getLastLocation());
                        }

                        startActivity(intent);
                    }
                });

                // Bind Broadcast to ViewHolder
                viewHolder.bindToBroadcast(model, new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        MainActivity mainActivity = (MainActivity) getActivity();

                        if (mainActivity != null)
                        {
                            switch (view.getId())
                            {
                                case R.id.button_view_location:
                                    mainActivity.launchViewMapsIntentForBroadcast(model, null);
                                    break;
                                case R.id.button_share:
                                    mainActivity.launchShareIntentForBroadcast(model);
                                    break;
                            }
                        }
                    }
                });
            }
        };
        mRecycler.setAdapter(mAdapter);
    }


    @Override
    public void onStart()
    {
        super.onStart();
        if (mAdapter != null)
        {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (mAdapter != null)
        {
            mAdapter.stopListening();
        }
    }

    /*
    @Override
    public void onResume()
    {
        super.onResume();
        if (mAdapter != null)
        {
            mAdapter.startListening();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mAdapter != null)
        {
            mAdapter.stopListening();
        }
    }
    */

    public String getUid()
    {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);
}