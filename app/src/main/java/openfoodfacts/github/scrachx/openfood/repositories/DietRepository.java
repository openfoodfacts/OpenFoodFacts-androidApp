package openfoodfacts.github.scrachx.openfood.repositories;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.WhereCondition;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.models.Diet;
import openfoodfacts.github.scrachx.openfood.models.DietDao;
import openfoodfacts.github.scrachx.openfood.models.DietIngredients;
import openfoodfacts.github.scrachx.openfood.models.DietIngredientsDao;
import openfoodfacts.github.scrachx.openfood.models.DietName;
import openfoodfacts.github.scrachx.openfood.models.DietNameDao;
import openfoodfacts.github.scrachx.openfood.models.Ingredient;
import openfoodfacts.github.scrachx.openfood.models.IngredientDao;
import openfoodfacts.github.scrachx.openfood.models.IngredientName;
import openfoodfacts.github.scrachx.openfood.models.IngredientNameDao;
import openfoodfacts.github.scrachx.openfood.models.IngredientsRelationDao;
import openfoodfacts.github.scrachx.openfood.models.LabelName;
import openfoodfacts.github.scrachx.openfood.models.LabelNameDao;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductIngredient;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static java.lang.Character.isDigit;
import static java.lang.Character.isLetter;

//import openfoodfacts.github.scrachx.openfood.models.IngredientsRelation;
//import openfoodfacts.github.scrachx.openfood.models.IngredientsWrapper;
//import openfoodfacts.github.scrachx.openfood.network.ProductApiService;

/**
 * <b>DietRepository is the class to manage the database tables for diet.</b>
 *
 * @author dobriseb
 * @version 0.2
 */

public class DietRepository implements IDietRepository {

    //private ProductApiService productApi;
    private static final String DEFAULT_LANGUAGE = "en";
    private static IDietRepository instance;
    private Database db;
    private DietDao dietDao;
    private DietNameDao dietNameDao;
    private IngredientDao ingredientDao;
    private IngredientNameDao ingredientNameDao;
    private IngredientsRelationDao ingredientsRelationDao;
    private DietIngredientsDao dietIngredientsDao;
    private LabelNameDao labelNameDao;
    private HashMap<Integer, String> colors = new HashMap<Integer, String>();

    //List of States :
    public static final int DIET_STATE_ALL = -2;
    public static final int DIET_STATE_FORBIDEN = -1;
    public static final int DIET_STATE_SOSO = 0;
    public static final int DIET_STATE_AUTHORISED = 1;
    public static final int DIET_STATE_UNKNOWN = 2;
    public static final int DIET_STATE_NO_DIET_ENABLED = 3;


    /**
     *
     * @return instance of dietRepository
     * @author dobriseb
     */
    public static IDietRepository getInstance() {
        if (instance == null) {
            instance = new DietRepository();
        }
        return instance;
    }

    private DietRepository() {
        //productApi = CommonApiManager.getInstance().getProductApiService();
        DaoSession daoSession = OFFApplication.getInstance().getDaoSession();
        db = daoSession.getDatabase();
        dietDao = daoSession.getDietDao();
        dietNameDao = daoSession.getDietNameDao();
        ingredientDao = daoSession.getIngredientDao();
        ingredientNameDao = daoSession.getIngredientNameDao();
        ingredientsRelationDao = daoSession.getIngredientsRelationDao();
        dietIngredientsDao = daoSession.getDietIngredientsDao();
        labelNameDao = daoSession.getLabelNameDao();

        colors.put(-1, "#ff0000");
        colors.put(0, "#ff9900");
        colors.put(1, "#00b400");
        colors.put(2, "#393939");
    }

