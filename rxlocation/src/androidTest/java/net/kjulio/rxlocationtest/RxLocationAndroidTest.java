package net.kjulio.rxlocationtest;

import android.content.Context;
import android.location.Location;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.location.LocationRequest;

import net.kjulio.rxlocation.RxLocation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import rx.observers.TestSubscriber;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class RxLocationAndroidTest {

    private Context context; // Context of the app under test.
    private TestSubscriber<Location> testSubscriber;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getTargetContext();
        testSubscriber = TestSubscriber.create();
    }

    @Test
    public void appPackageName() {
        assertEquals("net.kjulio.rxlocation.test", context.getPackageName());
    }

    @Test
    public void testLastLocation() {
        // Successful run of lastLocation() observable.
        RxLocation.lastLocation(context).toBlocking().subscribe(testSubscriber);
        testSubscriber.assertValueCount(1);
        testSubscriber.assertCompleted();
    }

    @Test
    public void testLocationUpdates() {
        // Successful run of locationUpdates() observable.
        LocationRequest defaultLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_NO_POWER);
        RxLocation.locationUpdates(context, defaultLocationRequest)
                .first().toBlocking().subscribe(testSubscriber);
        testSubscriber.assertValueCount(1);
    }

}