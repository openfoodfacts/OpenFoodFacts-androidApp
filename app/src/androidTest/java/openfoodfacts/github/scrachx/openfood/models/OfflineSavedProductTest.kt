package openfoodfacts.github.scrachx.openfood.models

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for [OfflineSavedProduct]
 */
@SmallTest
@RunWith(AndroidJUnit4::class)
class OfflineSavedProductTest {
    private lateinit var offlineSavedProduct: OfflineSavedProduct

    @Before
    fun setup() {
        offlineSavedProduct = OfflineSavedProduct(BARCODE, mapOf())
    }

    @Test
    fun getDetails_returnsEmptyMap() {
        assertThat(offlineSavedProduct.productDetails).isEmpty()
    }

    @Test
    fun getBarcode_returnsBarcode() {
        assertThat(offlineSavedProduct.barcode).isEqualTo(BARCODE)
    }

    @Test
    fun getProductDetailsMap_returnsProductDetailsMap() {

        val productDetails = mapOf(
                ApiFields.Keys.LANG to LANG,
                ApiFields.Keys.PRODUCT_NAME to PRODUCT_NAME,
                ApiFields.Keys.QUANTITY to QUANTITY,
                ApiFields.Keys.BRANDS to BRAND,
                ApiFields.Keys.PACKAGING to PACKAGING,
                ApiFields.Keys.CATEGORIES to CATEGORIES,
                ApiFields.Keys.LABELS to LABELS,
                ApiFields.Keys.EMB_CODES to EMB_CODE,
                ApiFields.Keys.STORES to STORES,
                ApiFields.Keys.COUNTRIES to COUNTRIES_WHERE_SOLD,
                ApiFields.Keys.INGREDIENTS_TEXT to INGREDIENTS,
                ApiFields.Keys.TRACES to TRACES,
                ApiFields.Keys.SERVING_SIZE to SERVING_SIZE,
                ApiFields.Keys.NUTRITION_DATA_PER to ApiFields.Defaults.NUTRITION_DATA_PER_100G,
                ApiFields.Keys.NUTRIMENT_ENERGY to ENERGY,
                ApiFields.Keys.NUTRIMENT_ENERGY_UNIT to ENERGY_UNIT,
                ApiFields.Keys.NUTRIMENT_FAT to FAT,
                ApiFields.Keys.NUTRIMENT_FAT_UNIT to FAT_UNIT
        )
        offlineSavedProduct.productDetails = productDetails
        assertThat(offlineSavedProduct.productDetails).isEqualTo(productDetails)
    }

    companion object {
        private const val BARCODE = "8888888888"
        private const val LANG = "en"
        private const val PRODUCT_NAME = "product name"
        private const val QUANTITY = "200g"
        private const val BRAND = "testing brand"
        private const val PACKAGING = "carton"
        private const val LABELS = "Halal, Brown Dot India"
        private const val CATEGORIES = "Meats"
        private const val EMB_CODE = "FR 40.001.053 EC"
        private const val STORES = "store, store 2"
        private const val COUNTRIES_WHERE_SOLD = "India, France"
        private const val INGREDIENTS = "Maltodextrin, buttermilk, salt, monosodium glutamate, lactic acid, dried garlic, dried onion, spices, natural flavors (soy)."
        private const val TRACES = "Gluten"
        private const val SERVING_SIZE = "75g"
        private const val ENERGY = "520"
        private const val ENERGY_UNIT = "kcal"
        private const val FAT = "25"
        private const val FAT_UNIT = "g"
    }
}