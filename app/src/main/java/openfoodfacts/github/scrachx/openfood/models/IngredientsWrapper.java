package openfoodfacts.github.scrachx.openfood.models;

import android.util.Log;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.network.deserializers.IngredientsWrapperDeserializer;

/**
 * JSON from URL https://ssl-api.openfoodfacts.org/data/taxonomies/ingredients.json
 *
 * @author dobriseb 2018-12-21 inspired by AllergensWrapper
 */

@JsonDeserialize(using = IngredientsWrapperDeserializer.class)
public class IngredientsWrapper {

    private List<IngredientResponse> ingredients;

    /**
     * @return A list of Ingredient objects
     */
    public List<Ingredient> map() {
        List<Ingredient> entityIngredients = new ArrayList<>();
        for (IngredientResponse ingredient : ingredients) {
            entityIngredients.add(ingredient.map());
        }

        return entityIngredients;
    }

    public void setIngredients(List<IngredientResponse> ingredients) {
        this.ingredients = ingredients;
    }

    public List<IngredientResponse> getIngredients() {
        return ingredients;
    }
}
