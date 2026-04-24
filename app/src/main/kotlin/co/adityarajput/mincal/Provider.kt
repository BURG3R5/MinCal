package co.adityarajput.mincal

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.adityarajput.mincal.viewmodels.HomeViewModel

object Provider {
    val Factory = viewModelFactory {
        initializer { HomeViewModel(this[AndroidViewModelFactory.APPLICATION_KEY] as MinCalApplication) }
    }
}
