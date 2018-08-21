package amirz.donation;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.billingclient.api.SkuDetails;

import java.util.Iterator;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DonationFragment extends DialogFragment {
    public static final String TAG = "DonationFragment";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.donate_title)
            .setMessage(R.string.donate_desc)
            .setNeutralButton(android.R.string.cancel, null);

        final DonationActivity activity = (DonationActivity) getActivity();
        Iterator<SkuDetails> skus = activity.getSkus().iterator();

        // First IAP
        if (skus.hasNext()) {
            final SkuDetails sku = skus.next();
            builder.setNegativeButton(sku.getPrice(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    activity.buy(sku);
                }
            });
        }

        // Second IAP
        if (skus.hasNext()) {
            final SkuDetails sku = skus.next();
            builder.setPositiveButton(sku.getPrice(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    activity.buy(sku);
                }
            });
        }

        return builder.create();
    }
}
