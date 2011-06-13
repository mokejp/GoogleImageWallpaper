package jp.mokejp.gilw;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class GoogleImageWallpaperSettings extends PreferenceActivity
	implements SharedPreferences.OnSharedPreferenceChangeListener{

	public static final String SETTING_KEYWORD = "gilw_keyword";
	public static final String SETTING_SAFE = "gilw_safe";
	public static final String SETTING_WIDTH = "gilw_width";
	public static final String SETTING_HEIGHT = "gilw_height";
	public static final String SETTING_REFRESHINTERVAL = "gilw_refreshinterval";
	
	/**
	 * 設定値入力チェック
	 */
	private OnPreferenceChangeListener editTextPreference_OnPreferenceChangeListener = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			// TODO 自動生成されたメソッド・スタブ
			return editTextPreference_OnPreferenceChange(preference,newValue);
		}
	};
	
	private void updateSummary() {
		SharedPreferences pref = getPreferenceManager().getSharedPreferences();
		String defaultAuto = getText(R.string.gilw_settings_default_auto).toString();
		String defaultKeyword = getText(R.string.gilw_settings_default_keyword).toString();
		String defaultRefreshInterval = getText(R.string.gilw_settings_default_refreshinterval).toString();
		String keyword = pref.getString(SETTING_KEYWORD, "");
		String width = pref.getString(SETTING_WIDTH, "");
		String height = pref.getString(SETTING_HEIGHT, "");
		String refreshInterval = pref.getString(SETTING_REFRESHINTERVAL, "");
		if ("".equals(keyword)) {
			keyword = defaultKeyword;
		}
		if ("".equals(width)) {
			width = defaultAuto;
		}
		if ("".equals(height)) {
			height = defaultAuto;
		}
		if ("".equals(refreshInterval)) {
			refreshInterval = defaultRefreshInterval;
		}
		findPreference(SETTING_KEYWORD).setSummary(keyword);
		String[] safeList = getResources().getStringArray(R.array.gilw_settings_list_safe);
		String[] safeValueList = getResources().getStringArray(R.array.gilw_settings_list_safe_value);
		String safe = pref.getString(SETTING_SAFE, "");
		for (int i = 0; i < safeValueList.length; i++) {
			if (safeValueList[i].equals(safe)) {
				findPreference(SETTING_SAFE).setSummary(safeList[i]);
			}
		}
		findPreference(SETTING_WIDTH).setSummary(width);
		findPreference(SETTING_HEIGHT).setSummary(height);
		findPreference(SETTING_REFRESHINTERVAL).setSummary(refreshInterval);
	}
	
	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getPreferenceManager().setSharedPreferencesName(GoogleImageWallpaper.SHARED_PREFS_NAME);
        addPreferencesFromResource(R.xml.gilw_settings);
        getPreferenceManager().getSharedPreferences()
        	.registerOnSharedPreferenceChangeListener(this);
        findPreference(SETTING_WIDTH).setOnPreferenceChangeListener(editTextPreference_OnPreferenceChangeListener);
        findPreference(SETTING_HEIGHT).setOnPreferenceChangeListener(editTextPreference_OnPreferenceChangeListener);
        findPreference(SETTING_REFRESHINTERVAL).setOnPreferenceChangeListener(editTextPreference_OnPreferenceChangeListener);
        updateSummary();
	}
	
	@Override
    protected void onResume() {
        super.onResume();
    }

	
	@Override
    protected void onDestroy() {
        getPreferenceManager()
        	.getSharedPreferences()
        	.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		updateSummary();
	}
	
	private boolean editTextPreference_OnPreferenceChange(Preference preference, Object newValue){
		if (SETTING_REFRESHINTERVAL.equals(preference.getKey())) {
			try {
				newValue = Integer.parseInt((String)newValue);
			} catch (NumberFormatException ex) {
				return false;
			}
		}
		if (SETTING_WIDTH.equals(preference.getKey())) {
			try {
				newValue = Integer.parseInt((String)newValue);
			} catch (NumberFormatException ex) {
				return false;
			}
		}
		if (SETTING_HEIGHT.equals(preference.getKey())) {
			try {
				newValue = Integer.parseInt((String)newValue);
			} catch (NumberFormatException ex) {
				return false;
			}
		}
		return true;
	}
}
