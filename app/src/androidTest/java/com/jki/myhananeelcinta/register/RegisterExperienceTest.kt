package com.jki.myhananeelcinta.register

import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.AutoCompleteTextView
import android.graphics.Bitmap
import androidx.core.content.res.ResourcesCompat
import java.io.ByteArrayOutputStream
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import com.google.android.material.textfield.TextInputLayout
import com.jki.myhananeelcinta.R
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterExperienceTest {
    @Test fun captureScreensForVisualVerification() {
        ActivityScenario.launch(RegisterActivity::class.java).use { scenario ->
            capture("register-account")
            scenario.onActivity { activity ->
                val email = activity.findViewById<EditText>(R.id.et_email)
                email.requestFocus()
                (activity.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager)
                    .showSoftInput(email, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            }
            capture("register-keyboard")
            scenario.onActivity { activity ->
                activity.findViewById<EditText>(R.id.et_email).setText("maria@example.com")
                activity.findViewById<EditText>(R.id.et_password).setText("example-password")
                activity.findViewById<EditText>(R.id.et_confirmation).setText("example-password")
                activity.findViewById<View>(R.id.btn_continue).performClick()
            }
            capture("register-personal")
            scenario.onActivity { activity ->
                activity.findViewById<EditText>(R.id.et_name).setText("Maria Santoso")
                activity.findViewById<RadioGroup>(R.id.rg_gender).check(R.id.rb_female)
                activity.findViewById<View>(R.id.btn_continue).performClick()
            }
            capture("register-profile")
            scenario.onActivity { it.findViewById<View>(R.id.btn_continue).performClick() }
            capture("register-church")
            scenario.onActivity { it.findViewById<View>(R.id.btn_continue).performClick() }
            capture("register-family")
            scenario.onActivity { it.findViewById<View>(R.id.btn_continue).performClick() }
            capture("register-review")
        }
    }

    private fun capture(name: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        // Accessibility idle also gives the compositor time to display the newly selected panel.
        instrumentation.uiAutomation.waitForIdle(300, 5000)
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        assertNotNull(bitmap)
        File(instrumentation.targetContext.getExternalFilesDir(null), "$name.png").outputStream().use {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }

    @Test fun emptyFormShowsInlineErrorsAndStaysOnAccountStep() {
        ActivityScenario.launch(RegisterActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.btn_continue).performClick()
                assertNotNull(activity.findViewById<TextInputLayout>(R.id.til_email).error)
                assertNotNull(activity.findViewById<TextInputLayout>(R.id.til_password).error)
                assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.panel_account).visibility)
            }
        }
    }

    @Test fun categorizedFlowKeepsAllLegacyFieldsAndPhotoOnActivityRecreation() {
        ActivityScenario.launch(RegisterActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<EditText>(R.id.et_email).setText("maria@example.com")
                activity.findViewById<EditText>(R.id.et_password).setText("example-password")
                activity.findViewById<EditText>(R.id.et_confirmation).setText("example-password")
                activity.findViewById<View>(R.id.btn_continue).performClick()
                assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.panel_personal).visibility)
                activity.findViewById<EditText>(R.id.et_name).setText("Maria Santoso")
                activity.findViewById<RadioGroup>(R.id.rg_gender).check(R.id.rb_female)
                activity.findViewById<View>(R.id.btn_continue).performClick()
                assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.panel_profile).visibility)
                activity.findViewById<AutoCompleteTextView>(R.id.et_blood).setText("AB", false)
                activity.findViewById<AutoCompleteTextView>(R.id.et_education).setText("S1", false)
                activity.findViewById<EditText>(R.id.et_address).setText("Jl. Cinta 1")
                activity.findViewById<View>(R.id.btn_continue).performClick()
                assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.panel_church).visibility)
                activity.findViewById<EditText>(R.id.et_baptism_church).setText("Gereja baptis")
                activity.findViewById<AutoCompleteTextView>(R.id.et_holy_spirit).setText("Sudah", false)
                activity.findViewById<EditText>(R.id.et_church_origin).setText("Gereja asal")
                activity.findViewById<EditText>(R.id.et_moving_reason).setText("Pindah kota")
                activity.findViewById<View>(R.id.btn_continue).performClick()
                assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.panel_family).visibility)
                activity.findViewById<AutoCompleteTextView>(R.id.et_family_status).setText("Istri", false)
                activity.findViewById<EditText>(R.id.et_husband).setText("Andi")
                activity.findViewById<EditText>(R.id.et_wife).setText("Maria")
                activity.findViewById<EditText>(R.id.et_children).setText("Anak satu\nAnak dua")
                activity.findViewById<EditText>(R.id.et_siblings).setText("Saudara satu\nSaudara dua")
                activity.findViewById<View>(R.id.btn_continue).performClick()
                assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.panel_review).visibility)
                assertEquals("Maria Santoso", activity.findViewById<TextView>(R.id.tv_review_name).text.toString())
                assertTrue(activity.findViewById<TextView>(R.id.tv_review_details).text.contains("AB"))
                assertTrue(activity.findViewById<TextView>(R.id.tv_review_details).text.contains("Saudara dua"))
                val titleFace = activity.findViewById<TextView>(R.id.tv_title).typeface
                assertEquals(android.graphics.Typeface.create(ResourcesCompat.getFont(activity, R.font.bold), titleFace.style), titleFace)
                // Exercise the same image handling used by CameraActivity's result without creating an account.
                val image = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
                val bytes = ByteArrayOutputStream().also { image.compress(Bitmap.CompressFormat.JPEG, 90, it) }.toByteArray()
                image.recycle()
                RegisterActivity::class.java.getDeclaredMethod("saveCapturedPhoto", ByteArray::class.java).apply { isAccessible = true }.invoke(activity, bytes)
                assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.btn_remove_photo).visibility)
                assertEquals(activity.getString(R.string.reg_create), activity.findViewById<TextView>(R.id.btn_continue).text.toString())
            }
            scenario.recreate()
            scenario.onActivity { activity ->
                assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.panel_review).visibility)
                assertEquals("AB", activity.findViewById<EditText>(R.id.et_blood).text.toString())
                assertEquals("S1", activity.findViewById<EditText>(R.id.et_education).text.toString())
                assertEquals("Gereja baptis", activity.findViewById<EditText>(R.id.et_baptism_church).text.toString())
                assertEquals("Sudah", activity.findViewById<EditText>(R.id.et_holy_spirit).text.toString())
                assertEquals("Gereja asal", activity.findViewById<EditText>(R.id.et_church_origin).text.toString())
                assertEquals("Pindah kota", activity.findViewById<EditText>(R.id.et_moving_reason).text.toString())
                assertEquals("Istri", activity.findViewById<EditText>(R.id.et_family_status).text.toString())
                assertEquals("Andi", activity.findViewById<EditText>(R.id.et_husband).text.toString())
                assertEquals("Maria", activity.findViewById<EditText>(R.id.et_wife).text.toString())
                assertEquals("Anak satu\nAnak dua", activity.findViewById<EditText>(R.id.et_children).text.toString())
                assertEquals("Saudara satu\nSaudara dua", activity.findViewById<EditText>(R.id.et_siblings).text.toString())
                assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.btn_remove_photo).visibility)
                activity.findViewById<View>(R.id.btn_remove_photo).performClick()
                assertEquals(View.GONE, activity.findViewById<View>(R.id.btn_remove_photo).visibility)
                activity.findViewById<View>(R.id.btn_back).performClick()
                assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.panel_family).visibility)
                assertEquals("Maria Santoso", activity.findViewById<EditText>(R.id.et_name).text.toString())
                assertEquals("example-password", activity.findViewById<EditText>(R.id.et_password).text.toString())
                assertEquals(R.id.rb_female, activity.findViewById<RadioGroup>(R.id.rg_gender).checkedRadioButtonId)
            }
        }
    }
}
