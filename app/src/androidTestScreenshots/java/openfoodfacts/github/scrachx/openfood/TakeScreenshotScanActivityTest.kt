package openfoodfacts.github.scrachx.openfood

import dagger.hilt.android.testing.HiltAndroidRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidTest
import openfoodfacts.github.scrachx.openfood.features.scan.ContinuousScanActivity
import openfoodfacts.github.scrachx.openfood.test.ScreenshotActivityTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Take screenshots...
 */
//@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class TakeScreenshotScanActivityTest : AbstractScreenshotTest() {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @get:Rule(order = 1)
    var activityRule = ScreenshotActivityTestRule(ContinuousScanActivity::class.java
    )

    @Test
    fun testTakeScreenshotScanActivity() {
        activityRule.afterActivityLaunchedAction = { testRule ->
            try {
                testRule.runOnUiThread {
                    val barcode = testRule.screenshotParameter!!.productCodes[0]
                    testRule.activity!!.showProduct(barcode)
                }
                Thread.sleep(MS_TO_WAIT_TO_DISPLAY_PRODUCT_IN_SCAN.toLong())
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }
        startForAllLocales(rules = listOf(activityRule))
    }

    companion object {
        const val MS_TO_WAIT_TO_DISPLAY_PRODUCT_IN_SCAN = 2000
    }
}
