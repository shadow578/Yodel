package io.github.shadow578.yodel.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * run a IO coroutine
 *
 * @param block coroutine block to run
 */
fun launchIO(block: suspend CoroutineScope.() -> Unit) =
    CoroutineScope(Dispatchers.IO).launch(block = block)


/**
 * run a coroutine in the main / UI thread
 *
 * @param block coroutine block to run
 */
fun launchMain(block: suspend CoroutineScope.() -> Unit) =
    CoroutineScope(Dispatchers.Main).launch(block = block)

/**
 * bride for [Optional] to kotlin nullables
 */
fun <T> Optional<T>.unwrap(): T? = orElse(null)
