package openfoodfacts.github.scrachx.openfood.views;

import android.Manifest;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.AllergensAlertFragment;
import openfoodfacts.github.scrachx.openfood.fragments.DietsFragment;
import openfoodfacts.github.scrachx.openfood.fragments.FindProductFragment;
import openfoodfacts.github.scrachx.openfood.fragments.HomeFragment;
import openfoodfacts.github.scrachx.openfood.fragments.OfflineEditFragment;
import openfoodfacts.github.scrachx.openfood.fragments.PreferencesFragment;
import openfoodfacts.github.scrachx.openfood.images.ProductImage;
import openfoodfacts.github.scrachx.openfood.models.OfflineSavedProductDao;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener;
import openfoodfacts.github.scrachx.openfood.utils.RealPathUtil;
import openfoodfacts.github.scrachx.openfood.utils.SearchSuggestionProvider;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.utils.ShakeDetector;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.PhotosAdapter;
import openfoodfacts.github.scrachx.openfood.views.category.activity.CategoryActivity;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;
import openfoodfacts.github.scrachx.openfood.views.listeners.BottomNavigationListenerInstaller;
import openfoodfacts.github.scrachx.openfood.workers.OfflineEditPendingProductsWorker;

import static openfoodfacts.github.scrachx.openfood.BuildConfig.APP_NAME;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.OTHER;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.OFFLINE_EDIT_PENDING_WORK_NAME;

public class MainActivity extends BaseActivity implements CustomTabActivityHelper.ConnectionCallback, NavigationDrawerListener, SharedPreferences.OnSharedPreferenceChangeListener {
    public static final int LOGIN_REQUEST = 1;
    private static final int SHOW_CONTRIBUTION_AFTER_LOGIN = 2;
    private static final long USER_ID = 500;
    private static final String CONTRIBUTIONS_SHORTCUT = "CONTRIBUTIONS";
    private static final String SCAN_SHORTCUT = "SCAN";
    private static final String BARCODE_SHORTCUT = "BARCODE";
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;

    PrimaryDrawerItem primaryDrawerItem;
    private AccountHeader headerResult = null;
    private Drawer result = null;
    private MenuItem searchMenuItem;
    private CustomTabActivityHelper customTabActivityHelper;
    private CustomTabsIntent customTabsIntent;
    private Uri userAccountUri;
    private Uri contributeUri;
    private Uri discoverUri;
    private Uri userContributeUri;
    private int numberOFSavedProducts;
    private String mBarcode;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    // boolean to determine if scan on shake feature should be enabled
    private boolean scanOnShake;
    private SharedPreferences shakePreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.activity_main);


        final PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(OfflineEditPendingProductsWorker.class, 60, TimeUnit.MINUTES, 10, TimeUnit.MINUTES)
            .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .addTag(OFFLINE_EDIT_PENDING_WORK_NAME)
            .build();

        WorkManager.getInstance(getApplicationContext()).enqueueUniquePeriodicWork(OFFLINE_EDIT_PENDING_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest);

        shakePreference = PreferenceManager.getDefaultSharedPreferences(this);

        Utils.hideKeyboard(this);

        final IProfile<ProfileDrawerItem> profile = getUserProfile();
        LocaleHelper.setLocale(this, LocaleHelper.getLanguage(this));

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);

        Bundle extras = getIntent().getExtras();
        FragmentManager fragmentManager = getSupportFragmentManager();
        OfflineSavedProductDao mOfflineSavedProductDao = Utils.getAppDaoSession(MainActivity.this).getOfflineSavedProductDao();
        numberOFSavedProducts = mOfflineSavedProductDao.loadAll().size();

