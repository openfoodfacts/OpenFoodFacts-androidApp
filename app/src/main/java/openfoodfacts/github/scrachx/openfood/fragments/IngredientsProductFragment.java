package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.AdditiveDao;
import openfoodfacts.github.scrachx.openfood.models.AdditiveName;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImage;
import openfoodfacts.github.scrachx.openfood.models.SendProduct;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.network.WikidataApiClient;
import openfoodfacts.github.scrachx.openfood.repositories.IProductRepository;
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.FullScreenImage;

import openfoodfacts.github.scrachx.openfood.views.ProductActivity;
import openfoodfacts.github.scrachx.openfood.views.ProductBrowsingListActivity;

import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabsHelper;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static android.Manifest.permission.CAMERA;
import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.INGREDIENTS;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.jsoup.helper.StringUtil.isBlank;

public class IngredientsProductFragment extends BaseFragment {

    public static final Pattern INGREDIENT_PATTERN = Pattern.compile("[\\p{L}\\p{Nd}(),.-]+");
    public static final Pattern ALLERGEN_PATTERN = Pattern.compile("[\\p{L}\\p{Nd}]+");
    @BindView(R.id.textIngredientProduct)
    TextView ingredientsProduct;
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

    private OpenFoodAPIClient api;
    private String mUrlImage;
    private State mState;
    private String barcode;
    private AdditiveDao mAdditiveDao;
    private IProductRepository productRepository;
    private IngredientsProductFragment mFragment;
    private SendProduct mSendProduct;
    private WikidataApiClient apiClientForWikiData;
    private CustomTabActivityHelper customTabActivityHelper;
    private CustomTabsIntent customTabsIntent;
    //boolean to determine if image should be loaded or not
    private boolean isLowBatteryMode = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        productRepository = ProductRepository.getInstance();
        customTabActivityHelper = new CustomTabActivityHelper();
        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(), customTabActivityHelper.getSession());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        api = new OpenFoodAPIClient(getActivity());
        apiClientForWikiData = new WikidataApiClient();
        mFragment = this;
        return createView(inflater, container, R.layout.fragment_ingredients_product);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Intent intent = getActivity().getIntent();
        mState = (State) intent.getExtras().getSerializable("state");
        try {
            mSendProduct = (SendProduct) getArguments().getSerializable("sendProduct");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        mAdditiveDao = Utils.getAppDaoSession(getActivity()).getAdditiveDao();

        // If Battery Level is low and the user has checked the Disable Image in Preferences , then set isLowBatteryMode to true
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        Utils.DISABLE_IMAGE_LOAD = preferences.getBoolean("disableImageLoad", false);
        if (Utils.DISABLE_IMAGE_LOAD && Utils.getBatteryLevel(getContext())) {
            isLowBatteryMode = true;
        }

        final Product product = mState.getProduct();
        barcode = product.getCode();
        List<String> vitaminTagsList = product.getVitaminTags();
        List<String> aminoAcidTagsList = product.getAminoAcidTags();
        List<String> mineralTags = product.getMineralTags();
        List<String> otherNutritionTags = product.getOtherNutritionTags();
        String prefix = " ";

        if (!vitaminTagsList.isEmpty()) {
            StringBuilder vitaminStringBuilder = new StringBuilder();
            vitaminTagsTextView.append(bold(getString(R.string.vitamin_tags_text)));
            for (String vitamins : vitaminTagsList) {
                vitaminStringBuilder.append(prefix);
                prefix = ", ";
                vitaminStringBuilder.append(trimLanguagePartFromString(vitamins));
            }
            vitaminTagsTextView.append(vitaminStringBuilder.toString());
        } else {
            vitaminTagsTextView.setVisibility(View.GONE);
        }

        if (!aminoAcidTagsList.isEmpty()) {
            String aminoPrefix = " ";
            StringBuilder aminoAcidStringBuilder = new StringBuilder();
            aminoAcidTagsTextView.append(bold(getString(R.string.amino_acid_tags_text)));
            for (String aminoAcid : aminoAcidTagsList) {
                aminoAcidStringBuilder.append(aminoPrefix);
                aminoPrefix = ", ";
                aminoAcidStringBuilder.append(trimLanguagePartFromString(aminoAcid));
            }
            aminoAcidTagsTextView.append(aminoAcidStringBuilder.toString());
        } else {
            aminoAcidTagsTextView.setVisibility(View.GONE);
        }

        if (!mineralTags.isEmpty()) {
            String mineralPrefix = " ";
            StringBuilder mineralsStringBuilder = new StringBuilder();
            mineralTagsTextView.append(bold(getString(R.string.mineral_tags_text)));
            for (String mineral : mineralTags) {
                mineralsStringBuilder.append(mineralPrefix);
                mineralPrefix = ", ";
                mineralsStringBuilder.append(trimLanguagePartFromString(mineral));
            }
            mineralTagsTextView.append(mineralsStringBuilder);
        } else {
            mineralTagsTextView.setVisibility(View.GONE);
        }

        if (!otherNutritionTags.isEmpty()) {
            String otherNutritionPrefix = " ";
            StringBuilder otherNutritionStringBuilder = new StringBuilder();
            otherNutritionTagTextView.append(bold(getString(R.string.other_tags_text)));
            for (String otherSubstance : otherNutritionTags) {
                otherNutritionStringBuilder.append(otherNutritionPrefix);
                otherNutritionPrefix = ", ";
                otherNutritionStringBuilder.append(trimLanguagePartFromString(otherSubstance));
            }
            otherNutritionTagTextView.append(otherNutritionStringBuilder.toString());
        } else {
            otherNutritionTagTextView.setVisibility(View.GONE);
        }

        if (isNotBlank(product.getImageIngredientsUrl())) {
            addPhotoLabel.setVisibility(View.GONE);

            // Load Image if isLowBatteryMode is false
            if(!isLowBatteryMode) {
                Picasso.with(view.getContext())
                        .load(product.getImageIngredientsUrl())
                        .into(mImageIngredients);
            }else{
                mImageIngredients.setVisibility(View.GONE);
            }

            mUrlImage = product.getImageIngredientsUrl();
        }

        //useful when this fragment is used in offline saving
        if (mSendProduct != null && isNotBlank(mSendProduct.getImgupload_ingredients())) {
            addPhotoLabel.setVisibility(View.GONE);
            mUrlImage = mSendProduct.getImgupload_ingredients();
            Picasso.with(getContext()).load("file://" + mUrlImage).config(Bitmap.Config.RGB_565).into(mImageIngredients);
        }

        List<String> allergens = getAllergens();

        if (mState != null && product.getIngredientsText() != null) {
            SpannableStringBuilder txtIngredients = new SpannableStringBuilder(product.getIngredientsText().replace("_", ""));
            txtIngredients = setSpanBoldBetweenTokens(txtIngredients, allergens);
            int ingredientsListAt = Math.max(0, txtIngredients.toString().indexOf(":"));
            if (!txtIngredients.toString().substring(ingredientsListAt).trim().isEmpty()) {
                ingredientsProduct.setText(txtIngredients);
            } else {
                ingredientsProduct.setVisibility(View.GONE);
            }
        }

        if (!allergens.isEmpty()) {
            substanceProduct.setMovementMethod(LinkMovementMethod.getInstance());
            substanceProduct.append(bold(getString(R.string.txtSubstances)));
            substanceProduct.append(" ");

            String allergen;
            for (int i = 0; i < allergens.size() - 1; i++) {
                allergen = allergens.get(i);
                substanceProduct.append(Utils.getClickableText(allergen, allergen, SearchType.ALLERGEN, getActivity(), customTabsIntent));
                substanceProduct.append(", ");
            }

            allergen = allergens.get(allergens.size() - 1);
            substanceProduct.append(Utils.getClickableText(allergen, allergen, SearchType.ALLERGEN, getActivity(), customTabsIntent));
        } else {
            substanceProduct.setVisibility(View.GONE);
        }

        if (isBlank(product.getTraces())) {
            traceProduct.setVisibility(View.GONE);
        } else {
            traceProduct.setMovementMethod(LinkMovementMethod.getInstance());
            traceProduct.append(bold(getString(R.string.txtTraces)));
            traceProduct.append(" ");

            String trace;
            String traces[] = product.getTraces().split(",");
            for (int i = 0; i < traces.length - 1; i++) {
                trace = traces[i];
                traceProduct.append(Utils.getClickableText(trace, trace, SearchType.TRACE, getActivity(), customTabsIntent));
                traceProduct.append(", ");
            }

            trace = traces[traces.length - 1];
            traceProduct.append(Utils.getClickableText(trace, trace, SearchType.TRACE, getActivity(), customTabsIntent));
        }

        if (!product.getAdditivesTags().isEmpty()) {
            additiveProduct.setMovementMethod(LinkMovementMethod.getInstance());
            additiveProduct.append(bold(getString(R.string.txtAdditives)));
            additiveProduct.append(" ");
            additiveProduct.append("\n");
            additiveProduct.setClickable(true);
            additiveProduct.setMovementMethod(LinkMovementMethod.getInstance());
            List<AdditiveName> additives = new ArrayList<>();

            AdditiveName additiveName;
            String languageCode = Locale.getDefault().getLanguage();
            for (String tag : product.getAdditivesTags()) {
                additiveName = productRepository.getAdditiveByTagAndLanguageCode(tag, languageCode);
                if (additiveName == null) {
                    additiveName = productRepository.getAdditiveByTagAndDefaultLanguageCode(tag);
                    if (additiveName == null) {
                        additiveName = new AdditiveName(StringUtils.capitalize(tag));
                    }
                }

                if (additiveName != null) {
                    additives.add(additiveName);
                }

                for (int i = 0; i < additives.size() - 1; i++) {
                    additiveProduct.append(getAdditiveTag((additives.get(i))));
                    additiveProduct.append("\n");
                }

                additiveProduct.append(getAdditiveTag((additives.get(additives.size() - 1))));
            }

        } else {
            additiveProduct.setVisibility(View.GONE);
        }

        if (product.getIngredientsFromPalmOilN() == 0 && product.getIngredientsFromOrThatMayBeFromPalmOilN() == 0) {
            palmOilProduct.setVisibility(View.GONE);
            mayBeFromPalmOilProduct.setVisibility(View.GONE);
        } else {
            if (!product.getIngredientsFromPalmOilTags().isEmpty()) {
                palmOilProduct.append(bold(getString(R.string.txtPalmOilProduct)));
                palmOilProduct.append(" ");
                palmOilProduct.append(product.getIngredientsFromPalmOilTags().toString().replaceAll("[\\[,\\]]", ""));
            } else {
                palmOilProduct.setVisibility(View.GONE);
            }
            if (!product.getIngredientsThatMayBeFromPalmOilTags().isEmpty()) {
                mayBeFromPalmOilProduct.append(bold(getString(R.string.txtMayBeFromPalmOilProduct)));
                mayBeFromPalmOilProduct.append(" ");
                mayBeFromPalmOilProduct.append(product.getIngredientsThatMayBeFromPalmOilTags().toString().replaceAll("[\\[,\\]]", ""));
            } else {
                mayBeFromPalmOilProduct.setVisibility(View.GONE);
            }
        }
    }


    private CharSequence getAdditiveTag(AdditiveName additive) {

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                if (additive.getIsWikiDataIdPresent()) {
                    apiClientForWikiData.doSomeThing(additive.getWikiDataId(), new WikidataApiClient.OnWikiResponse() {
                        @Override
                        public void onresponse(boolean value, JSONObject result) {
                            if (value) {
                                ProductActivity productActivity = (ProductActivity) getActivity();
                                productActivity.showBottomScreen(result, additive.getWikiDataId(), 3, additive.getName());
                            } else {
                                Intent intent = new Intent(getActivity(), ProductBrowsingListActivity.class);
                                intent.putExtra("search_query", additive.getName());
                                intent.putExtra("search_type", "additive");
                                startActivity(intent);
                            }
                        }
                    });
                } else {
                    Intent intent = new Intent(getActivity(), ProductBrowsingListActivity.class);
                    intent.putExtra("search_query", additive.getName());
                    intent.putExtra("search_type", "additive");
                    startActivity(intent);
                }
            }
        };


        spannableStringBuilder.append(additive.getName());

        spannableStringBuilder.setSpan(clickableSpan, 0, spannableStringBuilder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableStringBuilder;

    }

    /**
     * @return the string after trimming the language code from the tags
     * like it returns folic-acid for en:folic-acid
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

    private List<String> getAllergens() {
        if (mState.getProduct() == null || mState.getProduct().getAllergens() == null) {
            return Collections.emptyList();
        }

        List<String> list = new ArrayList<>();
        Matcher m = ALLERGEN_PATTERN.matcher(mState.getProduct().getAllergens().replace(",", ""));
        while (m.find()) {
            final String tma = m.group();
            boolean canAdd = true;

            for (String allergen : list) {
                if (tma.equalsIgnoreCase(allergen)) {
                    canAdd = false;
                    break;
                }
            }

            if (canAdd) {
                list.add(tma);
            }
        }
        return list;
    }

    @OnClick(R.id.imageViewIngredients)
    public void openFullScreen(View v) {
        if (mUrlImage != null) {
            Intent intent = new Intent(v.getContext(), FullScreenImage.class);
            Bundle bundle = new Bundle();
            bundle.putString("imageurl", mUrlImage);
            intent.putExtras(bundle);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(getActivity(), (View) mImageIngredients,
                                getActivity().getString(R.string.product_transition));
                startActivity(intent, options.toBundle());
            } else {
                startActivity(intent);
            }

        } else {
            // take a picture
            if (ContextCompat.checkSelfPermission(getActivity(), CAMERA) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            } else {
                EasyImage.openCamera(this, 0);
//                EasyImage.openGallery(this);
            }
        }
    }

    private void onPhotoReturned(File photoFile) {
        ProductImage image = new ProductImage(barcode, INGREDIENTS, photoFile);
        image.setFilePath(photoFile.getAbsolutePath());
        api.postImg(getContext(), image);
        addPhotoLabel.setVisibility(View.GONE);
        mUrlImage = photoFile.getAbsolutePath();

        Picasso.with(getContext())
                .load(photoFile)
                .fit()
                .into(mImageIngredients);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                onPhotoReturned(new File(resultUri.getPath()));
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

        EasyImage.handleActivityResult(requestCode, resultCode, data, getActivity(), new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
            }

            @Override
            public void onImagesPicked(List<File> imageFiles, EasyImage.ImageSource source, int type) {
                CropImage.activity(Uri.fromFile(imageFiles.get(0))).setAllowFlipping(false).setOutputUri(Utils.getOutputPicUri(getContext()))
                        .start(getContext(), mFragment);
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(getContext());
                    if (photoFile != null) photoFile.delete();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length <= 0 || grantResults[0] != PERMISSION_GRANTED) {
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.permission_title)
                            .content(R.string.permission_denied)
                            .negativeText(R.string.txtNo)
                            .positiveText(R.string.txtYes)
                            .onPositive((dialog, which) -> {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            })
                            .show();
                } else {
                    EasyImage.openCamera(this, 0);
                }
            }
        }
    }

    public String getIngredients() {
        return mUrlImage;
    }

}
