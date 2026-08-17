package com.maarifa.app.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

/** Wraps a single "how do I build this ViewModel" lambda — one instance created per screen call site. */
class SimpleViewModelFactory<T : ViewModel>(private val creator: () -> T) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <U : ViewModel> create(modelClass: Class<U>): U = creator() as U
}

/** Every screen resolves the shared AppContainer this way, then builds its own ViewModel:
 *
 *   val container = maarifaContainer()
 *   val vm: LibraryViewModel = viewModel(factory = SimpleViewModelFactory { LibraryViewModel(container.materialRepository) })
 */
@Composable
fun maarifaContainer(): AppContainer = AppContainer.get(LocalContext.current)