// Get the user preference for scan on shake feature and open ContinuousScanActivity if the user has enabled the feature
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        mShakeDetector = new ShakeDetector();

        setShakePreferences();

        fragmentManager.addOnBackStackChangedListener(() -> {

        });

        boolean isOpenOfflineEdit = extras != null && extras.getBoolean("openOfflineEdit");
        if (isOpenOfflineEdit) {
            fragmentManager.beginTransaction().replace(R.id.fragment_container, new
                OfflineEditFragment()).commit();
            getSupportActionBar().setTitle(getResources().getString(R.string.offline_edit_drawer));
        } else {
            fragmentManager.beginTransaction().replace(R.id.fragment_container, new HomeFragment
                ()).commit();
            bottomNavigationView.setSelectedItemId(R.id.home_page);
            toolbar.setTitle(APP_NAME);
        }

        // chrome custom tab init
        customTabActivityHelper = new CustomTabActivityHelper();
        customTabActivityHelper.setConnectionCallback(this);

        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getBaseContext(),
            customTabActivityHelper.getSession());

        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
            .withActivity(this)
            .withTranslucentStatusBar(true)
            .withHeaderBackground(R.drawable.header)
            .withTextColorRes(R.color.white)
            .addProfiles(profile)
            .withOnAccountHeaderProfileImageListener(new AccountHeader.OnAccountHeaderProfileImageListener() {
                @Override
                public boolean onProfileImageClick(View view, IProfile profile, boolean current) {

                    if (isUserNotLoggedIn()) {
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    }
                    return false;
                }

                @Override
                public boolean onProfileImageLongClick(View view, IProfile profile, boolean current) {
                    return false;
                }
            })
            .withOnAccountHeaderSelectionViewClickListener((view, profile12) -> {
                if (isUserNotLoggedIn()) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
                return false;
            })
            .withSelectionListEnabledForSingleProfile(false)
            .withOnAccountHeaderListener((view, profile1, current) -> {
                if (profile1 instanceof IDrawerItem) {
                    if (profile1.getIdentifier() == ITEM_MANAGE_ACCOUNT) {
                        CustomTabActivityHelper.openCustomTab(MainActivity.this,
                            customTabsIntent, userAccountUri, new WebViewFallback());
                    }
                }

                //false if you have not consumed the event and it should close the drawer
                return false;
            })
            .withSavedInstance(savedInstanceState)
            .build();

        // Add Manage Account profile if the user is connected
        SharedPreferences preferences = getSharedPreferences("login", 0);

        String userSession = preferences.getString("user_session", null);
        final boolean isUserLoggedIn = isUserLoggedIn();
        boolean isUserConnected = isUserLoggedIn && userSession != null;
        boolean isConnected = isUserLoggedIn;

        if (isUserConnected) {
            updateProfileForCurrentUser();
        }
        primaryDrawerItem = createOfflineEditDrawerItem();
        //Create the drawer
        result = new DrawerBuilder()
            .withActivity(this)
            .withToolbar(toolbar)
            .withHasStableIds(true)
            .withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header
            .withOnDrawerListener(new Drawer.OnDrawerListener() {
                @Override
                public void onDrawerOpened(View drawerView) {
                    Utils.hideKeyboard(MainActivity.this);
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                }

                @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        Utils.hideKeyboard(MainActivity.this);
                    }
                })
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.home_drawer).withIcon(GoogleMaterial.Icon.gmd_home).withIdentifier(ITEM_HOME),
                        new SectionDrawerItem().withName(R.string.search_drawer),
                        new PrimaryDrawerItem().withName(R.string.search_by_barcode_drawer).withIcon(GoogleMaterial.Icon.gmd_dialpad).withIdentifier(ITEM_SEARCH_BY_CODE),
                        new PrimaryDrawerItem().withName(R.string.search_by_category).withIcon(GoogleMaterial.Icon.gmd_filter_list).withIdentifier(ITEM_CATEGORIES).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.additives).withIcon(getResources().getDrawable(R.drawable.additives)).withIdentifier(ITEM_ADDITIVES).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.scan_search).withIcon(R.drawable.barcode_grey_24dp).withIdentifier(ITEM_SCAN).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.compare_products).withIcon(GoogleMaterial.Icon.gmd_swap_horiz).withIdentifier(ITEM_COMPARE).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.advanced_search_title).withIcon(GoogleMaterial.Icon.gmd_insert_chart).withIdentifier(ITEM_ADVANCED_SEARCH).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.scan_history_drawer).withIcon(GoogleMaterial.Icon.gmd_history).withIdentifier(ITEM_HISTORY).withSelectable(false),
                        new SectionDrawerItem().withName(R.string.user_drawer).withIdentifier(USER_ID),
                        new PrimaryDrawerItem().withName(getString(R.string.action_contributes)).withIcon(GoogleMaterial.Icon.gmd_rate_review).withIdentifier(ITEM_MY_CONTRIBUTIONS).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.your_lists).withIcon(GoogleMaterial.Icon.gmd_list).withIdentifier(ITEM_YOUR_LISTS).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.products_to_be_completed).withIcon(GoogleMaterial.Icon.gmd_edit).withIdentifier(ITEM_INCOMPLETE_PRODUCTS).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.alert_drawer).withIcon(GoogleMaterial.Icon.gmd_warning).withIdentifier(ITEM_ALERT),
                        new PrimaryDrawerItem().withName(R.string.your_diets).withIcon(GoogleMaterial.Icon.gmd_traffic).withIdentifier(ITEM_DIET),
                        new PrimaryDrawerItem().withName(R.string.action_preferences).withIcon(GoogleMaterial.Icon.gmd_settings).withIdentifier(ITEM_PREFERENCES),
                        new DividerDrawerItem(),
                        primaryDrawerItem,
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName(R.string.action_discover).withIcon(GoogleMaterial.Icon.gmd_info).withIdentifier(ITEM_ABOUT).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.contribute).withIcon(R.drawable.ic_group_grey_24dp).withIdentifier(ITEM_CONTRIBUTE).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.open_other_flavor_drawer).withIcon(GoogleMaterial.Icon.gmd_shop).withIdentifier(ITEM_OBF).withSelectable(false)
                )
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {

                    if (drawerItem == null) {
                        return false;
                    }


                    Fragment fragment = null;
                    switch ((int) drawerItem.getIdentifier()) {
                        case ITEM_HOME:
                            fragment = new HomeFragment();
                            break;
                        case ITEM_SEARCH_BY_CODE:
                            fragment = new FindProductFragment();
                            break;
                        case ITEM_CATEGORIES:
                            startActivity(CategoryActivity.getIntent(this));
                            break;
                        case ITEM_ADDITIVES:
                            startActivity(new Intent(this, AdditivesExplorer.class));
                            break;
                        case ITEM_SCAN:
                            scan();
                            break;
                        case ITEM_COMPARE:
                            startActivity(new Intent(MainActivity.this, ProductComparisonActivity.class));
                            break;
                        case ITEM_HISTORY:
                            startActivity(new Intent(MainActivity.this, HistoryScanActivity.class));
                            break;
                        case ITEM_LOGIN:
                            startActivityForResult(new Intent(MainActivity.this, LoginActivity
                                    .class), LOGIN_REQUEST);
                            break;
                        case ITEM_ALERT:
                            fragment = new AllergensAlertFragment();
                            break;
                        case ITEM_DIET:
                            fragment = new DietsFragment();
                            break;
                        case ITEM_PREFERENCES:
                            fragment = new PreferencesFragment();
                            break;
                        case ITEM_OFFLINE:
                            fragment = new OfflineEditFragment();
                            break;
                        case ITEM_ABOUT:
                            CustomTabActivityHelper.openCustomTab(MainActivity.this,
                                    customTabsIntent, discoverUri, new WebViewFallback());
                            break;
                        case ITEM_CONTRIBUTE:
                            CustomTabActivityHelper.openCustomTab(MainActivity.this,
                                    customTabsIntent, contributeUri, new WebViewFallback());
                            break;

                        case ITEM_INCOMPLETE_PRODUCTS:
                            /**
                             * Search and display the products to be completed by moving to ProductBrowsingListActivity
                             */
                            ProductBrowsingListActivity.startActivity(this, "", SearchType.INCOMPLETE_PRODUCT);
                            break;

                    case ITEM_OBF:
                        boolean otherOFAppInstalled = Utils.isApplicationInstalled
                            (MainActivity.this, BuildConfig.OFOTHERLINKAPP);
                        if (otherOFAppInstalled) {
                            Intent launchIntent = getPackageManager()
                                .getLaunchIntentForPackage(BuildConfig.OFOTHERLINKAPP);
                            if (launchIntent != null) {
                                startActivity(launchIntent);
                            } else {
                                Toast.makeText(this, R.string.app_disabled_text, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", BuildConfig.OFOTHERLINKAPP, null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        } else {
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                                    ("market://details?id=" + BuildConfig.OFOTHERLINKAPP)));
                            } catch (ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" +
                                    BuildConfig.OFOTHERLINKAPP)));
                            }
                        }
                        break;

                    case ITEM_ADVANCED_SEARCH:
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        CustomTabsIntent customTabsIntent = builder.build();
                        CustomTabActivityHelper.openCustomTab(this, customTabsIntent, Uri.parse(getString(R.string.advanced_search_url)), new
                            WebViewFallback());
                        break;

                    case ITEM_MY_CONTRIBUTIONS:
                        myContributions();
                        break;

                    case ITEM_YOUR_LISTS:
                        startActivity(ProductListsActivity.getIntent(this));
                        break;

                    case ITEM_LOGOUT:
                        new MaterialDialog.Builder(MainActivity.this)
                            .title(R.string.confirm_logout)
                            .content(R.string.logout_dialog_content)
                            .positiveText(R.string.txtOk)
                            .negativeText(R.string.dialog_cancel)
                            .onPositive((dialog, which) -> logout())
                            .onNegative((dialog, which) -> Toast.makeText(getApplicationContext(), "Cancelled",
                                Toast.LENGTH_SHORT).show()).show();
                        break;
                    default:
                        // nothing to do
                        break;
                }

                if (fragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
                }

                return false;
            })
            .withSavedInstance(savedInstanceState)
            .withShowDrawerOnFirstLaunch(false)
            .build();

        result.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);

        // Add Drawer items for the connected user
        result.addItemsAtPosition(result.getPosition(ITEM_MY_CONTRIBUTIONS), isConnected ?
            getLogoutDrawerItem() : getLoginDrawerItem());

        if (BuildConfig.FLAVOR.equals("obf")) {
            result.removeItem(ITEM_ALERT);
            result.removeItem(ITEM_ADDITIVES);
            result.updateName(ITEM_OBF, new StringHolder(getString(R.string.open_other_flavor_drawer)));
        }

        if (BuildConfig.FLAVOR.equals("opff")) {
            result.removeItem(ITEM_ALERT);
            result.removeItem(ITEM_DIET);
        }

        if (BuildConfig.FLAVOR.equals("opf")) {
            result.removeItem(ITEM_ALERT);
            result.removeItem(ITEM_DIET);
            result.removeItem(ITEM_ADDITIVES);
            result.removeItem(ITEM_ADVANCED_SEARCH);
        }

        if(!Utils.isApplicationInstalled(MainActivity.this, BuildConfig.OFOTHERLINKAPP)) {
            result.updateName(ITEM_OBF, new StringHolder(getString(R.string.install) + " " + getString(R.string.open_other_flavor_drawer)));
        } else {
            result.updateName(ITEM_OBF, new StringHolder(getString(R.string.open_other_flavor_drawer)));
        }

        // Remove scan item if the device does not have a camera, for example, Chromebooks or
        // Fire devices
        if (!Utils.isHardwareCameraInstalled(this)) {
            result.removeItem(ITEM_SCAN);
        }

//        //if you have many different types of DrawerItems you can magically pre-cache those items
//        // to get a better scroll performance
//        //make sure to init the cache after the DrawerBuilder was created as this will first
//        // clear the cache to make sure no old elements are in
//        new RecyclerViewCacheUtil<IDrawerItem>().withCacheSize(2).apply(result.getRecyclerView(),
//            result.getDrawerItems());

        //only set the active selection or active profile if we do not recreate the activity
        if (savedInstanceState == null) {
            // set the selection to the item with the identifier 1
            result.setSelection(ITEM_HOME, false);

            //set the active profile
            headerResult.setActiveProfile(profile);
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext
            ());
        if (settings.getBoolean("startScan", false)) {
            Intent cameraIntent = new Intent(MainActivity.this, ContinuousScanActivity.class);
            cameraIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(cameraIntent);
        }

        // prefetch uris
        contributeUri = Uri.parse(getString(R.string.website_contribute));
        discoverUri = Uri.parse(getString(R.string.website_discover));
        userContributeUri = Uri.parse(getString(R.string.website_contributor) + getUserLogin());

        customTabActivityHelper.mayLaunchUrl(contributeUri, null, null);
        customTabActivityHelper.mayLaunchUrl(discoverUri, null, null);
        customTabActivityHelper.mayLaunchUrl(userContributeUri, null, null);

        if (CONTRIBUTIONS_SHORTCUT.equals(getIntent().getAction())) {
            myContributions();
        }

        if (SCAN_SHORTCUT.equals(getIntent().getAction())) {
            scan();
        }

        if (BARCODE_SHORTCUT.equals(getIntent().getAction())) {
            moveToBarcodeEntry();
        }

        //Scheduling background image upload job
        Utils.scheduleProductUploadJob(this);



        //Adds nutriscore and quantity values in old history for schema 5 update
        SharedPreferences mSharedPref = getApplicationContext().getSharedPreferences("prefs", 0);
        boolean isOldHistoryDataSynced = mSharedPref.getBoolean("is_old_history_data_synced", false);
        if (!isOldHistoryDataSynced && Utils.isNetworkConnected(this)) {
            OpenFoodAPIClient apiClient = new OpenFoodAPIClient(this);
            apiClient.syncOldHistory();
        }

        BottomNavigationListenerInstaller.selectNavigationItem(bottomNavigationView, 0);
        BottomNavigationListenerInstaller.install(bottomNavigationView, this, this);


        handleIntent(getIntent());
    }

    private void scan() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) !=
            PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest
                .permission.CAMERA)) {
                new MaterialDialog.Builder(MainActivity.this)
                    .title(R.string.action_about)
                    .content(R.string.permission_camera)
                    .neutralText(R.string.txtOk)
                    .show().setOnDismissListener(dialogInterface -> ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    Utils.MY_PERMISSIONS_REQUEST_CAMERA));
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest
                    .permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA);
            }
        } else {
            Intent intent = new Intent(MainActivity.this, ContinuousScanActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    private void updateProfileForCurrentUser() {
        headerResult.updateProfile(getUserProfile());
        final boolean userLoggedIn = isUserLoggedIn();
        if (userLoggedIn) {
            if (headerResult.getProfiles().size() < 2) {
                headerResult.addProfiles(getProfileSettingDrawerItem());
            }
        } else {
            headerResult.removeProfileByIdentifier(ITEM_MANAGE_ACCOUNT);
        }
    }

    private void myContributions() {

        if (isUserLoggedIn()) {
            showMyContributions();
        } else {
            new MaterialDialog.Builder(MainActivity.this)
                .title(R.string.contribute)
                .content(R.string.contribution_without_account)
                .positiveText(R.string.create_account_button)
                .neutralText(R.string.login_button)
                .onPositive((dialog, which) -> CustomTabActivityHelper.openCustomTab(MainActivity.this, customTabsIntent, Uri.parse(getString(R
                    .string.website) + "cgi/user.pl"), new WebViewFallback()))
                .onNeutral((dialog, which) -> startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), SHOW_CONTRIBUTION_AFTER_LOGIN))
                .show();
        }
    }

    private void showMyContributions() {
        String userLogin = getUserLogin();
        userContributeUri = Uri.parse(getString(R.string.website_contributor) + userLogin);
        ProductBrowsingListActivity.startActivity(this, userLogin, SearchType.CONTRIBUTOR);
    }

    private IProfile<ProfileSettingDrawerItem> getProfileSettingDrawerItem() {

        String userLogin = getUserLogin();
        String userSession = getUserSession();
        userAccountUri = Uri.parse(getString(R.string.website) + "cgi/user.pl?type=edit&userid=" + userLogin + "&user_id=" + userLogin +
            "&user_session=" + userSession);
        customTabActivityHelper.mayLaunchUrl(userAccountUri, null, null);
        return new ProfileSettingDrawerItem()
            .withName(getString(R.string.action_manage_account))
            .withIcon(GoogleMaterial.Icon.gmd_settings)
            .withIdentifier(ITEM_MANAGE_ACCOUNT)
            .withSelectable(false);
    }

    /**
     * Replace logout menu item by the login menu item
     * Change current user profile (Anonymous)
     * Remove all Account Header items
     * Remove user login info
     */
    private void logout() {
        getSharedPreferences("login", MODE_PRIVATE).edit().clear().apply();
        updateConnectedState();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // do nothing
        if (requestCode == LOGIN_REQUEST && resultCode == RESULT_OK) {
            updateConnectedState();
        }
        if (requestCode == SHOW_CONTRIBUTION_AFTER_LOGIN && resultCode == RESULT_OK && isUserLoggedIn()) {
            showMyContributions();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = result.saveInstanceState(outState);
        //add the values which need to be saved from the accountHeader to the bundle
        outState = headerResult.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the
        // activity
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack(getSupportFragmentManager().getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                //recreate the activity onBackPressed
                recreate();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onCreate(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        if (searchManager.getSearchableInfo(getComponentName()) != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }

        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                return true;
            }
        });

        if (getIntent().getBooleanExtra("product_search", false)) {
            searchMenuItem.expandActionView();
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Utils.MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager
                    .PERMISSION_GRANTED) {
                    Intent intent = new Intent(MainActivity.this, ContinuousScanActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
            break;
        }
    }

    private IDrawerItem<PrimaryDrawerItem, com.mikepenz.materialdrawer.model
        .AbstractBadgeableDrawerItem.ViewHolder> getLogoutDrawerItem() {
        return new PrimaryDrawerItem()
            .withName(getString(R.string.logout_drawer))
            .withIcon(GoogleMaterial.Icon.gmd_settings_power)
            .withIdentifier(ITEM_LOGOUT)
            .withSelectable(false);
    }

    private IDrawerItem<PrimaryDrawerItem, com.mikepenz.materialdrawer.model
        .AbstractBadgeableDrawerItem.ViewHolder> getLoginDrawerItem() {
        return new PrimaryDrawerItem()
            .withName(R.string.sign_in_drawer)
            .withIcon(GoogleMaterial.Icon.gmd_account_circle)
            .withIdentifier(ITEM_LOGIN)
            .withSelectable(false);
    }

    private IProfile<ProfileDrawerItem> getUserProfile() {
        String userLogin = getSharedPreferences("login", 0)
            .getString("user", getResources().getString(R.string.txt_anonymous));

        return new ProfileDrawerItem()
            .withName(userLogin)
            .withIcon(R.drawable.img_home)
            .withIdentifier(ITEM_USER);
    }

    @Override
    public void onCustomTabsConnected() {

    }

    @Override
    public void onCustomTabsDisconnected() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        customTabActivityHelper.bindCustomTabsService(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        customTabActivityHelper.unbindCustomTabsService(this);
    }

    @Override
    protected void onDestroy() {
        customTabActivityHelper.setConnectionCallback(null);
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String type = intent.getType();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            Log.e("INTENT", "start activity");
            String query = intent.getStringExtra(SearchManager.QUERY);
            //Saves the most recent queries and adds it to the list of suggestions
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            ProductBrowsingListActivity.startActivity(this, query, SearchType.SEARCH);
            if (searchMenuItem != null) {
                searchMenuItem.collapseActionView();
            }
        } else if (Intent.ACTION_SEND.equals(intent.getAction()) && type != null) {
            if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction()) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent); // Handle multiple images being sent
            }
        }
    }

    /**
     * This moves the main activity to the barcode entry fragment.
     */
    public void moveToBarcodeEntry() {
        Fragment fragment = new FindProductFragment();
        changeFragment(fragment, getResources().getString(R.string.search_by_barcode_drawer), ITEM_SEARCH_BY_CODE);
    }

    /**
     * This moves the main activity to the preferences fragment.
     */
    public void moveToPreferences() {
        Fragment fragment = new PreferencesFragment();
        changeFragment(fragment, getString(R.string.preferences), ITEM_PREFERENCES);
    }

    /**
     * Create the drawer item. This adds a badge if there are items in the offline edit, otherwise
     * there is no badge present.
     *
     * @return drawer item.
     */
    private PrimaryDrawerItem createOfflineEditDrawerItem() {
        if (numberOFSavedProducts > 0) {
            return new PrimaryDrawerItem().withName(R.string.offline_edit_drawer).withIcon(GoogleMaterial.Icon.gmd_local_airport).withIdentifier(10)
                .withBadge(String.valueOf(numberOFSavedProducts)).withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R
                    .color.md_red_700));
        } else {
            return new PrimaryDrawerItem().withName(R.string.offline_edit_drawer).withIcon(GoogleMaterial.Icon.gmd_local_airport).withIdentifier(ITEM_OFFLINE);
        }
    }

    /**
     * Updates the drawer item. This updates the badge if there are items left in offline edit, otherwise
     * there is no badge present.
     * This function is called from OfflineEditFragment only.
     */
    public void updateBadgeOfflineEditDrawerITem(int size) {
        int positionOfOfflineBadeItem = result.getPosition(primaryDrawerItem);
        if (size > 0) {
            primaryDrawerItem = new PrimaryDrawerItem().withName(R.string.offline_edit_drawer).withIcon(GoogleMaterial.Icon.gmd_local_airport).withIdentifier(ITEM_OFFLINE)
                .withBadge(String.valueOf(size)).withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.md_red_700));
        } else {
            primaryDrawerItem = new PrimaryDrawerItem().withName(R.string.offline_edit_drawer).withIcon(GoogleMaterial.Icon.gmd_local_airport).withIdentifier(ITEM_OFFLINE);
        }
        result.updateItemAtPosition(primaryDrawerItem, positionOfOfflineBadeItem);
    }

    @Override
    public void setItemSelected(@NavigationDrawerType Integer type) {
        result.setSelection(type, false);
    }

    @Override
    public void onPause() {
        super.onPause();

        shakePreference.unregisterOnSharedPreferenceChangeListener(this);

        if (scanOnShake) {

            // unregister the listener
            mSensorManager.unregisterListener(mShakeDetector, mAccelerometer);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // change drawer menu item from "install" to "open" when navigating back from play store.
        if(Utils.isApplicationInstalled(MainActivity.this, BuildConfig.OFOTHERLINKAPP)) {
            result.updateName(ITEM_OBF, new StringHolder(getString(R.string.open_other_flavor_drawer)));

            result.getAdapter().notifyDataSetChanged();
        }

        updateConnectedState();
        shakePreference.registerOnSharedPreferenceChangeListener(this);
        if (scanOnShake) {

            //register the listener
            mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    private void updateConnectedState() {
        updateProfileForCurrentUser();
        result.removeItem(ITEM_LOGIN);
        result.removeItem(ITEM_LOGOUT);
        result.addItemAtPosition(super.isUserLoggedIn() ? getLogoutDrawerItem() : getLoginDrawerItem(), result.getPosition(ITEM_MY_CONTRIBUTIONS));
    }

    private void handleSendImage(Intent intent) {
        Uri selectedImage = null;
        ArrayList<Uri> selectedImagesArray = new ArrayList<>();
        selectedImage = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (selectedImage != null) {
            selectedImagesArray.add(selectedImage);
            chooseDialog(selectedImagesArray);
        }
    }

    private void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> selectedImagesArray = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (selectedImagesArray != null) {
            selectedImagesArray.removeAll(Collections.singleton(null));
            chooseDialog(selectedImagesArray);
        }
    }

    private void chooseDialog(ArrayList<Uri> selectedImagesArray) {
        boolean isBarCodePresent = detectBarCodeInImage(selectedImagesArray);
        if (isBarCodePresent) {
            createAlertDialog(false, mBarcode, selectedImagesArray);
        } else {
            createAlertDialog(true, "", selectedImagesArray);
        }
    }

    private boolean detectBarCodeInImage(ArrayList<Uri> selectedImages) {
        for (Uri uri : selectedImages) {
            Bitmap bMap = null;
            try (InputStream imageStream = getContentResolver().openInputStream(uri)) {
                bMap = BitmapFactory.decodeStream(imageStream);
            } catch (FileNotFoundException e) {
                Log.e(MainActivity.class.getSimpleName(), "Could not resolve file from Uri " + uri.toString(),e);
            } catch (IOException e) {
                Log.e(MainActivity.class.getSimpleName(), "IO error during bitmap stream decoding: " + e.getMessage(),e);
            }
            //decoding bitmap
            if (bMap != null) {
                int[] intArray = new int[bMap.getWidth() * bMap.getHeight()];
                bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());
                LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                Reader reader = new MultiFormatReader();
                try {
                    HashMap<DecodeHintType, Object> decodeHints = new HashMap<>();
                    decodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
                    decodeHints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
                    Result decodedResult = reader.decode(bitmap, decodeHints);
                    if (decodedResult != null) {
                        mBarcode = decodedResult.getText();
                    }
                    if (mBarcode != null) {
                        return true;
                    }
                } catch (FormatException e) {
                    Toast.makeText(getApplicationContext(), getString(R.string.format_error), Toast.LENGTH_SHORT).show();
                    Log.e(MainActivity.class.getSimpleName(), "Error decoding bitmap into barcode: " + e.getMessage());
                } catch (Exception e) {
                    Log.e(MainActivity.class.getSimpleName(), "Error decoding bitmap into barcode: " + e.getMessage());
                }
            }
        }
        return false;
    }

    private void createAlertDialog(boolean hasEditText, String barcode, ArrayList<Uri> uri) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_barcode, null);
        alertDialogBuilder.setView(dialogView);

        final EditText barcodeEditText = dialogView.findViewById(R.id.barcode);
        final RecyclerView productImages = dialogView.findViewById(R.id.product_image);
        LinearLayoutManager layoutManager
            = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        productImages.setLayoutManager(layoutManager);
        productImages.setAdapter(new PhotosAdapter(uri));

        if (hasEditText) {
            barcodeEditText.setVisibility(View.VISIBLE);
            alertDialogBuilder.setTitle(getString(R.string.no_barcode));
            alertDialogBuilder.setMessage(getString(R.string.enter_barcode));
        } else {
            alertDialogBuilder.setTitle(getString(R.string.code_detected));
            alertDialogBuilder.setMessage(barcode + "\n" + getString(R.string.do_you_want_to));
        }

        // set dialog message
        alertDialogBuilder
            .setCancelable(false)
            .setPositiveButton(R.string.txtYes, (dialog, id) -> {
                for (Uri selected : uri) {
                    OpenFoodAPIClient api = new OpenFoodAPIClient(MainActivity.this);
                    ProductImage image = null;
                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    String tempBarcode;
                    if (hasEditText) {

                        tempBarcode = barcodeEditText.getText().toString();
                    } else {
                        tempBarcode = barcode;
                    }

                    if (tempBarcode.length() > 0) {
                        dialog.cancel();
                        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                            File imageFile = new File(RealPathUtil.getRealPath(MainActivity.this, selected));
                            image = new ProductImage(tempBarcode, OTHER, imageFile);
                            api.postImg(MainActivity.this, image, null);
                        } else {
                            Intent intent = new Intent(MainActivity.this, AddProductActivity.class);
                            State st = new State();
                            Product pd = new Product();
                            pd.setCode(tempBarcode);
                            st.setProduct(pd);
                            intent.putExtra("state", st);
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.sorry_msg), Toast.LENGTH_LONG).show();
                    }
                }
            })

            .setNegativeButton(R.string.txtNo,
                (dialog, id) -> dialog.cancel());

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // properly handle scan on shake preferences changes
        setShakePreferences();
    }

    private void setShakePreferences() {
        scanOnShake = shakePreference.getBoolean("shakeScanMode", false);
        Log.i("Shake", String.valueOf(scanOnShake));
        mShakeDetector.setOnShakeListener(count -> {
            if (scanOnShake) {
                Utils.scan(MainActivity.this);
            }
        });
    }

    /**
     * Used to navigate Fragments which are children of <code>MainActivity</code>.
     * Use this method when the <code>Fragment</code> APPEARS in the <code>Drawer</code>.
     *
     * @param fragment The fragment class to display.
     * @param title The title that should be displayed on the top toolbar.
     * @param drawerName The fragment as it appears in the drawer. See {@link NavigationDrawerListener} for the value.
     * @author ross-holloway94
     * @see <a href="https://stackoverflow.com/questions/45138446/calling-fragment-from-recyclerview-adapter">Related Stack Overflow article</a>
     * @since 06/16/18
     */
    public void changeFragment(Fragment fragment, String title, long drawerName) {
        changeFragment(fragment, title);
        result.setSelection(drawerName);
    }

    /**
     * Used to navigate Fragments which are children of <code>MainActivity</code>.
     * Use this method when the <code>Fragment</code> DOES NOT APPEAR in the <code>Drawer</code>.
     *
     * @param fragment The fragment class to display.
     * @param title The title that should be displayed on the top toolbar.
     * @author ross-holloway94
     * @see <a href="https://stackoverflow.com/questions/45138446/calling-fragment-from-recyclerview-adapter">Related Stack Overflow article</a>
     * @since 06/16/18
     */
    public void changeFragment(Fragment fragment, String title) {

        String backStateName = fragment.getClass().getName();
        FragmentManager manager = getSupportFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

        if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) {
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.fragment_container, fragment, backStateName);
            ft.addToBackStack(backStateName);
            ft.commit();
        }

        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
    }
}
