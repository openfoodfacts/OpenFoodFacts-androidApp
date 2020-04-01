package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Locale;

import android.widget.CompoundButton;
import android.widget.Switch;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.models.Diet;
import openfoodfacts.github.scrachx.openfood.models.DietDao;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductIngredient;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.repositories.DietRepository;
import openfoodfacts.github.scrachx.openfood.repositories.IDietRepository;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import openfoodfacts.github.scrachx.openfood.views.adapters.DietIngredientsProductAdapter;

public class DietIngredientsProductFragment extends BaseFragment {

    Switch dietEnabled;
    RecyclerView ingredientsRV;
    DietIngredientsProductAdapter ingredientsRVAdapter;

    private Product product;
    private State mState;
    private String mDietTag;
    private Diet mDiet;
    private List<SpannableStringBuilder> mIngredients;
    private IDietRepository dietRepository;
    // Fetching of the (theoretical) language of input:
    //To be replaced with the product.getLang() !!!
    private String languageCode = Locale.getDefault().getLanguage();


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dietRepository = DietRepository.getInstance();

        Intent intent = getActivity().getIntent();
        mState = (State) intent.getExtras().getSerializable("state");
        product = mState.getProduct();
        Bundle parameters = getArguments();
        mDietTag = parameters.getString("dietTag");
        mDiet = dietRepository.getDietByTag(mDietTag);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //No diet in database, no fragment !!!
        DaoSession daoSession = OFFApplication.getInstance().getDaoSession();
        DietDao dietDao = daoSession.getDietDao();
        if (dietDao.loadAll().size() == 0) {
            return null;
        }
        return inflater.inflate(R.layout.fragment_diet_ingredients_product, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Intent intent = getActivity().getIntent();
        mState = (State) intent.getExtras().getSerializable("state");
        refreshView(mState);

        dietEnabled = view.findViewById(R.id.diet_enabled);
        dietEnabled.setChecked(mDiet.getEnabled());
        dietEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                IDietRepository dietRepository = DietRepository.getInstance();
                dietRepository.setDietEnabled(mDiet.getTag(), isChecked);
            }
        });

        ingredientsRV = (RecyclerView) view.findViewById(R.id.ingredients_recyclerView);
        ingredientsRV.setLayoutManager(new LinearLayoutManager(this.getContext()));
        DaoSession daoSession = OFFApplication.getInstance().getDaoSession();
        if (mState != null && product.getProductIngredients() != null) {
            languageCode = product.getLang();
            fillIngredients(coloredIngredientsFromProduct());
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && mIngredients != null){
            onChange();
        }
    }

    private void onChange() {
        mIngredients.clear();
        mIngredients.addAll(coloredIngredientsFromProduct());
        ingredientsRVAdapter.notifyDataSetChanged();
    }

    private List<SpannableStringBuilder> coloredIngredientsFromProduct() {
        return dietRepository.getColoredSSBFromProductAndDiet(product, mDietTag);
    }

    public void fillIngredients(List<SpannableStringBuilder> ingredients) {
        mIngredients = ingredients;
        ingredientsRVAdapter = new DietIngredientsProductAdapter(mIngredients, new ClickListener() {
            @Override
            public void onPositionClicked(int position, View v) {
                //addDietTagIngredients because languageCode of the product is not necessary languageCode of the Diet !
                String ingredientTag = "";
                List<ProductIngredient> productIngredients = product.getProductIngredients();
                for (int i = 0; i < productIngredients.size(); i++) {
                    ProductIngredient productIngredient =  productIngredients.get(i);
                    if (productIngredient.getText().replace("_","").equals(mIngredients.get(position).toString())) {
                        ingredientTag = productIngredient.getId();
                        //Sometimes ID passed from a product doesn't exists in the taxonomy from ingredient.json.
                        if (dietRepository.getIngredientByTag(productIngredient.getId()).getTag() == null) {
                            //That's it, create a new ingredient
                            dietRepository.addIngredient(productIngredient.getId(), productIngredient.getText(), productIngredient.getId().split(":")[0]);
                        }
                        break;
                    }
                }
                dietRepository.addDietIngredients(mDietTag, mIngredients.get(position).toString(), languageCode, stateFromView(v));
                onChange();
            }

            @Override
            public void onLongClicked(int position, View v) {
                String ingredientTag = "";
                List<ProductIngredient> productIngredients = product.getProductIngredients();
                for (int i = 0; i < productIngredients.size(); i++) {
                    ProductIngredient productIngredient =  productIngredients.get(i);
                    if (productIngredient.getText().replace("_","").equals(mIngredients.get(position).toString())) {
                        ingredientTag = productIngredient.getId();
                        //Sometimes ID passed from a product doesn't exists in the taxonomy from ingredient.json.
                        if (dietRepository.getIngredientByTag(productIngredient.getId()).getTag() == null) {
                            //That's it, create a new ingredient
                            dietRepository.addIngredient(productIngredient.getId(), productIngredient.getText(), productIngredient.getId().split(":")[0]);
                        }
                        break;
                    }
                }
                List<Diet> dietsEnabled = dietRepository.getDiets();
                for (int i = 0; i < dietsEnabled.size(); i++) {
                    Diet diet =  dietsEnabled.get(i);
                    dietRepository.addDietIngredients(diet.getTag(), mIngredients.get(position).toString(), languageCode, stateFromView(v));
                }
                onChange();
            }
        });
        ingredientsRV.setAdapter(ingredientsRVAdapter);
    }

    private int stateFromView(View v) {
        int state = DietRepository.DIET_STATE_UNKNOWN;
        String vName = v.toString().substring(v.toString().indexOf("state")+5);
        if (vName.equals("GreenImageButton}")) {
            state = DietRepository.DIET_STATE_AUTHORISED;
        } else if (vName.equals("OrangeImageButton}")) {
            state= DietRepository.DIET_STATE_SOSO;
        } else if (vName.equals("RedImageButton}")) {
            state= DietRepository.DIET_STATE_FORBIDEN;
        }
        return state;
    }

    @Override
    public void refreshView(State state) {
        super.refreshView(state);
        mState = state;
    }

    public interface ClickListener {
        void onPositionClicked(int position, View v);
        void onLongClicked(int position, View v);
    }
}
