package amirz.donation;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.android.billingclient.api.SkuDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DonationFragment extends DialogFragment {
    public static final String TAG = "DonationFragment";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.donate_title)
            .setMessage(R.string.donate_desc)
            .setNeutralButton(android.R.string.cancel, null);

        final DonationActivity activity = (DonationActivity) getActivity();

        List<SkuDetails> skus = new ArrayList<>(activity.getSkus());
        Collections.sort(skus, (o1, o2) -> Long.compare(o1.getPriceAmountMicros(), o2.getPriceAmountMicros()));

        for (int i = 0; i < skus.size() && i < 2; i++) {
            final SkuDetails sku = skus.get(i);

            String price = sku.getPrice();
            DialogInterface.OnClickListener listener = (dialog, which) -> activity.buy(sku);

            if (i == 0) {
                // Cheapest IAP
                builder.setNegativeButton(price, listener);
            } else {
                // More expensive IAP
                builder.setPositiveButton(price, listener);
            }
        }

        return builder.create();
    }
}
