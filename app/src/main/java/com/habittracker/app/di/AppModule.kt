package com.habittracker.app.di

import com.google.firebase.auth.FirebaseAuth
import com.habittracker.app.data.local.CompletionDao
import com.habittracker.app.data.local.HabitDao
import com.habittracker.app.data.local.UserProfileDao
import com.habittracker.app.data.remote.FirestoreDataSource
import com.habittracker.app.data.repository.HabitRepository
import com.habittracker.app.data.repository.HabitRepositoryImpl
import com.habittracker.app.domain.usecase.GetBestStreakUseCase
import com.habittracker.app.domain.usecase.GetCompletionRateUseCase
import com.habittracker.app.domain.usecase.GetCurrentStreakUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHabitRepository(
        habitDao: HabitDao,
        completionDao: CompletionDao,
        userProfileDao: UserProfileDao,
        firestoreDataSource: FirestoreDataSource,
        auth: FirebaseAuth
    ): HabitRepository = HabitRepositoryImpl(
        habitDao, completionDao, userProfileDao, firestoreDataSource, auth
    )

    @Provides
    fun provideGetCurrentStreakUseCase() = GetCurrentStreakUseCase()

    @Provides
    fun provideGetBestStreakUseCase() = GetBestStreakUseCase()

    @Provides
    fun provideGetCompletionRateUseCase() = GetCompletionRateUseCase()
}
