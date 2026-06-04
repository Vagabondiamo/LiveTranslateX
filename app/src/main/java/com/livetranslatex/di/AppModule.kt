package com.livetranslatex.di

import android.content.Context
import androidx.room.Room
import com.livetranslatex.BuildConfig
import com.livetranslatex.data.database.AppDatabase
import com.livetranslatex.data.database.TranslationHistoryDao
import com.livetranslatex.data.ocr.OcrEngine
import com.livetranslatex.data.translator.DeepLApi
import com.livetranslatex.data.translator.DeepLTranslator
import com.livetranslatex.data.translator.MlKitTranslator
import com.livetranslatex.data.translator.OpenAiApi
import com.livetranslatex.data.translator.OpenAiTranslator
import com.livetranslatex.data.translator.Translator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "livetranslatex.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideDao(db: AppDatabase): TranslationHistoryDao = db.translationHistoryDao()

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    @Provides
    @Singleton
    @Named("deepl")
    fun provideDeepLRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api-free.deepl.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("openai")
    fun provideOpenAiRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideDeepLApi(@Named("deepl") retrofit: Retrofit): DeepLApi =
        retrofit.create(DeepLApi::class.java)

    @Provides
    @Singleton
    fun provideOpenAiApi(@Named("openai") retrofit: Retrofit): OpenAiApi =
        retrofit.create(OpenAiApi::class.java)

    // Default: offline ML Kit translator. Can be swapped via SettingsViewModel.
    @Provides
    @Singleton
    fun provideTranslator(): Translator = MlKitTranslator()

    @Provides
    @Singleton
    fun provideOcrEngine(): OcrEngine = OcrEngine()
}
