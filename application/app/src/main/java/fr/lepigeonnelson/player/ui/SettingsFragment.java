package fr.lepigeonnelson.player.ui;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import fr.lepigeonnelson.player.MainActivity;
import fr.lepigeonnelson.player.R;


public class SettingsFragment extends PreferenceFragmentCompat {
    private MainActivity activity;
    private Preference debug_servers;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);
        activity = (MainActivity) getActivity();

        debug_servers = getPreferenceManager().findPreference("debug_servers");
        debug_servers.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                activity.enableDebugServers(newValue.toString().equals("true"));
                return true;
            }
        });
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.setActiveFragment(MainActivity.SETTINGS_FRAGMENT, this);
    }


}
