package com.ota.updater.two;

import android.app.Dialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class AccountsScreen extends PreferenceActivity implements OnPreferenceClickListener {
    private Dialog dialog;

    private Preference viewProfile;

    @Override
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.accounts_screen);

        dialog = new Dialog(this);
        dialog.setTitle("Profile");
        dialog.setContentView(R.layout.profile_dialog);

        viewProfile = findPreference("view_account_pref");
        viewProfile.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == viewProfile) {
            dialog.show();
            return true;
        }
        return false;
    }
}