    /**
     * Diets saving to local database
     * <p>
     * Diet and DietName has One-To-Many relationship, therefore we need to save them separately.
     */
    @Override
    public void saveDiets(List<Diet> diets) {
        db.beginTransaction();
        try {
            for (Diet diet : diets) {
                dietDao.insertOrReplace(diet);
                for (DietName dietName : diet.getNames()) {
                    dietNameDao.insertOrReplace(dietName);
                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Diet saving to local database
     */
    @Override
    public void saveDiet(Diet diet) {
        List<Diet> diets = new ArrayList<>();
        diets.add(diet);
        saveDiets(diets);
    }

    /**
     * remove a Diet from local database
     */
    @Override
    public void removeDiet(Long id) {
        Diet diet = getDietById(id);
        if (diet.getId() > 0) {
            List <DietName> dietNameList = getDietNameListByDietTag(diet.getTag());
            for (int i = 0; i < dietNameList.size(); i++) {
                DietName dietName =  dietNameList.get(i);
                dietNameDao.delete(dietName);
            }
            List <DietIngredients> dietIngredientsList = getDietIngredientsListByDietTag(diet.getTag());
            for (int i = 0; i < dietIngredientsList.size(); i++) {
                DietIngredients dietIngredients =  dietIngredientsList.get(i);
                dietIngredientsDao.delete(dietIngredients);
            }
            diet.delete();
        }
    }

    @Override
    public int getDietCount(){
        return (int) dietDao.count();
    }

    /**
     * DietIngredients saving to local database
     */
    @Override
    public void saveDietIngredients(DietIngredients dietIngredients) {
        dietIngredientsDao.insertOrReplace(dietIngredients);
    }

    /**
     * Changes enabled field of diet and updates it.
     *
     * @param isEnabled depends on whether user selected or unselected the diet
     * @param dietTag   is unique Id of diet
     */
    @Override
    public void setDietEnabled(String dietTag, Boolean isEnabled) {
        Diet diet = dietDao.queryBuilder()
                .where(DietDao.Properties.Tag.eq(dietTag))
                .unique();

        if (diet != null) {
            diet.setEnabled(isEnabled);
            dietDao.update(diet);
        }
    }

    /**
     * Load diets which user selected earlier (i.e user's diets)
     */
    @Override
    public List<Diet> getDiets() {
        return dietDao.loadAll();
    }

    /**
     * Load diets which user selected earlier (i.e user's diets)
     */
    @Override
    public List<Diet> getEnabledDiets() {
        return dietDao.queryBuilder().where(DietDao.Properties.Enabled.eq("true")).list();
    }

    /**
     * Return a Diet from its Tag.
     * In case of doubloons suppress the outnumbers rows.
     *
     * @param tag       The Tag
     * @return Diet     Diet object
     *
     * @author dobriseb
     */
    @Override
    public Diet getDietByTag(String tag) {
        //Looking for the Diet.
        List<Diet> diets = dietDao.queryBuilder().where(DietDao.Properties.Tag.eq(tag)).list();
        if (diets.size() == 0) {
            //Not found, return a new one
            return new Diet();
        } else if (diets.size() > 1) {
            //Too many diet objects with the same Tag. This will never happened, but, well just in case, suppress doubloons and return the survivor
            for (int i = 1; i < diets.size(); i++) {
                Diet diet = diets.get(i);
                diet.delete();
            }
        }
        //return the first (and only) Diet object
        return diets.get(0);
    }

    /**
     * Return a Diet from its ID.
     *
     * @param id       The ID
     * @return Diet     Diet object
     *
     * @author dobriseb
     */
    @Override
    public Diet getDietById(Long id) {
        //Looking for the Diet.
        List<Diet> diets = dietDao.queryBuilder().where(DietDao.Properties.Id.eq(id)).list();
        if (diets.size() == 0) {
            //Not found, return a new one
            return new Diet();
        }
        //return the first (and only) Diet object
        return diets.get(0);
    }

    /**
     * Return the Diet object from a Name and a languageCode
     *
     * @param name          The Name of the Diet
     * @param languageCode  is a 2-digit language code
     * @return Diet         Diet object
     *
     * @author dobriseb
     */
    @Override
    public Diet getDietByNameAndLanguageCode(String name, String languageCode) {
        name = name.trim();
        //Looking for a DietName with name and languageCode
        DietName dietName = getDietNameByNameAndLanguageCode(name, languageCode);
        if (dietName.getDietTag() == null) {
            //Not found, return a new Diet object
            return new Diet();
        }
        //Return the Diet with the Tag of the DietName found.
        return getDietByTag(dietName.getDietTag());
    }

    /**
     * Return the DietNames objects that correspond to the dietTag
     *
     * @param dietTag       The tag of the diet
     * @return DietNames    List of all the name of the diet
     *
     * @author dobriseb
     */
    @Override
    public List <DietName> getDietNameListByDietTag(String dietTag) {
        List<DietName> dietNameList = dietNameDao.queryBuilder().where(
            DietNameDao.Properties.DietTag.eq(dietTag)
        ).list();
        return dietNameList;
    }

    /**
     * Return the DietName object from a Name and langageCode.
     * In case of doubloons suppress the outnumbers rows.
     *
     * @param name          The Name
     * @param languageCode  is a 2-digit language code
     * @return DietName     DietName object
     *
     * @author dobriseb
     */
    @Override
    public DietName getDietNameByNameAndLanguageCode(String name, String languageCode) {
        name = name.trim();
        //Looking for the DietName with name and languageCode
        List<DietName> dietNames = dietNameDao.queryBuilder().where(
                DietNameDao.Properties.Name.eq(name),
                DietNameDao.Properties.LanguageCode.eq(languageCode)
        ).list();
        if (dietNames.size() == 0) {
            //Not fount, return a new one
            return new DietName();
        } else if (dietNames.size() > 1) {
            //Too many DietName objects found, suppress doubloons
            for (int i = 1; i < dietNames.size(); i++) {
                DietName dietName = dietNames.get(i);
                dietNameDao.delete(dietName);
            }
        }
        //Return the first (and only) DietName.
        return dietNames.get(0);
    }

    /**
     * Return the DietName from a Tag and LanguageCode
     * In case of doubloons suppress the outnumbers rows.
     *
     * @param dietTag       dietTag
     * @param languageCode  is a 2-digit language code
     * @return DietName     DietName object
     *
     * @author dobriseb
     */
    @Override
    public DietName getDietNameByDietTagAndLanguageCode(String dietTag, String languageCode) {
        //Looking for a DietName with the dietTag and LanguageCode passed
        List<DietName> dietNames = dietNameDao.queryBuilder().where(
                DietNameDao.Properties.DietTag.eq(dietTag),
                DietNameDao.Properties.LanguageCode.eq(languageCode)
        ).list();
        if (dietNames.size() == 0) {
            //Not fount, return a new one
            return new DietName();
        } else if (dietNames.size() > 1) {
            //Too many DietName, suppress doubloons
            for (int i = 1; i < dietNames.size(); i++) {
                DietName dietName = dietNames.get(i);
                dietNameDao.delete(dietName);
            }
        }
        //return the first (and only) DietName object.
        return dietNames.get(0);
    }

    /**
     * Add a new diet with Name, Description...
     * A test of non existance is done before insertion.
     *
     * @param name         Name of the diet
     * @param description  Description of the diet
     * @param isEnabled    depends on whether user enabled or disabled the diet
     * @param languageCode User's LanguageCode
     *
     * @author dobriseb
     */
    @Override
    public void addDiet(String name, String description, boolean isEnabled, String languageCode) {
        name = name.trim();
        if (name != "") {
            //Looking for a DietName object with this Name and LanguageCode
            DietName dietName = getDietNameByNameAndLanguageCode(name, languageCode);
            if (dietName.getDietTag() == null) {
                //Not found, so a new one has just be created, complete it.
                dietName.setName(name);
                dietName.setLanguageCode(languageCode);
                dietName.setDietTag(languageCode + ":" + name);
            }
            //In all the case update description and save the IngredientName object
            dietName.setDescription(description);
            dietNameDao.getSession().insertOrReplace(dietName);
            //Looking for an existing diet with this name and languageCode
            Diet diet = getDietByNameAndLanguageCode(name, languageCode);
            if (diet.getTag() == null) {
                //The returned Diet object is a new one, set its Tag
                diet.setTag(languageCode + ":" + name);
            }
            //In all the case, update its Enabled field and save the Diet object
            diet.setEnabled(isEnabled);
            dietDao.getSession().insertOrReplace(diet);
        }
    }

    /**
     * Add a new diet with Name, Description...
     * A test of non existance is done before insertion.
     *
     * @param id           ID of the diet
     * @param name         New name of the diet
     * @param description  New description of the diet
     * @param isEnabled    New state of the diet depends on whether user enabled or disabled the diet
     * @param languageCode User's LanguageCode
     *
     * @author dobriseb
     */
    @Override
    public void saveDiet(Long id, String name, String description, boolean isEnabled, String languageCode) {
        if (id.toString().isEmpty() || id==0) {
            addDiet(name, description, isEnabled, languageCode);
        } else {
            //Looking for the diet with this ID
            Diet diet = getDietById(id);
            if (diet.getTag() == null) {
                //The returned Diet object is a new one, set its Tag
                diet.setTag(languageCode + ":" + name);
            }
            //In all the case, update its Enabled field and save the Diet object
            diet.setEnabled(isEnabled);
            //Looking for a DietName object with this Tag and LanguageCode
            DietName dietName = getDietNameByDietTagAndLanguageCode(diet.getTag(), languageCode);
            if (dietName.getDietTag() == null) {
                //Not found, so a new one has just be created, complete it.
                dietName.setLanguageCode(languageCode);
                dietName.setDietTag(diet.getTag());
            }
            if (! dietName.getName().toString().equals(name) && diet.getTag().toString().equals(languageCode + ":" + dietName.getName().toString())) {
                //The name is the one that constitue the Tag with the language code. It's going to be changed. So the Tag must be changed...
                //Why didn't I listen to me and use the Id instead of a Tag !!!
                String newTag = languageCode + ":" + name;
                //First, test if there is no other diet with that Tag
                Diet dietExist = getDietByTag(newTag);
                if (dietExist.getTag() == null) {
                    //OK, we can use that Tag
                    //Direct SQL need refresh on each rows... better way like this.
                    List<DietIngredients> dietIngredientsList = getDietIngredientsListByDietTag(diet.getTag());
                    for (int i = 0; i < dietIngredientsList.size(); i++) {
                        DietIngredients dietIngredients =  dietIngredientsList.get(i);
                        dietIngredients.setDietTag(newTag);
                        saveDietIngredients(dietIngredients);
                    }
                    dietNameDao.getDatabase().execSQL("update DIET_NAME set DIET_TAG='" + newTag + "' where DIET_TAG='" + diet.getTag() + "';");
                    //Now we can change the Name and Tag (in dietName) and the Tag in diet
                    dietName.setName(name);
                    //Yes it seems to be a doublon, but the above SQL have not refreshed the data.
                    dietName.setDietTag(newTag);
                    diet.setTag(newTag);
                }
            }
            //In all the case update name and description and save the IngredientName object
            dietName.setDescription(description);
            dietNameDao.getSession().insertOrReplace(dietName);
            dietDao.getSession().insertOrReplace(diet);
        }
    }

    /**
     * Return an Ingredient object from its Tag
     * In case of doubloons suppress the outnumbers rows.
     *
     * @param tag   The Tag
     * @return Ingredient  Ingredient object
     *
     * @author dobriseb
     */
    @Override
    public Ingredient getIngredientByTag(String tag) {
        //Looking for the Ingredient associated.
        List<Ingredient> ingredients = ingredientDao.queryBuilder().where(IngredientDao.Properties.Tag.eq(tag)).list();
        if (ingredients.size() == 0) {
            //Not found, return a new Ingredient
            return new Ingredient();
        } else if (ingredients.size() > 1) {
            //Too many ingredient objects with the same Tag. This will never happened, but, well just in case, suppress doubloons and return the survivor
            for (int i = 1; i < ingredients.size(); i++) {
                Ingredient ingredient = ingredients.get(i);
                ingredient.delete();
            }
        }
        return ingredients.get(0);
    }

    /**
     * Return the ingredient object search by a name and a languageCode
     *
     * @param name         The Name
     * @param languageCode is a 2-digit language code
     * @return Ingredient  Ingredient object
     *
     * @author dobriseb
     */
    @Override
    public Ingredient getIngredientByNameAndLanguageCode(String name, String languageCode) {
        name = name.trim();
        //Looking for an IngredientName object with name and languageCode
        IngredientName ingredientName = getIngredientNameByNameAndLanguageCode(name, languageCode);
        if (ingredientName.getIngredientTag() == null) {
            //Not found, return a new Ingredient
            return new Ingredient();
        }
        //Return the ingredient that has this ingredientName.
        return getIngredientByTag(ingredientName.getIngredientTag());
    }

    /**
     * Return a new IngredientName object or the first one of a list
     * In case of doubloons suppress the outnumbers rows.
     *
     * @param ingredientNames   Liste of IngredientName objects
     * @return IngredientName   IngredientName object
     *
     * @author dobriseb
     */
    private IngredientName getIngredientNameFromDoublon(List<IngredientName> ingredientNames) {
        if (ingredientNames != null) {
            if (ingredientNames.size() == 0) {
                //No IngredientName, return a new one
                return new IngredientName();
            } else if (ingredientNames.size() > 1) {
                //Doubloon, suppress after test.
                String mLanguageCode = ingredientNames.get(0).getLanguageCode();
                String mName = ingredientNames.get(0).getName();
                String mIngredientTag = ingredientNames.get(0).getIngredientTag();
                for (int i = 1; i < ingredientNames.size(); i++) {
                    IngredientName ingredientName = ingredientNames.get(i);
                    if (ingredientName.getLanguageCode().equals(mLanguageCode) && (ingredientName.getName().equalsIgnoreCase(mName) || ingredientName.getIngredientTag().equalsIgnoreCase(mIngredientTag))) {
                        ingredientNameDao.delete(ingredientName);
                    }
                }
            }
            //Return the first IngredientName.
            return ingredientNames.get(0);
        }
        return null;
    }

    /**
     * Return the IngredientName object from a name and a LanguageCode
     *
     * @param name              le nom
     * @param languageCode      is a 2-digit language code
     * @return IngredientName   IngredientName object
     *
     * @author dobriseb
     */
    @Override
    public IngredientName getIngredientNameByNameAndLanguageCode(String name, String languageCode) {
        name = name.trim();
        //Recherche d'un IngredientName ayant name et languageCode
        List<IngredientName> ingredientNames = ingredientNameDao.queryBuilder().where(
                new WhereCondition.StringCondition("lower(NAME) = lower('" + name.replaceAll("'", "''") + "')"),
                IngredientNameDao.Properties.LanguageCode.eq(languageCode)
        ).list();
        return getIngredientNameFromDoublon(ingredientNames);
    }

    /**
     * Return the IngredientName object from an ingredientTag and a LanguageCode
     *
     * @param ingredientTag     The searched ingredientTag
     * @param languageCode      is a 2-digit language code
     * @return IngredientName   IngredientName object
     *
     * @author dobriseb
     */
    @Override
    public IngredientName getIngredientNameByIngredientTagAndLanguageCode(String ingredientTag, String languageCode) {
        List<IngredientName> ingredientNames = ingredientNameDao.queryBuilder().where(
                IngredientNameDao.Properties.IngredientTag.eq(ingredientTag),
                IngredientNameDao.Properties.LanguageCode.eq(languageCode)
        ).list();
        if (ingredientNames.size() == 0 && ingredientTag.indexOf(languageCode + ":") != 0) {
            //No value for this tag and language code. Looking for "default" value (Tag and LanguageCode of the Tag)
            return getIngredientNameByIngredientTagAndLanguageCode(ingredientTag, ingredientTag.split(":")[0]);
        }
        return getIngredientNameFromDoublon(ingredientNames);
    }

    /**
     * Add a new ingredient from the information languageCode and name of one of its ingredientName(s) if it doesn't already exists.
     *
     * @param name         Name of the ingredient
     * @param languageCode LanguageCode used
     *
     * @author dobriseb
     */
    @Override
    public void addIngredient(String name, String languageCode) {
        name = name.trim().replaceAll("_","").replaceAll("\\*","");
        if (name != "") {
            //Looking for the IngredientName with this name and languageCode
            IngredientName ingredientName = getIngredientNameByNameAndLanguageCode(name, languageCode);
            if (ingredientName.getIngredientTag() == null) {
                //The IngredientName is empty, so we complete it with name, languageCode and ingredientTag
                ingredientName.setName(name);
                ingredientName.setLanguageCode(languageCode);
                ingredientName.setIngredientTag(languageCode + ":" + name.replaceAll(" ","-"));
                ingredientNameDao.getSession().insert(ingredientName);
            }
            //Looking for the Ingredient corresponding at this name and languageCode
            Ingredient ingredient = getIngredientByNameAndLanguageCode(name, languageCode);
            if (ingredient.getTag() == null) {
                //The Ingredient is empty, complete it with the tag
                ingredient.setTag(languageCode + ":" + name.replaceAll(" ","-"));
                ingredientDao.getSession().insertOrReplace(ingredient);
            }
        }
    }

    /**
     * Add a new ingredient from the information ingredientTag, languageCode and name of one of its ingredientName(s) if it doesn't already exists.
     *
     * @param ingredientTag Tag of the ingredient
     * @param name          Name of the ingredient
     * @param languageCode  LanguageCode used
     *
     * @author dobriseb
     */
    @Override
    public void addIngredient(String ingredientTag, String name, String languageCode) {
        Ingredient ingredient = getIngredientByTag(ingredientTag);
        if ( ingredient.getTag() == null) {
            //No ingredient with this tag
            ingredient.setTag(ingredientTag);
            ingredientDao.getSession().insertOrReplace(ingredient);
            //By run addIngredient again we will create the name and associate it to this ingredient.
            addIngredient(ingredientTag, name, languageCode);
        } else {
            name = name.replaceAll("_","").replaceAll("\\*","");
            //A ingredient exists with this tag
            List<IngredientName> ingredientNames = ingredient.getNames();
            boolean addName = true;
            for (int i = 0; i < ingredientNames.size(); i++) {
                IngredientName ingredientName =  ingredientNames.get(i);
                if (ingredientName.getLanguageCode().equals(languageCode) && ingredientName.getName().equals(name)) {
                    //Ingredient exists and had this name in this languageCode. Nothings to do.
                    addName = false;
                }
            }
            if (addName) {
                //Add this name to the ingredient
                IngredientName ingredientName = new IngredientName();
                ingredientName.setName(name);
                ingredientName.setLanguageCode(languageCode);
                ingredientName.setIngredientTag(ingredientTag);
                ingredientNameDao.getSession().insert(ingredientName);
            }
        }
    }

    /**
     * Add a new link Diet/Ingredient from dietName, IngredientTag and state if it doesn't already exists.
     *
     * @param dietTag           Tag of the diet
     * @param ingredientTag     Tag of the ingredient
     * @param state             State code (-1: forbidden, 0: so-so, 1: authorised, 2: no impact)
     */
    @Override
    public void addDietIngredientsByTags(String dietTag, String ingredientTag, int state, String ingredientName, String languageCode) {
        DietIngredients dietIngredients = new DietIngredients();
        dietIngredients.setDietTag(dietTag);
        dietIngredients.setIngredientTag(ingredientTag);
        dietIngredients.setState(state);
        dietIngredients.setIngredientName(ingredientName);
        dietIngredients.setIngredientLanguageCode(languageCode);
        saveDietIngredients(dietIngredients);
    }

    /**
     * Add a new link Diet/Ingredient from dietName, IngredientName and languageCode and state if it doesn't already exists.
     *
     * @param dietTag        Tag of the diet
     * @param ingredientName Ingrédient we wan't to link to the diet
     * @param languageCode   Language code of the ingredientName
     * @param state          State code (-1: forbidden, 0: so-so, 1: authorised, 2: no impact)
     *
     * @author dobriseb
     */
    @Override
    public void addDietIngredients(String dietTag, String ingredientName, String languageCode, int state) {
        ingredientName = ingredientName.trim();
        if (ingredientName != "") {
            Ingredient ingredient = getIngredientByNameAndLanguageCode(ingredientName, languageCode);
            if (ingredient.getTag() != null) {
                //Found Ingredient. Associate it with diet.
                addDietIngredientsByTags(dietTag, ingredient.getTag(), state, ingredientName, languageCode);
            } else {
                //No ingredient found, create it and then create the link
                addIngredient(ingredientName, languageCode);
                addDietIngredients(dietTag, ingredientName, languageCode, state);
            }
        }
    }

    /**
     * Return the DietIngredients objects linked to a diet with a certain state from a dietTag
     *
     * @param dietTag               Tag of the diet
     * @param state                 The searched state (-2 : all, -1 : forbidden, 0 : problematic, 1 authorised, 2 no impact)
     * @return List<DietIngredients>   List of DietIngredients objects.
     *
     * @author dobriseb
     */
    @Override
    public List<DietIngredients> getDietIngredientsListByDietTagAndState(String dietTag, int state) {
        //Looking for DietIngredients having this dietTag
        List<DietIngredients> dietIngredientsList;
        if (state == DIET_STATE_ALL) {
            //Return all dietIngredients for this dietTag
            dietIngredientsList = dietIngredientsDao.queryBuilder().where(
                    DietIngredientsDao.Properties.DietTag.eq(dietTag)
            ).list();
        } else {
            //Return dietIngredients for this dietTag and that State
            dietIngredientsList = dietIngredientsDao.queryBuilder().where(
                    DietIngredientsDao.Properties.DietTag.eq(dietTag),
                    DietIngredientsDao.Properties.State.eq(state)
            ).list();
        }
        return dietIngredientsList;
    }

    /**
     * Return the DietIngredients objects linked to a diet from a dietTag
     *
     * @param dietTag                  Tag of the diet
     * @return List<DietIngredients>   List of DietIngredients objects.
     *
     * @author dobriseb
     */
    @Override
    public List<DietIngredients> getDietIngredientsListByDietTag(String dietTag) {
        return getDietIngredientsListByDietTagAndState(dietTag, DIET_STATE_ALL);
    }

    /**
     * Return a list of ingredient objects linked to a diet with a certain state from a dietTag
     *
     * @param dietTag               Tag of the diet
     * @param state                 The searched state (-2 : all, -1 : forbidden, 0 : problematic, 1 authorised, 2 no impact)
     * @return List<Ingredient>     List of ingredient objects.
     *
     * @author dobriseb
     */
    @Override
    public List<Ingredient> getIngredientsLinkedToDietByDietTagAndState(String dietTag, int state) {
        List<DietIngredients> dietIngredientsList = getDietIngredientsListByDietTagAndState(dietTag, state);
        if (dietIngredientsList != null) {
            DietIngredients dietIngredients;
            Ingredient ingredient;
            List<Ingredient> ingredients = new ArrayList<>();
            for (int i = 0; i < dietIngredientsList.size(); i++) {
                dietIngredients = dietIngredientsList.get(i);
                ingredient = getIngredientByTag(dietIngredients.getIngredientTag());
                if (ingredient.getTag() == null) {
                    //The ingredient was created by an association of a diet and a ingredient of a product.
                    //But it is no more in ingredient table (propably because of a new import of taxonomy.
                    //Add the ingredient :
                    String[] ingredientNT = dietIngredients.getIngredientTag().split(":");
                    addIngredient(ingredientNT[1],ingredientNT[0]);
                    ingredient = getIngredientByNameAndLanguageCode(ingredientNT[1],ingredientNT[0]);
                    if (ingredient.getTag() != null) {
                        if (ingredient.getTag() != dietIngredients.getIngredientTag()) {
                            dietIngredients.setIngredientTag(ingredient.getTag());
                            saveDietIngredients(dietIngredients);
                        }
                    }
                }
                ingredients.add(ingredient);
            }
            return ingredients;
        }
        return null;
    }

    /**
     * Return a list of ingredient objects linked to a diet from its dietTag
     *
     * @param dietTag               Tag of the diet
     * @return List<Ingredient>     List of ingredient objects.
     *
     * @author dobriseb
     */
    @Override
    public List<Ingredient> getIngredientsLinkedToDietByDietTag(String dietTag) {
        return getIngredientsLinkedToDietByDietTagAndState(dietTag,DIET_STATE_ALL);
    }

    /**
     * Return a list of ingredient objects linked to a diet with a certain state from a dietName and its languageCode
     *
     * @param dietName              Name of the diet
     * @param languageCode          LanguageCode of the dietName
     * @param state                 The searched state (-2 : all, -1 : forbidden, 0 : problematic, 1 authorised, 2 no impact)
     * @return List<Ingredient>     List of ingredient objects.
     *
     * @author dobriseb
     */
    @Override
    public List<Ingredient> getIngredientsLinkedToDietByDietNameLanguageCodeAndState(String dietName, String languageCode, int state) {
        Diet diet = getDietByNameAndLanguageCode(dietName, languageCode);
        return getIngredientsLinkedToDietByDietTagAndState(diet.getTag(), state);
    }

    /**
     * Return a list of ingredient objects linked to a diet from a dietName and his languageCode
     *
     * @param dietName          Name of the diet
     * @param languageCode      LanguageCode of the dietName
     * @return List<Ingredient>     List of ingredient objects.
     *
     * @author dobriseb
     */
    @Override
    public List<Ingredient> getIngredientsLinkedToDietByDietNameAndLanguageCode(String dietName, String languageCode) {
        return getIngredientsLinkedToDietByDietNameLanguageCodeAndState(dietName, languageCode, DIET_STATE_ALL);
    }

    /**
     * Return a list of names of ingredients in a languageCode from a list of ingredient (objects)
     *
     * @param ingredients               List of ingrédient objects
     * @param languageCode              LanguageCode to be used
     * @return List<IngredientName>     List of ingredientName objects.
     *
     * @author dobriseb
     */
    @Override
    public List<IngredientName> getIngredientNamesByIngredientsAndLanguageCode(List<Ingredient> ingredients, String languageCode) {
        List<IngredientName> ingredientNames = new ArrayList<>();
        if (ingredients != null) {
            for (int i = 0; i < ingredients.size(); i++) {
                Ingredient ingredient = ingredients.get(i);
                if (ingredient.getTag() != null) {
                    ingredientNames.add(getIngredientNameByIngredientTagAndLanguageCode(ingredient.getTag(), languageCode));
                }
            }
        }
        return ingredientNames;
    }

    /**
     * Return a sorted list of name of ingredients that have a relation with a diet with a certain state and i a languageCode
     *
     * @param dietTag           The tag of the diet
     * @param state             The searched state (-2 : all, -1 : forbidden, 0 : problematic, 1 authorised, 2 no impact)
     * @param languageCode      languageCode for the names of ingredients
     * @return List<String>     Sorted list of ingredients.
     *
     * @author dobriseb
     */
    @Override
    public String getSortedIngredientNameStringByDietTagStateAndLanguageCode(String dietTag, int state, String languageCode) {
        List<Ingredient> ingredients = getIngredientsLinkedToDietByDietTagAndState(dietTag, state);
        if (ingredients != null) {
            List<IngredientName> ingredientNames = getIngredientNamesByIngredientsAndLanguageCode(ingredients, languageCode);
            if (ingredientNames != null) {
                Collections.sort(ingredientNames, new Comparator<IngredientName>() {
                    @Override
                    public int compare(IngredientName iN1, IngredientName iN2) {
                        if (iN1.getName() == null) {
                            if (iN2.getName() == null) {
                                return 0;
                            }
                            return -1;
                        }
                        return iN1.getName().compareTo(iN2.getName());
                    }
                });
                List<String> ingredientNameList = new ArrayList<>();
                for (int i = 0; i < ingredientNames.size(); i++) {
                    ingredientNameList.add(ingredientNames.get(i).getName());
                }
                return android.text.TextUtils.join(", ", ingredientNameList);
            }
        }
        return null;
    }

    /**
     * Return a list of ingredient that match a state for all enabled diets in a languageCode
     *
     * @param state             The searched state (-2 : all, -1 : forbidden, 0 : problematic, 1 authorised, 2 no impact)
     * @param languageCode      languageCode for the names of ingredients
     * @return List<String>     list of ingredients.
     *
     * @author dobriseb
     */
    @Override
    public List<String> getIngredientNameLinkedToEnabledDietsByLanguageCode(int state, String languageCode) {
        List<Diet> diets = getEnabledDiets();
        List<Ingredient> ingredientsTBC = new ArrayList<>();
        for (int i = 0; i < diets.size(); i++) {
            Diet diet = diets.get(i);
            List<Ingredient> ingredients = getIngredientsLinkedToDietByDietTagAndState(diet.getTag(), state);
            for (int j = 0; j < ingredients.size(); j++) {
                Ingredient ingredient = ingredients.get(j);
                if (ingredient.getTag().equals("en:")) {
                    //In earlyer test, some dietIngredient where created without name. Here is the cleanup. Is it still usefull ?
                    List<DietIngredients> dietIngredientsList = dietIngredientsDao.queryBuilder()
                            .where(DietIngredientsDao.Properties.IngredientTag.eq(ingredient.getTag()))
                            .list();
                    for (int k = 0; k < dietIngredientsList.size(); k++) {
                        DietIngredients dietIngredients =  dietIngredientsList.get(k);
                        dietIngredientsDao.delete(dietIngredients);
                    }
                    List<IngredientName> ingredientNames = ingredientNameDao.queryBuilder()
                            .where(IngredientNameDao.Properties.IngredientTag.eq(ingredient.getTag()))
                            .list();
                    for (int k = 0; k < ingredientNames.size(); k++) {
                        IngredientName ingredientName =  ingredientNames.get(k);
                        ingredientNameDao.delete(ingredientName);
                    }
                    ingredientDao.delete(ingredient);
                } else {
                    if (!ingredientsTBC.contains(ingredient)) {
                        ingredientsTBC.add(ingredient);
                    }
                }
            }
        }
        List<IngredientName> ingredientNames = getIngredientNamesByIngredientsAndLanguageCode(ingredientsTBC, languageCode);
        if (ingredientNames != null) {
            List<String> ingredientNameList = new ArrayList<>();
            for (int i = 0; i < ingredientNames.size(); i++) {
                if (ingredientNames.get(i).getName() != null) {
                    ingredientNameList.add(ingredientNames.get(i).getName());
                }
            }
            return ingredientNameList;
        }
        return new ArrayList<String>();
    }

    /**
     * Return the state value for a ingretient/diet relation
     *
     * @param ingredientTag     Tag de l'ingrédient
     * @param dietTag           Tag de la diet
     * @return long             State value.
     *
     * @author dobriseb
     */
    @Override
    public int stateFromIngredientTagDietTag(String ingredientTag, String dietTag){
        int state = DIET_STATE_UNKNOWN;
        DietIngredients dietIngredients = dietIngredientsDao.queryBuilder()
                .where(
                        DietIngredientsDao.Properties.DietTag.eq(dietTag),
                        DietIngredientsDao.Properties.IngredientTag.eq(ingredientTag)
                )
                .unique();

        if (dietIngredients != null) {
            state = dietIngredients.getState();
        }
        return state;
    }

    /**
     * Return the minimal value of ths states that has an ingredient in its relation with enabled diets
     *
     * @param ingredientTag    the ingredient Tag
     * @return long             min State of the ingredient for enabled diets
     *
     * @author dobriseb
     */
    public int minStateForEnabledDietFromIngredientTag(String ingredientTag){
        int state = DIET_STATE_UNKNOWN;
        List<DietIngredients> dietIngredients = dietIngredientsDao.queryBuilder()
                .where(
                        new WhereCondition.StringCondition("DIET_TAG in (SELECT TAG FROM DIET WHERE ENABLED)"),
                        DietIngredientsDao.Properties.IngredientTag.eq(ingredientTag)
                )
                .list();

        if (dietIngredients.size() > 0) {
            for (int i = 0; i < dietIngredients.size(); i++) {
                DietIngredients dietIngredient =  dietIngredients.get(i);
                state = dietIngredient.getState() < state ? dietIngredient.getState() : state;
            }
        }
        return state;
    }

    private int stateFromIngredientDietTag(String ingredientxt, String dietTag, String languageCode){
        int state = DIET_STATE_UNKNOWN;
        Ingredient ingredient = getIngredientByNameAndLanguageCode(ingredientxt, languageCode);
        if (ingredient.getTag() != null) {
            state = stateFromIngredientTagDietTag(ingredient.getTag(), dietTag);
        }
        return state;
    }

    private int minStateForEnabledDietFromIngredient(String ingredientxt, String languageCode){
        int state = DIET_STATE_UNKNOWN;
        Ingredient ingredient = getIngredientByNameAndLanguageCode(ingredientxt, languageCode);
        if (ingredient.getTag() != null) {
            state = minStateForEnabledDietFromIngredientTag(ingredient.getTag());
        }
        return state;
    }

    private int stateFromIngredientDiet(String ingredientxt, String dietxt, String languageCode){
        int state = DIET_STATE_UNKNOWN;
        Diet diet = getDietByNameAndLanguageCode(dietxt, languageCode);
        if (diet != null) {
            state = stateFromIngredientDietTag(ingredientxt, diet.getTag(), languageCode);
        }
        return state;
    }

    /**
     * Return a SpannableStringBuilder of the ingredients colored when associate with a Diet. Parameters : list Pattern ingredient splitter, ingredients text.
     *
     * @param INGREDIENT_PATTERN    Pattern splitter.
     * @param txtIngredients        SpannableStringBuilder composed with the list of ingrédients.
     * @return SpannableStringBuilder   A SpannableStringBuilder with coloured text.
     *
     * @author dobriseb
     */
    @Override
    public SpannableStringBuilder getColoredSpannableStringBuilderFromSpannableIngredients(Pattern INGREDIENT_PATTERN, SpannableStringBuilder txtIngredients) {
        List<String> ingredientsToBeColoredInRed = getIngredientNameLinkedToEnabledDietsByLanguageCode(-1, Locale.getDefault().getLanguage());
        List<String> ingredientsToBeColoredInOrange = getIngredientNameLinkedToEnabledDietsByLanguageCode(0, Locale.getDefault().getLanguage());
        List<String> ingredientsToBeColoredInGreen = getIngredientNameLinkedToEnabledDietsByLanguageCode(1, Locale.getDefault().getLanguage());
        if (ingredientsToBeColoredInGreen.size() > 0) txtIngredients = setSpanColorBetweenTokens(INGREDIENT_PATTERN, txtIngredients, ingredientsToBeColoredInGreen, 1);
        if (ingredientsToBeColoredInOrange.size() > 0) txtIngredients = setSpanColorBetweenTokens(INGREDIENT_PATTERN, txtIngredients, ingredientsToBeColoredInOrange, 0);
        if (ingredientsToBeColoredInRed.size() > 0) txtIngredients = setSpanColorBetweenTokens(INGREDIENT_PATTERN, txtIngredients, ingredientsToBeColoredInRed, -1);
        return txtIngredients;
    }
    //To be delete ?

    /**
     * Return a SpannableStringBuilder of the ingredients colored when associate with an active Diet.
     * Parameters : ingredients on a SpannableStringBuilder form.
     *
     * @param ssbIngredients        SpannableStringBuilder composed with the ingrédients.
     * @param languageCode          LanguageCode of the product for search in the same languageCode.
     * @return SpannableStringBuilder   A SpannableStringBuilder with coloured text.
     *
     * @author dobriseb
     */
    @Override
    public SpannableStringBuilder getColoredSSBFromSSBIngredients(SpannableStringBuilder ssbIngredients, String languageCode) {
        String ingredientsText = ssbIngredients.toString();
        List<String> ingredientsList = getIngredientsListFromIngredientsText(ingredientsText, true);
        List<SpannableStringBuilder> ssbIngredientsList = getColoredSSBFromIngredientsDiet(ingredientsList, "enabled", languageCode);
        //For preserve eventually spanned text, we need to add our own spanned and not replace the SSB
        int start = 0;
        int end = 0;
        int fromIndex = 0;
        for (int i = 0; i < ssbIngredientsList.size(); i++) {
            SpannableStringBuilder ssbIngredient =  ssbIngredientsList.get(i);
            start = ingredientsText.indexOf(ssbIngredient.toString(),fromIndex);
            fromIndex += ssbIngredient.length();
            if (start >= 0) {
                end = start + ssbIngredient.length();
                ssbIngredients.replace(start, end, ssbIngredient);
            }
        }
        return ssbIngredients;
    }

    /**
     * Return a SpannableStringBuilder of the ingredients colored when associate with an active Diet.
     * Parameters : ingredients on a SpannableStringBuilder form and the product.
     *
     * @param ssbIngredients        SpannableStringBuilder composed with the ingrédients already spanned by allergen...
     * @param product               product itself.
     * @return SpannableStringBuilder   A SpannableStringBuilder with coloured text.
     *
     * @author dobriseb
     */
    @Override
    public Object[] getColoredSSBAndProductStateFromSSBAndProduct(SpannableStringBuilder ssbIngredients, Product product) {
        if (getEnabledDiets().size() == 0) {
            //No diets are enabled so no colorisation of the ssb and no state
            return new Object[]{ssbIngredients, DIET_STATE_NO_DIET_ENABLED};
        }
        int state;
        int productState = DIET_STATE_AUTHORISED;
        int start = 0;
        int end = 0;
        int fromIndex = 0;
        int startInIngredients = 0;
        int endInIngredients = 0;
        int fromIndexInIngredients = 0;
        String ingredient = "";
        String languageCode = product.getLang();;
        //List of the ingredients of the product in text and lower case to optimize indexOf.
        //In lowercase will find more ingredient in ingredients...
        //When an additive is alone between parenthesis, it is consider like an ingredient of non-zero rank, so let replace it's parenthesis by some comas.
        String ingredients = ssbIngredients.toString().toLowerCase().replaceAll("\\((e[0-9]+)\\)",",$1,");
        //Let have 2 ingredients list, one with only rank>1 and one with rank0 (in other words, one without anything between parenthesis and one qith only things between parenthesis)
        String ingredients0 = ingredients;
        start = ingredients.indexOf("(");
        end = ingredients.length();
        while (start>0) {
            end = ingredients.indexOf(")", start);
            if (end == -1) {
                //Parenthesis not closed !!! go to the end
                end = ingredients.length();
            }
            ingredients = ingredients.substring(0,start) + new String(new char[end-start]).replace('\0', ' ') + ingredients.substring(end);
            ingredients0 = ingredients0.substring(0,fromIndex) + new String(new char[start-fromIndex]).replace('\0', ' ') + ingredients0.substring(start);
            fromIndex = end + 1;
            start = ingredients.indexOf("(", fromIndex);
        }
        ingredients0 = ingredients0.substring(0,end) + new String(new char[ingredients0.length()-end]).replace('\0', ' ');

        //List of productIngredient of the product
        List<ProductIngredient> productIngredients = product.getProductIngredients();
        //For each productIngredient
        for (int i = 0; i < productIngredients.size(); i++) {
            ProductIngredient productIngredient = productIngredients.get(i);
            //Are we still in the productIngredients with a Rank>0 ?
            // If yes continue to test on ingredients, if no switch with ingredients0
            if (productIngredient.getRank() == 0 && ingredients0 != null) {
                //This ingredient is part of another, for example "sucre" in "..., chocolat noir 6.1% [pâte de cacao, sucre, cacao maigre en poudre, émulsifiant (lécithine de soja), arôme]..."
                //We already have treated the other which have a rank>1, so we have to get back to the beginning to treat this ingredient.
                ingredients = ingredients0;
                ingredients0 = null;
                //fromIndex=0;
            }
            //Suppress underscore from the getText string cause there is no underscore in ssbIngredients and pass it in lowercase.
            ingredient = productIngredient.getText().replaceAll("_","").toLowerCase();

            //Looking for the place of ingredient in ingredients
            startInIngredients = ingredients.indexOf(ingredient);
            if (startInIngredients < 0) {
                //Now, label are sometimes added to text in ingredients
                // so "Quinoa Real rouge et blanc Bio" was not found in "... Quinoa Real rouge* et blanc*, ..."
                // because of the label "Bio" have been added
                //Remove labels
                if (productIngredient.getAdditionalProperties().containsKey("labels")) {
                    String labelTag = (String) productIngredient.getAdditionalProperties().get("labels");
                    //Looking for the name of this label in the language code of the product
                    LabelName labelName = labelNameDao.queryBuilder()
                        .where(
                            LabelNameDao.Properties.LabelTag.eq(labelTag),
                            LabelNameDao.Properties.LanguageCode.eq(languageCode)
                        )
                        .unique();
                    if (labelName != null) {
                        ingredient = ingredient.replaceAll(" " + labelName.getName().toLowerCase(), "");
                        startInIngredients = ingredients.indexOf(ingredient);
                    }
                }
            }
            if (startInIngredients >= 0) {
                //found ingredient in ingredients
                endInIngredients = startInIngredients + ingredient.length();
                fromIndexInIngredients = startInIngredients;
            } else {
                //The ingredient is not in the list. Theoretically, this is not possible !!!
                // But, yes, it is... "huile de tournesol désodorisée" was not found in "..., huile de tournesol* désodorisée, ..."
                // because of the position of the asterisk
                //Search for words by words
                if (ingredient.indexOf(" ") > 0) {
                    //Ingredient is composed from at least 2 words, search each one from the end of the other.
                    String[] ingredientWords = ingredient.split(" ");
                    startInIngredients = ingredients.indexOf(ingredientWords[0]);
                    if (startInIngredients > 0) {
                        fromIndexInIngredients = startInIngredients + ingredientWords[0].length();
                        for (int j = 1; j < ingredientWords.length; j++) {
                            String ingredientWord = ingredientWords[j];
                            endInIngredients = ingredients.indexOf(ingredientWord, fromIndexInIngredients);
                            if (endInIngredients == -1){
                                //Well this word has not been found
                                // Don't know what to to
                                startInIngredients = -1;
                                endInIngredients = -1;
                                break;
                            } else if (endInIngredients - fromIndexInIngredients > 2) {
                                //More than 2 characters between the words, doesn't seems reasonable.
                                startInIngredients = -1;
                                endInIngredients = -1;
                                break;
                            } else {
                                fromIndexInIngredients = endInIngredients + ingredientWord.length();
                            }
                        }
                        if (startInIngredients > 0) {
                            //It's look like one character has been added between 2 words of ingredient once or more time
                            //Let conserve this result
                            endInIngredients = fromIndexInIngredients;
                        }
                    }
                }
            }

            //Now, looking for a state lower than 2 from the relation between actives diets and this productIngredient
            //First looking for the state of the tag
            state = minStateForEnabledDietFromIngredientTag(productIngredient.getId());
            if (state == DIET_STATE_UNKNOWN) {
                //The ingredientTag doesn't seems to have a relation with an active diet, looking for the sentence.
                //Theoretically this will never append. But this is theory.
                state = minStateForEnabledDietFromIngredient(ingredient, languageCode);
            }
            if (state == DIET_STATE_UNKNOWN) {
                //The sentence doesn't seems to have a relation with a diet, looking for each word.
                //First, if we don't know the relation of the ingredient and the diet then the productState can't stay at 1
                if (productState == DIET_STATE_AUTHORISED) productState=DIET_STATE_UNKNOWN;
                //Search for each words if there is many.
                if (ingredient.indexOf(" ") > 0) {
                    //Ingredient is composed from at least 2 words, test each one.
                    String[] ingredientWords = ingredient.split(" ");
                    for (int j = 0; j < ingredientWords.length; j++) {
                        String ingredientWord = ingredientWords[j];
                        state = minStateForEnabledDietFromIngredient(ingredientWord, languageCode);
                        if (state < DIET_STATE_UNKNOWN) {
                            //This word must be colored
                            //If this state is under the productState and 1, then it is the new productState
                            if (state < DIET_STATE_AUTHORISED && state < productState) productState = state;
                            start = ingredients.indexOf(ingredientWord);
                            end = start + ingredientWord.length();
                            ssbIngredients = coloredSSBFromState(ssbIngredients, start, end, state);
                        }
                    }
                }
            } else {
                //The Tag or the complete sentence has been found and must be colored
                //If this state is under the productState and DIET_STATE_AUTHORISED, then it is the new productState
                if (state <DIET_STATE_AUTHORISED && state < productState) productState = state;
                if (startInIngredients >= 0) {
                    //Just colored the sentence found.
                    ssbIngredients = coloredSSBFromState(ssbIngredients, startInIngredients, endInIngredients, state); // .setSpan(new ForegroundColorSpan(Color.parseColor(colors.get((int) (long) state))),0,24,SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    //This means that we haven't found the productIngredient in the ingredients of the product
                    Log.i("IMPOSSIBLE", "The ingredient \"" + ingredient + "\" was not found in \"" + ingredients +"\".");
                    //TODO search another way to find the ingredient or add it at the bottom of the list
                }
            }
            //Replace ingredient by white spaces in ingredients.
            //The goal is to treat only once the ingredients that are write more than one time, for example "sucre" in "..., sucre, oeufs 11.4%, chocolat noir 6.1% [pâte de cacao, sucre,..."
            //The problem is that the first "sucre" is given with a rank>1 and the second, which is a component of "chocolat noir" came later with a rank=0.
            //When we will treat "sucre" with rank>1, the example above will become "...,      , oeufs 11.4%, chocolat noir 6.1% [pâte de cacao, sucre,..."
            //In this example : "..., oeufs 11.4%, chocolat noir 6.1% [pâte de cacao, sucre, cacao maigre en poudre, émulsifiant (lécithine de soja), arôme], sucre..."
            //"oeufs" then "chocolat noir" will transform ingredients in "...,       11.4%,               6.1% [pâte de cacao, sucre, cacao maigre en poudre, émulsifiant (lécithine de soja), arôme], sucre..."
            //then came "sucre" with rank>0 : "...,       11.4%,               6.1% [pâte de cacao,      , cacao maigre en poudre, émulsifiant (lécithine de soja), arôme], sucre..."
            //The erased "sucre" is not the good one, but it doesn't matter cause the second (and "good") "sucre" will be treated when the "sucre" of rank=0 will come.
            //So we split the ingredients in two (before start and after) and replace the ingredient from the second one by spaces (as many as the length of ingredient).
            //ingredients = ingredients.substring(0,startInIngredients) + ingredients.substring(startInIngredients).replaceFirst(Pattern.quote(ingredient), new String(new char[ingredient.length()]).replace('\0', ' '));
            if (startInIngredients > -1) {
                ingredients = ingredients.substring(0,startInIngredients) + new String(new char[endInIngredients-startInIngredients]).replace('\0', ' ') + ingredients.substring(endInIngredients);
            }
            //Now we can suppress asterisk(s).
            //ingredient = productIngredient.getText().replaceAll("\\*","");
        }

        return new Object[]{ssbIngredients, productState};
    }

    /**
     * search
     */
    /**
     * Return a SpannableStringBuilder of the ingredients colored when associate with an active Diet.
     *
     * @param INGREDIENT_PATTERN        Pattern to separate each ingredient from
     * @param text                      String of ingredients
     * @param ingredientsToBeColored    List of the ingredients to be colored
     * @param state                     State for the color (-1 : red, 0, orange, 1 green)
     * @return
     */
    private SpannableStringBuilder setSpanColorBetweenTokens(Pattern INGREDIENT_PATTERN, CharSequence text, List<String> ingredientsToBeColored, int state) {
        final SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        Matcher m = INGREDIENT_PATTERN.matcher(ssb);
        while (m.find()) {
            final String tm = m.group();
            final String ingredientValue = tm.replaceAll("[(),.-]+", "");
            for (String ingredientToBeColored : ingredientsToBeColored) {
                if (ingredientToBeColored.equalsIgnoreCase(ingredientValue)) {
                    int start = m.start();
                    int end = m.end();
                    if (tm.contains("(")) {
                        start += 1;
                    } else if (tm.contains(")")) {
                        end -= 1;
                    }
                    ssb.setSpan(new ForegroundColorSpan(Color.parseColor(colors.get((int) (long) state))), start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        return ssb;
    }

    /**
     * Return a list of colored SpannableStringBuilder from a product and a diet
     *
     * @param product                           The product
     * @param dietTag        dietTag of the diet or "enabled" for all diets that are enabled.
     * @return List<SpannableStringBuilder>   List of SpannableStringBuilder with coloured text.
     */
    @Override
    public List<SpannableStringBuilder> getColoredSSBFromProductAndDiet(Product product, String dietTag) {
        List<SpannableStringBuilder> ingredientsSp = new ArrayList<>();
        SpannableStringBuilder ingredientSp;
        int state;
        int start = 0;
        int end = 0;
        String ingredient = "";
        String languageCode = product.getLang();
        //Iterate the list of ingredient of the product
        List<ProductIngredient> productIngredients = product.getProductIngredients();
        for (int i = 0; i < productIngredients.size(); i++) {
            ProductIngredient productIngredient =  productIngredients.get(i);
            //Suppress underscore
            ingredient = productIngredient.getText().replaceAll("_","");
            ingredientSp = new SpannableStringBuilder(ingredient);
            //Search for state by ingredientTag (and dietTag)
            state = dietTag.equalsIgnoreCase("enabled") ? minStateForEnabledDietFromIngredientTag(productIngredient.getId()) : stateFromIngredientTagDietTag(productIngredient.getId(), dietTag);
            if (state == DIET_STATE_UNKNOWN) {
                //Search for state by ingredient text, languageCode (and dietTag)
                state = dietTag.equalsIgnoreCase("enabled") ? minStateForEnabledDietFromIngredient(ingredient, languageCode) : stateFromIngredientDietTag(ingredient, dietTag, languageCode);
            }
            if (state == DIET_STATE_UNKNOWN) {
                //Search state for each words of ingredients text
                if (ingredient.indexOf(" ") > 0) {
                    //Ingredient is composed from at least 2 words, test each one.
                    String[] ingredientWords = ingredient.split(" ");
                    for (int j = 0; j < ingredientWords.length; j++) {
                        String ingredientWord = ingredientWords[j];
                        state = dietTag.equalsIgnoreCase("enabled") ? minStateForEnabledDietFromIngredient(ingredientWord, languageCode) : stateFromIngredientDietTag(ingredientWord, dietTag, languageCode);
                        if (state != DIET_STATE_UNKNOWN) {
                            //This word must be colored
                            start = ingredient.indexOf(ingredientWord);
                            end = start + ingredientWord.length();
                            ingredientSp = coloredSSBFromState(ingredientSp, start, end, state);
                        }
                    }
                }
            } else {
                //The whole text has to be colored
                ingredientSp = coloredSSBFromState(ingredientSp,0,ingredient.length(), state);
           }
            ingredientsSp.add(ingredientSp);
        }
        return ingredientsSp;
    }

    /**
     * Return a list of SpannableStringBuilder of ingredients witch color depend of state from a list of ingredients, a dietTag and a languageCode
     *
     * @param ingredients    List of ingrédients to be colored.
     * @param dietTag        dietTag of the diet or "enabled" for all diets that are enabled.
     * @param languageCode   Language code of the user.
     * @return List<SpannableStringBuilder>   List of SpannableStringBuilder with coloured text.
     *
     * @author dobriseb
     */
    @Override
    public List<SpannableStringBuilder> getColoredSSBFromIngredientsDiet(List<String> ingredients, String dietTag, String languageCode) {
        List<SpannableStringBuilder> ingredientsSp = new ArrayList<>();
        int state;
        for (int i = 0; i < ingredients.size(); i++) {
            String ingredient =  ingredients.get(i);
            state = dietTag.equalsIgnoreCase("enabled") ? minStateForEnabledDietFromIngredient(ingredient, languageCode) : stateFromIngredientDietTag(ingredient, dietTag, languageCode);
            if (state == DIET_STATE_UNKNOWN && ingredient.indexOf(" ") > 0) {
                //Ingredient is composed from at least 2 words, test each one.
                SpannableStringBuilder ingredientSp = new SpannableStringBuilder(ingredient);
                String[] ingredientWords = ingredient.split(" ");
                for (int j = 0; j < ingredientWords.length; j++) {
                    String ingredientWord = ingredientWords[j];
                    state = dietTag.equalsIgnoreCase("enabled") ? minStateForEnabledDietFromIngredient(ingredientWord, languageCode) : stateFromIngredientDietTag(ingredientWord, dietTag, languageCode);
                    if (state != DIET_STATE_UNKNOWN) {
                        int start = ingredient.indexOf(ingredientWord);
                        int end = start + ingredientWord.length();
                        ingredientSp = coloredSSBFromState(ingredientSp, start, end, state);
                    }
                }
                ingredientsSp.add(ingredientSp);
            } else {
                ingredientsSp.add(coloredSSBFromState(new SpannableStringBuilder(ingredient), 0, ingredient.length(), state));
            }
        }
        return ingredientsSp;
    }

    private SpannableStringBuilder coloredSSBFromState(SpannableStringBuilder ss, int start, int end, int state) {
        ss.setSpan(new ForegroundColorSpan(Color.parseColor(colors.get((int) (long) state))), start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    /**
     * Return a String list ingredients from a text of ingredients
     *
     * @param ingredientsText    Ingrédients in a text form.
     * @param preserveAllSign    true : preserve oll Sign except _; false just preserve ingredient
     * @return List<String>      List of ingredients.
     *
     * @author dobriseb
     */
    @Override
    public List<String> getIngredientsListFromIngredientsText(String ingredientsText, boolean preserveAllSign){
        //Remove underscore "_".
        ingredientsText = ingredientsText.replaceAll("_","");
        List<String> ingredientsList = new ArrayList<String>();
        if (preserveAllSign) {
            ingredientsList.addAll(Arrays.asList(ingredientsText.replaceAll("([\\*\\n]|[ (]+\\p{Nd}.[\\.,]*\\p{Nd}*+[ %]*|\\s*[,()\\[\\]:.]+\\s*)","_$1_").split("_")));
        } else {
            //Remove percent, underscore, asterisk, then split on coma",", point".", parentheses"()", brackets "[]", colon":".
            ingredientsList.addAll(Arrays.asList(ingredientsText.replaceAll("[_\\*]|[ (]+\\p{Nd}.[\\.,]*\\p{Nd}*+[ %]*", "").split("\\s*[,()\\[\\]:.]+\\s*")));
            //Remove blank (or so) lines
            for (int i = ingredientsList.size()-1; i >= 0; i--) {
                String s =  ingredientsList.get(i);
                if (s.matches("^\\s*$")) {
                    ingredientsList.remove(i);
                }
            }
        }
        return ingredientsList;
    }

    /**
     * Export diet informations as JSON objetc
     *
     * @param diet          The diet to be exported
     * @return JSONObject
     */
    static JSONObject dietToJson(Diet diet) {
        JSONObject retVal = new JSONObject();
        try {
            retVal.put("Tag", diet.getTag());
            retVal.put("Enabled", diet.getEnabled() ? "1" : "0");
        } catch(Exception ex) {
                Log.e("dietToJson", "Exception converting cursor column to json field: " + diet.getTag());
        }
        return retVal;
    }

    /**
     * Export DietName informations as JSON objetc
     * @param dietName      The dietName to be exported
     * @return JSONObject
     */
    static JSONObject dietNameToJson(DietName dietName) {
        JSONObject retVal = new JSONObject();
        try {
            retVal.put("DietTag", dietName.getDietTag());
            retVal.put("LanguageCode", dietName.getLanguageCode());
            retVal.put("Name", dietName.getName());
            retVal.put("Description", dietName.getDescription());
        } catch(Exception ex) {
                Log.e("dietNameToJson", "Exception converting cursor column to json field: " + dietName.getDietTag());
        }
        return retVal;
    }

    /**
     * Export dietIngredients information as JSON objetc
     *
     * @param dietIngredients   The dietIngredients to be exported
     * @return JSONObject
     */
    static JSONObject dietIngredientsToJson(DietIngredients dietIngredients) {
        JSONObject retVal = new JSONObject();
        try {
            retVal.put("dietTag", dietIngredients.getDietTag());
            retVal.put("ingredientTag", dietIngredients.getIngredientTag());
            retVal.put("state", dietIngredients.getState());
        } catch(Exception ex) {
                Log.e("dietIngredientsToJson", "Exception converting cursor column to json field: " + dietIngredients.getDietTag() + "/" + dietIngredients.getIngredientTag());
        }
        return retVal;
    }

    /**
     * Export Diet and it's dietNames and dietIngredients information in a json string
     * @param diet      The diet to be exported
     * @return String   json string format.
     */
    @Override
    public String exportDietToJson(Diet diet){
        JSONObject diet_export = dietToJson(diet);
        JSONArray namesJson = new JSONArray();
        List<DietName> dietNames = dietNameDao.queryBuilder()
                .where(DietNameDao.Properties.DietTag.eq(diet.getTag()))
                .list();
        for (int i = 0; i < dietNames.size(); i++) {
            DietName dietName =  dietNames.get(i);
            namesJson.put(dietNameToJson(dietName));
        }
        try {
            diet_export.put("Names", namesJson);
        } catch (JSONException e) {
            Log.e("exportDietToJson", "unexpected JSON exception", e);
        }
        JSONArray dietIngredientsListJson = new JSONArray();
        List<DietIngredients> dietIngredientsList = dietIngredientsDao.queryBuilder()
                .where(DietIngredientsDao.Properties.DietTag.eq(diet.getTag()))
                .list();
        for (int i = 0; i < dietIngredientsList.size(); i++) {
            DietIngredients dietIngredients =  dietIngredientsList.get(i);
            dietIngredientsListJson.put(dietIngredientsToJson(dietIngredients));
        }
        try {
            diet_export.put("DietIngredients", dietIngredientsListJson);
        } catch (JSONException e) {
            Log.e("exportDietToJson", "unexpected JSON exception", e);
        }
        return diet_export.toString();
    }
}