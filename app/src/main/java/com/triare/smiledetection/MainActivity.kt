package com.triare.smiledetection

import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.triare.smiledetection.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by viewBinding(CreateMethod.INFLATE)

    private lateinit var detector: FaceDetector

    private fun getBitmap() = BitmapFactory.decodeResource(resources, R.drawable.img_s_face)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initUi()
    }

    private fun initUi() {
        initDetector()
        initAction()
    }

    private fun initDetector() {
        detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .build()
        )
    }

    private fun initAction() {

        binding.action.setOnClickListener {
            val inputImage = InputImage.fromBitmap(getBitmap(), 0)

            detectingSmile(inputImage)
        }
    }

    private fun detectingSmile(inputImage: InputImage) {
        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                Toast.makeText(this, "Done", Toast.LENGTH_LONG).show()

                for (face in faces) {

                    val upperLipBottomContour =
                        face.getContour(FaceContour.UPPER_LIP_BOTTOM)?.points
                    val bottomLipTopContour = face.getContour(FaceContour.LOWER_LIP_TOP)?.points

                    val mergedPointsList = mutableListOf<PointF>()

                    mergedPointsList.addAll(upperLipBottomContour!!)
                    mergedPointsList.addAll(bottomLipTopContour!!)

                    eraseTeeth(mergedPointsList)
                }
            }
            .addOnFailureListener { e ->
                Log.d(MainActivity::class.java.simpleName, e.message!!)
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun eraseTeeth(pointsList: List<PointF>) {
        val croppedBitmap =
            Bitmap.createBitmap(getBitmap().width, getBitmap().height, Bitmap.Config.ARGB_8888)

        val imageView = binding.original
        imageView.adjustViewBounds = true
        imageView.setImageBitmap(croppedBitmap)

        val cropCanvas = Canvas(croppedBitmap)
        val path = Path()

        val paintFill = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
        }

        path.apply {
            fillType = Path.FillType.EVEN_ODD

            moveTo(pointsList[0].x, pointsList[0].y)

            pointsList.forEachIndexed { index, _ ->
                if (index != pointsList.size - 1) {
                    lineTo(pointsList[index + 1].x, pointsList[index + 1].y)
                } else {
                    lineTo(pointsList[0].x, pointsList[0].y)
                }
            }
            close()
        }

        cropCanvas.drawPath(path, paintFill)
        cropCanvas.drawBitmap(getBitmap(), 0f, 0f, paintFill)
        imageView.setImageBitmap(croppedBitmap)
    }
}
