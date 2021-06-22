package openfoodfacts.github.scrachx.openfood.test

import android.app.Activity
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import openfoodfacts.github.scrachx.openfood.utils.PrefManager
import org.apache.commons.lang3.StringUtils
import org.junit.Assert

class ScreenshotActivityTestRule<T : Activity?>
@JvmOverloads constructor(
        activityClass: Class<T>,
        var name: String = activityClass.simpleName,
        val context: Context,
        private val localeManager: LocaleManager,
) : ActivityTestRule<T>(activityClass, false, false) {
    var afterActivityLaunchedAction: ((ScreenshotActivityTestRule<T>) -> Unit)? = null
    var beforeActivityStartedAction: ((ScreenshotActivityTestRule<T>) -> Unit)? = null
    var firstTimeLaunched = false
    var screenshotParameter: ScreenshotParameter? = null


    @Suppress("DEPRECATION")
    override fun beforeActivityLaunched() {
        try {
            runOnUiThread {
                PrefManager(ApplicationProvider.getApplicationContext()).isFirstTimeLaunch = firstTimeLaunched
                LocaleHelper.setContextLanguage(
                        InstrumentationRegistry.getInstrumentation().targetContext,
                        screenshotParameter!!.locale,
                )
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            Assert.fail(throwable.message)
        }
        beforeActivityStartedAction?.invoke(this)
    }

    override fun afterActivityLaunched() {
        try {
            afterActivityLaunchedAction?.invoke(this)

            Thread.sleep(MILLIS_TO_WAIT_TO_DISPLAY_ACTIVITY.toLong())
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            takeScreenshot()
            finishActivity()
        } catch (throwable: Throwable) {
            Log.e(ScreenshotActivityTestRule::class.simpleName, "run on ui", throwable)
        }
    }

    private fun takeScreenshot(suffix: String = StringUtils.EMPTY) {
        ScreenshotTaker.takeScreenshot(screenshotParameter!!, suffix, this)
    }

    companion object {
        const val MILLIS_TO_WAIT_TO_DISPLAY_ACTIVITY = 5000
    }

}
