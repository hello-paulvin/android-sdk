/*
 * Copyright (c) 2020 optile GmbH
 * https://www.optile.net
 *
 * This file is open source and available under the MIT license.
 * See the LICENSE file for more information.
 */

package net.optile.payment.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ProductTypeTest {

    @Test
    public void isProductType_invalidValue_false() {
        assertFalse(RedirectType.isValid("foo"));
    }

    @Test
    public void isProductType_validValue_true() {
        assertTrue(ProductType.isValid(ProductType.PHYSICAL));
        assertTrue(ProductType.isValid(ProductType.DIGITAL));
        assertTrue(ProductType.isValid(ProductType.SERVICE));
        assertTrue(ProductType.isValid(ProductType.TAX));
        assertTrue(ProductType.isValid(ProductType.OTHER));
    }
}
