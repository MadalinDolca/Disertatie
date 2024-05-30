package com.madalin.disertatie.core.domain

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.madalin.disertatie.BuildConfig
import com.madalin.disertatie.core.domain.result.SuggestionResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SuggestionGenerator(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GENERATIVE_LANGUAGE_API_KEY
    )

    fun getActivitySuggestions(details: String, images: List<Bitmap>): Flow<SuggestionResult> = flow {
        emit(SuggestionResult.Loading)

        val text = "Enumerate some activity recommendations to do in the place that has the following details: $details"

        val inputContent = content {
            images.forEach { image(it) }
            text(text)
        }

        val response = generativeModel.generateContent(inputContent).text
        if (response == null) {
            emit(SuggestionResult.Error)
        } else {
            emit(SuggestionResult.Success(response))
        }

    }.flowOn(dispatcher)
}
