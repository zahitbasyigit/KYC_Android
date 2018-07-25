package com.valensas.kyc_android.identitycamera.model.tensorflow


import android.content.res.AssetManager
import android.graphics.Bitmap
import android.support.v4.os.TraceCompat
import android.util.Log

import org.tensorflow.contrib.android.TensorFlowInferenceInterface

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.ArrayList
import java.util.Comparator
import java.util.PriorityQueue
import java.util.Vector

/**
 * A classifier specialized to label images using TensorFlow.
 */
class TensorFlowImageClassifier private constructor() : Classifier {

    // Config values.
    private var inputName: String? = null
    private var outputName: String? = null
    private var inputSize: Int = 0
    private var outputSize: Int = 0
    private var imageMean: Int = 0
    private var imageStd: Float = 0.toFloat()

    // Pre-allocated buffers.
    private val labels = Vector<String>()
    private var intValues: IntArray? = null
    private var floatValues: FloatArray? = null
    private var outputs: FloatArray? = null
    private var outputNames: Array<String>? = null

    private var inferenceInterface: TensorFlowInferenceInterface? = null

    private var runStats = false

    override val statString: String
        get() = inferenceInterface!!.statString

    override fun recognizeImage(bitmap: Bitmap): List<Classifier.Recognition> {
        // Log this method so that it can be analyzed with systrace.
        TraceCompat.beginSection("recognizeImage")

        TraceCompat.beginSection("preprocessBitmap")
        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        for (i in intValues!!.indices) {
            val `val` = intValues!![i]
            floatValues?.set(i * 3 + 0, ((`val` shr 16 and 0xFF) - imageMean) / imageStd)
            floatValues?.set(i * 3 + 1, ((`val` shr 8 and 0xFF) - imageMean) / imageStd)
            floatValues?.set(i * 3 + 2, ((`val` and 0xFF) - imageMean) / imageStd)
        }
        TraceCompat.endSection()

        // Copy the input data into TensorFlow.
        TraceCompat.beginSection("feed")
        inferenceInterface!!.feed(
                inputName!!, floatValues!!, *longArrayOf(1, inputSize.toLong(), outputSize.toLong(), 3))
        TraceCompat.endSection()

        // Run the inference call.
        TraceCompat.beginSection("run")
        inferenceInterface!!.run(outputNames!!, runStats)
        TraceCompat.endSection()

        // Copy the output Tensor back into the output array.
        TraceCompat.beginSection("fetch")
        inferenceInterface!!.fetch(outputName, outputs!!)
        TraceCompat.endSection()

        // Find the best classifications.
        val pq = PriorityQueue<Classifier.Recognition>(
                3,
                Comparator<Classifier.Recognition> { lhs, rhs ->
                    // Intentionally reversed to put high confidence at the head of the queue.
                    Float
                    java.lang.Float.compare(rhs.confidence!!, lhs.confidence!!)
                })
        for (i in outputs!!.indices) {
            if (outputs!![i] > THRESHOLD) {
                pq.add(
                        Classifier.Recognition(
                                "" + i, if (labels.size > i) labels[i] else "unknown", outputs!![i], null))
            }
        }
        val recognitions = ArrayList<Classifier.Recognition>()
        val recognitionsSize = Math.min(pq.size, MAX_RESULTS)
        for (i in 0 until recognitionsSize) {
            recognitions.add(pq.poll())
        }
        TraceCompat.endSection() // "recognizeImage"
        return recognitions
    }

    override fun enableStatLogging(debug: Boolean) {
        runStats = debug
    }

    override fun close() {
        inferenceInterface!!.close()
    }

    companion object {

        private val TAG = "ImageClassifier"

        // Only return this many results with at least this confidence.
        private val MAX_RESULTS = 1
        private val THRESHOLD = 0.75f

        /**
         * Initializes a native TensorFlow session for classifying images.
         *
         * @param assetManager  The asset manager to be used to load assets.
         * @param modelFilename The filepath of the model GraphDef protocol buffer.
         * @param labelFilename The filepath of label file for classes.
         * @param inputWidth    The input size. A square image of inputWidth x inputHeight is assumed.
         * @param inputHeight   The input size. A square image of inputWidth x inputHeight is assumed.
         * @param imageMean     The assumed mean of the image values.
         * @param imageStd      The assumed std of the image values.
         * @param inputName     The label of the image input node.
         * @param outputName    The label of the output node.
         * @throws IOException
         */
        @Throws(IOException::class)
        fun create(
                assetManager: AssetManager,
                modelFilename: String,
                labelFilename: String,
                inputWidth: Int,
                inputHeight: Int,
                imageMean: Int,
                imageStd: Float,
                inputName: String,
                outputName: String): Classifier {
            val c = TensorFlowImageClassifier()
            c.inputName = inputName
            c.outputName = outputName

            // Read the label names into memory.
            // TODO(andrewharp): make this handle non-assets.
            val actualFilename = labelFilename.split("file:///android_asset/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            Log.i(TAG, "Reading labels from: $actualFilename")
            var br: BufferedReader? = null
            br = BufferedReader(InputStreamReader(assetManager.open(actualFilename)))
            var line = br.readLine()
            while (line != null) {
                c.labels.add(line)
                line = br.readLine()
            }
            br.close()

            c.inferenceInterface = TensorFlowInferenceInterface(assetManager, modelFilename)
            // The shape of the output is [N, NUM_CLASSES], where N is the batch size.
            val numClasses = c.inferenceInterface!!.graph().operation(outputName).output<Any>(0).shape().size(1).toInt()
            Log.i(TAG, "Read " + c.labels.size + " labels, output layer size is " + numClasses)

            // Ideally, inputWidth could have been retrieved from the shape of the input operation.  Alas,
            // the placeholder node for input in the graphdef typically used does not specify a shape, so it
            // must be passed in as a parameter.
            c.inputSize = inputWidth
            c.outputSize = inputHeight
            c.imageMean = imageMean
            c.imageStd = imageStd

            // Pre-allocate buffers.
            c.outputNames = arrayOf(outputName)
            c.intValues = IntArray(inputWidth * inputHeight)
            c.floatValues = FloatArray(inputWidth * inputHeight * 3)
            c.outputs = FloatArray(numClasses)

            return c
        }
    }
}