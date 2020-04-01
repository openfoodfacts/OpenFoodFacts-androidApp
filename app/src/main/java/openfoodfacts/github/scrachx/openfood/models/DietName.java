package openfoodfacts.github.scrachx.openfood.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by dobriseb on 2018.10.15.
 */

@Entity(indexes = {
        @Index(value = "languageCode, dietTag", unique = true)
})
public class DietName {
    @Id(autoincrement = true)
    Long id;
    private String dietTag;
    private String languageCode;
    private String name;
    private String description;
@Generated(hash = 1260288320)
public DietName(Long id, String dietTag, String languageCode, String name,
        String description) {
    this.id = id;
    this.dietTag = dietTag;
    this.languageCode = languageCode;
    this.name = name;
    this.description = description;
}
@Generated(hash = 1305148853)
public DietName() {
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
public String getLanguageCode() {
    return this.languageCode;
}
public void setLanguageCode(String languageCode) {
    this.languageCode = languageCode;
}
public String getName() {
    return this.name;
}
public void setName(String name) {
    this.name = name;
}
public String getDescription() {
    return this.description;
}
public void setDescription(String description) {
    this.description = description;
}
}
