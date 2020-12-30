package team.marker.view.breach.complete.photo

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_photo.*
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel
import team.marker.R
import team.marker.util.shortToast
import team.marker.view.breach.complete.BreachCompleteViewModel
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PhotoFragment : Fragment(R.layout.fragment_photo) {

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private lateinit var viewModel: BreachCompleteViewModel
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService
    private var isTorchEnable = false
    private var orient = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getSharedViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (grantedPermissions()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        flash_button.setOnClickListener {
            isTorchEnable = !isTorchEnable
            toggleTorch()
        }
        cancel_button.setOnClickListener { activity?.onBackPressed() }
        capture_button.setOnClickListener { takePhoto() }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantedPermissions()) {
                startCamera()
            } else {
                context?.shortToast(getString(R.string.no_camera_permissions))
                activity?.onBackPressed()
            }
        }
    }

    private fun grantedPermissions() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(preview_view.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            CameraSelector.LENS_FACING_BACK

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture

        val orientationEventListener = object : OrientationEventListener(requireContext()) {
            override fun onOrientationChanged(orientation: Int) {
                orient = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
            }
        }
        orientationEventListener.enable()

        val cw = ContextWrapper(requireContext())
        val directory = cw.getDir("imageDir", Context.MODE_PRIVATE)
        val photoFile = File(directory, "${UUID.randomUUID()}.jpg")


        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(e: ImageCaptureException) {
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    val resized = getResizedRotatedBitmap(bitmap)

                    val file = File(directory, "${UUID.randomUUID()}.jpg")
                    val outputStream: OutputStream = BufferedOutputStream(FileOutputStream(file))
                    resized.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)

                    outputStream.flush()
                    outputStream.close()
                    resized.recycle()
                    viewModel.addPhoto(file)

                    activity?.onBackPressed()
                }
            })
    }

    private fun toggleTorch() {
        val cameraProcessFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProcessFuture.addListener(Runnable {
            val cameraProvider = cameraProcessFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val camera = cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector)
            val cameraControl = camera.cameraControl
            cameraControl.enableTorch(isTorchEnable)
            flash_button.setImageResource(if (isTorchEnable) R.drawable.ic_flash_off_2 else R.drawable.ic_flash_2)
        }, ContextCompat.getMainExecutor(context))
    }

    private fun getResizedRotatedBitmap(bm: Bitmap): Bitmap {
        // vars
        val width = bm.width
        val height = bm.height
        val scaleWidth = 1600.toFloat() / width
        val scaleHeight = 1600.toFloat() / height
        val ratio = if (scaleWidth > scaleHeight) scaleHeight else scaleWidth
        val x = if (width > height) (width - height) / 2 else 0
        val y = if (width > height) 0 else (height - width) / 2
        val size = if (width > height) height else width
        // rotate
        var rotate = 0
        when (orient) {
            0 -> rotate = 90
            1 -> rotate = 0
            2 -> rotate = 270
            3 -> rotate = 180
        }
        // matrix
        val matrix = Matrix()
        matrix.postScale(ratio, ratio)
        matrix.postRotate(rotate.toFloat())
        // create
        val resizedBitmap = Bitmap.createBitmap(bm, 0, y.toInt(), size, size, matrix, true)
        bm.recycle()
        // output
        return resizedBitmap
    }
}