/*
 * Copyright(c) 2012-2018 optile GmbH. All Rights Reserved.
 * https://www.optile.net
 *
 * This software is the property of optile GmbH. Distribution  of  this
 * software without agreement in writing is strictly prohibited.
 *
 * This software may not be copied, used or distributed unless agreement
 * has been received in full.
 */

package net.optile.payment.ui.paymentpage;

import net.optile.payment.model.ListResult;
import java.util.List;
import java.util.Properties;

/**
 * Class for holding the ListResult and the list of supported PaymentMethods
 */
final class PaymentHolder {

    final ListResult listResult;

    final List<PaymentMethod> methods;

    private Properties language;

    /** 
     * Construct a new PaymentHolder object
     * 
     * @param listResult Object holding the current list session data 
     * @param methods    list of PaymentMethods supported by the current Payment session
     */
    PaymentHolder(ListResult listResult, List<PaymentMethod> methods) {
        this.listResult = listResult;
        this.methods = methods;
    }

    void setLanguage(Properties language) {
        this.language = language;
    }
    
    String translate(String key, String defValue) {
        return language != null ? language.getProperty(key, defValue) : defValue;
    }
}
