package amirz.donation;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.billingclient.api.BillingClient.BillingResponseCode.OK;

public class BillingHandler {
    private static final String TAG = "BillingHandler";

    private final Activity mActivity;
    private final BillingCallbacks mCb;
    private final List<String> mSkuKeys;

    private final BillingClient mBilling;
    private final Map<String, SkuDetails> mSkus = new HashMap<>();

    public BillingHandler(Activity activity, BillingCallbacks cb, List<String> skus) {
        mActivity = activity;
        mCb = cb;
        mSkuKeys = skus;

        Listener listener = new Listener();

        mBilling = BillingClient.newBuilder(mActivity)
                .setListener(listener)
                .enablePendingPurchases()
                .build();

        mBilling.startConnection(listener);
    }

    public Collection<SkuDetails> getSkus() {
        return mSkus.values();
    }

    public void buy(SkuDetails sku) {
        BillingResult response =
                mBilling.launchBillingFlow(mActivity, BillingFlowParams.newBuilder()
                        .setSkuDetails(sku)
                        .build());
        Log.w(TAG, "buy " + response.getDebugMessage());
    }

    public void destroy() {
        mBilling.endConnection();
    }

    public interface BillingCallbacks {
        void onStateChanged(boolean connected);

        void onPurchased(SkuDetails sku, boolean isNew);
    }

    private class Listener implements BillingClientStateListener, SkuDetailsResponseListener,
            PurchasesUpdatedListener, PurchaseHistoryResponseListener {
        @Override
        public void onBillingSetupFinished(BillingResult billingResult) {
            Log.w(TAG, "onBillingSetupFinished " + billingResult.getDebugMessage());
            mBilling.querySkuDetailsAsync(SkuDetailsParams.newBuilder()
                    .setSkusList(mSkuKeys)
                    .setType(BillingClient.SkuType.INAPP)
                    .build(), this);
        }

        @Override
        public void onBillingServiceDisconnected() {
            Log.w(TAG, "onBillingServiceDisconnected");
            mCb.onStateChanged(false);
        }

        @Override
        public void onPurchaseHistoryResponse(BillingResult billingResult,
                                              List<PurchaseHistoryRecord> list) {
            Log.w(TAG, "onPurchaseHistoryResponse " + billingResult.getDebugMessage());
            if (list != null) {
                for (PurchaseHistoryRecord purchase : list) {
                    SkuDetails sku = mSkus.get(purchase.getSku());
                    mCb.onPurchased(sku, false);
                }
            }
        }

        @Override
        public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> list) {
            Log.w(TAG, "onPurchaseUpdated " + billingResult.getDebugMessage());
            if (list != null) {
                for (Purchase purchase : list) {
                    final SkuDetails sku = mSkus.get(purchase.getSku());
                    mBilling.consumeAsync(ConsumeParams.newBuilder()
                            .setDeveloperPayload(purchase.getDeveloperPayload())
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build(), (b, s) -> {
                        Log.w(TAG, "onConsumePurchase " + billingResult.getDebugMessage());
                        if (billingResult.getResponseCode() == OK) {
                            mCb.onPurchased(sku, true);
                        }
                    });
                }
            }
        }

        @Override
        public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> list) {
            Log.w(TAG, "onSkuDetailsResponse " + billingResult.getDebugMessage());

            mSkus.clear();
            if (list == null) {
                return;
            }
            for (SkuDetails sku : list) {
                mSkus.put(sku.getSku(), sku);
            }

            mCb.onStateChanged(true);
            mBilling.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, this);
        }
    }
}
