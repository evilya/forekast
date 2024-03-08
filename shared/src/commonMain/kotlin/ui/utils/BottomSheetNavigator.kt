package ui.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.Stack
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.compositionUniqueId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias BottomSheetNavigatorContent = @Composable (bottomSheetNavigator: BottomSheetNavigator) -> Unit

val LocalBottomSheetNavigator: ProvidableCompositionLocal<BottomSheetNavigator> =
    staticCompositionLocalOf { error("BottomSheetNavigator not initialized") }

/**
 * Voyager doesn't provide navigator for Material3 bottom sheet
 * https://github.com/adrielcafe/voyager/issues/253
 */
@OptIn(InternalVoyagerApi::class)
@Composable
fun BottomSheetNavigator(
    modifier: Modifier = Modifier,
    skipPartiallyExpanded: Boolean = true,
    // todo implement back handling in bottom sheet
    hideOnBackPress: Boolean = true,
    key: String = compositionUniqueId(),
    sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = BottomSheetDefaults.Elevation,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    windowInsets: WindowInsets = BottomSheetDefaults.windowInsets,
    properties: ModalBottomSheetProperties = ModalBottomSheetDefaults.properties(),
    content: BottomSheetNavigatorContent,
) {
    var hideBottomSheet: (() -> Unit)? = null
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded,
        confirmValueChange = {
            if (it == SheetValue.Hidden) {
                hideBottomSheet?.invoke()
            }
            true
        },
    )
    val coroutineScope = rememberCoroutineScope()

    Navigator(HiddenBottomSheetScreen, onBackPressed = null, key = key) { navigator ->
        val bottomSheetNavigator = remember(navigator, sheetState, coroutineScope) {
            BottomSheetNavigator(sheetState, navigator, coroutineScope)
        }

        hideBottomSheet = bottomSheetNavigator::hide

        CompositionLocalProvider(LocalBottomSheetNavigator provides bottomSheetNavigator) {
            content(bottomSheetNavigator)

            if (bottomSheetNavigator.lastItemOrNull != HiddenBottomSheetScreen) {
                ModalBottomSheet(
                    modifier = modifier,
                    scrimColor = scrimColor,
                    sheetState = sheetState,
                    onDismissRequest = { hideBottomSheet?.invoke() },
                    shape = shape,
                    sheetMaxWidth = sheetMaxWidth,
                    containerColor = containerColor,
                    contentColor = contentColor,
                    tonalElevation = tonalElevation,
                    windowInsets = windowInsets,
                    properties = properties,
                    dragHandle = dragHandle,
                    content = { CurrentScreen() },
                )
            }
        }
    }
}

class BottomSheetNavigator(
    val sheetState: SheetState,
    private val navigator: Navigator,
    private val coroutineScope: CoroutineScope,
) : Stack<Screen> by navigator {

    val isVisible: Boolean
        get() = sheetState.isVisible

    fun show(screen: Screen) {
        coroutineScope.launch {
            replaceAll(screen)
            sheetState.show()
        }
    }

    fun hide() {
        coroutineScope.launch {
            if (isVisible) {
                sheetState.hide()
                replaceAll(HiddenBottomSheetScreen)
            } else if (sheetState.targetValue == SheetValue.Hidden) {
                replaceAll(HiddenBottomSheetScreen)
            }
        }
    }
}

private object HiddenBottomSheetScreen : Screen {

    @Composable
    override fun Content() {
    }
}
