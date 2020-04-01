package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "text",
        "id",
        "rank",
        "percent"
})
public class IngredientOld implements Serializable {
    private static final long serialVersionUID = 1L;
    private String text;
    private String id;
    private long rank;
    private String percent;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     *
     * @return
     * The text
     */
    public String getText() {
        return text;
    }

    /**
     *
     * @param text
     * The text
     */
    public void setText(String text) {
        this.text = text;
    }

    public IngredientOld withText(String text) {
        this.text = text;
        return this;
    }

    /**
     *
     * @return
     * The id
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(String id) {
        this.id = id;
    }

    public IngredientOld withId(String id) {
        this.id = id;
        return this;
    }

    /**
     *
     * @return
     * The rank
     */
    public long getRank() {
        return rank;
    }

    /**
     *
     * @param rank
     * The rank
     */
    public void setRank(long rank) {
        this.rank = rank;
    }

    public IngredientOld withRank(long rank) {
        this.rank = rank;
        return this;
    }

    /**
     *
     * @return
     * The percent
     */
    public String getPercent() {
        return percent;
    }

    /**
     *
     * @param percent
     * The percent
     */
    public void setPercent(String percent) {
        this.percent = percent;
    }

    public IngredientOld withPercent(String percent) {
        this.percent = percent;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public IngredientOld withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "text='" + text + '\'' +
                ", id='" + id + '\'' +
                ", rank=" + rank +
                ", percent='" + percent + '\'' +
                ", additionalProperties=" + additionalProperties +
                '}';
    }
}
