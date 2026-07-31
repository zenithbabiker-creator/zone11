package com.example.homelandscape.ui

import com.example.homelandscape.R
import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PointF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.homelandscape.ar.ArSessionFacade
import com.example.homelandscape.databinding.ActivityCaptureBinding
import com.example.homelandscape.measure.MeasurementMode
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CaptureActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCaptureBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var arSessionFacade: ArSessionFacade
    private val measurementViewModel = MeasurementViewModel()

    private var imageCapture: ImageCapture? = null
    private var frozenBitmap: Bitmap? = null
    private var snapshotUri: Uri? = null
    private var imageWidth: Int = 0
    private var imageHeight: Int = 0
    private var selectedThicknessCm: Int = 5
    private var lastResult: com.example.homelandscape.measure.MeasurementResult? = null

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCamera()
            } else {
                Toast.makeText(this, R.string.permission_camera_denied, Toast.LENGTH_LONG).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCaptureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()
        arSessionFacade = ArSessionFacade(this)

        binding.textArBackend.text = getString(
            R.string.ar_backend_label,
            com.example.homelandscape.ar.ArEngineSelector.displayName(arSessionFacade.backend()),
        )

        setupThicknessDropdown()
        setupActions()

        if (hasCameraPermission()) {
            startCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun setupThicknessDropdown() {
        val labels = measurementViewModel.thicknessOptionsCm.map { getString(R.string.depth_option_cm, it) }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)
        binding.spinnerThickness.adapter = adapter
        binding.spinnerThickness.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedThicknessCm = measurementViewModel.thicknessOptionsCm[position]
                recalculateIfPossible()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun setupActions() {
        binding.buttonFreeze.setOnClickListener { captureSnapshot() }
        binding.buttonClearOutline.setOnClickListener {
            binding.polygonOverlay.clearPolygon()
            hideResults()
        }
        binding.buttonCalculate.setOnClickListener { calculateMeasurement() }
        binding.buttonSave.setOnClickListener { saveAnnotatedSnapshot() }
        binding.buttonRetake.setOnClickListener { retakePhoto() }

        binding.polygonOverlay.onPolygonChanged = { points ->
            binding.buttonCalculate.isEnabled = points.size >= 3 && frozenBitmap != null
        }
    }

    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        binding.previewView.visibility = View.VISIBLE
        binding.imageFrozen.visibility = View.GONE
        binding.polygonOverlay.clearPolygon()
        frozenBitmap = null
        hideResults()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = binding.previewView.surfaceProvider
            }
            imageCapture = ImageCapture.Builder().build()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture,
                )
            } catch (ex: Exception) {
                Toast.makeText(this, R.string.camera_start_failed, Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureSnapshot() {
        val capture = imageCapture ?: return
        val outputFile = File(cacheDir, "capture_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
        capture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val bitmap = android.graphics.BitmapFactory.decodeFile(outputFile.absolutePath)
                    runOnUiThread {
                        if (bitmap == null) {
                            Toast.makeText(this@CaptureActivity, R.string.capture_failed, Toast.LENGTH_SHORT).show()
                            return@runOnUiThread
                        }
                        showFrozen(bitmap, Uri.fromFile(outputFile))
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    runOnUiThread {
                        Toast.makeText(this@CaptureActivity, R.string.capture_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            },
        )
    }

    private fun showFrozen(bitmap: Bitmap, uri: Uri) {
        frozenBitmap = bitmap
        snapshotUri = uri
        imageWidth = bitmap.width
        imageHeight = bitmap.height
        binding.previewView.visibility = View.GONE
        binding.imageFrozen.visibility = View.VISIBLE
        binding.imageFrozen.setImageBitmap(bitmap)
        binding.polygonOverlay.clearPolygon()
        binding.buttonCalculate.isEnabled = false
        binding.buttonSave.isEnabled = false
        binding.buttonRetake.isEnabled = true
        hideResults()
    }

    private fun calculateMeasurement() {
        val points = binding.polygonOverlay.getPoints()
        if (points.size < 3 || imageWidth == 0 || imageHeight == 0) {
            Toast.makeText(this, R.string.outline_required, Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val depthProvider = arSessionFacade.ensureDepthProvider()
            val result = measurementViewModel.calculate(
                outlinePoints = points,
                imageWidth = imageWidth,
                imageHeight = imageHeight,
                depthProvider = depthProvider,
                selectedThicknessCm = selectedThicknessCm,
            )
            lastResult = result
            applyResultUi(result)
            binding.buttonSave.isEnabled = true
        } catch (ex: Exception) {
            Toast.makeText(this, ex.message ?: getString(R.string.calculation_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun recalculateIfPossible() {
        if (lastResult?.mode == MeasurementMode.FLAT_SURFACE && binding.polygonOverlay.getPoints().size >= 3) {
            calculateMeasurement()
        }
    }

    private fun applyResultUi(result: com.example.homelandscape.measure.MeasurementResult) {
        binding.panelResults.visibility = View.VISIBLE
        binding.textAreaValue.text = getString(R.string.result_area, result.displayArea)
        binding.textVolumeValue.text = getString(R.string.result_volume, result.displayVolume)

        when (result.mode) {
            MeasurementMode.EXCAVATION_HOLE -> {
                binding.textModeLabel.text = getString(R.string.mode_excavation)
                binding.textModeDescription.text = getString(R.string.mode_excavation_description)
                binding.labelThickness.visibility = View.GONE
                binding.spinnerThickness.visibility = View.GONE
            }
            MeasurementMode.FLAT_SURFACE -> {
                binding.textModeLabel.text = getString(R.string.mode_flat_surface)
                binding.textModeDescription.text = getString(R.string.mode_flat_description)
                binding.labelThickness.visibility = View.VISIBLE
                binding.spinnerThickness.visibility = View.VISIBLE
                val index = measurementViewModel.thicknessOptionsCm.indexOf(result.selectedThicknessCm ?: selectedThicknessCm)
                if (index >= 0) {
                    binding.spinnerThickness.setSelection(index)
                }
            }
        }
    }

    private fun hideResults() {
        binding.panelResults.visibility = View.GONE
        lastResult = null
    }

    private fun saveAnnotatedSnapshot() {
        val bitmap = frozenBitmap ?: return
        val annotated = binding.polygonOverlay.renderSnapshotWithOutline(bitmap)
        try {
            val savedUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "landscape_$name.jpg")
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/HomeLandscaping")
                }
                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: throw IllegalStateException("Unable to create MediaStore entry")
                contentResolver.openOutputStream(uri)?.use { out ->
                    annotated.compress(Bitmap.CompressFormat.JPEG, 92, out)
                }
                uri
            } else {
                @Suppress("DEPRECATION")
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val folder = File(dir, "HomeLandscaping")
                folder.mkdirs()
                val file = File(folder, "landscape_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    annotated.compress(Bitmap.CompressFormat.JPEG, 92, out)
                }
                Uri.fromFile(file)
            }
            Toast.makeText(this, getString(R.string.snapshot_saved, savedUri.toString()), Toast.LENGTH_LONG).show()
        } catch (ex: Exception) {
            Toast.makeText(this, R.string.snapshot_save_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun retakePhoto() {
        startCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        arSessionFacade.release()
    }
}
