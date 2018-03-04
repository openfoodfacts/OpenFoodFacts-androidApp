package openfoodfacts.github.scrachx.openfood.repositories;

import org.greenrobot.greendao.AbstractDao;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import openfoodfacts.github.scrachx.openfood.models.Additive;
import openfoodfacts.github.scrachx.openfood.models.AdditiveDao;
import openfoodfacts.github.scrachx.openfood.models.Allergen;
import openfoodfacts.github.scrachx.openfood.models.AllergenDao;
import openfoodfacts.github.scrachx.openfood.models.AllergenName;
import openfoodfacts.github.scrachx.openfood.models.AllergenNameDao;
import openfoodfacts.github.scrachx.openfood.models.AllergensWrapper;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.models.Label;
import openfoodfacts.github.scrachx.openfood.models.LabelDao;
import openfoodfacts.github.scrachx.openfood.models.LabelName;
import openfoodfacts.github.scrachx.openfood.models.LabelNameDao;
import openfoodfacts.github.scrachx.openfood.models.LabelsWrapper;
import openfoodfacts.github.scrachx.openfood.models.Tag;
import openfoodfacts.github.scrachx.openfood.models.TagDao;
import openfoodfacts.github.scrachx.openfood.models.TagsWrapper;
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIService;
import openfoodfacts.github.scrachx.openfood.network.ProductApiService;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

/**
 * Created by Lobster on 03.03.18.
 */

public class ProductRepository implements IProductRepository {

    private static IProductRepository instance;

    private ProductApiService productApi;
    private OpenFoodAPIService openFooApi;

    private LabelDao labelDao;
    private LabelNameDao labelNameDao;
    private TagDao tagDao;
    private AllergenDao allergenDao;
    private AllergenNameDao allergenNameDao;
    private AdditiveDao additiveDao;

    public static IProductRepository getInstance() {
        if (instance == null) {
            instance = new ProductRepository();
        }

        return instance;
    }

    private ProductRepository() {
        productApi = CommonApiManager.getInstance().getProductApiService();
        openFooApi = CommonApiManager.getInstance().getOpenFoodApiService();

        DaoSession daoSession = OFFApplication.getInstance().getDaoSession();
        labelDao = daoSession.getLabelDao();
        labelNameDao = daoSession.getLabelNameDao();
        tagDao = daoSession.getTagDao();
        allergenDao = daoSession.getAllergenDao();
        allergenNameDao = daoSession.getAllergenNameDao();
        additiveDao = daoSession.getAdditiveDao();
    }

    /**
     * Load labels from the server or local database
     *
     * @param refresh defines the source of data.
     *                If refresh is true (or local database is empty) than load it from the server,
     *                else from the local database.
     */
    @Override
    public Single<List<Label>> getLabels(Boolean refresh) {
        if (refresh || tableIsEmpty(labelDao)) {
            return productApi.getLabels()
                    .map(LabelsWrapper::map);
        } else {
            return Single.fromCallable(() -> labelDao.loadAll());
        }
    }

    /**
     * Load tags from the server or local database
     *
     * @param refresh defines the source of data.
     *                If refresh is true (or local database is empty) than load it from the server,
     *                else from the local database.
     */
    @Override
    public Single<List<Tag>> getTags(Boolean refresh) {
        if (refresh || tableIsEmpty(labelDao)) {
            return openFooApi.getTags()
                    .map(TagsWrapper::getTags);
        } else {
            return Single.fromCallable(() -> tagDao.loadAll());
        }
    }

    /**
     * Load allergens from the server or local database
     *
     * @param refresh defines the source of data.
     *                If refresh is true (or local database is empty) than load it from the server,
     *                else from the local database.
     */
    @Override
    public Single<List<Allergen>> getAllergens(Boolean refresh) {
        if (refresh || tableIsEmpty(allergenDao)) {
            return productApi.getAllergens()
                    .map(AllergensWrapper::map);
        } else {
            return Single.fromCallable(() -> allergenDao.loadAll());
        }
    }

    @Override
    public List<Allergen> getEnabledAllergens() {
        return allergenDao.queryBuilder().where(AllergenDao.Properties.Enabled.eq("true")).list();
    }

    @Override
    public Single<List<Additive>> getAdditives() {
        return Single.fromCallable(() -> additiveDao.loadAll());
    }

    @Override
    public void saveLabels(List<Label> labels) {
        for (Label label : labels) {
            labelDao.insertOrReplaceInTx(label);
            for (LabelName labelName : label.getNames()) {
                labelNameDao.insertOrReplace(labelName);
            }
        }
    }

    @Override
    public void saveTags(List<Tag> tags) {
        tagDao.insertOrReplaceInTx(tags);
    }

    @Override
    public void saveAllergens(List<Allergen> allergens) {
        for (Allergen allergen : allergens) {
            allergenDao.insertOrReplaceInTx(allergen);
            for (AllergenName allergenName : allergen.getNames()) {
                allergenNameDao.insertOrReplace(allergenName);
            }
        }
    }

    @Override
    public void saveAdditives(List<Additive> additives) {
        additiveDao.insertOrReplaceInTx(additives);
    }

    @Override
    public void setAllergenEnabled(String allergenTag, Boolean isEnabled) {
        Allergen allergen = allergenDao.queryBuilder()
                .where(AllergenDao.Properties.Tag.eq(allergenTag))
                .unique();

        if (allergen != null) {
            allergen.setEnabled(isEnabled);
            allergenDao.update(allergen);
        }
    }

    @Override
    public LabelName getLabelByTagAndLanguageCode(String labelTag, String languageCode) {
        return labelNameDao.queryBuilder()
                .where(
                        LabelNameDao.Properties.LabelTag.eq(labelTag),
                        LabelNameDao.Properties.LanguageCode.eq(languageCode)
                ).unique();
    }

    @Override
    public LabelName getLabelByTagAndDefaultLanguageCode(String labelTag) {
        return getLabelByTagAndLanguageCode(labelTag, "en");
    }

    @Override
    public List<AllergenName> getAllergensByEnabledAndLanguageCode(Boolean isEnabled, String languageCode) {
        List<Allergen> allergens = allergenDao.queryBuilder().where(AllergenDao.Properties.Enabled.eq(isEnabled)).list();
        if (allergens != null) {
            List<AllergenName> allergenNames = new ArrayList<>();
            for (Allergen allergen : allergens) {
                AllergenName name = allergenNameDao.queryBuilder()
                        .where(
                                AllergenNameDao.Properties.AllergenTag.eq(allergen.getTag()),
                                AllergenNameDao.Properties.LanguageCode.eq(languageCode)
                        ).unique();

                if (name != null) {
                    allergenNames.add(name);
                }
            }

            return allergenNames;
        }

        return null;
    }

    @Override
    public List<AllergenName> getAllergensByLanguageCode(String languageCode) {
        return allergenNameDao.queryBuilder()
                .where(AllergenNameDao.Properties.LanguageCode.eq(languageCode))
                .list();
    }

    private Boolean tableIsEmpty(AbstractDao dao) {
        return dao.count() == 0;
    }

    private Boolean tableIsNotEmpty(AbstractDao dao) {
        return dao.count() != 0;
    }

    @Override
    public Boolean additivesIsEmpty() {
        return tableIsEmpty(additiveDao);
    }
}
