package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.preference.PreferenceManager;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.IOException;
import java.util.Locale;

import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.ResponseBody;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback;
import openfoodfacts.github.scrachx.openfood.databinding.FragmentHomeBinding;
import openfoodfacts.github.scrachx.openfood.models.Search;
import openfoodfacts.github.scrachx.openfood.models.TaglineLanguageModel;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.ITEM_HOME;

/**
 * @see R.layout#fragment_home
 */
public class HomeFragment extends NavigationBaseFragment implements CustomTabActivityHelper.ConnectionCallback {
    private FragmentHomeBinding binding;
    private ProductsAPI apiClient;
    private SharedPreferences sp;
    private String taglineURL;
    private CompositeDisposable compDisp = new CompositeDisposable();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.tvDailyFoodFact.setOnClickListener(v -> setDailyFoodFact());

        apiClient = new OpenFoodAPIClient(getActivity()).getRawAPI();
        checkUserCredentials();
        sp = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //stop the call to off to get total product counts:
        compDisp.dispose();
        binding = null;
    }

    private void setDailyFoodFact() {
        // chrome custom tab init
        CustomTabsIntent customTabsIntent;
        CustomTabActivityHelper customTabActivityHelper = new CustomTabActivityHelper();
        customTabActivityHelper.setConnectionCallback(this);
        Uri dailyFoodFactUri = Uri.parse(taglineURL);
        customTabActivityHelper.mayLaunchUrl(dailyFoodFactUri, null, null);

        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(),
            customTabActivityHelper.getSession());
        CustomTabActivityHelper.openCustomTab(requireActivity(),
            customTabsIntent, dailyFoodFactUri, new WebViewFallback());
    }

    @Override
    @NavigationDrawerType
    public int getNavigationDrawerType() {
        return ITEM_HOME;
    }

    private void checkUserCredentials() {
        final SharedPreferences settings = OFFApplication.getInstance().getSharedPreferences("login", 0);
        String login = settings.getString("user", "");
        String password = settings.getString("pass", "");

        if (!login.isEmpty() && !password.isEmpty()) {
            apiClient.signIn(login, password, "Sign-in").enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    String htmlNoParsed = null;
                    try {
                        htmlNoParsed = response.body().string();
                    } catch (IOException e) {
                        Log.e(HomeFragment.class.getSimpleName(), "signin", e);
                    }
                    if (htmlNoParsed != null && (htmlNoParsed.contains("Incorrect user name or password.")
                        || htmlNoParsed.contains("See you soon!"))) {
                        settings.edit()
                            .putString("user", "")
                            .putString("pass", "")
                            .apply();

                        if (getActivity() != null) {
                            new MaterialDialog.Builder(getActivity())
                                .title(R.string.alert_dialog_warning_title)
                                .content(R.string.alert_dialog_warning_msg_user)
                                .positiveText(R.string.txtOk)
                                .show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Log.e(HomeFragment.class.getName(), "Unable to Sign-in");
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        int productCount = sp.getInt("productCount", 0);
        refreshProductCount(productCount);

        refreshTagline();

        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("");
            }
        }
    }

    private void refreshProductCount(int oldCount) {
        apiClient.getTotalProductCount(Utils.getUserAgent())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new SingleObserver<Search>() {
                @Override
                public void onSubscribe(Disposable d) {
                    compDisp.add(d);
                    if (isAdded()) {
                        updateTextHome(oldCount);
                    }
                }

                @Override
                public void onSuccess(Search search) {
                    if (isAdded()) {
                        int totalProductCount = oldCount;
                        try {
                            totalProductCount = Integer.parseInt(search.getCount());
                        } catch (NumberFormatException e) {
                            Log.w(HomeFragment.class.getSimpleName(), "can parse " + search.getCount() + " as int", e);
                        }
                        updateTextHome(totalProductCount);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putInt("productCount", totalProductCount);
                        editor.apply();
                    }
                }

                @Override
                public void onError(Throwable e) {
                    if (isAdded()) {
                        updateTextHome(oldCount);
                    }
                }
            });
    }

    /**
     * Set text displayed on Home based on build variant
     *
     * @param totalProductCount count of total products available on the apps database
     */
    private void updateTextHome(int totalProductCount) {
        try {
            binding.textHome.setText(R.string.txtHome);
            if (totalProductCount != 0) {
                String txtHomeOnline = getResources().getString(R.string.txtHomeOnline);
                binding.textHome.setText(String.format(txtHomeOnline, totalProductCount));
            }
        } catch (Exception e) {
            Log.w(HomeFragment.class.getSimpleName(), "can format text for home", e);
        }
    }

    @Override
    public void onCustomTabsConnected() {

    }

    @Override
    public void onCustomTabsDisconnected() {

    }

    /**
     * get tag line url from OpenFoodAPIService
     */
    private void refreshTagline() {
        compDisp.add(apiClient.getTaglineSingle(Utils.getUserAgent())
            .subscribeOn(Schedulers.io()) // io for network
            .observeOn(AndroidSchedulers.mainThread()) // Move to main thread for UI changes
            .subscribe(models -> {
                final Locale locale = LocaleHelper.getLocale(getContext());
                String localAsString = locale.toString();
                boolean isLanguageFound = false;
                boolean isExactLanguageFound = false;

                for (TaglineLanguageModel tagLine : models) {
                    final String languageCountry = tagLine.getLanguage();
                    if (!isExactLanguageFound && (languageCountry.equals(localAsString) || languageCountry.contains(localAsString))) {
                        isExactLanguageFound = languageCountry.equals(localAsString);
                        taglineURL = tagLine.getTaglineModel().getUrl();
                        binding.tvDailyFoodFact.setText(tagLine.getTaglineModel().getMessage());
                        binding.tvDailyFoodFact.setVisibility(View.VISIBLE);
                        isLanguageFound = true;
                    }
                }

                if (!isLanguageFound) {
                    taglineURL = models.get(models.size() - 1).getTaglineModel().getUrl();
                    binding.tvDailyFoodFact.setText(models.get(models.size() - 1).getTaglineModel().getMessage());
                    binding.tvDailyFoodFact.setVisibility(View.VISIBLE);
                }
            }, e -> Log.w("getTagline", "cannot get tagline from server", e)));
    }
}
