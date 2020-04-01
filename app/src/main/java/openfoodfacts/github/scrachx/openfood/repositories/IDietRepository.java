
package openfoodfacts.github.scrachx.openfood.repositories;

import android.text.SpannableStringBuilder;

import java.util.List;
import java.util.regex.Pattern;

import openfoodfacts.github.scrachx.openfood.models.Diet;
import openfoodfacts.github.scrachx.openfood.models.DietIngredients;
import openfoodfacts.github.scrachx.openfood.models.DietName;
import openfoodfacts.github.scrachx.openfood.models.Ingredient;
import openfoodfacts.github.scrachx.openfood.models.IngredientName;
import openfoodfacts.github.scrachx.openfood.models.Product;

/**
 * Created by dobriseb on 1018.10.17.
 */

public interface IDietRepository {

    void saveDiets(List<Diet> diets);

    void saveDiet(Diet diet);

    void saveDietIngredients(DietIngredients dietIngredients);

    void setDietEnabled(String dietTag, Boolean isEnabled);

    List<Diet> getDiets();

    List<Diet> getEnabledDiets();

    Diet getDietByTag(String tag);

    Diet getDietById(Long id);

    Diet getDietByNameAndLanguageCode(String name, String languageCode);

    List <DietName> getDietNameListByDietTag(String dietTag);

    DietName getDietNameByNameAndLanguageCode(String name, String languageCode);

    DietName getDietNameByDietTagAndLanguageCode(String dietTag, String languageCode);

    void addDiet(String name, String description, boolean isEnabled, String languageCode);

    void saveDiet(Long id, String name, String description, boolean isEnabled, String languageCode);

    void removeDiet(Long id);

    int getDietCount();

    Ingredient getIngredientByTag(String tag);

    Ingredient getIngredientByNameAndLanguageCode(String name, String languageCode);

    IngredientName getIngredientNameByNameAndLanguageCode(String name, String languageCode);

    IngredientName getIngredientNameByIngredientTagAndLanguageCode(String ingredientTag, String languageCode);

    void addIngredient(String name, String languageCode);

    void addIngredient(String ingredientTag, String name, String languageCode);

    void addDietIngredientsByTags(String dietTag, String ingredientTag, int state, String ingredientName, String languageCode);

    void addDietIngredients(String dietTag, String ingredientName, String languageCode, int state);

    List<DietIngredients> getDietIngredientsListByDietTagAndState(String dietTag, int state);

    List<DietIngredients> getDietIngredientsListByDietTag(String dietTag);

    List<Ingredient> getIngredientsLinkedToDietByDietTagAndState(String dietTag, int state);

    List<Ingredient> getIngredientsLinkedToDietByDietTag(String dietTag);

    List<Ingredient> getIngredientsLinkedToDietByDietNameLanguageCodeAndState(String dietName, String languageCode, int state);

    List<Ingredient> getIngredientsLinkedToDietByDietNameAndLanguageCode(String dietName, String languageCode);

    List<IngredientName> getIngredientNamesByIngredientsAndLanguageCode(List<Ingredient> ingredients, String languageCode);

    String getSortedIngredientNameStringByDietTagStateAndLanguageCode(String dietTag, int state, String languageCode);

    List<String> getIngredientNameLinkedToEnabledDietsByLanguageCode(int state, String languageCode);

    int stateFromIngredientTagDietTag(String ingredientTag, String dietTag);

    List<SpannableStringBuilder> getColoredSSBFromProductAndDiet(Product product, String dietTag);

    List<SpannableStringBuilder> getColoredSSBFromIngredientsDiet(List<String> ingredients, String dietTag, String languageCode);

    SpannableStringBuilder getColoredSpannableStringBuilderFromSpannableIngredients(Pattern INGREDIENT_PATTERN, SpannableStringBuilder txtIngredients);

    SpannableStringBuilder getColoredSSBFromSSBIngredients(SpannableStringBuilder ssbIngredients, String languageCode);

    Object getColoredSSBAndProductStateFromSSBAndProduct(SpannableStringBuilder ssbIngredients, Product product);

    List<String> getIngredientsListFromIngredientsText (String ingredientsText, boolean preserveAllSign);

    String exportDietToJson(Diet diet);
}