/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package openfoodfacts.github.scrachx.openfood.features.login

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.analytics.AnalyticsEvent
import openfoodfacts.github.scrachx.openfood.analytics.MatomoAnalytics
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback
import openfoodfacts.github.scrachx.openfood.databinding.ActivityLoginBinding
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.utils.Utils
import openfoodfacts.github.scrachx.openfood.utils.getLoginPreferences
import java.io.IOException
import java.net.HttpCookie
import javax.inject.Inject

/**
 * A login screen that offers login via login/password.
 * This Activity connect to the Chrome Custom Tabs Service on startup to prefetch the url.
 */
@AndroidEntryPoint
class LoginActivity : BaseActivity() {
    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginActivityViewModel by viewModels()

    @Inject
    lateinit var productsApi: ProductsAPI

    @Inject
    lateinit var matomoAnalytics: MatomoAnalytics

    private val customTabActivityHelper: CustomTabActivityHelper by lazy { CustomTabActivityHelper() }
    private val userLoginUri by lazy { "${getString(R.string.website)}cgi/user.pl".toUri() }
    private val resetPasswordUri by lazy { "${getString(R.string.website)}cgi/reset_password.pl".toUri() }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            super.onBackPressed()
            true
        }
        else -> false
    }

    private fun doAttemptLogin() {
        // Disable login button
        viewModel.canLogIn.postValue(false)

        Utils.hideKeyboard(this)

        // Start checks
        val login = binding.loginInput.text.toString()
        val password = binding.passInput.text.toString()
        if (login.isBlank()) {
            binding.loginInput.error = getString(R.string.error_field_required)
            binding.loginInput.requestFocus()
            return
        }
        if (password.isBlank()) {
            binding.passInput.error = getString(R.string.error_field_required)
            binding.passInput.requestFocus()
            return
        } else if (password.length < 6) {
            binding.passInput.error = getText(R.string.error_invalid_password)
            binding.passInput.requestFocus()
            return
        }
        // End checks

        val loadingSnackbar = Snackbar.make(binding.loginLinearlayout, R.string.toast_retrieving, LENGTH_LONG)
            .apply { show() }
        binding.btnLogin.isClickable = false

        lifecycleScope.launch(Dispatchers.Main) {
            val response = withContext(Dispatchers.IO) {
                try {
                    productsApi.signIn(login, password, "Sign-in")
                } catch (err: Exception) {
                    Toast.makeText(this@LoginActivity, this@LoginActivity.getString(R.string.errorWeb), Toast.LENGTH_LONG).show()
                    Log.e(this::class.simpleName, "onFailure", err)

                    viewModel.canLogIn.postValue(true)
                    null
                }
            } ?: return@launch

            if (!response.isSuccessful) {
                Toast.makeText(this@LoginActivity, R.string.errorWeb, Toast.LENGTH_LONG).show()
                viewModel.canLogIn.postValue(true)
                return@launch
            }
            val htmlNoParsed = withContext(Dispatchers.IO) {
                try {
                    response.body()?.string()
                } catch (e: IOException) {
                    Log.e("LOGIN", "Unable to parse the login response page", e)
                    viewModel.canLogIn.postValue(true)
                    null
                }
            } ?: return@launch
            val pref = this@LoginActivity.getSharedPreferences("login", 0)
            if (isHtmlNotValid(htmlNoParsed)) {
                loadingSnackbar.dismiss()

                Snackbar.make(binding.loginLinearlayout, R.string.errorLogin, LENGTH_LONG).show()

                binding.txtInfoLogin.setTextColor(ContextCompat.getColor(this@LoginActivity, R.color.red))
                binding.txtInfoLogin.setText(R.string.txtInfoLoginNo)

                binding.passInput.setText("")
                viewModel.canLogIn.postValue(true)
            } else {
                // store the user session id (user_session and user_id)
                for (httpCookie in HttpCookie.parse(response.headers()["set-cookie"])) {
                    // Example format of set-cookie: session=user_session&S0MeR@nD0MSECRETk3Y&user_id&testuser; domain=.openfoodfacts.org; path=/
                    if (BuildConfig.HOST.contains(httpCookie.domain) && httpCookie.path == "/") {
                        httpCookie.value
                            .split("&")
                            .windowed(2, 2)
                            .forEach { (name, value) -> pref.edit { putString(name, value) } }
                        break
                    }
                }
                Snackbar.make(binding.loginLinearlayout, R.string.connection, LENGTH_LONG).show()
                pref.edit {
                    putString("user", login)
                    putString("pass", password)
                }
                binding.txtInfoLogin.setTextColor(ContextCompat.getColor(this@LoginActivity, R.color.green_500))
                binding.txtInfoLogin.setText(R.string.txtInfoLoginOk)

                matomoAnalytics.trackEvent(AnalyticsEvent.UserLogin)

                setResult(RESULT_OK)
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // check flavour and show helper text for user account
        if (!isFlavors(OFF)) {
            binding.txtLoginHelper.visibility = View.VISIBLE
        }

        // Setup view listeners
        binding.btnLogin.setOnClickListener { doAttemptLogin() }
        binding.btnCreateAccount.setOnClickListener { doRegister() }
        binding.btnForgotPass.setOnClickListener { doForgotPassword() }
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.txtSignIn)

        // Prefetch the uri
        customTabActivityHelper.setConnectionCallback(
            onConnected = { binding.btnCreateAccount.isEnabled = true },
            onDisconnected = { binding.btnCreateAccount.isEnabled = false }
        )
        customTabActivityHelper.mayLaunchUrl(userLoginUri, null, null)
        binding.btnCreateAccount.isEnabled = true

        val loginS = getLoginPreferences().getString(resources.getString(R.string.user), resources.getString(R.string.txt_anonymous))
        if (loginS == resources.getString(R.string.user)) {
            MaterialAlertDialogBuilder(this).apply {
                setTitle(R.string.log_in)
                setMessage(R.string.login_true)
                setNeutralButton(R.string.ok_button) { _, _ -> finish() }
            }.show()
        }
    }

    private fun doRegister() {
        // uncomment the below lines for native sign-up activity
        // startActivity(Intent(this,SignUpActivity::class.java))
        // finish()

        // comment the below lines if using native sign-up activity
        val customTabsIntent = CustomTabsHelper.getCustomTabsIntent(this, customTabActivityHelper.session)
        CustomTabActivityHelper.openCustomTab(this, customTabsIntent, userLoginUri, WebViewFallback())
    }

    private fun doForgotPassword() {
        val customTabsIntent = CustomTabsHelper.getCustomTabsIntent(this, customTabActivityHelper.session)
        CustomTabActivityHelper.openCustomTab(this, customTabsIntent, resetPasswordUri, WebViewFallback())
    }

    override fun onStart() {
        super.onStart()
        customTabActivityHelper.bindCustomTabsService(this)
    }

    override fun onStop() {
        super.onStop()
        customTabActivityHelper.unbindCustomTabsService(this)
        binding.btnCreateAccount.isEnabled = false
    }

    override fun onDestroy() {
        customTabActivityHelper.connectionCallback = null
        _binding = null
        super.onDestroy()
    }

    companion object {
        class LoginContract : ActivityResultContract<Unit?, Boolean>() {
            override fun createIntent(context: Context, input: Unit?) = Intent(context, LoginActivity::class.java)

            override fun parseResult(resultCode: Int, intent: Intent?) = resultCode == RESULT_OK
        }

        internal fun isHtmlNotValid(html: String?) = (html == null
                || html.contains("Incorrect user name or password.")
                || html.contains("See you soon!"))
    }
}
