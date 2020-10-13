package com.example.exercise2.ui.community;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.exercise2.BaseActivity;
import com.example.exercise2.FirebasePost;
import com.example.exercise2.MainActivity;
import com.example.exercise2.NewPostActivity;
import com.example.exercise2.R;
import com.example.exercise2.databinding.FragmentCommunityBinding;
import com.example.exercise2.fragment.MyPostsFragment;
import com.example.exercise2.fragment.MyTopPostsFragment;
import com.example.exercise2.fragment.RecentPostsFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class CommunityFragment extends Fragment {

    private static final String TAG = "CommunityFragment";
    private CommunityViewModel slideshowViewModel;
    private FragmentCommunityBinding binding;

    public static CommunityFragment newInstance()
    {
        return  new CommunityFragment();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        slideshowViewModel =
                ViewModelProviders.of(this).get(CommunityViewModel.class);

        binding = FragmentCommunityBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Create the adapter that will return a fragment for each section
        FragmentPagerAdapter mPagerAdapter = new FragmentPagerAdapter(((MainActivity)getActivity()).getSupportFragmentChildManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            private final Fragment[] mFragments = new Fragment[]{
                    new RecentPostsFragment(),
                    new MyPostsFragment(),
                    new MyTopPostsFragment(),
            };
            private final String[] mFragmentNames = new String[]{
                    getString(R.string.heading_recent),
                    getString(R.string.heading_my_posts),
                    getString(R.string.heading_my_top_posts)
            };

            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }

            @Override
            public int getCount() {
                return mFragments.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mFragmentNames[position];
            }
        };
        // Set up the ViewPager with the sections adapter.
        binding.container.setAdapter(mPagerAdapter);
        binding.tabs.setupWithViewPager(binding.container);

        // Button launches NewPostActivity
        binding.fabNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).Post_activity();
            }
        });

        //FloatingActionButton fab = root.findViewById(R.id.fabNewPost);
        //fab.setOnClickListener(new View.OnClickListener(){
        //    @Override
        //    public void onClick(View v) {
        //        ((MainActivity)getActivity()).Post_activity();
        //    }
        //});

        return root;
    }
    public MyPostsFragment getVisibleFragment_Mypost() {
        FragmentManager fragmentManager = ((MainActivity)getActivity()).getSupportFragmentChildManager();
        for(Fragment fragment: fragmentManager.getFragments())
        {
            if(fragment.isVisible())
            {
                return ((MyPostsFragment)fragment.getChildFragmentManager().getPrimaryNavigationFragment());
            }
        }
        return null;
    }
}