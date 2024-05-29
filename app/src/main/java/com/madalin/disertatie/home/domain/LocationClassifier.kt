package com.madalin.disertatie.home.domain

import android.content.Context
import android.graphics.Bitmap
import com.madalin.disertatie.home.domain.model.LocationClassification
import com.madalin.disertatie.home.domain.model.LocationClassifications
import com.madalin.disertatie.home.domain.model.LocationType
import com.madalin.disertatie.home.domain.result.LocationClassificationResult
import com.madalin.disertatie.ml.Model
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Class used to classify the location of an [image].
 */
class LocationClassifier(
    private val applicationContext: Context,
    private var image: Bitmap
) {
    /**
     * The list of classes the model was trained on.
     */
    private val classes = arrayOf(
        LocationType.BEACH,
        LocationType.FOREST,
        LocationType.GARDEN,
        LocationType.LAKE,
        LocationType.MOUNTAIN,
        LocationType.PARK
    )

    /**
     * The width of the image the model was trained on.
     */
    private val trainedImageWidth = 300

    /**
     * The height of the image the model was trained on.
     */
    private val trainedImageHeight = 233

    /**
     * The scale factor used when rescaling the image.
     *
     * The images are already preprocessed in the model's rescaling layer. In case the model is
     * different and does not do this preprocessing and it is supposed to receive inputs from 0 to 1
     * the value should be 255.
     */
    private val rescaleFactor = 1

    /**
     * Rescales the image to the size the model was trained on and returns it.
     */
    private fun rescaleImage(): Bitmap {
        return Bitmap.createScaledBitmap(image, trainedImageWidth, trainedImageHeight, false)
    }

    /**
     * Maps the [confidence values][confidences] to the corresponding [classes] and returns a list
     * of [classified locations][LocationClassification].
     */
    private fun mapConfidencesToClassifications(confidences: FloatArray): List<LocationClassification> {
        val mappedLocations = mutableListOf<LocationClassification>()

        confidences.forEachIndexed { index, value ->
            val classifiedLocation = LocationClassification(classes[index], value)
            mappedLocations.add(classifiedLocation)
        }

        return mappedLocations
    }

    /**
     * Finds the most accurate classification from [locationsClassifications] and returns it
     * alongside the other classification results as a [LocationClassifications].
     */
    private fun buildClassificationResult(locationsClassifications: List<LocationClassification>): LocationClassifications {
        val topResult = locationsClassifications.maxBy { it.accuracy } // finds the classification with the highest confidence
        val otherResults = locationsClassifications.filter { it != topResult } // creates a list  without the top result

        return LocationClassifications(topResult, otherResults)
    }

    /**
     * Classifies the location of the [image] and emits the [results][LocationClassificationResult]
     * as a [Flow].
     */
    fun classifyImage(): Flow<LocationClassificationResult> = flow {
        emit(LocationClassificationResult.Loading)
        val scaledImage = rescaleImage()

        try {
            val model = Model.newInstance(applicationContext)

            // creates inputs for reference
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, trainedImageHeight, trainedImageWidth, 3), DataType.FLOAT32)
            val byteBuffer = ByteBuffer.allocateDirect(4 * trainedImageWidth * trainedImageHeight * 3) // 4 bytes per float, image size, 3 channels for each pixel

            byteBuffer.order(ByteOrder.nativeOrder())
            inputFeature0.loadBuffer(byteBuffer)

            // creates an array with the pixel values of the image
            val intValues = IntArray(trainedImageWidth * trainedImageHeight)
            scaledImage.getPixels(intValues, 0, scaledImage.width, 0, 0, scaledImage.width, scaledImage.height)

            // iterates over each pixel of the image, extracts their RGB values and adds those values individually to the byte buffer
            var pixel = 0
            for (i in 0 until trainedImageHeight) {
                for (j in 0 until trainedImageWidth) {
                    val value = intValues[pixel++] // RGB

                    byteBuffer.putFloat(((value shr 16) and 0xFF) * (1f / rescaleFactor))
                    byteBuffer.putFloat(((value shr 8) and 0xFF) * (1f / rescaleFactor))
                    byteBuffer.putFloat((value and 0xFF) * (1f / rescaleFactor))
                }
            }

            // runs model inference and gets result
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            val confidences = outputFeature0.floatArray

            // releases model resources if no longer used
            model.close()

            // emits the classification results
            val mappedConfidences = mapConfidencesToClassifications(confidences)
            val result = buildClassificationResult(mappedConfidences)

            emit(LocationClassificationResult.Success(result))

        } catch (exception: Exception) {
            emit(LocationClassificationResult.Error(exception.message.orEmpty()))
        }
    }
}