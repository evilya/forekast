package ui.core

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

data class AnimatedItem<T>(
    val item: T,
    val visibility: MutableTransitionState<Boolean> = MutableTransitionState(true),
) {
    override fun hashCode(): Int = item?.hashCode() ?: 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AnimatedItem<*>

        return item == other.item
    }
}

inline fun <T> LazyListScope.animatedItemsIndexed(
    state: List<AnimatedItem<T>>,
    enterTransition: EnterTransition = expandVertically(),
    exitTransition: ExitTransition = shrinkVertically(),
    noinline key: ((item: T) -> Any)? = null,
    crossinline itemContent: @Composable LazyItemScope.(index: Int, item: T) -> Unit,
) {
    items(
        state.size,
        if (key != null) { keyIndex: Int -> key(state[keyIndex].item) } else null,
    ) { index ->
        val item = state[index]
        val visibility = item.visibility

        key(key?.invoke(item.item)) {
            AnimatedVisibility(
                visibleState = visibility,
                enter = enterTransition,
                exit = exitTransition,
            ) {
                itemContent(index, item.item)
            }
        }
    }
}

// todo: rework with diffutil
@Composable
fun <T> updateAnimatedItemsState(newList: List<T>): State<List<AnimatedItem<T>>> {
    val state = remember { mutableStateOf(emptyList<AnimatedItem<T>>()) }
    LaunchedEffect(newList) {
        val oldList = state.value
        if (newList.size >= oldList.size) {
            state.value = newList.map(::AnimatedItem)
        } else {
            val newItems = newList.toSet()
            oldList.forEach {
                it.visibility.targetState = it.item in newItems
            }
            state.value = oldList
        }
    }
    return state
}
