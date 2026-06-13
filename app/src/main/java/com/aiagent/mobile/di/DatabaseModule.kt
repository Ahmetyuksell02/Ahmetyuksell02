package com.aiagent.mobile.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Provides Room database and DAO instances.
 * Database entity definitions, DAOs, and @Provides methods
 * are added in Phase 2 (Database layer).
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule
