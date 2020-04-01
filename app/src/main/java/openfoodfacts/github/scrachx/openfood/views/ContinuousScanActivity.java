package openfoodfacts.github.scrachx.openfood.views;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.journeyapps.barcodescanner.camera.CameraSettings;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.AllergenHelper;
import openfoodfacts.github.scrachx.openfood.models.AllergenName;
import openfoodfacts.github.scrachx.openfood.models.AnalysisTagConfig;
import openfoodfacts.github.scrachx.openfood.models.HistoryProductDao;
import openfoodfacts.github.scrachx.openfood.models.InvalidBarcode;
import openfoodfacts.github.scrachx.openfood.models.InvalidBarcodeDao;
import openfoodfacts.github.scrachx.openfood.models.OfflineSavedProduct;
import openfoodfacts.github.scrachx.openfood.models.OfflineSavedProductDao;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.ProductUtils;
import openfoodfacts.github.scrachx.openfood.utils.SwipeDetector;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.listeners.BottomNavigationListenerInstaller;
import openfoodfacts.github.scrachx.openfood.views.product.ProductFragment;
import openfoodfacts.github.scrachx.openfood.views.product.ingredients_analysis.IngredientsWithTagDialogFragment;
import openfoodfacts.github.scrachx.openfood.views.product.summary.IngredientAnalysisTagsAdapter;
import openfoodfacts.github.scrachx.openfood.views.product.summary.SummaryProductPresenter;
import openfoodfacts.github.scrachx.openfood.views.product.summary.SummaryProductPresenterView;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class ContinuousScanActivity extends androidx.appcompat.app.AppCompatActivity {
    private static final int ADD_PRODUCT_ACTIVITY_REQUEST_CODE = 1;
    private static final int LOGIN_ACTIVITY_REQUEST_CODE = 2;
    private HistoryProductDao mHistoryProductDao;
    private InvalidBarcodeDao mInvalidBarcodeDao;
    @BindView(R.id.quick_view)
    LinearLayout quickView;
    @BindView(R.id.barcode_scanner)
    DecoratedBarcodeView barcodeView;
    @BindView(R.id.imageForScreenshotGenerationOnly)
    ImageView imageForScreenshotGenerationOnly;
    @BindView(R.id.toggle_flash)
    ImageView toggleFlashView;
    @BindView(R.id.button_more)
    ImageView moreOptions;
    @BindView(R.id.frame_layout)
    FrameLayout frameLayout;
    @BindView(R.id.txt_product_call_to_action)
    TextView txtProductCallToAction;
    @BindView(R.id.call_to_action_imageProgress)
    View callToActionProgress;
    @BindView(R.id.quickView_slideUpIndicator)
    View slideUpIndicator;
    @BindView(R.id.quickView_progress)
    ProgressBar progressBar;
    @BindView(R.id.quickView_progressText)
    TextView progressText;
    @BindView(R.id.quickView_productNotFound)
    TextView productNotFound;
    @BindView(R.id.quickView_productNotFoundButton)
    Button productNotFoundButton;
    @BindView(R.id.quickView_image)
    ImageView productImage;
    @BindView(R.id.quickView_name)
    TextView name;
    @BindView(R.id.quickView_additives)
    TextView additives;
    @BindView(R.id.quickView_dietState)
    ImageView dietState;
    @BindView(R.id.quickView_nutriScore)
    ImageView nutriScore;
    @BindView(R.id.quickView_novaGroup)
    ImageView novaGroup;
    @BindView(R.id.quickView_co2_icon)
    ImageView co2Icon;
    @BindView(R.id.quickView_imageProgress)
    ProgressBar imageProgress;
    @BindView(R.id.quickView_searchByBarcode)
    EditText searchByBarcode;
    @BindView(R.id.quickView_tags)
    RecyclerView productTags;
    @BindView(R.id.quickView_details)
    ConstraintLayout details;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;
    private SummaryProductPresenter summaryProductPresenter;
    private OpenFoodAPIClient client;
    private OfflineSavedProductDao mOfflineSavedProductDao;
    private Product product;
    private ProductFragment productFragment;
    private SharedPreferences.Editor editor;
    private BeepManager beepManager;
    private String lastText;
    private SharedPreferences sp;
    private boolean mFlash;
    private boolean mRing;
    private boolean mAutofocus;
    private int cameraState;
    private Disposable disposable;
    private PopupMenu popup;
    private Handler handler;
    private Runnable runnable;
    private BottomSheetBehavior bottomSheetBehavior;
    private int peekLarge;
    private int peekSmall;
    private boolean isAnalysisTagsEmpty = true;
    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            handler.removeCallbacks(runnable);
            if (result.getText() == null || result.getText().isEmpty() || result.getText().equals(lastText)) {
                // Prevent duplicate scans
                return;
            }
            InvalidBarcode invalidBarcode = mInvalidBarcodeDao.queryBuilder()
                .where(InvalidBarcodeDao.Properties.Barcode.eq(result.getText())).unique();
            if (invalidBarcode != null) {
                // scanned barcode is in the list of invalid barcodes, do nothing
                return;
            }

            if (mRing) {
                beepManager.playBeepSound();
            }

            lastText = result.getText();
            if (!(isFinishing())) {
                findProduct(lastText, false);
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
            // Here possible results are useless but we must implement this
        }
    };
    private boolean productShowing = false;

    /**
     * Used by screenshot tests.
     *
     * @param barcode barcode to serach
     */
    @SuppressWarnings("unused")
    public void showProduct(String barcode) {
        productShowing = true;
        barcodeView.setVisibility(GONE);
        barcodeView.pause();
        imageForScreenshotGenerationOnly.setVisibility(VISIBLE);
        findProduct(barcode, false);
    }

    /**
     * Makes network call and search for the product in the database
     *
     * @param lastBarcode Barcode to be searched
     * @param newlyAdded true if the product is added using the product addition just now
     */
    private void findProduct(String lastBarcode, boolean newlyAdded) {
        if (isFinishing()) {
            return;
        }
        if (disposable != null && !disposable.isDisposed()) {
            //dispove the previous call if not ended.
            disposable.dispose();
        }
        if (summaryProductPresenter != null) {
            summaryProductPresenter.dispose();
        }
        client.getProductFullSingle(lastBarcode, Utils.HEADER_USER_AGENT_SCAN)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(a -> {
                hideAllViews();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                quickView.setOnClickListener(null);
                progressBar.setVisibility(VISIBLE);
                progressText.setVisibility(VISIBLE);
                progressText.setText(getString(R.string.loading_product, lastBarcode));
            })
            .subscribe(new SingleObserver<State>() {
                @Override
                public void onSubscribe(Disposable d) {
                    disposable = d;
                }

                @Override
                public void onSuccess(State state) {
                    //clear product tags
                    isAnalysisTagsEmpty = true;
                    productTags.setAdapter(null);

                    progressBar.setVisibility(GONE);
                    progressText.setVisibility(GONE);
                    if (state.getStatus() == 0) {
                        productNotFound(getString(R.string.product_not_found, lastBarcode));
                    } else {
                        product = state.getProduct();
                        if (getIntent().getBooleanExtra("compare_product", false)) {
                            Intent intent = new Intent(ContinuousScanActivity.this, ProductComparisonActivity.class);
                            intent.putExtra("product_found", true);
                            ArrayList<Product> productsToCompare = (ArrayList<Product>) getIntent().getExtras().get("products_to_compare");
                            if (productsToCompare.contains(product)) {
                                intent.putExtra("product_already_exists", true);
                            } else {
                                productsToCompare.add(product);
                            }
                            intent.putExtra("products_to_compare", productsToCompare);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                        new HistoryTask(mHistoryProductDao).execute(product);
                        showAllViews();
                        txtProductCallToAction.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        txtProductCallToAction.setBackground(ContextCompat.getDrawable(ContinuousScanActivity.this, R.drawable.rounded_quick_view_text));
                        txtProductCallToAction.setText(isProductIncomplete() ? R.string.product_not_complete : R.string.scan_tooltip);
                        txtProductCallToAction.setVisibility(VISIBLE);

                        manageSummary(product);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        productShownInBottomView();
                        productNotFound.setVisibility(GONE);
                        productNotFoundButton.setVisibility(GONE);

                        if (product.getProductName() == null || product.getProductName().equals("")) {
                            name.setText(R.string.productNameNull);
                        } else {
                            name.setText(product.getProductName());
                        }
                        List<String> addTags = product.getAdditivesTags();
                        if (!addTags.isEmpty()) {
                            additives.setText(getResources().getQuantityString(R.plurals.productAdditives, addTags.size(), addTags.size()));
                        } else if (product.getStatesTags().contains("en:ingredients-completed")) {
                            additives.setText(getString(R.string.productAdditivesNone));
                        } else {
                            additives.setText(getString(R.string.productAdditivesUnknown));
                        }

                        final String imageUrl = product.getImageUrl(LocaleHelper.getLanguage(getBaseContext()));
                        if (imageUrl != null) {
                            try {
                                Picasso.get()
                                    .load(imageUrl)
                                    .error(R.drawable.placeholder_thumb)
                                    .into(productImage, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            imageProgress.setVisibility(GONE);
                                        }

                                        @Override
                                        public void onError(Exception ex) {
                                            imageProgress.setVisibility(GONE);
                                        }
                                    });
                            } catch (IllegalStateException e) {
                                //could happen if Picasso is not instanciate correctly...
                                Log.w(this.getClass().getSimpleName(), e.getMessage(), e);
                            }
                        } else {
                            productImage.setImageResource(R.drawable.placeholder_thumb);
                            imageProgress.setVisibility(GONE);
                        }
                        //Hide dietState from quickView if app flavour is not OFF
                        if (! BuildConfig.FLAVOR.equals("off")) {
                            dietState.setVisibility(GONE);
                        }
                        // Hide nutriScore from quickView if app flavour is not OFF or there is no nutriscore
                        if (BuildConfig.FLAVOR.equals("off") && product.getNutritionGradeFr() != null) {
                            if (Utils.getImageGrade(product.getNutritionGradeFr()) != Utils.NO_DRAWABLE_RESOURCE) {
                                nutriScore.setVisibility(VISIBLE);
                                nutriScore.setImageResource(Utils.getImageGrade(product.getNutritionGradeFr()));
                            } else {
                                nutriScore.setVisibility(INVISIBLE);
                            }
                        } else {
                            nutriScore.setVisibility(GONE);
                        }
                        // Hide nova group from quickView if app flavour is not OFF or there is no nova group
                        if (BuildConfig.FLAVOR.equals("off") && product.getNovaGroups() != null) {
                            final int novaGroupDrawable = Utils.getNovaGroupDrawable(product);
                            if (novaGroupDrawable != Utils.NO_DRAWABLE_RESOURCE) {
                                novaGroup.setVisibility(VISIBLE);
                                additives.setVisibility(VISIBLE);
                                novaGroup.setImageResource(novaGroupDrawable);
                            } else {
                                novaGroup.setVisibility(GONE);
                            }
                        } else {
                            novaGroup.setVisibility(GONE);
                        }
                        int environmentImpactResource = Utils.getImageEnvironmentImpact(product);
                        if (environmentImpactResource != Utils.NO_DRAWABLE_RESOURCE) {
                            co2Icon.setVisibility(VISIBLE);
                            co2Icon.setImageResource(environmentImpactResource);
                        } else {
                            co2Icon.setVisibility(GONE);
                        }
                        FragmentManager fm = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fm.beginTransaction();
                        ProductFragment newProductFragment = new ProductFragment();

                        Bundle bundle = new Bundle();
                        bundle.putSerializable("state", state);

                        newProductFragment.setArguments(bundle);
                        fragmentTransaction.replace(R.id.frame_layout, newProductFragment);
                        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        fragmentTransaction.commitAllowingStateLoss();
                        productFragment = newProductFragment;
                    }
                }

                @Override
                public void onError(Throwable e) {
                    try {
                        // A network error happened
                        if (e instanceof IOException) {
                            hideAllViews();
                            OfflineSavedProduct offlineSavedProduct = mOfflineSavedProductDao.queryBuilder().where(OfflineSavedProductDao.Properties.Barcode.eq(lastBarcode))
                                .unique();
                            if (offlineSavedProduct != null) {
                                showOfflineSavedDetails(offlineSavedProduct);
                            } else {
                                productNotFound(getString(R.string.addProductOffline, lastBarcode));
                            }
                            quickView.setOnClickListener(v -> navigateToProductAddition(lastBarcode));
                        } else {
                            progressBar.setVisibility(GONE);
                            progressText.setVisibility(GONE);
                            final Toast errorMessage = Toast.makeText(ContinuousScanActivity.this.getBaseContext(), R.string.txtConnectionError, Toast.LENGTH_LONG);
                            errorMessage.setGravity(Gravity.CENTER, 0, 0);
                            errorMessage.show();
                            Log.i(this.getClass().getSimpleName(), e.getMessage(), e);
                        }
                    } catch (Exception e1) {
                        Log.i(this.getClass().getSimpleName(), e1.getMessage(), e1);
                    }
                }
            });
    }

    private void manageSummary(Product product) {
        callToActionProgress.setVisibility(VISIBLE);
        summaryProductPresenter = new SummaryProductPresenter(product, new SummaryProductPresenterView() {
            @Override
            public void showAllergens(List<AllergenName> allergens) {
                final AllergenHelper.Data data = AllergenHelper.computeUserAllergen(product, allergens);
                callToActionProgress.setVisibility(GONE);
                if (data.isEmpty()) {
                    return;
                }
                final IconicsDrawable iconicsDrawable = new IconicsDrawable(ContinuousScanActivity.this)
                    .icon(GoogleMaterial.Icon.gmd_warning)
                    .color(ContextCompat.getColor(ContinuousScanActivity.this, R.color.white))
                    .sizeDp(24);
                txtProductCallToAction.setCompoundDrawablesWithIntrinsicBounds(iconicsDrawable, null, null, null);
                txtProductCallToAction.setBackground(ContextCompat.getDrawable(ContinuousScanActivity.this, R.drawable.rounded_quick_view_text_warn));
                if (data.isIncomplete()) {
                    txtProductCallToAction.setText(R.string.product_incomplete_message);
                } else {
                    String text = String.format("%s\n", getResources().getString(R.string.product_allergen_prompt)) +
                        StringUtils.join(data.getAllergens(), ", ");
                    txtProductCallToAction.setText(text);
                }
            }

            @Override
            public void showAnalysisTags(List<AnalysisTagConfig> analysisTags) {
                super.showAnalysisTags(analysisTags);

                if (analysisTags.size() == 0) {
                    productTags.setVisibility(GONE);
                    isAnalysisTagsEmpty = true;
                    return;
                }

                productTags.setVisibility(VISIBLE);
                isAnalysisTagsEmpty = false;

                IngredientAnalysisTagsAdapter adapter = new IngredientAnalysisTagsAdapter(ContinuousScanActivity.this, analysisTags);
                adapter.setOnItemClickListener((view, position) -> {
                    IngredientsWithTagDialogFragment fragment = IngredientsWithTagDialogFragment
                        .newInstance(product, (AnalysisTagConfig) view.getTag(R.id.analysis_tag_config));
                    fragment.show(getSupportFragmentManager(), "fragment_ingredients_with_tag");

                    fragment.setOnDismissListener(dialog -> adapter.filterVisibleTags());
                });
                productTags.setAdapter(adapter);
            }
        });
        summaryProductPresenter.loadAllergens(() -> callToActionProgress.setVisibility(GONE));
        summaryProductPresenter.loadAnalysisTags();
    }

    private void productNotFound(String text) {
        hideAllViews();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        quickView.setOnClickListener(v -> navigateToProductAddition(lastText));
        productNotFound.setText(text);
        productNotFound.setVisibility(VISIBLE);
        productNotFoundButton.setVisibility(VISIBLE);
        productNotFoundButton.setOnClickListener(v -> navigateToProductAddition(lastText));
    }

    private void productShownInBottomView() {
        bottomSheetBehavior.setPeekHeight(peekLarge);
        quickView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        quickView.requestLayout();
        quickView.getRootView().requestLayout();
    }

    private void showOfflineSavedDetails(OfflineSavedProduct offlineSavedProduct) {
        showAllViews();
        HashMap<String, String> productDetails = offlineSavedProduct.getProductDetailsMap();
        String lc = productDetails.get("lang") != null ? productDetails.get("lang") : "en";
        if (productDetails.get("product_name_" + lc) != null) {
            name.setText(productDetails.get("product_name_" + lc));
        } else if (productDetails.get("product_name_en") != null) {
            name.setText(productDetails.get("product_name_en"));
        } else {
            name.setText(R.string.productNameNull);
        }
        if (productDetails.get("image_front") != null) {
            Picasso.get()
                .load("file://" + productDetails.get("image_front"))
                .error(R.drawable.placeholder_thumb)
                .into(productImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        imageProgress.setVisibility(GONE);
                    }

                    @Override
                    public void onError(Exception ex) {
                        imageProgress.setVisibility(GONE);
                    }
                });
        } else {
            productImage.setImageResource(R.drawable.placeholder_thumb);
            imageProgress.setVisibility(GONE);
        }
    }

    private void navigateToProductAddition(String lastText) {
        Intent intent = new Intent(ContinuousScanActivity.this, AddProductActivity.class);
        State st = new State();
        Product pd = new Product();
        pd.setCode(lastText);
        st.setProduct(pd);
        intent.putExtra("state", st);
        startActivityForResult(intent, ADD_PRODUCT_ACTIVITY_REQUEST_CODE);
    }

    private void showAllViews() {
        slideUpIndicator.setVisibility(VISIBLE);
        productImage.setVisibility(VISIBLE);
        name.setVisibility(VISIBLE);
        frameLayout.setVisibility(VISIBLE);
        additives.setVisibility(VISIBLE);
        imageProgress.setVisibility(VISIBLE);
        if (!isAnalysisTagsEmpty) {
            productTags.setVisibility(VISIBLE);
        } else {
            productTags.setVisibility(GONE);
        }
    }

    private void hideAllViews() {
        searchByBarcode.setVisibility(GONE);
        progressBar.setVisibility(GONE);
        progressText.setVisibility(GONE);
        slideUpIndicator.setVisibility(GONE);
        productImage.setVisibility(GONE);
        name.setVisibility(GONE);
        frameLayout.setVisibility(GONE);
        additives.setVisibility(GONE);
        dietState.setVisibility(GONE);
        nutriScore.setVisibility(GONE);
        novaGroup.setVisibility(GONE);
        co2Icon.setVisibility(GONE);
        productNotFound.setVisibility(GONE);
        productNotFoundButton.setVisibility(GONE);
        imageProgress.setVisibility(GONE);
        txtProductCallToAction.setVisibility(GONE);
        productTags.setVisibility(GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        if (summaryProductPresenter != null) {
            summaryProductPresenter.dispose();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            barcodeView.resume();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //status bar will remain visible if user presses home and then reopens the activity
        // hence hiding status bar again
        hideSystemUI();
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        OFFApplication.getAppComponent().inject(this);
        client = new OpenFoodAPIClient(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_continuous_scan);
        ButterKnife.bind(this);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        peekLarge = getResources().getDimensionPixelSize(R.dimen.scan_summary_peek_large);
        peekSmall = getResources().getDimensionPixelSize(R.dimen.scan_summary_peek_small);

        productTags.setNestedScrollingEnabled(false);

        Intent intent = new Intent(this, MainActivity.class);

        new SwipeDetector(barcodeView).setOnSwipeListener((v, swipeType) -> {
            if (swipeType == SwipeDetector.SwipeTypeEnum.TOP_TO_BOTTOM) {
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
            }
        });

        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener
            (visibility -> {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    // The system bars are visible.
                    hideSystemUI();
                }
            });

        handler = new Handler();
        runnable = () -> {
            if (productShowing) {
                return;
            }
            hideAllViews();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            searchByBarcode.setVisibility(VISIBLE);
            searchByBarcode.requestFocus();
        };
        handler.postDelayed(runnable, 15000);

        bottomSheetBehavior = BottomSheetBehavior.from(quickView);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    lastText = null;
                    txtProductCallToAction.setVisibility(GONE);
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    barcodeView.resume();
                }
                if (searchByBarcode.getVisibility() == VISIBLE) {
                    bottomSheetBehavior.setPeekHeight(peekSmall);
                    bottomSheet.getLayoutParams().height = bottomSheetBehavior.getPeekHeight();
                    bottomSheet.requestLayout();
                } else {
                    bottomSheetBehavior.setPeekHeight(peekLarge);
                    bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                    bottomSheet.requestLayout();
                }
            }

            float previousSlideOffset = 0;

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                float slideDelta = slideOffset - previousSlideOffset;
                if (searchByBarcode.getVisibility() != VISIBLE && progressBar.getVisibility() != VISIBLE) {
                    if (slideOffset > 0.01f || slideOffset < -0.01f) {
                        txtProductCallToAction.setVisibility(GONE);
                    } else {
                        if (productNotFound.getVisibility() != VISIBLE) {
                            txtProductCallToAction.setVisibility(VISIBLE);
                        }
                    }
                    if (slideOffset > 0.01f) {
                        details.setVisibility(GONE);
                        productTags.setVisibility(GONE);
                        barcodeView.pause();
                        if (slideDelta > 0 && productFragment != null) {
                            productFragment.bottomSheetWillGrow();
                            bottomNavigationView.setVisibility(GONE);
                        }
                    } else {
                        barcodeView.resume();
                        details.setVisibility(VISIBLE);
                        if (!isAnalysisTagsEmpty) {
                            productTags.setVisibility(VISIBLE);
                        } else {
                            productTags.setVisibility(GONE);
                        }
                        bottomNavigationView.setVisibility(VISIBLE);
                        if (productNotFound.getVisibility() != VISIBLE) {
                            txtProductCallToAction.setVisibility(VISIBLE);
                        }
                    }
                }
                previousSlideOffset = slideOffset;
            }
        });

        mHistoryProductDao = Utils.getAppDaoSession(ContinuousScanActivity.this).getHistoryProductDao();
        mInvalidBarcodeDao = Utils.getAppDaoSession(ContinuousScanActivity.this).getInvalidBarcodeDao();
        mOfflineSavedProductDao = Utils.getAppDaoSession(ContinuousScanActivity.this).getOfflineSavedProductDao();

        sp = getSharedPreferences("camera", 0);
        mRing = sp.getBoolean("ring", false);
        mFlash = sp.getBoolean("flash", false);
        mAutofocus = sp.getBoolean("focus", true);
        cameraState = sp.getInt("cameraState", 0);

        popup = new PopupMenu(this, moreOptions);
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.UPC_A,
            BarcodeFormat.UPC_E, BarcodeFormat.EAN_13, BarcodeFormat.EAN_8,
            BarcodeFormat.RSS_14, BarcodeFormat.CODE_39, BarcodeFormat.CODE_93,
            BarcodeFormat.CODE_128, BarcodeFormat.ITF);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        barcodeView.setStatusText(null);
        CameraSettings settings = barcodeView.getBarcodeView().getCameraSettings();
        settings.setRequestedCameraId(cameraState);
        if (mFlash) {
            barcodeView.setTorchOn();
            toggleFlashView.setImageResource(R.drawable.ic_flash_on_white_24dp);
        }
        if (mRing) {
            popup.getMenu().findItem(R.id.toggleBeep).setChecked(true);
        }
        if (mAutofocus) {
            settings.setAutoFocusEnabled(true);
            popup.getMenu().findItem(R.id.toggleAutofocus).setChecked(true);
        } else {
            settings.setAutoFocusEnabled(false);
        }
        barcodeView.decodeContinuous(callback);
        beepManager = new BeepManager(this);

        searchByBarcode.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Utils.hideKeyboard(this);
                hideSystemUI();
                if (searchByBarcode.getText().toString().isEmpty()) {
                    Toast.makeText(this, getString(R.string.txtBarcodeNotValid), Toast.LENGTH_SHORT).show();
                } else {
                    String barcodeText = searchByBarcode.getText().toString();
                    //for debug only:the barcode 1 is used for test:
                    if (barcodeText.length() <= 2 && !ProductUtils.DEBUG_BARCODE.equals(barcodeText)) {
                        Toast.makeText(this, getString(R.string.txtBarcodeNotValid), Toast.LENGTH_SHORT).show();
                    } else {
                        if (ProductUtils.isBarcodeValid(barcodeText)) {
                            lastText = barcodeText;
                            searchByBarcode.setVisibility(GONE);
                            findProduct(barcodeText, false);
                        } else {
                            searchByBarcode.requestFocus();
                            Toast.makeText(this, getString(R.string.txtBarcodeNotValid), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                return true;
            }
            return false;
        });

        BottomNavigationListenerInstaller.selectNavigationItem(bottomNavigationView, R.id.scan_bottom_nav);
        BottomNavigationListenerInstaller.install(bottomNavigationView, this, this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onCreate(newBase));
    }

    private boolean isProductIncomplete() {
        return product != null && (product.getImageFrontUrl() == null || product.getImageFrontUrl().equals("") ||
            product.getQuantity() == null || product.getQuantity().equals("") ||
            product.getProductName() == null || product.getProductName().equals("") ||
            product.getBrands() == null || product.getBrands().equals("") ||
            product.getIngredientsText() == null || product.getIngredientsText().equals(""));
    }

    void toggleCamera() {
        editor = sp.edit();
        CameraSettings settings = barcodeView.getBarcodeView().getCameraSettings();
        if (barcodeView.getBarcodeView().isPreviewActive()) {
            barcodeView.pause();
        }
        if (settings.getRequestedCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK) {
            cameraState = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            cameraState = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        settings.setRequestedCameraId(cameraState);
        barcodeView.getBarcodeView().setCameraSettings(settings);
        editor.putInt("cameraState", cameraState);
        editor.apply();
        barcodeView.resume();
    }

    @OnClick(R.id.toggle_flash)
    void toggleFlash() {
        editor = sp.edit();
        if (mFlash) {
            barcodeView.setTorchOff();
            mFlash = false;
            toggleFlashView.setImageResource(R.drawable.ic_flash_off_white_24dp);
            editor.putBoolean("flash", false);
        } else {
            barcodeView.setTorchOn();
            mFlash = true;
            toggleFlashView.setImageResource(R.drawable.ic_flash_on_white_24dp);
            editor.putBoolean("flash", true);
        }
        editor.apply();
    }

    @OnClick(R.id.button_more)
    void moreSettings() {
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.toggleBeep:
                    editor = sp.edit();
                    if (mRing) {
                        mRing = false;
                        item.setChecked(false);
                        editor.putBoolean("ring", false);
                    } else {
                        mRing = true;
                        item.setChecked(true);
                        editor.putBoolean("ring", true);
                    }
                    editor.apply();
                    break;
                case R.id.toggleAutofocus:
                    if (barcodeView.getBarcodeView().isPreviewActive()) {
                        barcodeView.pause();
                    }
                    editor = sp.edit();
                    CameraSettings settings = barcodeView.getBarcodeView().getCameraSettings();
                    if (mAutofocus) {
                        mAutofocus = false;
                        settings.setAutoFocusEnabled(false);
                        item.setChecked(false);
                        editor.putBoolean("focus", false);
                    } else {
                        mAutofocus = true;
                        settings.setAutoFocusEnabled(true);
                        item.setChecked(true);
                        editor.putBoolean("focus", true);
                    }
                    barcodeView.getBarcodeView().setCameraSettings(settings);
                    barcodeView.resume();
                    editor.apply();
                    break;
                case R.id.troubleScanning:
                    hideAllViews();
                    handler.removeCallbacks(runnable);
                    quickView.setOnClickListener(null);
                    searchByBarcode.setText(null);
                    searchByBarcode.setVisibility(VISIBLE);
                    quickView.setVisibility(INVISIBLE);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    handler.postDelayed(() -> quickView.setVisibility(VISIBLE), 500);
                    searchByBarcode.requestFocus();
                    break;
                case R.id.toggleCamera:
                    toggleCamera();
                    break;
                default:
                    break;
            }
            return true;
        });
        popup.show();
    }

    /**
     * Overridden to collapse bottom view after a back action from edit form.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_PRODUCT_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            boolean uploadedToServer = data.getBooleanExtra(AddProductActivity.UPLOADED_TO_SERVER, true);
            if (uploadedToServer) {
                findProduct(lastText, true);
            } else {
                // Not uploaded to server, saved locally
                OfflineSavedProduct offlineSavedProduct = mOfflineSavedProductDao.queryBuilder().where(OfflineSavedProductDao.Properties.Barcode.eq(lastText)).unique();
                if (offlineSavedProduct != null) {
                    hideAllViews();
                    showOfflineSavedDetails(offlineSavedProduct);
                }
            }
        } else if (requestCode == LOGIN_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Intent intent = new Intent(ContinuousScanActivity.this, AddProductActivity.class);
            intent.putExtra(AddProductActivity.KEY_EDIT_PRODUCT, product);
            startActivityForResult(intent, ADD_PRODUCT_ACTIVITY_REQUEST_CODE);
        }
    }

    private static class HistoryTask extends AsyncTask<Product, Void, Void> {
        private final HistoryProductDao mHistoryProductDao;

        private HistoryTask(HistoryProductDao mHistoryProductDao) {
            this.mHistoryProductDao = mHistoryProductDao;
        }

        @Override
        protected Void doInBackground(Product... products) {
            OpenFoodAPIClient.addToHistory(mHistoryProductDao, products[0]);
            return null;
        }
    }

    public void collapseBottomSheet() {
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    public void setDietState(long productState){
        dietState.setVisibility(VISIBLE);
        switch (Long.toString(productState)) {
            case "-1":
                dietState.setImageResource(R.drawable.trafficligth_red);
                break;
            case "0":
                dietState.setImageResource(R.drawable.trafficligth_orange);
                break;
            case "1":
                dietState.setImageResource(R.drawable.trafficligth_green);
                break;
            case "2":
                dietState.setImageResource(R.drawable.trafficligth_grey);
                break;
            default:
                dietState.setVisibility(GONE);
        }
    }

    public void showIngredientsTab(String action) {
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        productFragment.goToIngredients(action);
    }
}
