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

package net.optile.payment.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * The RegistrationType test
 */
public class RegistrationTypeTest {

    @Test
    public void isRegistrationType_invalidValue_false() {
        assertFalse(RegistrationType.isValid("foo"));
    }

    @Test
    public void isRegistrationType_validValue_true() {
        assertTrue(RegistrationType.isValid(RegistrationType.NONE));
        assertTrue(RegistrationType.isValid(RegistrationType.OPTIONAL));
        assertTrue(RegistrationType.isValid(RegistrationType.FORCED));
        assertTrue(RegistrationType.isValid(RegistrationType.OPTIONAL_PRESELECTED));
        assertTrue(RegistrationType.isValid(RegistrationType.FORCED_DISPLAYED));
    }
}
