/*
 *  Copyright (C) 2016 The Dirty Unicorns Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.lessaosp.settings.fragments;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.ListPreference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import android.os.Handler;
import android.os.RemoteException;
import android.content.res.Resources;
import android.app.ActivityManager;
import android.os.UserHandle;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.lessaosp.settings.preferences.SystemSettingSwitchPreference;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class ButtonSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_NAVIGATION_BAR_ENABLED = "force_show_navbar";
    private static final String KEY_LAYOUT_SETTINGS = "layout_settings";
    private static final String KEY_NAVIGATION_BAR_ARROWS = "navigation_bar_menu_arrow_keys";
    private static final String TORCH_POWER_BUTTON_GESTURE = "torch_power_button_gesture";

    private ListPreference mTorchPowerButton;
    private Preference mLayoutSettings;
    private SwitchPreference mNavigationBar;
    private SystemSettingSwitchPreference mNavigationArrows;

    private boolean mIsNavSwitchingMode = false;

    private Handler mHandler;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.lessaosp_settings_button);

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefSet = getPreferenceScreen();

        // screen off torch
        mTorchPowerButton = (ListPreference) findPreference(TORCH_POWER_BUTTON_GESTURE);
        int mTorchPowerButtonValue = Settings.System.getInt(resolver,
                Settings.System.TORCH_POWER_BUTTON_GESTURE, 0);
        mTorchPowerButton.setValue(Integer.toString(mTorchPowerButtonValue));
        mTorchPowerButton.setSummary(mTorchPowerButton.getEntry());
        mTorchPowerButton.setOnPreferenceChangeListener(this);

        // navigation bar
        final boolean defaultToNavigationBar = getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar);
        final boolean navigationBarEnabled = Settings.System.getIntForUser(
                resolver, Settings.System.FORCE_SHOW_NAVBAR,
                defaultToNavigationBar ? 1 : 0, UserHandle.USER_CURRENT) != 0;

        mNavigationBar = (SwitchPreference) findPreference(KEY_NAVIGATION_BAR_ENABLED);
        mNavigationBar.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.FORCE_SHOW_NAVBAR,
                defaultToNavigationBar ? 1 : 0) == 1));
        mNavigationBar.setOnPreferenceChangeListener(this);

        mLayoutSettings = (Preference) findPreference(KEY_LAYOUT_SETTINGS);

        mNavigationArrows = (SystemSettingSwitchPreference) findPreference(KEY_NAVIGATION_BAR_ARROWS);

        mHandler = new Handler();

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mTorchPowerButton) {
            int mTorchPowerButtonValue = Integer.valueOf((String) newValue);
            int index = mTorchPowerButton.findIndexOfValue((String) newValue);
            mTorchPowerButton.setSummary(
                    mTorchPowerButton.getEntries()[index]);
            Settings.System.putInt(resolver, Settings.System.TORCH_POWER_BUTTON_GESTURE,
                    mTorchPowerButtonValue);
        } else if (preference == mNavigationBar) {
            boolean value = (Boolean) newValue;
            if (mIsNavSwitchingMode) {
                return false;
            }
            mIsNavSwitchingMode = true;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.FORCE_SHOW_NAVBAR, value ? 1 : 0);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIsNavSwitchingMode = false;
                }
            }, 1500);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.LESS_SETTINGS;
    }
    /**
     * For Search.
     */

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.lessaosp_settings_button);
}