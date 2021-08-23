package com.prasoon.whatsappclone.adapters;

import android.telecom.Call;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.prasoon.whatsappclone.fragments.CallFragment;
import com.prasoon.whatsappclone.fragments.ChatFragment;
import com.prasoon.whatsappclone.fragments.StatusFragment;

public class HomeAdapter extends FragmentStateAdapter {


    public HomeAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0: return new ChatFragment();
            case 1: return new StatusFragment();
            case 2: return new CallFragment();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
