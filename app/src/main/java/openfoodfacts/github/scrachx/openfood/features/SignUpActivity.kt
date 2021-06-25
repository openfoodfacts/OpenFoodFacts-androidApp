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
package openfoodfacts.github.scrachx.openfood.features

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback
import openfoodfacts.github.scrachx.openfood.databinding.ActivitySignupBinding
import openfoodfacts.github.scrachx.openfood.utils.Utils
import java.util.regex.Pattern

/**
 * A sign-up screen that offers sign-up via email/name/username/password.
 */

class SignUpActivity : AppCompatActivity() {
    private var _binding: ActivitySignupBinding? = null
    private val binding get() = _binding!!

    private lateinit var customTabActivityHelper: CustomTabActivityHelper

    private var termsOfServiceUri: Uri? = null

    private lateinit var pattern: Pattern

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        _binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        termsOfServiceUri = Uri.parse(getString(R.string.tos_url))

        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.signup)

        customTabActivityHelper = CustomTabActivityHelper()
        customTabActivityHelper.connectionCallback = object : CustomTabActivityHelper.ConnectionCallback {
            override fun onCustomTabsConnected() {
            }

            override fun onCustomTabsDisconnected() {
                //TODO find out what do do with it
            }
        }
        customTabActivityHelper.mayLaunchUrl(termsOfServiceUri, null, null)

        binding.tvTos.setOnClickListener {
            binding.checkboxTos.isChecked = !binding.checkboxTos.isChecked
        }
        binding.relFoodPro.setOnClickListener {
            binding.checkboxFoodPro.isChecked = !binding.checkboxFoodPro.isChecked
            producerNameVisibility()
        }
        binding.checkboxFoodPro.setOnClickListener {
            producerNameVisibility()
        }
        binding.relNewsLetter.setOnClickListener {
            binding.checkboxNewsLetter.isChecked = !binding.checkboxNewsLetter.isChecked
        }
        binding.btnSignup.setOnClickListener {
            doAttemptSignUp()
        }
        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        spanTos()
    }

    private fun doAttemptSignUp() {
        Utils.hideKeyboard(this)

        val name = binding.signupName.text.toString()
        val email = binding.signupInput.text.toString()
        val username = binding.signupUserName.text.toString()
        val password = binding.passInput.text.toString()
        val confirmPassword = binding.passConfirmInput.text.toString()

        if (name.isBlank()) {
            binding.signupName.error = getString(R.string.error_field_required)
            binding.signupName.requestFocus()
            return
        }
        if (email.isBlank()) {
            binding.signupInput.error = getString(R.string.error_field_required)
            binding.signupInput.requestFocus()
            return
        }
        pattern = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        if (!pattern.toRegex().matches(email)) {
            binding.signupInput.error = getText(R.string.invalid_email)
            binding.signupInput.requestFocus()
            return
        }
        if (username.isBlank()) {
            binding.signupUserName.error = getString(R.string.error_field_required)
            binding.signupUserName.requestFocus()
            return
        }
        pattern = Pattern.compile("^[a-z0-9]*$")
        if (!pattern.toRegex().matches(username)) {
            binding.signupUserName.error = getText(R.string.username_condition)
            binding.signupUserName.requestFocus()
            return
        }
        if (password.isBlank()) {
            binding.passInput.error = getString(R.string.error_field_required)
            binding.passInput.requestFocus()
            return
        }
        if (password.length < 6) {
            binding.passInput.error = getText(R.string.error_invalid_password)
            binding.passInput.requestFocus()
            return
        }
        if (confirmPassword.isBlank()) {
            binding.passConfirmInput.error = getString(R.string.error_field_required)
            binding.passConfirmInput.requestFocus()
            return
        }
        if (password != confirmPassword) {
            binding.passConfirmInput.error = getText(R.string.confirm_password_error)
            binding.passConfirmInput.requestFocus()
            return
        }
        if (!binding.checkboxTos.isChecked) {
            Toast.makeText(this, getString(R.string.acceptTos), Toast.LENGTH_SHORT).show()
            return
        }
        // End checks
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                super.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun spanTos() {
        val stringTos = getString(R.string.tos)
        val spannableString = SpannableString(stringTos)
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                openTermsOfService()
            }
        }
        spannableString.setSpan(clickableSpan, stringTos.indexOf("terms"), stringTos.indexOf("terms") + 29, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvTos.text = spannableString
        binding.tvTos.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun openTermsOfService() {
        val customTabsIntent = CustomTabsHelper.getCustomTabsIntent(baseContext, customTabActivityHelper.session)
        CustomTabActivityHelper.openCustomTab(this, customTabsIntent, termsOfServiceUri!!, WebViewFallback())
    }

    private fun producerNameVisibility() {
        if (binding.checkboxFoodPro.isChecked)
            binding.producerName.visibility = View.VISIBLE
        else
            binding.producerName.visibility = View.GONE
    }
}