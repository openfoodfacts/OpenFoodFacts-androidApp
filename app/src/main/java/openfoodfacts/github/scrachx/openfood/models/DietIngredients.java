package openfoodfacts.github.scrachx.openfood.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.JoinProperty;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by dobriseb on 2018.10.15.
 */

@Entity(indexes = {
        @Index(value = "dietTag, ingredientTag", unique = true)
})
public class DietIngredients {
    @Id(autoincrement = true)
    Long id;
    private String dietTag;
    private String ingredientTag;
    private int state;
    private String ingredientLanguageCode;
    private String ingredientName;
@Generated(hash = 1021631038)
public DietIngredients(Long id, String dietTag, String ingredientTag, int state,
        String ingredientLanguageCode, String ingredientName) {
    this.id = id;
    this.dietTag = dietTag;
    this.ingredientTag = ingredientTag;
    this.state = state;
    this.ingredientLanguageCode = ingredientLanguageCode;
    this.ingredientName = ingredientName;
}
@Generated(hash = 362957604)
public DietIngredients() {
}
public Long getId() {
    return this.id;
}
public void setId(Long id) {
    this.id = id;
}
public String getDietTag() {
    return this.dietTag;
}
public void setDietTag(String dietTag) {
    this.dietTag = dietTag;
}
public String getIngredientTag() {
    return this.ingredientTag;
}
public void setIngredientTag(String ingredientTag) {
    this.ingredientTag = ingredientTag;
}
public int getState() {
    return this.state;
}
public void setState(int state) {
    this.state = state;
}
public String getIngredientLanguageCode() {
    return this.ingredientLanguageCode;
}
public void setIngredientLanguageCode(String ingredientLanguageCode) {
    this.ingredientLanguageCode = ingredientLanguageCode;
}
public String getIngredientName() {
    return this.ingredientName;
}
public void setIngredientName(String ingredientName) {
    this.ingredientName = ingredientName;
}
}
