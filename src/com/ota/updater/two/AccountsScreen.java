package com.ota.updater.two;

import android.app.Dialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class AccountsScreen extends PreferenceActivity {
    public static Dialog dialog;

    @Override
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.accounts_screen);

        dialog = new Dialog(this);
        dialog.setTitle("Profile");
        dialog.setContentView(R.layout.profile_dialog);

        Preference view_profile = findPreference("view_account_pref");
        view_profile.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                dialog.show();
                return false;
            }
        });
    }
}
