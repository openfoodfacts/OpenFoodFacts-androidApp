package openfoodfacts.github.scrachx.openfood.test

import android.app.Activity
import android.util.Log
import tools.fastlane.screengrab.FalconScreenshotStrategy
import tools.fastlane.screengrab.Screengrab

/**
 * Take screenshots...
 */
object ScreenshotTaker {
    fun takeScreenshot(
            screenshotParameter: ScreenshotParameter,
            suffix: String,
            activityRule: ScreenshotActivityTestRule<out Activity?>
    ) {
        val screenshotName = activityRule.name + suffix
        Log.d(LOG_TAG, "Start screenshots for screenshotParameter $screenshotParameter for activity $screenshotName")
        Screengrab.screenshot(
                screenshotName,
                FalconScreenshotStrategy(activityRule.activity),
                FileWritingScreenshotCallback(screenshotParameter)
        )
    }


    private val LOG_TAG = ScreenshotTaker::class.simpleName

}