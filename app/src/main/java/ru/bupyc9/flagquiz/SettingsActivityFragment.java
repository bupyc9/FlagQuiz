package ru.bupyc9.flagquiz;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

public class SettingsActivityFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.preferences);
    }
}