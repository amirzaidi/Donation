package amirz.donation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.android.billingclient.api.SkuDetails;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import amirz.shade.ShadeFont;

public class DonationActivity extends AppCompatActivity implements BillingHandler.BillingCallbacks {
    static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    private static final String TAG = "DonationActivity";
    private static final List<String> SKU = Arrays.asList("donation.regular", "donation.large");

    private BillingHandler mBilling;
    private FloatingActionButton mFab;
    private boolean mHideSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ShadeFont.override(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mBilling = new BillingHandler(this, this, SKU);

        mFab = findViewById(R.id.fab);
        mFab.setEnabled(false);
        mFab.setOnClickListener(v -> {
            List<SkuDetails> skus = new ArrayList<>(mBilling.getSkus());
            if (!skus.isEmpty()) {
                Collections.sort(skus, (o1, o2) -> Long.compare(
                        o1.getPriceAmountMicros(),
                        o2.getPriceAmountMicros()));
                mBilling.buy(skus.get(0));
            }
        });
    }

    @Override
    public void onStateChanged(boolean connected) {
        Log.w(TAG, "onStateChanged " + connected);
        mFab.setEnabled(connected);
    }

    @Override
    public void onPurchased(SkuDetails sku, boolean isNew) {
        Log.w(TAG, "onPurchased " + sku.getSku() + " " + isNew);
        if (!isNew) {
            if (mHideSnackbar) {
                return;
            } else {
                mHideSnackbar = true;
            }
        }
        Snackbar.make(mFab, isNew
                ? R.string.donate_new
                : R.string.donate_history,
                Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        mBilling.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_donation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_telegram) {
            sendTelegram();
            return true;
        } else if (id == R.id.action_mail) {
            sendMail();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendTelegram() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://t.me/mirai"));
        startActivity(intent);
    }

    private void sendMail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        String uriText = "mailto:" + Uri.encode("azaidi@live.nl") +
                "?subject=" + Uri.encode("Hi Amir") +
                "&body=" + Uri.encode("What's up?");
        intent.setData(Uri.parse(uriText));
        startActivity(intent);
    }
}
