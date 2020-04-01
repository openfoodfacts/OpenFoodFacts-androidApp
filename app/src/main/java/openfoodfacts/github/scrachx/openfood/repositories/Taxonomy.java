package openfoodfacts.github.scrachx.openfood.repositories;

import io.reactivex.Single;
import openfoodfacts.github.scrachx.openfood.models.*;
import openfoodfacts.github.scrachx.openfood.network.ProductApiService;

import java.util.List;

public enum Taxonomy {
    LABEL(ProductApiService.LABELS_JSON) {
        @Override
        public Single<List<Label>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadLabels(lastModifiedDate);
        }
    },
    COUNTRY(ProductApiService.COUNTRIES_JSON) {
        @Override
        public Single<List<Country>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadCountries(lastModifiedDate);
        }
    },
    CATEGORY(ProductApiService.CATEGORIES_JSON) {
        @Override
        public Single<List<Category>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadCategories(lastModifiedDate);
        }
    },
    ADDITIVE(ProductApiService.ADDITIVES_JSON) {
        @Override
        public Single<List<Additive>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadAdditives(lastModifiedDate);
        }
    },
    INGREDIENT(ProductApiService.INGREDIENTS_JSON) {
        @Override
        public Single<List<Ingredient>> load(ProductRepository repository, long lastModifiedDate) {
            //Because Ingredients can came from Product, we need to truncate the table before re-fill it.
            //Table dietIngredients is a case of use.
            repository.deleteIngredientCascade();
            return repository.loadIngredients(lastModifiedDate);
        }
    },
    ALLERGEN(ProductApiService.ALLERGENS_JSON) {
        @Override
        public Single<List<Allergen>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadAllergens(lastModifiedDate);
        }
    },
    ANALYSIS_TAGS(ProductApiService.ANALYSIS_TAG_JSON) {
        @Override
        public Single<List<AnalysisTag>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadAnalysisTags(lastModifiedDate);
        }
    },
    ANALYSIS_TAG_CONFIG(ProductApiService.ANALYSIS_TAG_CONFIG_JSON) {
        @Override
        public Single<List<AnalysisTagConfig>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadAnalysisTagConfigs(lastModifiedDate);
        }
    },
    TAGS(ProductApiService.TAGS_JSON) {
        @Override
        public Single<List<Tag>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadTags(lastModifiedDate);
        }
    },
    INVALID_BARCODES(ProductApiService.INVALID_BARCODES_JSON) {
        @Override
        public Single<List<InvalidBarcode>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadInvalidBarcodes(lastModifiedDate);
        }
    };
    public final String jsonUrl;

    Taxonomy(String jsonUrl) {
        this.jsonUrl = jsonUrl;
    }

    public String getJsonUrl() {
        return jsonUrl;
    }

    public String getLastDownloadTimeStampPreferenceId() {
        return "taxonomy_lastDownloadTimeStamp_" + name();
    }

    public String getDownloadActivatePreferencesId() {
        return "taxonomy_download_" + name();
    }

    public abstract <T> Single<List<T>> load(ProductRepository repository, long lastModifiedDate);
}
