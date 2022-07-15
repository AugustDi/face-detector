package com.triare.smiledetection

import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*
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

                    drawResult(upperLipBottomContour!!, bottomLipTopContour!!)

                    Log.d(
                        MainActivity::class.java.simpleName,
                        "upperLipBottomContour: $upperLipBottomContour"
                    )
                    Log.d(
                        MainActivity::class.java.simpleName,
                        "upperLipTopContour: $bottomLipTopContour"
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.d(MainActivity::class.java.simpleName, e.message!!)
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun drawResult(topLip: List<PointF>, bottomLip: List<PointF>) {

        val myOptions = BitmapFactory.Options()
        myOptions.inScaled = false
        myOptions.inDither = true
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888
        myOptions.inPurgeable = true

        val paintRed = Paint()
        paintRed.isAntiAlias = true
        paintRed.color = Color.RED

        val paintBlue = Paint()
        paintBlue.isAntiAlias = true
        paintBlue.color = Color.BLUE

        val workingBitmap = Bitmap.createBitmap(getBitmap())
        val mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas(mutableBitmap)

        topLip.onEach {
            canvas.drawCircle(it.x, it.y, 10f, paintRed)
        }

        bottomLip.onEach {
            canvas.drawCircle(it.x, it.y, 10f, paintBlue)
        }

        paintBlue.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

        val imageView = binding.original
        imageView.adjustViewBounds = true
        imageView.setImageBitmap(mutableBitmap)
    }
}