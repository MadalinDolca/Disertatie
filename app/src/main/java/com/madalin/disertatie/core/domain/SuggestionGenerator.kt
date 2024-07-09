package com.madalin.disertatie.core.domain

import android.graphics.Bitmap
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.madalin.disertatie.BuildConfig
import com.madalin.disertatie.core.domain.result.SuggestionResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SuggestionGenerator(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GENERATIVE_LANGUAGE_API_KEY
    )

    fun getActivitySuggestionsForLocation(info: String, images: List<Bitmap>) = flow {
        emit(SuggestionResult.Loading)

        val prompt = if (images.isNotEmpty()) {
            "List some outdoor activities that can be done in the place shown in the images. The activities must meet the following conditions: $info"
        } else {
            "List some outdoor activities that can be done in the place with the following details: $info"
        }
        Log.d("SuggestionGenerator", prompt)
        val inputContent = content {
            images.forEach { image(it) }
            text(prompt)
        }
        val response = generativeModel.generateContent(inputContent).text

        if (response != null) {
            emit(SuggestionResult.Success(response))
        } else {
            emit(SuggestionResult.Error)
        }
    }.flowOn(dispatcher)
}