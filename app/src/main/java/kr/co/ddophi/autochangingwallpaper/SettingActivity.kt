package kr.co.ddophi.autochangingwallpaper

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*

//Preference Activity 로 설정화면 만들기
class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setTitle("설정 화면")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, SettingFragment())
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingFragment : PreferenceFragmentCompat(){
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.preferences_setting)

            setOnPreferenceChange(findPreference("TimeCycleNumber")!!)
            setOnPreferenceChange(findPreference("TimeCycleType")!!)
            setOnPreferenceChange(findPreference("ImageResize")!!)
            setOnPreferenceChange(findPreference("ImageOrder")!!)
        }

        val onPreferenceChangeListener : Preference.OnPreferenceChangeListener = object: Preference.OnPreferenceChangeListener{
            override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {

                val stringValue = newValue.toString()

                when(preference){
                    is EditTextPreference -> {
                        preference.setSummary(stringValue)
                    }
                    is ListPreference -> {
                        val listPreference = preference as ListPreference
                        val index = listPreference.findIndexOfValue(stringValue)
                        preference.setSummary(listPreference.entries[index])
                    }
                }
                return true
            }
        }

        fun setOnPreferenceChange(preference : Preference){
            preference.onPreferenceChangeListener = onPreferenceChangeListener
            onPreferenceChangeListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.context).getString(preference.key, ""))
        }
    }
}