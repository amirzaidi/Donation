package amirz.donation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class DonationActivity extends AppCompatActivity
        implements PurchasesUpdatedListener, BillingClientStateListener, SkuDetailsResponseListener, PurchaseHistoryResponseListener {
    private static final String TAG = "DonationActivity";
    private static final List<String> SKU = Arrays.asList("donation.regular", "donation.large");

    private FloatingActionButton mFab;
    private BillingClient mBilling;
    private Map<String, SkuDetails> mIAPs = new HashMap<>();
    private String mCurrency = "";
    private double mGiven;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFab = findViewById(R.id.fab);
        mFab.setEnabled(false);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPurchased();
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

        if (mBilling.isReady()) {
            mBilling.querySkuDetailsAsync(SkuDetailsParams.newBuilder()
                    .setSkusList(SKU)
                    .setType(BillingClient.SkuType.INAPP)
                    .build(), DonationActivity.this);
        }
    }

    // Load IAPs
    @Override
    public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
        Log.w(TAG, "onSkuDetailsResponse " + responseCode + " " + skuDetailsList.size());

        if (mBilling.isReady()) {
            mIAPs.clear();
            for (SkuDetails sku : skuDetailsList) {
                mCurrency = sku.getPriceCurrencyCode();
                mIAPs.put(sku.getSku(), sku);
            }

            mFab.setEnabled(true);
            mBilling.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, this);
        }
    }

    // Previous purchases
    @Override
    public void onPurchaseHistoryResponse(int responseCode, List<Purchase> purchasesList) {
        Log.w(TAG, "onPurchaseHistoryResponse " + responseCode);
        calculatePurchased(purchasesList);
    }

    // Purchased
    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        Log.w(TAG, "onPurchasesUpdated " + responseCode);
        Snackbar.make(mFab, "Thank you for your support! <3", Snackbar.LENGTH_LONG).show();
        calculatePurchased(purchases);
    }

    private void calculatePurchased(List<Purchase> purchases) {
        mGiven = 0;
        if (purchases != null) {
            for (Purchase purchase : purchases) {
                SkuDetails IAP = mIAPs.get(purchase.getSku());
                mGiven += IAP.getPriceAmountMicros() / 1000000d;
            }
        }
    }

    private void showPurchased() {
        if (mGiven == 0) {
            Snackbar.make(mFab, "No purchases", Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(mFab, "You have supported me with " + mGiven +
                    " " + mCurrency, Snackbar.LENGTH_LONG).show();
        }
    }

    // Disconnected
    @Override
    public void onBillingServiceDisconnected() {
        Log.w(TAG, "onBillingServiceDisconnected");
        mFab.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        mBilling.endConnection();
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
