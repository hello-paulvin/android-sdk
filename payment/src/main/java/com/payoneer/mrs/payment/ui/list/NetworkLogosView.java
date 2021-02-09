/*
 * Copyright (c) 2020 Payoneer Germany GmbH
 * https://www.payoneer.com
 *
 * This file is open source and available under the MIT license.
 * See the LICENSE file for more information.
 */

package com.payoneer.mrs.payment.ui.list;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ViewSwitcher;

import com.payoneer.mrs.payment.R;
import com.payoneer.mrs.payment.ui.model.PaymentNetwork;
import com.payoneer.mrs.payment.util.NetworkLogoLoader;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class displaying the network logo images
 */
class NetworkLogosView {

    private final ViewSwitcher switcher;
    private final Map<String, NetworkLogo> logos;
    private final ImageView selImage;
    private final int margin;

    /**
     * Construct a new NetworkLogosView
     *
     * @param parent of this view
     * @param networks list of payment networks for which logos should be shown
     */
    NetworkLogosView(View parent, List<PaymentNetwork> networks) {
        switcher = parent.findViewById(R.id.viewswitcher_logos);
        switcher.setVisibility(View.VISIBLE);

        logos = new HashMap<>();
        selImage = parent.findViewById(R.id.image_selected);

        Resources resources = parent.getContext().getResources();
        margin = (int) resources.getDimension(R.dimen.pmborder_small);

        LinearLayout layout = parent.findViewById(R.id.layout_logos);
        for (PaymentNetwork network : networks) {
            addNetworkLogo(network, layout);
        }
    }

    private void addNetworkLogo(PaymentNetwork network, LinearLayout layout) {
        String code = network.getCode();
        ImageView imageView = inflateLogoImage(layout);
        logos.put(code, new NetworkLogo(code, network.getLink("logo"), imageView));
        layout.addView(imageView);
    }

    private ImageView inflateLogoImage(LinearLayout layout) {
        LayoutInflater inflater = LayoutInflater.from(layout.getContext());
        ImageView view = (ImageView) inflater.inflate(R.layout.view_logosmall, layout, false);
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        params.setMargins(0, 0, margin, 0);
        view.setLayoutParams(params);
        return view;
    }

    void setSelected(String networkCode) {
        if (networkCode != null) {
            showSelectedLogo(networkCode);
        } else {
            showAllLogos();
        }
    }

    private void showSelectedLogo(String networkCode) {
        NetworkLogo logo = logos.get(networkCode);
        NetworkLogoLoader.loadNetworkLogo(selImage, networkCode, logo.url);
        switcher.setDisplayedChild(1);
    }

    private void showAllLogos() {
        for (Map.Entry<String, NetworkLogo> entry : logos.entrySet()) {
            NetworkLogo logo = entry.getValue();
            NetworkLogoLoader.loadNetworkLogo(logo.image, logo.networkCode, logo.url);
        }
        switcher.setDisplayedChild(0);
    }

    class NetworkLogo {

        final String networkCode;
        final URL url;
        final ImageView image;

        NetworkLogo(String networkCode, URL url, ImageView image) {
            this.networkCode = networkCode;
            this.url = url;
            this.image = image;
        }
    }
}
