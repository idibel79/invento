package ma.bam.inventaire.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import ma.bam.inventaire.data.export.SessionExporter
import ma.bam.inventaire.data.export.XlsxSessionExporter
import ma.bam.inventaire.data.importfile.ContentResolverImportFileSource
import ma.bam.inventaire.data.importfile.ImportFileSource
import ma.bam.inventaire.data.repository.InventoryRepository
import ma.bam.inventaire.data.repository.InventoryRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindInventoryRepository(impl: InventoryRepositoryImpl): InventoryRepository

    @Binds
    abstract fun bindSessionExporter(impl: XlsxSessionExporter): SessionExporter

    @Binds
    abstract fun bindImportFileSource(impl: ContentResolverImportFileSource): ImportFileSource
}
