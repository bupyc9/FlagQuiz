package ru.bupyc9.flagquiz;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

public class SettingsActivityFragment extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.preferences);
    }
}