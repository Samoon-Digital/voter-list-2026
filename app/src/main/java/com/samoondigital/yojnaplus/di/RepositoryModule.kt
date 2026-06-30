package com.samoondigital.yojnaplus.di

import com.samoondigital.yojnaplus.data.repository.ElectoralRepositoryImpl
import com.samoondigital.yojnaplus.domain.repository.ElectoralRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindElectoralRepository(
        impl: ElectoralRepositoryImpl,
    ): ElectoralRepository
}
