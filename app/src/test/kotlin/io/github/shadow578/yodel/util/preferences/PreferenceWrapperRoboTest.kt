package io.github.shadow578.yodel.util.preferences

import io.github.shadow578.yodel.RoboTest
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.junit.*

/**
 * robolectric tests for [PreferenceWrapper]
 */
class PreferenceWrapperRoboTest : RoboTest() {
    private lateinit var stringPref: PreferenceWrapper<String>
    private lateinit var booleanPref: PreferenceWrapper<Boolean>
    private lateinit var objectPref: PreferenceWrapper<CustomClass>

    @Before
    fun setupPrefs() {
        stringPref = PreferenceWrapper.create(String::class.java, "string_pref", "foo")
        booleanPref = PreferenceWrapper.create(Boolean::class.java, "boolean_pref", false)
        objectPref =
            PreferenceWrapper.create(CustomClass::class.java, "object_pref", CustomClass("bar"))

        stringPref.reset()
        booleanPref.reset()
        objectPref.reset()
    }

    @Test
    fun shouldHaveDefaultValues() {
        withClue("prefs are not set. they should return their default values")
        {
            stringPref.get() shouldBe "foo"
            booleanPref.get() shouldBe false
            objectPref.get() shouldBe CustomClass("bar")
        }
    }

    @Test
    fun shouldUseFallbackValues() {
        withClue("prefs are not set. they should return their defined fallback values")
        {
            stringPref.get("bar") shouldBe "bar"
            booleanPref.get(true) shouldBe true
            objectPref.get(CustomClass("yee")) shouldBe CustomClass("yee")
        }
    }

    @Test
    fun shouldApplySetValues() {
        withClue("prefs should set their values correctly")
        {
            // set
            stringPref.set("yee")
            booleanPref.set(true)
            objectPref.set(CustomClass("ree"))

            // check values
            stringPref.get() shouldBe "yee"
            booleanPref.get() shouldBe true
            objectPref.get() shouldBe CustomClass("ree")
        }
    }

    @Test
    fun shouldNotUseFallbackValues() {
        withClue("prefs that are set should not return their fallback values")
        {
            // set
            stringPref.set("yee")
            booleanPref.set(true)
            objectPref.set(CustomClass("ree"))

            // check values
            stringPref.get("ree") shouldBe "yee"
            booleanPref.get(false) shouldBe true
            objectPref.get(CustomClass("se_no")) shouldBe CustomClass("ree")
        }
    }

    @Test
    fun shouldResetValues() {
        withClue("prefs should reset their values to default")
        {
            // set
            stringPref.set("yee")
            booleanPref.set(true)
            objectPref.set(CustomClass("ree"))

            // check values
            stringPref.get() shouldBe "yee"
            booleanPref.get() shouldBe true
            objectPref.get() shouldBe CustomClass("ree")

            // reset
            stringPref.reset()
            booleanPref.reset()
            objectPref.reset()

            // check
            shouldHaveDefaultValues()
        }
    }

    data class CustomClass(val value: String)
}