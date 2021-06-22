package openfoodfacts.github.scrachx.openfood.repositories

import android.content.Context
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.test.runBlockingTest
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.Allergen
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

/**
 * Created by Lobster on 05.03.18.
 */
@ExperimentalCoroutinesApi
@SmallTest
@HiltAndroidTest
class ProductRepositoryTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)


    @Inject
    lateinit var productRepository: ProductRepository

    @Inject
    @ApplicationContext
    lateinit var instance: Context

    @Inject
    lateinit var daoSession: DaoSession


    @Before
    fun cleanAllergens() {
        hiltRule.inject()
        clearDatabase(daoSession)
        productRepository.saveAllergens(createAllergens())
    }

    @Test
    fun testGetAllergens() {
        val mSettings = instance.getSharedPreferences("prefs", 0)
        val isDownloadActivated = mSettings.getBoolean(Taxonomy.ALLERGEN.downloadActivatePreferencesId, false)
        val allergens = productRepository.reloadAllergensFromServer().blockingGet()

        assertThat(allergens).isNotNull()

        if (!isDownloadActivated) {
            assertThat(allergens).hasSize(0)
        } else {
            assertThat(allergens).hasSize(2)

            val allergen = allergens[0]
            assertThat(allergen.tag).isEqualTo(TEST_ALLERGEN_TAG)

            val allergenNames = allergen.names
            assertThat(allergenNames).hasSize(3)

            val allergenName = allergenNames[0]
            assertThat(allergen.tag).isEqualTo(allergenName.allergenTag)
            assertThat(allergenName.languageCode).isEqualTo(TEST_LANGUAGE_CODE)
            assertThat(allergenName.name).isEqualTo(TEST_ALLERGEN_NAME)
        }
    }

    @Test
    fun testGetEnabledAllergens() = runBlockingTest {
        val allergens = productRepository.getEnabledAllergens()
        assertThat(allergens).isNotNull()
        assertThat(allergens).hasSize(1)
        assertThat(allergens[0].tag).isEqualTo(TEST_ALLERGEN_TAG)
    }

    @Test
    fun testGetAllergensByEnabledAndLanguageCode() {
        val enabledAllergenNames = productRepository.getAllergensByEnabledAndLanguageCode(true, TEST_LANGUAGE_CODE).blockingGet()
        val notEnabledAllergenNames = productRepository.getAllergensByEnabledAndLanguageCode(false, TEST_LANGUAGE_CODE).blockingGet()

        assertThat(enabledAllergenNames).isNotNull()
        assertThat(notEnabledAllergenNames).isNotNull()

        assertThat(enabledAllergenNames).hasSize(1)
        assertThat(notEnabledAllergenNames).hasSize(1)

        assertThat(enabledAllergenNames[0].name).isEqualTo(TEST_ALLERGEN_NAME)
        assertThat(notEnabledAllergenNames[0].name).isEqualTo("Molluschi")
    }

    @Test
    fun testGetAllergensByLanguageCode() = runBlockingTest {
        val allergenNames = productRepository.getAllergensByLanguageCode(TEST_LANGUAGE_CODE).await()
        assertNotNull(allergenNames)
        assertEquals(2, allergenNames.size.toLong())
    }


    @After
    fun close() = clearDatabase(daoSession)

    companion object {
        private const val TEST_ALLERGEN_TAG = "en:lupin"
        private const val TEST_LANGUAGE_CODE = "es"
        private const val TEST_ALLERGEN_NAME = "Altramuces"

        private fun clearDatabase(daoSession: DaoSession) = daoSession.allergenDao.deleteAll()

        private fun createAllergens(): List<Allergen> {
            var tag = TEST_ALLERGEN_TAG
            val allergen1 = Allergen(tag, listOf(
                    AllergenName(tag, TEST_LANGUAGE_CODE, TEST_ALLERGEN_NAME),
                    AllergenName(tag, "bg", "Лупина"),
                    AllergenName(tag, "fr", "Lupin")
            )).apply {
                enabled = true
            }

            tag = "en:molluscs"
            val allergen2 = Allergen(tag, listOf(
                    AllergenName(tag, TEST_LANGUAGE_CODE, "Molluschi"),
                    AllergenName(tag, "en", "Mollusques")
            ))

            return listOf(allergen1, allergen2)
        }
    }
}