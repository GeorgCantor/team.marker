package team.marker.util.scanner

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.TextureView.SurfaceTextureListener
import com.google.zxing.client.android.R
import com.journeyapps.barcodescanner.RotationCallback
import com.journeyapps.barcodescanner.RotationListener
import com.journeyapps.barcodescanner.Size
import com.journeyapps.barcodescanner.Util
import com.journeyapps.barcodescanner.camera.*
import java.util.*

/**
 * CameraPreview is a view that handles displaying of a camera preview on a SurfaceView. It is
 * intended to be used as a base for realtime processing of camera images, e.g. barcode decoding
 * or OCR, although none of this happens in CameraPreview itself.
 *
 * The camera is managed on a separate thread, using CameraInstance.
 *
 * Two methods MUST be called on CameraPreview to manage its state:
 * 1. resume() - initialize the camera and start the preview. Call from the Activity's onResume().
 * 2. pause() - stop the preview and release any resources. Call from the Activity's onPause().
 *
 * Startup sequence:
 *
 * 1. Create SurfaceView.
 * 2. open camera.
 * 2. layout this container, to get size
 * 3. set display config, according to the container size
 * 4. configure()
 * 5. wait for preview size to be ready
 * 6. set surface size according to preview size
 * 7. set surface and start preview
 */
open class ScannerCameraPreview : ViewGroup {
    interface StateListener {
        fun previewSized()
        fun previewStarted()
        fun previewStopped()
        fun cameraError(error: Exception?)
        fun cameraClosed()
    }

    var cameraInstance: CameraInstance? = null
    private var windowManager: WindowManager? = null
    private var stateHandler: Handler? = null

    var isUseTextureView = false
    private var surfaceView: SurfaceView? = null
    private var textureView: TextureView? = null

    var isPreviewActive = false
    private var rotationListener: RotationListener? = null
    private var openedOrientation = -1
    private val stateListeners: MutableList<StateListener> = ArrayList()
    private var displayConfiguration: DisplayConfiguration? = null

    var cameraSettings = CameraSettings()

    // Size of this container, non-null after layout is performed
    private var containerSize: Size? = null

    // Size of the preview resolution
    private var previewSize: Size? = null

    // Rect placing the preview surface
    private var surfaceRect: Rect? = null

    // Size of the current surface. non-null if the surface is ready
    private var currentSurfaceSize: Size? = null

    // Framing rectangle relative to this view
    var framingRect: Rect? = null

    // Framing rectangle relative to the preview resolution
    var previewFramingRect: Rect? = null

    // Size of the framing rectangle. If null, defaults to using a margin percentage.
    var framingRectSize: Size? = null

    // Fraction of the width / heigth to use as a margin. This fraction is used on each size, so
    // must be smaller than 0.5;
    private var marginFraction = 0.1
    private var previewScalingStrategy: PreviewScalingStrategy? = null
    private var torchOn = false

    @TargetApi(14)
    private fun surfaceTextureListener(): SurfaceTextureListener {
        // Cannot initialize automatically, since we may be API < 14
        return object : SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                onSurfaceTextureSizeChanged(surface, width, height)
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                currentSurfaceSize = Size(width, height)
                startPreviewIfReady()
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
    }

