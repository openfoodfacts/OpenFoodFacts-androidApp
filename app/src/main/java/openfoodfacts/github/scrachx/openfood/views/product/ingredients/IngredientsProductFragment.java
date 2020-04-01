package openfoodfacts.github.scrachx.openfood.views.product.ingredients;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import androidx.cardview.widget.CardView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.AdditiveFragmentHelper;
import openfoodfacts.github.scrachx.openfood.fragments.BaseFragment;
import openfoodfacts.github.scrachx.openfood.images.PhotoReceiver;
import openfoodfacts.github.scrachx.openfood.images.ProductImage;
import openfoodfacts.github.scrachx.openfood.jobs.PhotoReceiverHandler;
import openfoodfacts.github.scrachx.openfood.models.AdditiveDao;
import openfoodfacts.github.scrachx.openfood.models.AdditiveName;
import openfoodfacts.github.scrachx.openfood.models.AllergenName;
import openfoodfacts.github.scrachx.openfood.models.AllergenNameDao;
import openfoodfacts.github.scrachx.openfood.models.BottomScreenCommon;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.SendProduct;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.network.WikidataApiClient;
import openfoodfacts.github.scrachx.openfood.repositories.DietRepository;
import openfoodfacts.github.scrachx.openfood.repositories.IDietRepository;
import openfoodfacts.github.scrachx.openfood.repositories.IProductRepository;
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository;
import openfoodfacts.github.scrachx.openfood.utils.FileUtils;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import openfoodfacts.github.scrachx.openfood.views.ContinuousScanActivity;
import openfoodfacts.github.scrachx.openfood.views.FullScreenActivityOpener;
import openfoodfacts.github.scrachx.openfood.views.LoginActivity;
import openfoodfacts.github.scrachx.openfood.views.ProductBrowsingListActivity;
import openfoodfacts.github.scrachx.openfood.views.ProductImageManagementActivity;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;
import openfoodfacts.github.scrachx.openfood.views.product.ProductDietsActivity;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.IllegalClassException;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.INGREDIENTS;
import static openfoodfacts.github.scrachx.openfood.utils.ProductInfoState.EMPTY;
import static openfoodfacts.github.scrachx.openfood.utils.ProductInfoState.LOADING;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class IngredientsProductFragment extends BaseFragment implements IIngredientsProductPresenter.View, PhotoReceiver {
    public static final Pattern INGREDIENT_PATTERN = Pattern.compile("[\\p{L}\\p{Nd}(),.-]+");
    private static final int LOGIN_ACTIVITY_REQUEST_CODE = 1;
    private static final int EDIT_REQUEST_CODE = 2;
    private static final int EDIT_PRODUCT_DIETS_CODE = 3;
    @BindView(R.id.textIngredientProduct)
    TextView ingredientsProduct;
    private AllergenNameDao mAllergenNameDao;
    @BindView(R.id.textSubstanceProduct)
    TextView substanceProduct;
    @BindView(R.id.textTraceProduct)
    TextView traceProduct;
    @BindView(R.id.textAdditiveProduct)
    TextView additiveProduct;
    @BindView(R.id.textPalmOilProduct)
    TextView palmOilProduct;
    @BindView(R.id.textMayBeFromPalmOilProduct)
    TextView mayBeFromPalmOilProduct;
    @BindView(R.id.novaLayout)
    LinearLayout novaLayout;
    @BindView(R.id.nova_group)
    ImageView novaGroup;
    @BindView(R.id.novaExplanation)
    TextView novaExplanation;
    @BindView(R.id.novaMethodLink)
    TextView novaMethodLink;
    @BindView(R.id.imageViewIngredients)
    ImageView mImageIngredients;
    @BindView(R.id.addPhotoLabel)
    TextView addPhotoLabel;
    @BindView(R.id.vitaminsTagsText)
    TextView vitaminTagsTextView;
    @BindView(R.id.mineralTagsText)
    TextView mineralTagsTextView;
    @BindView(R.id.aminoAcidTagsText)
    TextView aminoAcidTagsTextView;
    @BindView(R.id.otherNutritionTags)
    TextView otherNutritionTagTextView;
    @BindView(R.id.cvTextIngredientProduct)
    CardView textIngredientProductCardView;
    @BindView(R.id.cvTextTraceProduct)
    CardView textTraceProductCardView;
    @BindView(R.id.cvTextAdditiveProduct)
    CardView textAdditiveProductCardView;
    @BindView(R.id.cvTextPalmOilProduct)
    CardView textPalmOilProductCardView;
    @BindView(R.id.cvTextMayBePalmOilProduct)
    CardView textMayBePalmOilProductCardView;
    @BindView(R.id.cvVitaminsTagsText)
    CardView vitaminsTagsTextCardView;
    @BindView(R.id.cvAminoAcidTagsText)
    CardView aminoAcidTagsTextCardView;
    @BindView(R.id.cvMineralTagsText)
    CardView mineralTagsTextCardView;
    @BindView(R.id.cvOtherNutritionTags)
    CardView otherNutritionTagsCardView;
    @BindView(R.id.extract_ingredients_prompt)
    Button extractIngredientsPrompt;
    @BindView(R.id.change_ing_img)
    Button updateImageBtn;
    private OpenFoodAPIClient api;
    private String mUrlImage;
    private State activityState;
    private String barcode;
    private AdditiveDao mAdditiveDao;
    private IProductRepository productRepository;
    private IDietRepository dietRepository;
    private IngredientsProductFragment mFragment;
    private SendProduct mSendProduct;
    private WikidataApiClient apiClientForWikiData;
    private CustomTabActivityHelper customTabActivityHelper;
    private CustomTabsIntent customTabsIntent;
    private IIngredientsProductPresenter.Actions presenter;
    private boolean extractIngredients = false;
    private boolean sendUpdatedIngredientsImage = false;
    //boolean to determine if image should be loaded or not
    private boolean isLowBatteryMode = false;
    private PhotoReceiverHandler photoReceiverHandler;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        productRepository = ProductRepository.getInstance();
        dietRepository = DietRepository.getInstance();
        customTabActivityHelper = new CustomTabActivityHelper();
        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(), customTabActivityHelper.getSession());

        activityState = getStateFromActivityIntent();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        api = new OpenFoodAPIClient(getActivity());
        apiClientForWikiData = new WikidataApiClient();

        return createView(inflater, container, R.layout.fragment_ingredients_product);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activityState = getStateFromActivityIntent();
        extractIngredientsPrompt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_box_blue_18dp, 0, 0, 0);
        updateImageBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_a_photo_blue_18dp, 0, 0, 0);
        photoReceiverHandler = new PhotoReceiverHandler(this);
        refreshView(activityState);
    }

    @Override
    public void refreshView(State state) {
        super.refreshView(state);
        activityState = state;
        String langCode = LocaleHelper.getLanguage(getContext());
        if (getArguments() != null) {
            mSendProduct = (SendProduct) getArguments().getSerializable("sendProduct");
        }

        mAllergenNameDao = Utils.getAppDaoSession(getActivity()).getAllergenNameDao();

        // If Battery Level is low and the user has checked the Disable Image in Preferences , then set isLowBatteryMode to true
        if (Utils.isDisableImageLoad(getContext()) && Utils.getBatteryLevel(getContext())) {
            isLowBatteryMode = true;
        }

        final Product product = activityState.getProduct();
        presenter = new IngredientsProductPresenter(product, this);
        barcode = product.getCode();
        List<String> vitaminTagsList = product.getVitaminTags();
        List<String> aminoAcidTagsList = product.getAminoAcidTags();
        List<String> mineralTags = product.getMineralTags();
        List<String> otherNutritionTags = product.getOtherNutritionTags();

        if (!vitaminTagsList.isEmpty()) {
            vitaminsTagsTextCardView.setVisibility(View.VISIBLE);
            vitaminTagsTextView.setText(bold(getString(R.string.vitamin_tags_text)));
            vitaminTagsTextView.append(buildStringBuilder(vitaminTagsList, Utils.SPACE));
        }

        if (!aminoAcidTagsList.isEmpty()) {
            aminoAcidTagsTextCardView.setVisibility(View.VISIBLE);
            aminoAcidTagsTextView.setText(bold(getString(R.string.amino_acid_tags_text)));
            aminoAcidTagsTextView.append(buildStringBuilder(aminoAcidTagsList, Utils.SPACE));
        }

        if (!mineralTags.isEmpty()) {
            mineralTagsTextCardView.setVisibility(View.VISIBLE);
            mineralTagsTextView.setText(bold(getString(R.string.mineral_tags_text)));
            mineralTagsTextView.append(buildStringBuilder(mineralTags, Utils.SPACE));
        }

        if (!otherNutritionTags.isEmpty()) {
            otherNutritionTagTextView.setVisibility(View.VISIBLE);
            otherNutritionTagTextView.setText(bold(getString(R.string.other_tags_text)));
            otherNutritionTagTextView.append(buildStringBuilder(otherNutritionTags, Utils.SPACE));
        }

        additiveProduct.setText(bold(getString(R.string.txtAdditives)));
        presenter.loadAdditives();

        if (isNotBlank(product.getImageIngredientsUrl(langCode))) {
            addPhotoLabel.setVisibility(View.GONE);
            updateImageBtn.setVisibility(View.VISIBLE);

            // Load Image if isLowBatteryMode is false
            if (!isLowBatteryMode) {
                Picasso.get()
                    .load(product.getImageIngredientsUrl(langCode))
                    .into(mImageIngredients);
            } else {
                mImageIngredients.setVisibility(View.GONE);
            }
            mUrlImage = product.getImageIngredientsUrl(langCode);
        }

        //useful when this fragment is used in offline saving
        if (mSendProduct != null && isNotBlank(mSendProduct.getImgupload_ingredients())) {
            addPhotoLabel.setVisibility(View.GONE);
            mUrlImage = mSendProduct.getImgupload_ingredients();
            Picasso.get().load(FileUtils.LOCALE_FILE_SCHEME + mUrlImage).config(Bitmap.Config.RGB_565).into(mImageIngredients);
        }

        List<String> allergens = getAllergens();

        if (activityState != null && StringUtils.isNotEmpty(product.getIngredientsText(langCode))) {
            textIngredientProductCardView.setVisibility(View.VISIBLE);
            SpannableStringBuilder txtIngredients = new SpannableStringBuilder(product.getIngredientsText(langCode).replace("_", ""));
            txtIngredients = setSpanBoldBetweenTokens(txtIngredients, allergens);
            //txtIngredients = dietRepository.getColoredSSBFromSSBAndProduct(txtIngredients, product);
            //txtIngredients.subSequence(txtIngredients.length()-2,txtIngredients.length()).toString();
            //txtIngredients = txtIngredients.replace(txtIngredients.length()-2,txtIngredients.length(),"");
            Object[] txtIngredientsProductState = (Object[]) dietRepository.getColoredSSBAndProductStateFromSSBAndProduct(txtIngredients, product);
            txtIngredients = (SpannableStringBuilder) txtIngredientsProductState[0];
            int productState = (int) txtIngredientsProductState[1];
            FragmentManager fragmentManager = getFragmentManager();
            Fragment fragmentSummary = fragmentManager.getFragments().get(0);
            ImageView dietState = fragmentSummary.getView().findViewById(R.id.dietState);
            dietState.setVisibility(View.VISIBLE);
            //If Continuous ScanActivity is active, then add the productState
            try {
                ((ContinuousScanActivity) getActivity()).setDietState(productState);
            } catch (Exception e) {
                //Just continue
                Toast.makeText(getActivity(), e.getMessage(),Toast.LENGTH_LONG).show();
            }
            switch (productState) {
                case DietRepository.DIET_STATE_FORBIDEN:
                    dietState.setImageResource(R.drawable.trafficligth_red);
                    dietState.setContentDescription("@string/edit_diet_unauthorised_ingredients");
                    break;
                case DietRepository.DIET_STATE_SOSO:
                    dietState.setImageResource(R.drawable.trafficligth_orange);
                    dietState.setContentDescription("@string/edit_diet_so_so_ingredients");
                    break;
                case DietRepository.DIET_STATE_AUTHORISED:
                    dietState.setImageResource(R.drawable.trafficligth_green);
                    dietState.setContentDescription("@string/edit_authorised_ingredients");
                    break;
                case DietRepository.DIET_STATE_UNKNOWN:
                    dietState.setImageResource(R.drawable.trafficligth_grey);
                    //dietState.setContentDescription("@string/edit_authorised_ingredients");
                    break;
                default:
                    dietState.setVisibility(View.INVISIBLE);
            }
            if (TextUtils.isEmpty(product.getIngredientsText(langCode))) {
                extractIngredientsPrompt.setVisibility(View.VISIBLE);
            }
            int ingredientsListAt = Math.max(0, txtIngredients.toString().indexOf(":"));
            if (!txtIngredients.toString().substring(ingredientsListAt).trim().isEmpty()) {
                ingredientsProduct.setText(txtIngredients);
            }
        } else {
            textIngredientProductCardView.setVisibility(View.GONE);
            if (isNotBlank(product.getImageIngredientsUrl(langCode))) {
                extractIngredientsPrompt.setVisibility(View.VISIBLE);
            }
        }
        presenter.loadAllergens();

        if (!StringUtils.isBlank(product.getTraces())) {
            String language = LocaleHelper.getLanguage(getContext());
            textTraceProductCardView.setVisibility(View.VISIBLE);
            traceProduct.setMovementMethod(LinkMovementMethod.getInstance());
            traceProduct.setText(bold(getString(R.string.txtTraces)));
            traceProduct.append(" ");

            String[] traces = product.getTraces().split(",");
            for (int i = 0; i < traces.length; i++) {
                String trace = traces[i];
                if (i > 0) {
                    traceProduct.append(", ");
                }
                traceProduct.append(Utils.getClickableText(getTracesName(language, trace), trace, SearchType.TRACE, getActivity(), customTabsIntent));
            }
        } else {
            textTraceProductCardView.setVisibility(View.GONE);
        }

        textPalmOilProductCardView.setVisibility(View.GONE);
        palmOilProduct.setVisibility(View.GONE);
        if (CollectionUtils.isNotEmpty(product.getIngredientsFromPalmOilTags())) {
            textPalmOilProductCardView.setVisibility(View.VISIBLE);
            palmOilProduct.setVisibility(View.VISIBLE);
            palmOilProduct.setText(bold(getString(R.string.txtPalmOilProduct)));
            palmOilProduct.append(" ");
            palmOilProduct.append(product.getIngredientsFromPalmOilTags().toString().replaceAll("[\\[,\\]]", ""));
        }
        textMayBePalmOilProductCardView.setVisibility(View.GONE);
        mayBeFromPalmOilProduct.setVisibility(View.GONE);
        if (CollectionUtils.isNotEmpty(product.getIngredientsThatMayBeFromPalmOilTags())) {
            textMayBePalmOilProductCardView.setVisibility(View.VISIBLE);
            mayBeFromPalmOilProduct.setVisibility(View.VISIBLE);
            mayBeFromPalmOilProduct.setText(bold(getString(R.string.txtMayBeFromPalmOilProduct)));
            mayBeFromPalmOilProduct.append(" ");
            mayBeFromPalmOilProduct.append(product.getIngredientsThatMayBeFromPalmOilTags().toString().replaceAll("[\\[,\\]]", ""));
        }

        if (product.getNovaGroups() != null) {
            novaLayout.setVisibility(View.VISIBLE);
            novaExplanation.setText(Utils.getNovaGroupExplanation(product.getNovaGroups(), getContext()));
            novaGroup.setImageResource(Utils.getNovaGroupDrawable(product));
            novaGroup.setOnClickListener((View v) -> {
                Uri uri = Uri.parse(getString(R.string.url_nova_groups));
                CustomTabsIntent tabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(), customTabActivityHelper.getSession());
                CustomTabActivityHelper.openCustomTab(IngredientsProductFragment.this.getActivity(), tabsIntent, uri, new WebViewFallback());
            });
        } else {
            novaLayout.setVisibility(View.GONE);
        }
    }

    private String getTracesName(String languageCode, String tag) {
        AllergenName allergenName = mAllergenNameDao.queryBuilder().where(AllergenNameDao.Properties.AllergenTag.eq(tag), AllergenNameDao.Properties.LanguageCode.eq(languageCode))
            .unique();
        if (allergenName != null) {
            return allergenName.getName();
        }
        return tag;
    }

    private StringBuilder buildStringBuilder(List<String> stringList, String prefix) {
        StringBuilder otherNutritionStringBuilder = new StringBuilder();
        for (String otherSubstance : stringList) {
            otherNutritionStringBuilder.append(prefix);
            prefix = ", ";
            otherNutritionStringBuilder.append(trimLanguagePartFromString(otherSubstance));
        }
        return otherNutritionStringBuilder;
    }

    private CharSequence getAllergensTag(AllergenName allergen) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                if (allergen.getIsWikiDataIdPresent()) {
                    apiClientForWikiData.doSomeThing(
                        allergen.getWikiDataId(),
                        (value, result) -> {
                            if (value) {
                                FragmentActivity activity = getActivity();
                                if (activity != null && !activity.isFinishing()) {
                                    BottomScreenCommon.showBottomScreen(result, allergen,
                                        activity.getSupportFragmentManager());
                                }
                            } else {
                                ProductBrowsingListActivity.startActivity(getContext(),
                                    allergen.getAllergenTag(),
                                    allergen.getName(),
                                    SearchType.ALLERGEN);
                            }
                        });
                } else {
                    ProductBrowsingListActivity.startActivity(getContext(),
                        allergen.getAllergenTag(),
                        allergen.getName(),
                        SearchType.ALLERGEN);
                }
            }
        };

        ssb.append(allergen.getName());
        ssb.setSpan(clickableSpan, 0, ssb.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        // If allergen is not in the taxonomy list then italicize it
        if (!allergen.isNotNull()) {
            StyleSpan iss =
                new StyleSpan(android.graphics.Typeface.ITALIC); //Span to make text italic
            ssb.setSpan(iss, 0, ssb.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return ssb;
    }

    /**
     * @return the string after trimming the language code from the tags
     *     like it returns folic-acid for en:folic-acid
     */
    private String trimLanguagePartFromString(String string) {
        return string.substring(3);
    }

    private SpannableStringBuilder setSpanBoldBetweenTokens(CharSequence text, List<String> allergens) {
        final SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        Matcher m = INGREDIENT_PATTERN.matcher(ssb);
        while (m.find()) {
            final String tm = m.group();
            final String allergenValue = tm.replaceAll("[(),.-]+", "");

            for (String allergen : allergens) {
                if (allergen.equalsIgnoreCase(allergenValue)) {
                    int start = m.start();
                    int end = m.end();

                    if (tm.contains("(")) {
                        start += 1;
                    } else if (tm.contains(")")) {
                        end -= 1;
                    }

                    ssb.setSpan(new StyleSpan(Typeface.BOLD), start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        ssb.insert(0, Utils.bold(getString(R.string.txtIngredients) + ' '));
        return ssb;
    }

    @Override
    public void showAdditives(List<AdditiveName> additives) {
        AdditiveFragmentHelper.showAdditives(additives, additiveProduct, apiClientForWikiData, this);
    }

    @Override
    public void showAdditivesState(String state) {
        switch (state) {
            case LOADING:
                textAdditiveProductCardView.setVisibility(View.VISIBLE);
                additiveProduct.append(getString(R.string.txtLoading));
                break;

            case EMPTY:
                textAdditiveProductCardView.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void showAllergens(List<AllergenName> allergens) {
        substanceProduct.setMovementMethod(LinkMovementMethod.getInstance());
        substanceProduct.setText(bold(getString(R.string.txtSubstances)));
        substanceProduct.append(" ");

        for (int i = 0, lastIdx = allergens.size() - 1; i <= lastIdx; i++) {
            AllergenName allergen = allergens.get(i);
            substanceProduct.append(getAllergensTag(allergen));
            // Add comma if not the last item
            if (i != lastIdx) {
                substanceProduct.append(", ");
            }
        }
    }

    @OnClick(R.id.change_ing_img)
    public void change_ing_image() {
        sendUpdatedIngredientsImage = true;

        if (getActivity() == null) {
            return;
        }
        ViewPager viewPager = getActivity().findViewById(
            R.id.pager);
        if (BuildConfig.FLAVOR.equals("off")) {
            final SharedPreferences settings = getActivity().getSharedPreferences("login", 0);
            final String login = settings.getString("user", "");
            if (login.isEmpty()) {
                showSignInDialog();
            } else {
                activityState = getStateFromActivityIntent();
                if (activityState != null) {
                    Intent intent = new Intent(getContext(), AddProductActivity.class);
                    intent.putExtra("send_updated", sendUpdatedIngredientsImage);
                    intent.putExtra(AddProductActivity.KEY_EDIT_PRODUCT, activityState.getProduct());
                    startActivityForResult(intent, EDIT_REQUEST_CODE);
                }
            }
        }
        if (BuildConfig.FLAVOR.equals("opff")) {
            viewPager.setCurrentItem(4);
        }

        if (BuildConfig.FLAVOR.equals("obf")) {
            viewPager.setCurrentItem(1);
        }

        if (BuildConfig.FLAVOR.equals("opf")) {
            viewPager.setCurrentItem(0);
        }
    }

    @Override
    public void showAllergensState(String state) {
        switch (state) {
            case LOADING:
                substanceProduct.setVisibility(View.VISIBLE);
                substanceProduct.append(getString(R.string.txtLoading));
                break;

            case EMPTY:
                substanceProduct.setVisibility(View.GONE);
                break;
        }
    }

    private List<String> getAllergens() {
        List<String> allergens = activityState.getProduct().getAllergensTags();
        if (activityState.getProduct() == null || allergens == null || allergens.isEmpty()) {
            return Collections.emptyList();
        } else {
            return allergens;
        }
    }

    @OnClick(R.id.novaMethodLink)
    void novaMethodLinkDisplay() {
        if (activityState != null && activityState.getProduct() != null && activityState.getProduct().getNovaGroups() != null) {
            Uri uri = Uri.parse(getString(R.string.url_nova_groups));
            CustomTabsIntent tabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(), customTabActivityHelper.getSession());
            CustomTabActivityHelper.openCustomTab(IngredientsProductFragment.this.getActivity(), tabsIntent, uri, new WebViewFallback());
        }
    }

    @OnClick(R.id.extract_ingredients_prompt)
    public void extractIngredients() {
        extractIngredients = true;
        final SharedPreferences settings = getActivity().getSharedPreferences("login", 0);
        final String login = settings.getString("user", "");
        if (login.isEmpty()) {

            showSignInDialog();
        } else {
            activityState = getStateFromActivityIntent();
            Intent intent = new Intent(getContext(), AddProductActivity.class);
            intent.putExtra(AddProductActivity.KEY_EDIT_PRODUCT, activityState.getProduct());
            intent.putExtra("perform_ocr", extractIngredients);
            startActivityForResult(intent, EDIT_REQUEST_CODE);
        }
    }

    private void showSignInDialog() {
        new MaterialDialog.Builder(getContext())
            .title(R.string.sign_in_to_edit)
            .positiveText(R.string.txtSignIn)
            .negativeText(R.string.dialog_cancel)
            .onPositive((dialog, which) -> {
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivityForResult(intent, LOGIN_ACTIVITY_REQUEST_CODE);
                dialog.dismiss();
            })
            .onNegative((dialog, which) -> dialog.dismiss())
            .build().show();
    }

    @OnClick(R.id.imageViewIngredients)
    void openFullScreen(View v) {
        if (mUrlImage != null && activityState != null && activityState.getProduct() != null) {
            FullScreenActivityOpener.openForUrl(this, activityState.getProduct(), INGREDIENTS, mUrlImage, mImageIngredients);
        } else {
            newIngredientImage();
        }
    }

    public void newIngredientImage() {
        doChooseOrTakePhotos(getString(R.string.ingredients_picture));
    }

    @Override
    protected void doOnPhotosPermissionGranted() {
        newIngredientImage();
    }

    public void onPhotoReturned(File newPhotoFile) {
        ProductImage image = new ProductImage(barcode, INGREDIENTS, newPhotoFile);
        image.setFilePath(newPhotoFile.getAbsolutePath());
        api.postImg(getContext(), image, null);
        addPhotoLabel.setVisibility(View.GONE);
        mUrlImage = newPhotoFile.getAbsolutePath();

        Picasso.get()
            .load(newPhotoFile)
            .fit()
            .into(mImageIngredients);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //added case for sending updated ingredients image
        if (requestCode == LOGIN_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Intent intent = new Intent(getContext(), AddProductActivity.class);
            intent.putExtra("send_updated", sendUpdatedIngredientsImage);
            intent.putExtra("perform_ocr", extractIngredients);
            intent.putExtra(AddProductActivity.KEY_EDIT_PRODUCT, activityState.getProduct());
            startActivity(intent);
        }
        if (requestCode == EDIT_REQUEST_CODE && resultCode == RESULT_OK) {
            onRefresh();
        }
        if (requestCode == EDIT_PRODUCT_DIETS_CODE) {
            onRefresh();
        }
        if (ProductImageManagementActivity.isImageModified(requestCode, resultCode)) {
            onRefresh();
        }

        photoReceiverHandler.onActivityResult(this, requestCode, resultCode, data);
    }

    public String getIngredients() {
        return mUrlImage;
    }

    @OnClick(R.id.textIngredientProduct)
    public void openFragmentDietIngredientsProduct(View v) {
        if (dietRepository.getDietCount() > 0) {
            Intent intent = new Intent(getActivity(), ProductDietsActivity.class);
            intent.putExtra("state", activityState);
            startActivityForResult(intent, EDIT_PRODUCT_DIETS_CODE);
        }
    }

    @Override
    public void onDestroyView() {
        if (presenter != null) {
            presenter.dispose();
        }
        super.onDestroyView();
    }
}
