package com.livetranslatex.di

import com.livetranslatex.data.ocr.OcrEngine
import com.livetranslatex.data.translator.TranslatorEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideOcrEngine(): OcrEngine = OcrEngine()

    @Provides @Singleton
    fun provideTranslatorEngine(): TranslatorEngine = TranslatorEngine()
}