    private val surfaceCallback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {}
        override fun surfaceDestroyed(holder: SurfaceHolder) {
            currentSurfaceSize = null
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            currentSurfaceSize = Size(width, height)
            startPreviewIfReady()
        }
    }
    private val stateCallback = Handler.Callback { message ->
        if (message.what == R.id.zxing_prewiew_size_ready) {
            previewSized(message.obj as Size)
            return@Callback true
        } else if (message.what == R.id.zxing_camera_error) {
            val error = message.obj as Exception
            if (isActive) {
                // This check prevents multiple errors from begin passed through.
                pause()
                fireState.cameraError(error)
            }
        } else if (message.what == R.id.zxing_camera_closed) {
            fireState.cameraClosed()
        }
        false
    }
    private val rotationCallback = RotationCallback { // Make sure this is run on the main thread.
        stateHandler!!.postDelayed({ rotationChanged() }, ROTATION_LISTENER_DELAY_MS.toLong())
    }

    constructor(context: Context) : super(context) {
        initialize(context, null, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context, attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(context, attrs, defStyleAttr, 0)
    }

    private fun initialize(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        if (background == null) {
            // Default to SurfaceView colour, so that there are less changes.
            setBackgroundColor(Color.BLACK)
        }
        initializeAttributes(attrs)
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        stateHandler = Handler(stateCallback)
        rotationListener = RotationListener()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setupSurfaceView()
    }

    fun initializeAttributes(attrs: AttributeSet?) {
        val styledAttributes =
            context.obtainStyledAttributes(attrs, R.styleable.zxing_camera_preview)
        val framingRectWidth = styledAttributes.getDimension(
            R.styleable.zxing_camera_preview_zxing_framing_rect_width,
            -1f
        ).toInt()
        val framingRectHeight = styledAttributes.getDimension(
            R.styleable.zxing_camera_preview_zxing_framing_rect_height,
            -1f
        ).toInt()
        if (framingRectWidth > 0 && framingRectHeight > 0) {
            framingRectSize =
                Size(framingRectWidth, framingRectHeight)
        }
        isUseTextureView = styledAttributes.getBoolean(
            R.styleable.zxing_camera_preview_zxing_use_texture_view,
            true
        )

        val scalingStrategyNumber = styledAttributes.getInteger(R.styleable.zxing_camera_preview_zxing_preview_scaling_strategy, -1)
        if (scalingStrategyNumber == 1) {
            previewScalingStrategy = CenterCropStrategy()
        } else if (scalingStrategyNumber == 2) {
            previewScalingStrategy = FitCenterStrategy()
        } else if (scalingStrategyNumber == 3) {
            previewScalingStrategy = FitXYStrategy()
        }
        styledAttributes.recycle()
    }

    private fun rotationChanged() {
        if (isActive && displayRotation != openedOrientation) {
            pause()
            resume()
        }
    }

    @SuppressLint("NewAPI")
    private fun setupSurfaceView() {
        textureView = TextureView(context)
        textureView!!.surfaceTextureListener = surfaceTextureListener()
        addView(textureView)
    }

    fun addStateListener(listener: StateListener) {
        stateListeners.add(listener)
    }

    private val fireState: StateListener = object :
        StateListener {
        override fun previewSized() {
            for (listener in stateListeners) {
                listener.previewSized()
            }
        }

        override fun previewStarted() {
            for (listener in stateListeners) {
                listener.previewStarted()
            }
        }

        override fun previewStopped() {
            for (listener in stateListeners) {
                listener.previewStopped()
            }
        }

        override fun cameraError(error: Exception?) {
            for (listener in stateListeners) {
                listener.cameraError(error)
            }
        }

        override fun cameraClosed() {
            for (listener in stateListeners) {
                listener.cameraClosed()
            }
        }
    }

    private fun calculateFrames() {
        if (containerSize == null || previewSize == null || displayConfiguration == null) {
            previewFramingRect = null
            framingRect = null
            surfaceRect = null
            throw IllegalStateException("containerSize or previewSize is not set yet")
        }
        val previewWidth = previewSize!!.width
        val previewHeight = previewSize!!.height
        val width = containerSize!!.width
        val height = containerSize!!.height
        surfaceRect = displayConfiguration!!.scalePreview(previewSize)
        val container = Rect(0, 0, width, height)
        framingRect = calculateFramingRect(container, surfaceRect)
        val frameInPreview = Rect(framingRect)
        frameInPreview.offset(-surfaceRect!!.left, -surfaceRect!!.top)
        previewFramingRect = Rect(
            frameInPreview.left * previewWidth / surfaceRect!!.width(),
            frameInPreview.top * previewHeight / surfaceRect!!.height(),
            frameInPreview.right * previewWidth / surfaceRect!!.width(),
            frameInPreview.bottom * previewHeight / surfaceRect!!.height()
        )
        if (previewFramingRect!!.width() <= 0 || previewFramingRect!!.height() <= 0) {
            previewFramingRect = null
            framingRect = null
            Log.w(TAG, "Preview frame is too small")
        } else {
            fireState.previewSized()
        }
    }

    fun setTorch(on: Boolean) {
        torchOn = on
        if (cameraInstance != null) {
            cameraInstance!!.setTorch(on)
        }
    }

    private fun containerSized(containerSize: Size) {
        this.containerSize = containerSize
        if (cameraInstance != null) {
            if (cameraInstance!!.displayConfiguration == null) {
                displayConfiguration = DisplayConfiguration(displayRotation, containerSize)
                displayConfiguration!!.previewScalingStrategy = getPreviewScalingStrategy()
                cameraInstance!!.displayConfiguration = displayConfiguration
                cameraInstance!!.configureCamera()
                if (torchOn) {
                    cameraInstance!!.setTorch(torchOn)
                }
            }
        }
    }

    fun setPreviewScalingStrategy(previewScalingStrategy: PreviewScalingStrategy?) {
        this.previewScalingStrategy = previewScalingStrategy
    }

    fun getPreviewScalingStrategy(): PreviewScalingStrategy {
        if (previewScalingStrategy != null) return previewScalingStrategy as PreviewScalingStrategy

        // If we are using SurfaceTexture, it is safe to use centerCrop.
        // For SurfaceView, it's better to use fitCenter, otherwise the preview may overlap to
        // other views.
        return if (textureView != null) CenterCropStrategy() else FitCenterStrategy()
    }

    private fun previewSized(size: Size) {
        previewSize = size
        if (containerSize != null) {
            calculateFrames()
            requestLayout()
            startPreviewIfReady()
        }
    }

    /**
     * Calculate transformation for the TextureView.
     *
     * An identity matrix would cause the preview to be scaled up/down to fill the TextureView.
     *
     * @param textureSize the size of the textureView
     * @param previewSize the camera preview resolution
     * @return the transform matrix for the TextureView
     */
    protected fun calculateTextureTransform(
        textureSize: Size,
        previewSize: Size
    ): Matrix {
        val ratioTexture =
            textureSize.width.toFloat() / textureSize.height.toFloat()
        val ratioPreview =
            previewSize.width.toFloat() / previewSize.height.toFloat()
        val scaleX: Float
        val scaleY: Float

        // We scale so that either width or height fits exactly in the TextureView, and the other
        // is bigger (cropped).
        if (ratioTexture < ratioPreview) {
            scaleX = ratioPreview / ratioTexture
            scaleY = 1f
        } else {
            scaleX = 1f
            scaleY = ratioTexture / ratioPreview
        }
        val matrix = Matrix()
        matrix.setScale(scaleX, scaleY)

        // Center the preview
        val scaledWidth = textureSize.width * scaleX
        val scaledHeight = textureSize.height * scaleY
        val dx = (textureSize.width - scaledWidth) / 2
        val dy = (textureSize.height - scaledHeight) / 2

        // Perform the translation on the scaled preview
        matrix.postTranslate(dx, dy)
        return matrix
    }

    private fun startPreviewIfReady() {
        //Log.e("scan", "resume")
        if (currentSurfaceSize != null && previewSize != null && surfaceRect != null) {
            if (surfaceView != null && currentSurfaceSize == Size(surfaceRect!!.width(), surfaceRect!!.height())) {
                startCameraPreview(CameraSurface(surfaceView!!.holder))
            } else if (textureView != null && Build.VERSION.SDK_INT >= 14 && textureView!!.surfaceTexture != null) {
                if (previewSize != null) {
                    val transform = calculateTextureTransform(Size(textureView!!.width, textureView!!.height), previewSize!!)
                    textureView!!.setTransform(transform)
                }
                startCameraPreview(CameraSurface(textureView!!.surfaceTexture))
            } else {
                // Surface is not the correct size yet
            }
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(
        changed: Boolean,
        l: Int,
        t: Int,
        r: Int,
        b: Int
    ) {
        containerSized(Size(r - l, b - t))
        if (surfaceView != null) {
            if (surfaceRect == null) {
                // Match the container, to reduce the risk of issues. The preview should never be drawn
                // while the surface has this size.
                surfaceView!!.layout(0, 0, width, height)
            } else {
                surfaceView!!.layout(
                    surfaceRect!!.left,
                    surfaceRect!!.top,
                    surfaceRect!!.right,
                    surfaceRect!!.bottom
                )
            }
        } else if (textureView != null && Build.VERSION.SDK_INT >= 14) {
            textureView!!.layout(0, 0, width, height)
        }
    }

    /**
     * Start the camera preview and decoding. Typically this should be called from the Activity's
     * onResume() method.
     *
     * Call from UI thread only.
     */
    fun resume() {
        // This must be safe to call multiple times
        Util.validateMainThread()
        Log.d(TAG, "resume()")

        // initCamera() does nothing if called twice, but does log a warning
        initCamera()
        if (currentSurfaceSize != null) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            startPreviewIfReady()
        } else if (surfaceView != null) {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceView!!.holder.addCallback(surfaceCallback)
        } else if (textureView != null && Build.VERSION.SDK_INT >= 14) {
            if (textureView!!.isAvailable) {
                surfaceTextureListener().onSurfaceTextureAvailable(
                    textureView!!.surfaceTexture,
                    textureView!!.width,
                    textureView!!.height
                )
            } else {
                textureView!!.surfaceTextureListener = surfaceTextureListener()
            }
        }

        // To trigger surfaceSized again
        requestLayout()
        rotationListener!!.listen(context, rotationCallback)
    }

    /**
     * Pause scanning and the camera preview. Typically this should be called from the Activity's
     * onPause() method.
     *
     * Call from UI thread only.
     */
    open fun pause() {
        // This must be safe to call multiple times.
        Util.validateMainThread()
        Log.d(TAG, "pause()")
        openedOrientation = -1
        if (cameraInstance != null) {
            cameraInstance!!.close()
            cameraInstance = null
            isPreviewActive = false
        } else {
            stateHandler!!.sendEmptyMessage(R.id.zxing_camera_closed)
        }
        if (currentSurfaceSize == null && surfaceView != null) {
            val surfaceHolder = surfaceView!!.holder
            surfaceHolder.removeCallback(surfaceCallback)
        }
        if (currentSurfaceSize == null && textureView != null && Build.VERSION.SDK_INT >= 14) {
            textureView!!.surfaceTextureListener = null
        }
        containerSize = null
        previewSize = null
        previewFramingRect = null
        rotationListener!!.stop()
        fireState.previewStopped()
    }

    /**
     * Pause scanning and preview; waiting for the Camera to be closed.
     *
     * This blocks the main thread.
     */
    fun pauseAndWait() {
        val instance = cameraInstance
        pause()
        val startTime = System.nanoTime()
        while (instance != null && !instance.isCameraClosed) {
            if (System.nanoTime() - startTime > 2000000000) {
                // Don't wait for longer than 2 seconds
                break
            }
            try {
                Thread.sleep(1)
            } catch (e: InterruptedException) {
                break
            }
        }
    }

    fun getMarginFraction(): Double {
        return marginFraction
    }

    /**
     * The the fraction of the width/height of view to be used as a margin for the framing rect.
     * This is ignored if framingRectSize is specified.
     *
     * @param marginFraction the fraction
     */
    fun setMarginFraction(marginFraction: Double) {
        require(marginFraction < 0.5) { "The margin fraction must be less than 0.5" }
        this.marginFraction = marginFraction
    }

    private val isActive: Boolean
        get() = cameraInstance != null

    private val displayRotation: Int
        get() = windowManager!!.defaultDisplay.rotation

    private fun initCamera() {
        if (cameraInstance != null) {
            Log.w(TAG, "initCamera called twice")
            return
        }
        cameraInstance = createCameraInstance()
        cameraInstance!!.setReadyHandler(stateHandler)
        cameraInstance!!.open()

        // Keep track of the orientation we opened at, so that we don't reopen the camera if we
        // don't need to.
        openedOrientation = displayRotation
    }

    private fun createCameraInstance(): CameraInstance {
        val cameraInstance = CameraInstance(context)
        cameraInstance.cameraSettings = cameraSettings
        return cameraInstance
    }

    private fun startCameraPreview(surface: CameraSurface) {
        if (!isPreviewActive && cameraInstance != null) {
            Log.i(TAG, "Starting preview")
            cameraInstance!!.setSurface(surface)
            cameraInstance!!.startPreview()
            isPreviewActive = true
            previewStarted()
            fireState.previewStarted()
        }
    }

    /**
     * Called when the preview is started. Override this to start decoding work.
     */
    protected open fun previewStarted() {}

    private fun calculateFramingRect(container: Rect?, surface: Rect?): Rect {
        // intersection is the part of the container that is used for the preview
        val intersection = Rect(container)
        val intersects = intersection.intersect(surface)
        if (framingRectSize != null) {
            // Specific size is specified. Make sure it's not larger than the container or surface.
            val horizontalMargin = Math.max(0, (intersection.width() - framingRectSize!!.width) / 2)
            val verticalMargin = Math.max(0, (intersection.height() - framingRectSize!!.height) / 2)
            intersection.inset(horizontalMargin, verticalMargin)
            return intersection
        }
        // margin as 10% (default) of the smaller of width, height
        val margin = Math.min(
            intersection.width() * marginFraction,
            intersection.height() * marginFraction
        ).toInt()
        intersection.inset(margin, margin)
        if (intersection.height() > intersection.width()) {
            // We don't want a frame that is taller than wide.
            intersection.inset(0, (intersection.height() - intersection.width()) / 2)
        }
        return intersection
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val myState = Bundle()
        myState.putParcelable("super", superState)
        myState.putBoolean("torch", torchOn)
        return myState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is Bundle) {
            super.onRestoreInstanceState(state)
            return
        }
        val myState = state
        val superState = myState.getParcelable<Parcelable>("super")
        super.onRestoreInstanceState(superState)
        val torch = myState.getBoolean("torch")
        setTorch(torch)
    }

    val isCameraClosed: Boolean get() = cameraInstance == null || cameraInstance!!.isCameraClosed

    companion object {
        private val TAG = ScannerCameraPreview::class.java.simpleName
        // Delay after rotation change is detected before we reorientate ourselves.
        // This is to avoid double-reinitialization when the Activity is destroyed and recreated.
        private const val ROTATION_LISTENER_DELAY_MS = 250
    }
}