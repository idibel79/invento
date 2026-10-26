package ma.bam.inventaire.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ma.bam.inventaire.util.FeedbackUtil
import ma.bam.inventaire.util.ScanFeedback

@Module
@InstallIn(SingletonComponent::class)
abstract class FeedbackModule {

    @Binds
    abstract fun bindScanFeedback(impl: FeedbackUtil): ScanFeedback
}
