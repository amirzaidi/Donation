package amirz.donation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

public class DonationActivity extends AppCompatActivity
        implements PurchasesUpdatedListener, BillingClientStateListener, SkuDetailsResponseListener {
    private static final String TAG = "DonationActivity";
    private BillingClient mBilling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final List<String> skuList = new ArrayList<>();
        skuList.add("donate.regular");
        skuList.add("donate.large");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBilling.isReady()) {
                    mBilling.querySkuDetailsAsync(SkuDetailsParams.newBuilder()
                            .setSkusList(skuList)
                            .setType(BillingClient.SkuType.INAPP)
                            .build(), DonationActivity.this);
                } else {
                    Snackbar.make(view, "Connection to Google Play failed", Snackbar.LENGTH_LONG)
                            .setAction("Reconnect", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mBilling.startConnection(DonationActivity.this);
                                }
                            }).show();
                }
            }
        });

        BillingClient.Builder builder = BillingClient.newBuilder(this);
        builder.setListener(this);
        mBilling = builder.build();

        mBilling.startConnection(this);
    }

    // Connected
    @Override
    public void onBillingSetupFinished(int responseCode) {
        Log.w(TAG, "onBillingSetupFinished " + responseCode);
    }

    // Start purchase
    @Override
    public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
        Log.w(TAG, "onSkuDetailsResponse " + responseCode + " " + skuDetailsList.size());
    }

    // Purchased
    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        Log.w(TAG, "onPurchasesUpdated " + responseCode);
    }

    // Disconnected
    @Override
    public void onBillingServiceDisconnected() {
        Log.w(TAG, "onBillingServiceDisconnected");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBilling.endConnection();
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
