package kr.co.ddophi.autochangingwallpaper

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*

//Preference Activity 로 설정화면 만들기
class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title = "설정 화면"
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

    override fun onBackPressed() {
        val returnIntent = Intent()
        setResult(Activity.RESULT_OK, returnIntent)
        super.onBackPressed()
    }

    class SettingFragment : PreferenceFragmentCompat(){
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.preferences_setting)

            setOnPreferenceChange(findPreference("TimeValue_Home")!!)
            setOnPreferenceChange(findPreference("TimeType_Home")!!)
            setOnPreferenceChange(findPreference("ImageResize_Home")!!)
            setOnPreferenceChange(findPreference("ImageOrder_Home")!!)
            setOnPreferenceChange(findPreference("TimeValue_Lock")!!)
            setOnPreferenceChange(findPreference("TimeType_Lock")!!)
            setOnPreferenceChange(findPreference("ImageResize_Lock")!!)
            setOnPreferenceChange(findPreference("ImageOrder_Lock")!!)
        }

        //설정 바꿀때마다 Summary 글도 바꾸기
        private val onPreferenceChangeListener : Preference.OnPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                val stringValue = newValue.toString()

                when(preference){
                    is EditTextPreference -> {
                        preference.setSummary(stringValue)
                    }
                    is ListPreference -> {
                        val listPreference : ListPreference = preference
                        val index = listPreference.findIndexOfValue(stringValue)
                        preference.setSummary(listPreference.entries[index])
                    }
                }
                true
            }

        private fun setOnPreferenceChange(preference : Preference){
            preference.onPreferenceChangeListener = onPreferenceChangeListener
            onPreferenceChangeListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.context).getString(preference.key, ""))
        }
    }
}