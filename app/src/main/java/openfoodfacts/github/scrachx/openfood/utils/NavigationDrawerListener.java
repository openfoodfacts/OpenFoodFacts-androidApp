package openfoodfacts.github.scrachx.openfood.utils;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by Lobster on 06.03.18.
 */

public interface NavigationDrawerListener {

    int ITEM_USER = 0;
    int ITEM_HOME = 1;
    int ITEM_SEARCH_BY_CODE = 2;
    int ITEM_CATEGORIES = 3;
    int ITEM_SCAN = 4;
    int ITEM_COMPARE = 5;
    int ITEM_HISTORY = 6;
    int ITEM_LOGIN = 7;
    int ITEM_ALERT = 8;
    int ITEM_DIET = 9;
    int ITEM_PREFERENCES = 10;
    int ITEM_OFFLINE = 11;
    int ITEM_ABOUT = 12;
    int ITEM_CONTRIBUTE = 13;
    int ITEM_OBF = 14;
    int ITEM_ADVANCED_SEARCH = 15;
    int ITEM_MY_CONTRIBUTIONS = 16;
    int ITEM_LOGOUT = 17;
    int ITEM_MANAGE_ACCOUNT = 18;
    int ITEM_INCOMPLETE_PRODUCTS = 19;
    int ITEM_ADDITIVES = 20;
    int ITEM_YOUR_LISTS = 21;

    @Retention(SOURCE)
    @IntDef({
            ITEM_HOME,
            ITEM_SEARCH_BY_CODE,
            ITEM_CATEGORIES,
            ITEM_SCAN,
            ITEM_COMPARE,
            ITEM_HISTORY,
            ITEM_LOGIN,
            ITEM_ALERT,
            ITEM_DIET,
            ITEM_PREFERENCES,
            ITEM_OFFLINE,
            ITEM_ABOUT,
            ITEM_CONTRIBUTE,
            ITEM_OBF,
            ITEM_ADVANCED_SEARCH,
            ITEM_MY_CONTRIBUTIONS,
            ITEM_LOGOUT,
            ITEM_INCOMPLETE_PRODUCTS,
            ITEM_ADDITIVES
    })
    @interface NavigationDrawerType {
    }

    void setItemSelected(@NavigationDrawerType Integer type);

}
