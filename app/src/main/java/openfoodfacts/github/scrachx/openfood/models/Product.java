package openfoodfacts.github.scrachx.openfood.models;

import android.widget.Toast;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import openfoodfacts.github.scrachx.openfood.images.ImageSize;

import static org.apache.commons.lang.StringUtils.isNotBlank;

class ProductStringConverter extends StdConverter<String, String> {
    public String convert(String value) {
        return StringEscapeUtils.unescapeHtml(value).replace("\\'", "'").replace("&quot", "'");
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty("image_small_url")
    private String imageSmallUrl;
    @JsonProperty("image_nutrition_url")
    private String imageNutritionUrl;
    @JsonProperty("image_front_url")
    private String imageFrontUrl;
    @JsonProperty("image_ingredients_url")
    private String imageIngredientsUrl;
    @JsonProperty("link")
    private String manufactureUrl;
    private String url;
    private String code;
    @JsonProperty("traces_tags")
    private List<String> tracesTags = new ArrayList<>();
    @JsonProperty("ingredients_that_may_be_from_palm_oil_tags")
    private List<String> ingredientsThatMayBeFromPalmOilTags = new ArrayList<>();
    @JsonProperty("additives_tags")
    private List<String> additivesTags = new ArrayList<>();
    @JsonProperty("allergens_hierarchy")
    private List<String> allergensHierarchy = new ArrayList<>();
    @JsonProperty("manufacturing_places")
    private String manufacturingPlaces;
    private Nutriments nutriments;
    @JsonProperty("ingredients_from_palm_oil_tags")
    private List<Object> ingredientsFromPalmOilTags = new ArrayList<>();
    @JsonProperty("brands_tags")
    private List<String> brandsTags = new ArrayList<>();
    private String traces;
    @JsonProperty("categories_tags")
    private List<String> categoriesTags;
    @JsonProperty("ingredients_text")
    @JsonDeserialize(converter = ProductStringConverter.class)
    private String ingredientsText;
    @JsonProperty("product_name")
    @JsonDeserialize(converter = ProductStringConverter.class)
    private String productName;
    @JsonProperty("generic_name")
    @JsonDeserialize(converter = ProductStringConverter.class)
    private String genericName;
    @JsonProperty("ingredients_from_or_that_may_be_from_palm_oil_n")
    private long ingredientsFromOrThatMayBeFromPalmOilN;
    @JsonProperty("serving_size")
    private String servingSize;
    @JsonProperty("last_modified_by")
    private String lastModifiedBy;
    @JsonProperty("allergens_tags")
    private List<String> allergensTags;
    private String allergens;
    private String origins;
    private String stores;
    @JsonProperty("nutrition_grade_fr")
    private String nutritionGradeFr;
    @JsonProperty("nutrient_levels")
    private NutrientLevels nutrientLevels;
    private String countries;
    @JsonProperty("countries_tags")
    private List<String> countriesTags;
    private String brands;
    private String packaging;
    @JsonProperty("labels_hierarchy")
    private List<String> labelsHierarchy;
    @JsonProperty("labels_tags")
    private List<String> labelsTags;
    @JsonProperty("cities_tags")
    private List<Object> citiesTags = new ArrayList<>();
    private String quantity;
    @JsonProperty("ingredients_from_palm_oil_n")
    private long ingredientsFromPalmOilN;
    @JsonProperty("image_url")
    private String imageUrl;
    @JsonProperty("emb_codes_tags")
    private List<Object> embTags = new ArrayList<>();
    @JsonProperty("states_tags")
    private List<String> statesTags = new ArrayList<>();
    @JsonProperty("vitamins_tags")
    private List<String> vitaminTags = new ArrayList<>();
    @JsonProperty("minerals_tags")
    private List<String> mineralTags = new ArrayList<>();
    @JsonProperty("amino_acids_tags")
    private List<String> aminoAcidTags = new ArrayList<>();
    @JsonProperty("other_nutritional_substances_tags")
    private List<String> otherNutritionTags = new ArrayList<>();
    @JsonProperty("created_t")
    private String createdDateTime;
    @JsonProperty("creator")
    private String creator;
    @JsonProperty("last_modified_t")
    private String lastModifiedTime;
    @JsonProperty("editors_tags")
    private List<String> editorsTags = new ArrayList<>();
    @JsonProperty("nova_groups")
    private String novaGroups;
    @JsonProperty("environment_impact_level_tags")
    private List<String> environmentImpactLevelTags;
    @JsonProperty("lang")
    private String lang;
    @JsonProperty("purchase_places")
    private String purchasePlaces;
    @JsonProperty("nutrition_data_per")
    private String nutritionDataPer;
    @JsonProperty("no_nutrition_data")
    private String noNutritionData;
    @JsonProperty("other_information")
    private String otherInformation;
    @JsonProperty("conservation_conditions")
    private String conservationConditions;
    @JsonProperty("recycling_instructions_to_discard")
    private String recyclingInstructionsToDiscard;
    @JsonProperty("recycling_instructions_to_recycle")
    private String recyclingInstructionsToRecycle;
    @JsonProperty("warning")
    private String warning;
    @JsonProperty("customer_service")
    private String customerService;
    @JsonProperty("environment_infocard")
    private String environmentInfocard;
    private Map<String, Object> additionalProperties = new HashMap<>();
    @JsonProperty("ingredients_analysis_tags")
    private List<String> ingredientsAnalysisTags = new ArrayList<>();
    @JsonProperty("ingredients")
    private List<LinkedHashMap<String, String>> ingredients = new ArrayList<>();
    private List<ProductIngredient> productIngredients = new ArrayList<>();
/*
    @JsonProperty("ingredients")
*/

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public String getProductName(String languageCode) {
        String result = getFieldHelper("product_name", languageCode);
        if (result != null) {
            return result;
        } else {
            return getProductName();
        }
    }

    public boolean hasProductNameIn(String languageCode) {
        return additionalProperties.get("product_name_" + languageCode) != null;
    }

    public String getGenericName(String languageCode) {
        String result = getFieldHelper("generic_name", languageCode);
        if (result != null) {
            return result;
        } else {
            return getGenericName();
        }
    }

    public String getIngredientsText(String languageCode) {
        String result = getFieldHelper("ingredients_text", languageCode);
        if (result != null) {
            return result;
        } else {
            return getIngredientsText();
        }
    }

    public String getImageIngredientsUrl(String languageCode) {
        String result = getSelectedImage(languageCode, ProductImageField.INGREDIENTS, ImageSize.DISPLAY);
        if (StringUtils.isNotBlank(result)) {
            return result;
        } else {
            return getImageIngredientsUrl();
        }
    }

    public String getImageNutritionUrl(String languageCode) {
        String result = getSelectedImage(languageCode, ProductImageField.NUTRITION, ImageSize.DISPLAY);
        if (StringUtils.isNotBlank(result)) {
            return result;
        } else {
            return getImageNutritionUrl();
        }
    }

    private String getFieldHelper(String field, String languageCode) {

        if (!languageCode.equals("en") && additionalProperties.get(field + "_" + languageCode) != null
            && isNotBlank(additionalProperties.get(field + "_" + languageCode).toString())) {
            return additionalProperties.get(field + "_" + languageCode)
                .toString()
                .replace("\\'", "'")
                .replace("&quot", "'");
        } else if (additionalProperties.get(field + "_en") != null
            && isNotBlank(additionalProperties.get(field + "_en").toString())) {
            return additionalProperties.get(field + "_en")
                .toString()
                .replace("\\'", "'")
                .replace("&quot", "'");
        } else {
            return null;
        }
    }

    /**
     * @return The statesTags
     */
    public List<String> getStatesTags() {
        return statesTags;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public String getWarning() {
        return warning;
    }

    /**
     * @return The vitaminTags
     */
    public List<String> getVitaminTags() {
        return vitaminTags;
    }

    public void setVitaminTags(List<String> vitaminTags) {
        this.vitaminTags = vitaminTags;
    }

    /**
     * @return The mineralsTags
     */
    public List<String> getMineralTags() {
        return mineralTags;
    }

    public void setMineralTags(List<String> mineralTags) {
        this.mineralTags = mineralTags;
    }

    /**
     * @return The aminoAcidTags
     */
    public List<String> getAminoAcidTags() {
        return aminoAcidTags;
    }

    public void setAminoAcidTags(List<String> aminoAcidTags) {
        this.aminoAcidTags = aminoAcidTags;
    }

    /**
     * @return The otherNutritionTags
     */
    public List<String> getOtherNutritionTags() {
        return otherNutritionTags;
    }

    public void setOtherNutritionTags(List<String> otherNutritionTags) {
        this.otherNutritionTags = otherNutritionTags;
    }

    /**
     * @return The imageSmallUrl
     */
    private String getImageSmallUrl() {
        return imageSmallUrl;
    }

    public String getImageSmallUrl(String languageCode) {
        String image = getSelectedImage(languageCode, ProductImageField.FRONT, ImageSize.SMALL);
        if (StringUtils.isNotBlank(image)) {
            return image;
        }
        return getImageSmallUrl();
    }

    public String getSelectedImage(String languageCode, ProductImageField type, ImageSize size) {
        Map<String, Map> images = (Map<String, Map>) additionalProperties.get("selected_images");
        if (images != null) {
            images = (Map<String, Map>) images.get(type.name().toLowerCase());
            if (images != null) {
                Map<String, String> imagesByLocale = (Map<String, String>) images.get(size.name().toLowerCase());
                if (imagesByLocale != null) {
                    String url = imagesByLocale.get(languageCode);
                    if (StringUtils.isNotBlank(url)) {
                        return url;
                    }
                }
            }
        }
        switch (type) {
            case FRONT:
                return getImageUrl();
            case INGREDIENTS:
                return getImageIngredientsUrl();
            case NUTRITION:
                return getImageNutritionUrl();
            case OTHER:
                return null;
        }
        return null;
    }

    public List<String> getAvailableLanguageForImage(ProductImageField type, ImageSize size) {
        Map<String, Map> images = (Map<String, Map>) additionalProperties.get("selected_images");
        if (images != null) {
            images = (Map<String, Map>) images.get(type.name().toLowerCase());
            if (images != null) {
                Map<String, String> imagesByLocale = (Map<String, String>) images.get(size.name().toLowerCase());
                return new ArrayList<>(imagesByLocale.keySet());
            }
        }
        return Collections.emptyList();
    }

    public Map<String, ?> getImageDetails(String imageKey) {
        Map<String, Map> images = (Map<String, Map>) additionalProperties.get("images");
        if (images != null) {
            Map<String, ?> imagesDetails = (Map<String, ?>) images.get(imageKey);
            return imagesDetails;
        }
        return null;
    }

    public boolean isLanguageSupported(String languageCode) {
        Map<String, Map> languagesCodes = (Map<String, Map>) additionalProperties.get("languages_codes");
        return languageCode != null && languagesCodes != null && languagesCodes.containsKey(languageCode.toLowerCase());
    }

    /**
     * @return The imageFrontUrl
     */
    public String getImageFrontUrl() {
        return imageFrontUrl;
    }

    public String getImageFrontUrl(String languageCode) {
        String image = getSelectedImage(languageCode, ProductImageField.FRONT, ImageSize.DISPLAY);
        if (StringUtils.isNotBlank(image)) {
            return image;
        }
        return getImageFrontUrl();
    }

    /**
     * @return The imageIngredientsUrl
     */
    public String getImageIngredientsUrl() {
        return imageIngredientsUrl;
    }

    /**
     * @return The imageNutritionUrl
     */
    public String getImageNutritionUrl() {
        return imageNutritionUrl;
    }

    /**
     * @return The manufactureUrl
     */
    public String getManufactureUrl() {
        return manufactureUrl;
    }

    /**
     * @return The url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return The code
     */
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return The tracesTags
     */
    public List<String> getTracesTags() {
        return tracesTags;
    }

    /**
     * @return The ingredientsThatMayBeFromPalmOilTags
     */
    public List<String> getIngredientsThatMayBeFromPalmOilTags() {
        return ingredientsThatMayBeFromPalmOilTags;
    }

    /**
     * @return The additivesTags
     */
    public List<String> getAdditivesTags() {
        return additivesTags;
    }

    /**
     * @return The allergensHierarchy
     */
    public List<String> getAllergensHierarchy() {
        return allergensHierarchy;
    }

    /**
     * @return The manufacturingPlaces
     */
    public String getManufacturingPlaces() {
        return manufacturingPlaces;
    }

    /**
     * @return The nutriments
     */
    public Nutriments getNutriments() {
        return nutriments;
    }

    /**
     * @return The ingredientsFromPalmOilTags
     */
    public List<Object> getIngredientsFromPalmOilTags() {
        return ingredientsFromPalmOilTags;
    }

    /**
     * @return The brandsTags
     */
    public List<String> getBrandsTags() {
        return brandsTags;
    }

    /**
     * @return The traces
     */
    public String getTraces() {
        return traces;
    }

    /**
     * @return The categoriesTags
     */
    public List<String> getCategoriesTags() {
        return categoriesTags;
    }

    /**
     * @return The ingredientsText
     */
    public String getIngredientsText() {
        return ingredientsText;
    }

    /**
     * @return The ingredients
     */
    public List<ProductIngredient> getProductIngredients() {
        if (productIngredients.size() == 0) {
            for (int i = 0; i < ingredients.size(); i++) {
                ProductIngredient productIngredient = new ProductIngredient();
                LinkedHashMap<String, String> ingredientHM =  ingredients.get(i);
                for (Map.Entry<String, String> entry : ingredientHM.entrySet()) {
                    switch (entry.getKey()) {
                        case "id":
                            productIngredient.setId(entry.getValue());
                            break;
                        case "percent":
                            productIngredient.setPercent(entry.getValue());
                            break;
                        case "text":
                            productIngredient.setText(entry.getValue());
                            break;
                        case "rank":
                            productIngredient.setRank(Long.valueOf(entry.getValue()));
                            break;
                        default:
                            productIngredient.setAdditionalProperty(entry.getKey(),entry.getValue());
                    }
                }
                productIngredients.add(productIngredient);
            }
        }
        return productIngredients;
    }

    /**
     * @return The productName
     */
    public String getProductName() {
        return productName;
    }

    /**
     * @return The genericName
     */
    public String getGenericName() {
        return genericName;
    }

    /**
     * @return The ingredientsFromOrThatMayBeFromPalmOilN
     */
    public long getIngredientsFromOrThatMayBeFromPalmOilN() {
        return ingredientsFromOrThatMayBeFromPalmOilN;
    }

    /**
     * @return The servingSize
     */
    public String getServingSize() {
        return servingSize;
    }

    public List<String> getAllergensTags() {
        return allergensTags;
    }

    /**
     * @return The allergens
     */
    public String getAllergens() {
        return allergens;
    }

    /**
     * @return The origins
     */
    public String getOrigins() {
        return origins;
    }

    /**
     * @return The stores
     */
    public String getStores() {
        if (stores == null) {
            return null;
        }
        return stores.replace(",", ", ");
    }

    /**
     * @return The nutritionGradeFr
     */
    public String getNutritionGradeFr() {
        return nutritionGradeFr;
    }

    /**
     * @return The nutrientLevels
     */
    public NutrientLevels getNutrientLevels() {
        return nutrientLevels;
    }

    /**
     * @return The countries
     */
    public String getCountries() {
        if (countries == null) {
            return null;
        }
        return countries.replace(",", ", ");
    }

    /**
     * @return The brands
     */
    public String getBrands() {
        if (brands == null) {
            return null;
        }
        return brands.replace(",", ", ");
    }

    /**
     * @return The packaging
     */
    public String getPackaging() {
        if (packaging == null) {
            return null;
        }
        return packaging.replace(",", ", ");
    }

    /**
     * @return The labels tags
     */
    public List<String> getLabelsTags() {
        return labelsTags;
    }

    /**
     * @return The labels hierarchy
     */
    public List<String> getLabelsHierarchy() {
        return labelsHierarchy;
    }

    /**
     * @return The citiesTags
     */
    public List<Object> getCitiesTags() {
        return citiesTags;
    }

    /**
     * @return The quantity
     */
    public String getQuantity() {
        return quantity;
    }

    /**
     * @return The ingredientsFromPalmOilN
     */
    public long getIngredientsFromPalmOilN() {
        return ingredientsFromPalmOilN;
    }

    /**
     * @return The imageUrl
     */
    private String getImageUrl() {
        return imageUrl;
    }

    public String getImageUrl(String languageCode) {
        String url = getSelectedImage(languageCode, ProductImageField.FRONT, ImageSize.DISPLAY);
        if (StringUtils.isNotBlank(url)) {
            return url;
        }
        return getImageUrl();
    }

    /**
     * @return The Emb_codes
     */
    public List<Object> getEmbTags() {
        return embTags;
    }

    public List<String> getCountriesTags() {
        return countriesTags;
    }

    public String getCreator() {
        return creator;
    }

    public String getCreatedDateTime() {
        return createdDateTime;
    }

    public String getLastModifiedTime() {
        return lastModifiedTime;
    }

    public List<String> getEditors() {
        return editorsTags;
    }

    public String getNovaGroups() {
        return novaGroups;
    }

    public List<String> getEnvironmentImpactLevelTags() {
        return environmentImpactLevelTags;
    }

    public String getLang() {
        return lang;
    }

    public String getPurchasePlaces() {
        return purchasePlaces;
    }

    public String getNutritionDataPer() {
        return nutritionDataPer;
    }

    public String getNoNutritionData() {
        return noNutritionData;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getEnvironmentInfocard() {
        return environmentInfocard;
    }

    public List<String> getIngredientsAnalysisTags() {
        return ingredientsAnalysisTags;
    }

    public List<LinkedHashMap<String, String>> getIngredients() {
        return ingredients;
    }

    /**
     * @return Other information
     */
    public String getOtherInformation() {
        return otherInformation;
    }

    /**
     * @return Conservation conditions
     */
    public String getConservationConditions() {
        return conservationConditions;
    }

    /**
     * @return Recycling instructions to discard
     */
    public String getRecyclingInstructionsToDiscard() {
        return recyclingInstructionsToDiscard;
    }

    /**
     * @return Recycling instructions to recycle
     */
    public String getRecyclingInstructionsToRecycle() {
        return recyclingInstructionsToRecycle;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("code", code)
            .append("productName", productName)
            .append("additional_properties", additionalProperties)
            .toString();
    }
}
