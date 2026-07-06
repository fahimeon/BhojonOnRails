package com.example.bhojhon.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordUtilTest {

    @Test
    void hashThenVerifySucceeds() {
        String hash = PasswordUtil.hash("s3cret-password");
        assertTrue(PasswordUtil.verify("s3cret-password", hash));
    }

    @Test
    void verifyRejectsWrongPassword() {
        String hash = PasswordUtil.hash("correct horse");
        assertFalse(PasswordUtil.verify("battery staple", hash));
    }

    @Test
    void hashIsSaltedSoOutputsDiffer() {
        String a = PasswordUtil.hash("same");
        String b = PasswordUtil.hash("same");
        assertNotEquals(a, b, "Two hashes of the same password must differ (random salt)");
        assertTrue(PasswordUtil.verify("same", a));
        assertTrue(PasswordUtil.verify("same", b));
    }

    @Test
    void newHashIsNotFlaggedLegacy() {
        assertFalse(PasswordUtil.isLegacy(PasswordUtil.hash("x")));
    }

    @Test
    void legacyHashCodeFormatStillVerifies() {
        // The old scheme stored Integer.toString(password.hashCode()).
        String legacy = Integer.toString("oldpass".hashCode());
        assertTrue(PasswordUtil.isLegacy(legacy));
        assertTrue(PasswordUtil.verify("oldpass", legacy));
        assertFalse(PasswordUtil.verify("wrong", legacy));
    }

    @Test
    void nullInputsAreSafe() {
        assertFalse(PasswordUtil.verify(null, "x"));
        assertFalse(PasswordUtil.verify("x", null));
    }
}
