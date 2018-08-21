package amirz.donation;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                .build();

        mBilling.startConnection(listener);
    }

    public Collection<SkuDetails> getSkus() {
        return mSkus.values();
    }

    public void buy(SkuDetails sku) {
        int response = mBilling.launchBillingFlow(mActivity, BillingFlowParams.newBuilder()
                .setSku(sku.getSku())
                .setType(sku.getType())
                .build());

        Log.w(TAG, "buy " + response);
    }

    public void destroy() {
        mBilling.endConnection();
    }

    public interface BillingCallbacks {
        void onStateChanged(boolean connected);

        void onPurchased(SkuDetails sku, boolean isNew);
    }

    private class Listener implements BillingClientStateListener, SkuDetailsResponseListener, PurchasesUpdatedListener, PurchaseHistoryResponseListener {
        @Override
        public void onBillingSetupFinished(int responseCode) {
            Log.w(TAG, "onBillingSetupFinished " + responseCode);

            mBilling.querySkuDetailsAsync(SkuDetailsParams.newBuilder()
                    .setSkusList(mSkuKeys)
                    .setType(BillingClient.SkuType.INAPP)
                    .build(), this);
        }

        @Override
        public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
            Log.w(TAG, "onSkuDetailsResponse " + responseCode + " " + skuDetailsList.size());

            mSkus.clear();
            for (SkuDetails sku : skuDetailsList) {
                mSkus.put(sku.getSku(), sku);
            }

            mCb.onStateChanged(true);
            mBilling.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, this);
        }

        @Override
        public void onPurchaseHistoryResponse(int responseCode, List<Purchase> purchasesList) {
            Log.w(TAG, "onPurchaseHistoryResponse " + responseCode);
            if (purchasesList != null) {
                for (Purchase purchase : purchasesList) {
                    SkuDetails sku = mSkus.get(purchase.getSku());
                    mCb.onPurchased(sku, false);
                }
            }
        }

        @Override
        public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
            Log.w(TAG, "onPurchaseUpdated " + responseCode);
            if (purchases != null) {
                for (Purchase purchase : purchases) {
                    final SkuDetails sku = mSkus.get(purchase.getSku());
                    mBilling.consumeAsync(purchase.getPurchaseToken(), new ConsumeResponseListener() {
                        @Override
                        public void onConsumeResponse(int responseCode, String purchaseToken) {
                            Log.w(TAG, "consumePurchase " + responseCode);
                            if (responseCode == BillingClient.BillingResponse.OK) {
                                mCb.onPurchased(sku, true);
                            }
                        }
                    });
                }
            }
        }

        @Override
        public void onBillingServiceDisconnected() {
            Log.w(TAG, "onBillingServiceDisconnected");
            mCb.onStateChanged(false);
        }
    }
}
