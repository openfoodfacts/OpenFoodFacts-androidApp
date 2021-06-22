package openfoodfacts.github.scrachx.openfood

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidTest
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity
import openfoodfacts.github.scrachx.openfood.models.SearchInfo
import openfoodfacts.github.scrachx.openfood.test.ScreenshotActivityTestRule
import openfoodfacts.github.scrachx.openfood.test.ScreenshotParameter
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Take screenshots...
 */
//@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class TakeScreenshotIncompleteProductsTest : AbstractScreenshotTest() {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @get:Rule(order = 1)
    var incompleteRule = ScreenshotActivityTestRule(
            ProductSearchActivity::class.java,
            "incompleteProducts"
    )

    @Test
    fun testTakeScreenshot() = startForAllLocales(createSearchIntent, listOf(incompleteRule))

    private val createSearchIntent: (ScreenshotParameter) -> List<Intent?> = {
        listOf(Intent(ApplicationProvider.getApplicationContext(), ProductSearchActivity::class.java).apply {
            putExtra(ProductSearchActivity.SEARCH_INFO, SearchInfo.emptySearchInfo())
        })
    }
}
