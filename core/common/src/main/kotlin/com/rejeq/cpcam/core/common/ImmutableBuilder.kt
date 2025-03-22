package com.rejeq.cpcam.core.common

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf

/**
 * Copied from this pull request:
 * https://github.com/Kotlin/kotlinx.collections.immutable/pull/166
 **/

/**
 * Builds a new [PersistentList] by populating a [PersistentList.Builder] using the given [builderAction]
 * and returning an immutable list with the same elements.
 *
 * The list passed as a receiver to the [builderAction] is valid only inside
 * that function.
 * Using it outside the function produces an unspecified behavior.
 */
@OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
inline fun <T> buildPersistentList(
    @BuilderInference builderAction: PersistentList.Builder<T>.() -> Unit,
): PersistentList<T> {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return persistentListOf<T>().builder().apply(builderAction).build()
}

/**
 * Builds a new [PersistentSet] by populating a [PersistentSet.Builder] using the given [builderAction]
 * and returning an immutable set with the same elements.
 *
 * The set passed as a receiver to the [builderAction] is valid only inside that
 * function.
 * Using it outside the function produces an unspecified behavior.
 *
 * Elements of the set are iterated in the order they were added by the
 * [builderAction].
 */
@OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
inline fun <T> buildPersistentSet(
    @BuilderInference builderAction: PersistentSet.Builder<T>.() -> Unit,
): PersistentSet<T> {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return persistentSetOf<T>().builder().apply(builderAction).build()
}

/**
 * Builds a new [PersistentSet] by populating a [PersistentSet.Builder] using the given [builderAction]
 * and returning an immutable set with the same elements.
 *
 * The set passed as a receiver to the [builderAction] is valid only inside that
 * function.
 * Using it outside the function produces an unspecified behavior.
 *
 * Order of the elements in the returned set is unspecified.
 */
@OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
inline fun <T> buildPersistentHashSet(
    @BuilderInference builderAction: PersistentSet.Builder<T>.() -> Unit,
): PersistentSet<T> {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return persistentHashSetOf<T>().builder().apply(builderAction).build()
}

/**
 * Builds a new [PersistentMap] by populating a [PersistentMap.Builder] using the given [builderAction]
 * and returning an immutable map with the same key-value pairs.
 *
 * The map passed as a receiver to the [builderAction] is valid only inside that
 * function.
 * Using it outside the function produces an unspecified behavior.
 *
 * Entries of the map are iterated in the order they were added by the
 * [builderAction].
 */
@OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
inline fun <K, V> buildPersistentMap(
    @BuilderInference builderAction: PersistentMap.Builder<K, V>.() -> Unit,
): PersistentMap<K, V> {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return persistentMapOf<K, V>().builder().apply(builderAction).build()
}

/**
 * Builds a new [PersistentMap] by populating a [PersistentMap.Builder] using the given [builderAction]
 * and returning an immutable map with the same key-value pairs.
 *
 * The map passed as a receiver to the [builderAction] is valid only inside that
 * function.
 * Using it outside the function produces an unspecified behavior.
 *
 * Order of the entries in the returned map is unspecified.
 */
@OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
inline fun <K, V> buildPersistentHashMap(
    @BuilderInference builderAction: PersistentMap.Builder<K, V>.() -> Unit,
): PersistentMap<K, V> {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return persistentHashMapOf<K, V>().builder().apply(builderAction).build()
}
