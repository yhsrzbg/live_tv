package com.yhsrzbg.live_tv.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yhsrzbg.live_tv.data.LiveRepository
import com.yhsrzbg.live_tv.ui.state.MainViewModel

class MainViewModelFactory(
    private val repository: LiveRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository) as T
    }
}
