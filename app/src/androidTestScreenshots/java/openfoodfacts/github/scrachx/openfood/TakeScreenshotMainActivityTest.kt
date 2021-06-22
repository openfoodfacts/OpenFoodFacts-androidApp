package openfoodfacts.github.scrachx.openfood

import dagger.hilt.android.testing.HiltAndroidRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidTest
import openfoodfacts.github.scrachx.openfood.features.MainActivity
import openfoodfacts.github.scrachx.openfood.features.welcome.WelcomeActivity
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
class TakeScreenshotMainActivityTest : AbstractScreenshotTest() {


    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }


    @get:Rule(order = 1)
    var activityRule = ScreenshotActivityTestRule(MainActivity::class.java)

    @get:Rule(order = 1)
    var welcomeActivityRule = ScreenshotActivityTestRule(WelcomeActivity::class.java)

    @Test
    fun testTakeScreenshotMainActivity() {
        welcomeActivityRule.firstTimeLaunched = true
        startForAllLocales(rules = listOf(welcomeActivityRule, activityRule))
    }
}
