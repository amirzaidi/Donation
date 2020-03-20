package amirz.donation;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.appcompat.app.AppCompatDelegate;

public class DonationApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        Resources res = base.getResources();

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        Configuration config = new Configuration(res.getConfiguration());
        config.uiMode = Configuration.UI_MODE_NIGHT_YES;

        super.attachBaseContext(base.createConfigurationContext(config));
    }
}
