package io.github.shadow578.yodel.util.storage

/**
 * a storage key, used by [StorageHelper]
 */
data class StorageKey(
    val key: String
) {

    override fun toString(): String {
        return key;
    }

    companion object {
        /**
         * a empty storage key
         */
        val EMPTY = StorageKey("")
    }
}