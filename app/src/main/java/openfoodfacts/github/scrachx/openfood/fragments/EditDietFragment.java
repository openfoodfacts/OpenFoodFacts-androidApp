package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.chip.Chip;
import com.hootsuite.nachos.validator.ChipifyingNachoValidator;
import org.greenrobot.greendao.async.AsyncSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.models.Diet;
import openfoodfacts.github.scrachx.openfood.models.DietIngredients;
import openfoodfacts.github.scrachx.openfood.models.DietName;
import openfoodfacts.github.scrachx.openfood.models.IngredientName;
import openfoodfacts.github.scrachx.openfood.models.IngredientNameDao;
import openfoodfacts.github.scrachx.openfood.repositories.DietRepository;
import openfoodfacts.github.scrachx.openfood.repositories.IDietRepository;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import openfoodfacts.github.scrachx.openfood.views.listeners.BottomNavigationListenerInstaller;

import static com.hootsuite.nachos.terminator.ChipTerminatorHandler.BEHAVIOR_CHIPIFY_CURRENT_TOKEN;

public class EditDietFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_DIET_NAME = "dietName";
    //Get the components of the fragment_diet_edit
    @BindView(R.id.diet_id)
    EditText dietId;
    @BindView(R.id.diet_name)
    EditText dietName;
    @BindView(R.id.diet_description)
    EditText dietDescription;
    @BindView(R.id.diet_enabled)
    Switch dietEnabled;
    @BindView(R.id.ingredients_authorised)
    NachoTextView ingredientsAuthorised;
    @BindView(R.id.ingredients_soso)
    NachoTextView ingredientsSoSo;
    @BindView(R.id.ingredients_unauthorised)
    NachoTextView ingredientsUnauthorised;
    @BindView(R.id.save_edits)
    Button saveEdits;
    // Fetching of the (theoretical) language of input:
    private String languageCode = Locale.getDefault().getLanguage();
    private String appLanguageCode;
    private Activity activity;
    private IDietRepository dietRepository;
    private IngredientNameDao mIngredientNameDao;
    private List<String> ingredients = new ArrayList<>();
    private String mDietName;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;

    //private OnFragmentInteractionListener mListener;

    public EditDietFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param dietName Name off the diet to edit, null if new diet to be created.
     * @return A new instance of fragment EditDietFragment.
     */
    public static EditDietFragment newInstance(String dietName) {
        EditDietFragment fragment = new EditDietFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DIET_NAME, dietName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mDietName = getArguments().getString(ARG_DIET_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_diet, container, false);
        ButterKnife.bind(this, view);
        if (mDietName != null) {
            IDietRepository dietRepository = DietRepository.getInstance();
            Diet diet = dietRepository.getDietByNameAndLanguageCode(mDietName, languageCode);
            dietName.setText(mDietName);
            DietName dietName = dietRepository.getDietNameByDietTagAndLanguageCode(diet.getTag(), languageCode);
            dietDescription.setText(dietName.getDescription());
            dietEnabled.setChecked(diet.getEnabled());
            dietId.setText(dietName.getId().toString());
            String ingredientNames = dietRepository.getSortedIngredientNameStringByDietTagStateAndLanguageCode(diet.getTag(), DietRepository.DIET_STATE_AUTHORISED, languageCode);
            if (!ingredientNames.equals("")) {
                ingredientsAuthorised.setText(Arrays.asList(ingredientNames.split("\\s*,\\s*")));
            }
            ingredientNames = dietRepository.getSortedIngredientNameStringByDietTagStateAndLanguageCode(diet.getTag(), DietRepository.DIET_STATE_SOSO, languageCode);
            if (!ingredientNames.equals("")) {
                ingredientsSoSo.setText(Arrays.asList(ingredientNames.split("\\s*,\\s*")));
            }
            ingredientNames = dietRepository.getSortedIngredientNameStringByDietTagStateAndLanguageCode(diet.getTag(), DietRepository.DIET_STATE_FORBIDEN, languageCode);
            if (!ingredientNames.equals("")) {
                ingredientsUnauthorised.setText(Arrays.asList(ingredientNames.split("\\s*,\\s*")));
            }
        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_diet_edit, menu);
        super.onCreateOptionsMenu(menu, inflater);
        if (dietId.getText().toString().isEmpty()) {
            MenuItem pinMenuItem = menu.findItem(R.id.action_remove);
            pinMenuItem.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        dietRepository = DietRepository.getInstance();
        switch (item.getItemId()) {
            case R.id.action_remove:
                new MaterialDialog.Builder(this.getContext())
                    .title(R.string.title_remove_diet_dialog)
                    .content(R.string.text_remove_diet_dialog)
                    .onPositive((dialog, which) -> {
                        if (! dietId.getText().toString().isEmpty()) {
                            dietRepository.removeDiet(Long.parseLong(dietId.getText().toString()));
                        }
                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        fm.popBackStack();
                    })
                    .positiveText(R.string.txtYes)
                    .negativeText(R.string.txtNo)
                    .show();
                //Back to the DietsFragment.
                return true;

            case R.id.action_share:
                StringBuffer extraText = new StringBuffer();
                extraText.append(getString(R.string.diet_export_1) + " \"" + getString(R.string.your_diets) + "\"\n" + getString(R.string.diet_export_2) + "\n");
                extraText.append(getString(R.string.edit_diet_name) + " : " + dietName.getText().toString() + "\n");
                extraText.append(getString(R.string.edit_diet_description) + " : " + dietDescription.getText().toString() + "\n");
                extraText.append(getString(R.string.edit_diet_authorised_ingredients) + "  : " + ingredientsAuthorised.getAllChips().toString().replace("[","").replace("]",",") + "\n");
                extraText.append(getString(R.string.edit_diet_so_so_ingredients) + "  : " + ingredientsSoSo.getAllChips().toString().replace("[","").replace("]",",") + "\n");
                extraText.append(getString(R.string.edit_diet_unauthorised_ingredients) + "  : " + ingredientsUnauthorised.getAllChips().toString().replace("[","").replace("]",",") + "\n");
                extraText.append(getString(R.string.diet_export_3) + " \"" + getString(R.string.save_edits) + "\"");
                //String jsonDiet = dietRepository.exportDietToJson(dietRepository.getDietByNameAndLanguageCode(dietName.getText().toString(), languageCode));
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, extraText.toString());
                //sendIntent.putExtra(Intent.EXTRA_TEXT, jsonDiet);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appLanguageCode = LocaleHelper.getLanguage(getActivity());
        initializeChips();
        loadAutoSuggestions();
        BottomNavigationListenerInstaller.install(bottomNavigationView,getActivity(),getContext());
    }

    @OnClick(R.id.save_edits)
    void saveEdits() {
        if (dietName.getText().length() == 0) {
            dietName.setHintTextColor(getResources().getColor(R.color.red));
            dietName.requestFocus();
            Toast toast = Toast.makeText(OFFApplication.getInstance(), R.string.edit_diet_toast_name_of_the_diet, Toast.LENGTH_LONG);
            toast.show();
        } else {
            dietRepository = DietRepository.getInstance();
            if (dietId.getText().toString().isEmpty()) {
                //Add a diet row with the form's informations.
                dietRepository.addDiet(dietName.getText().toString(), dietDescription.getText().toString(), dietEnabled.isChecked(), languageCode);
            } else {
                //Update a diet row with the form's informations.
                dietRepository.saveDiet(Long.valueOf(dietId.getText().toString()), dietName.getText().toString(), dietDescription.getText().toString(), dietEnabled.isChecked(), languageCode);
            }
            //Get the diet
            Diet diet = dietRepository.getDietByNameAndLanguageCode(dietName.getText().toString(), languageCode);
            //Set the state at DIET_STATE_UNKNOWN (no impact) for all ingredients associated with the diet. Sort of reset before the next steps
            List<DietIngredients> dietIngredientsList = dietRepository.getDietIngredientsListByDietTag(diet.getTag());
            for (int i = 0; i < dietIngredientsList.size(); i++) {
                DietIngredients dietIngredients = dietIngredientsList.get(i);
                dietIngredients.setState(DietRepository.DIET_STATE_UNKNOWN);
                dietRepository.saveDietIngredients(dietIngredients);
            }
            //Set the goods state for each ingredients in the 3 lists
            for (Chip chip : ingredientsAuthorised.getAllChips()) {
                String ingredient = (String) chip.getText();
                dietRepository.addIngredient(ingredient, languageCode);
                dietRepository.addDietIngredients(diet.getTag(), ingredient, languageCode, DietRepository.DIET_STATE_AUTHORISED);
            }
            for (Chip chip : ingredientsSoSo.getAllChips()) {
                String ingredient = (String) chip.getText();
                dietRepository.addIngredient(ingredient, languageCode);
                dietRepository.addDietIngredients(diet.getTag(), ingredient, languageCode, DietRepository.DIET_STATE_SOSO);
            }
            for (Chip chip : ingredientsUnauthorised.getAllChips()) {
                String ingredient = (String) chip.getText();
                dietRepository.addIngredient(ingredient, languageCode);
                dietRepository.addDietIngredients(diet.getTag(), ingredient, languageCode, DietRepository.DIET_STATE_FORBIDEN);
            }
            //Back to the DietsFragment.
            Fragment fragment = new DietsFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);  // if written, this transaction will be added to backstack
            transaction.commit();
        }
    }

    private void initializeChips() {
        NachoTextView nachoTextViews[] = {ingredientsAuthorised, ingredientsSoSo, ingredientsUnauthorised};
        for (NachoTextView nachoTextView : nachoTextViews) {
            nachoTextView.addChipTerminator(',', BEHAVIOR_CHIPIFY_CURRENT_TOKEN);
            nachoTextView.setNachoValidator(new ChipifyingNachoValidator());
            nachoTextView.enableEditChipOnTouch(false, true);
        }
    }

    private void loadAutoSuggestions() {
        DaoSession daoSession = OFFApplication.getInstance().getDaoSession();
        AsyncSession asyncSessionIngredients = daoSession.startAsyncSession();
        IngredientNameDao ingredientNameDao = daoSession.getIngredientNameDao();

        asyncSessionIngredients.queryList(ingredientNameDao.queryBuilder()
                .where(IngredientNameDao.Properties.LanguageCode.eq(appLanguageCode))
                .orderAsc(IngredientNameDao.Properties.Name).build());

        asyncSessionIngredients.setListenerMainThread(operation -> {
            @SuppressWarnings("unchecked")
            List<IngredientName> ingredientNames = (List<IngredientName>) operation.getResult();
            ingredients.clear();
            for (int i = 0; i < ingredientNames.size(); i++) {
                ingredients.add(ingredientNames.get(i).getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, ingredients);
            ingredientsAuthorised.setAdapter(adapter);
            ingredientsSoSo.setAdapter(adapter);
            ingredientsUnauthorised.setAdapter(adapter);
        });
    }

    //TODO Add a delete button witch suppress rows of DietIngredients, DietName and Diet
    //TODO Change name only change row and don't create a new one.
    //TODO Internationalisation made easy : en:Vegetarian, fr:Végétarien
    //TODO Export Diet and associated DietName, DietIngredients, Ingredients and IngredientName for sharing with friends and family (very usefull when you go and eat at a friend's house).
    //Others todos without direct link with this Class.
    //TODO Internationalised ingredients.
    //TODO Add a Red or Orange traffic Light on the first resume that appear when you scan a product if at list one ingredient is forbidden or at list one ingredient is so-so
}
