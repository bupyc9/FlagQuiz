package ru.bupyc9.flagquiz;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    public static final String CHOICES = "pref_numberOfChoices";
    public static final String REGIONS = "pref_regionsToInclude";
    private boolean phoneDevice = true;
    private boolean preferenceChange = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        // Задание значений по умолчанию в файле SharedPreferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        // Регистрация слушателя для изменений SharedPreferences
        PreferenceManager
                .getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(preferenceChangeListListener);
        // Определение размера экрана
        int screenSize = this.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        // Для планшетного устройства phoneDevice присваивается false
        if (
            screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE
            || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE
        ) {
            this.phoneDevice = false; // Не соответствует размерам телефона
        }

        // На телефоне разрешена только портретная ориентация
        if (this.phoneDevice) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (this.preferenceChange) {
            // После задания настроек по умолчанию инициализировать
            // MainActivityFragment и запустить викторину
            MainActivityFragment quizFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.quizFragment);
            quizFragment.updateGuessRows(PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.updateRegions(PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.resetQuiz();
            this.preferenceChange = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Получение текущей ориентации устройства
        int orientation = this.getResources().getConfiguration().orientation;
        // Отображение меню приложения только в портретной ориентации
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Заполненое меню
            this.getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent preferenceIntent = new Intent(this, SettingsActivity.class);
        this.startActivity(preferenceIntent);

        return super.onOptionsItemSelected(item);
    }
}
