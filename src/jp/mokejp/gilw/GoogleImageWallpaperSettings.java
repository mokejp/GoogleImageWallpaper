package jp.mokejp.gilw;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class GoogleImageWallpaperSettings extends PreferenceActivity
	implements SharedPreferences.OnSharedPreferenceChangeListener{

	public static final String SETTING_KEYWORD = "gilw_keyword";
	public static final String SETTING_SAFE = "gilw_safe";
	public static final String SETTING_WIDTH = "gilw_width";
	public static final String SETTING_HEIGHT = "gilw_height";
	
	private void updateSummary() {
		SharedPreferences pref = getPreferenceManager().getSharedPreferences();
		String defaultAuto = getText(R.string.gilw_settings_default_auto).toString();
		String defaultKeyword = getText(R.string.gilw_settings_default_keyword).toString();
		String keyword = pref.getString(SETTING_KEYWORD, "");
		String width = pref.getString(SETTING_WIDTH, "");
		String height = pref.getString(SETTING_HEIGHT, "");
		if ("".equals(keyword)) {
			keyword = defaultKeyword;
		}
		if ("".equals(width)) {
			width = defaultAuto;
		}
		if ("".equals(height)) {
			height = defaultAuto;
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
	}
	
	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getPreferenceManager().setSharedPreferencesName(GoogleImageWallpaper.SHARED_PREFS_NAME);
        addPreferencesFromResource(R.xml.gilw_settings);
        getPreferenceManager().getSharedPreferences()
        	.registerOnSharedPreferenceChangeListener(this);
        
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

}
