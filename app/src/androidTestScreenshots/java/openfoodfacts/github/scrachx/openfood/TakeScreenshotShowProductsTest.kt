package openfoodfacts.github.scrachx.openfood

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidTest
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_STATE
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity
import openfoodfacts.github.scrachx.openfood.features.scanhistory.ScanHistoryActivity
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductState
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
class TakeScreenshotShowProductsTest : AbstractScreenshotTest() {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @get:Rule(order = 1)
    var activityHistoryRule = ScreenshotActivityTestRule(ScanHistoryActivity::class.java)

    @get:Rule(order = 1)
    var activityShowProductRule = ScreenshotActivityTestRule(ProductViewActivity::class.java)

    @Test
    fun testTakeScreenshot() {
        startForAllLocales(createProductIntent, listOf(activityShowProductRule))
        startForAllLocales(rules = listOf(activityHistoryRule))
    }

    private val createProductIntent: (ScreenshotParameter) -> List<Intent?> = { parameter ->
        parameter.productCodes.map { productCode ->
            Intent(ApplicationProvider.getApplicationContext(), ProductViewActivity::class.java).apply {
                putExtra(KEY_STATE, ProductState().apply {
                    product = Product().apply { code = productCode }
                })
                putExtra(ACTION_NAME, ProductViewActivity::class.java.simpleName + "-" + productCode)
            }
        }
    }
}
