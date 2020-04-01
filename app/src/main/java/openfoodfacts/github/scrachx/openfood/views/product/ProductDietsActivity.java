package openfoodfacts.github.scrachx.openfood.views.product;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import com.google.android.material.tabs.TabLayout;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.DietIngredientsProductFragment;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.models.Diet;
import openfoodfacts.github.scrachx.openfood.models.DietDao;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.views.BaseActivity;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import openfoodfacts.github.scrachx.openfood.views.adapters.DietIngredientsProductFragmentPagerAdapter;
import openfoodfacts.github.scrachx.openfood.views.listeners.BottomNavigationListenerInstaller;

public class ProductDietsActivity extends BaseActivity {

	@BindView( R.id.pager )
    ViewPager viewPager;
	@BindView( R.id.toolbar )
    Toolbar toolbar;
	@BindView( R.id.tabs )
    TabLayout tabLayout;
    @BindView( R.id.bottom_navigation )
	BottomNavigationView bottomNavigationView;

    DietIngredientsProductFragmentPagerAdapter adapterResult;

    private State mState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.activity_product);
        setTitle(getString(R.string.app_name_long));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mState = (State) getIntent().getSerializableExtra("state" );

		setupViewPager( viewPager );

		tabLayout.setupWithViewPager( viewPager );

        BottomNavigationListenerInstaller.install(bottomNavigationView,this,this);
	}

	private void setupViewPager( ViewPager viewPager )
	{
        adapterResult = new DietIngredientsProductFragmentPagerAdapter(getSupportFragmentManager());
		adapterResult = setupViewPager(viewPager, adapterResult, mState, this);
    }

    /**
     * CAREFUL ! YOU MUST INSTANTIATE YOUR OWN ADAPTERRESULT BEFORE CALLING THIS METHOD
     * @param viewPager
     * @param adapterResult
     * @param mState
     * @param activity
     * @return
     */
    public static DietIngredientsProductFragmentPagerAdapter setupViewPager (ViewPager viewPager, DietIngredientsProductFragmentPagerAdapter adapterResult, State mState, Activity activity) {
        DaoSession daoSession = OFFApplication.getInstance().getDaoSession();
        DietDao dietDao = daoSession.getDietDao();
        List<Diet> dietList =  dietDao.loadAll();
        Collections.sort(dietList, new Comparator<Diet>() {
            @Override
            public int compare(Diet d1, Diet d2) {
                if (d1.getEnabled()) {
                    if (d2.getEnabled()) {
                        return d1.getTag().substring(3).compareToIgnoreCase(d2.getTag().substring(3));
                    } else {
                        return -1;
                    }
                } else if (d2.getEnabled()) {
                    return 1;
                } else {
                    return d1.getTag().substring(3).compareToIgnoreCase(d2.getTag().substring(3));
                }
            }
        });
        for (int i = 0; i < dietList.size(); i++) {
            Diet diet =  dietList.get(i);
            //Prepare a new dietIngredientsProductFragment and show it
            Bundle parameters = new Bundle();
            parameters.putString("dietTag", diet.getTag());
            DietIngredientsProductFragment dietIngredientsProductFragment = new DietIngredientsProductFragment();
            dietIngredientsProductFragment.setArguments(parameters);
            adapterResult.addFragment( dietIngredientsProductFragment, diet.getTag().substring(3));
        }

        viewPager.setAdapter(adapterResult);
        return adapterResult;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return onOptionsItemSelected(item, this);
    }

    public static boolean onOptionsItemSelected(MenuItem item, Activity activity) {
        // Respond to the action bar's Up/Home button
        if (item.getItemId() == android.R.id.home) {
            activity.finish();
        }
        return true;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        mState = (State) intent.getSerializableExtra("state");
        adapterResult.refresh(mState);
    }
}
