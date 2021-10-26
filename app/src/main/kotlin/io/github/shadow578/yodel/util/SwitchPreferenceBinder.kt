package io.github.shadow578.yodel.util

import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.github.shadow578.yodel.util.preferences.PreferenceWrapper

/**
 * a class to quickly bind a preference wrapper to a UI switch
 */
class SwitchPreferenceBinder(private val preference: PreferenceWrapper<Boolean>) {

    /**
     * backing livedata to handle preference changes
     */
    private val data = MutableLiveData(preference.get())

    /**
     * same as [data], but visible to the outside (and not mutable)
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val observableData: LiveData<Boolean>
        get() = data

    /**
     * function to update the value of the preference (and switch)
     *
     * @param newValue the new value of the preference
     */
    fun update(newValue: Boolean) {
        // do not update if equal value
        if (data.value == newValue) return

        // update the value and pref
        preference.set(newValue)
        data.value = newValue
    }

    /**
     * bind a [SwitchCompat] to the preference
     *
     * @param owner the lifecycle owner, like the activity
     * @param switch the switch instance
     */
    fun bind(owner: LifecycleOwner, switch: SwitchCompat) {
        // update value when switch changes
        switch.setOnCheckedChangeListener { _, checked -> update(checked) }

        // update switch when value changes
        observableData.observe(owner, { newValue -> switch.isChecked = newValue })
    }

}