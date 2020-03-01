package com.trevjonez.atinstall;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@RunWith(AndroidJUnit4.class)
public class FeatureTesting {

    @Test
    public void consumeFeatureCode() {
        new FeatureCode();
    }

    @Test
    public void basicMaths() {
        Log.w("AppTesting", "Can do maths and log?");
        assertEquals(4, 2 + 2);
    }

    @Test
    public void assumeFailed() {
        //noinspection ConstantConditions
        assumeTrue("This assumption is clearly bad",false);
    }

    @Test
    public void assertFailed() {
        //noinspection ConstantConditions,SimplifiableJUnitAssertion
        assertTrue("This assert is clearly bad",false);
    }
}
