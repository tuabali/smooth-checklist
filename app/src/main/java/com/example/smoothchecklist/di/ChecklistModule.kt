package com.example.smoothchecklist.di

import com.example.smoothchecklist.data.ChecklistRepository
import com.example.smoothchecklist.data.InMemoryChecklistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChecklistModule {
    @Binds
    @Singleton
    abstract fun bindChecklistRepository(
        repository: InMemoryChecklistRepository
    ): ChecklistRepository
}
