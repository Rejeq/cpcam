package com.rejeq.cpcam.core.common

import kotlinx.collections.immutable.ImmutableList

inline fun <T : List<B>, B, C> T.mapToImmutableList(
    action: (B) -> C,
): ImmutableList<C> = buildPersistentList {
    this@mapToImmutableList.fastForEach {
        add(action(it))
    }
}

inline fun <T, R> List<T>.mapFilteredToImmutableList(
    filter: (T) -> Boolean,
    transform: (T) -> R,
): ImmutableList<R> = buildPersistentList {
    this@mapFilteredToImmutableList.fastForEach {
        if (filter(it)) {
            add(transform(it))
        }
    }
}

inline fun <T> List<T>.fastForEach(action: (T) -> Unit) {
    for (index in indices) {
        val item = get(index)
        action(item)
    }
}
