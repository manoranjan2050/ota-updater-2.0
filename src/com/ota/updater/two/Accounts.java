/*
 * Copyright (C) 2012 OTA Update Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may only use this file in compliance with the license and provided you are not associated with or are in co-operation anyone by the name 'X Vanderpoel'.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ota.updater.two;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

public class Accounts extends DialogPreference implements OnSharedPreferenceChangeListener {
    private AutoCompleteTextView user;
    private EditText pass;

    public Accounts(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.accounts);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        user = (AutoCompleteTextView) view.findViewById(R.id.usernameView);
        pass = (EditText) view.findViewById(R.id.passwordView);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            String username = user.getText().toString();
            String password = pass.getText().toString();

            SharedPreferences prefs = getContext().getSharedPreferences("VRToolkit", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("key_username", username);
            editor.putString("key_password", password);
            editor.apply();
        } else {
            user.setText("");
            pass.setText("");
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
        // TODO Auto-generated method stub
    }
}
