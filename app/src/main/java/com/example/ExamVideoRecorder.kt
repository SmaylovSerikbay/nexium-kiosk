package com.example

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Range
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

/**
 * Пишет процесс осмотра на фронтальную камеру со звуком, без предпросмотра.
 * MediaRecorder используется напрямую, чтобы планшет не заменял заданный
 * битрейт параметрами SD-профиля CameraX.
 */
class ExamVideoRecorder(private val context: Context) {
  private var cameraDevice: CameraDevice? = null
  private var captureSession: CameraCaptureSession? = null
  private var mediaRecorder: MediaRecorder? = null
  private var cameraThread: HandlerThread? = null
  private var startSignal = CompletableDeferred<Boolean>()

  @Volatile
  private var isRecording = false

  var outputFile: File? = null
    private set

  @SuppressLint("MissingPermission")
  fun start(@Suppress("UNUSED_PARAMETER") lifecycleOwner: LifecycleOwner) {
    if (mediaRecorder != null) return

    val thread = HandlerThread("ExamVideoRecorder").apply { start() }
    cameraThread = thread
    val handler = Handler(thread.looper)
    startSignal = CompletableDeferred()

    try {
      val cameraManager = context.getSystemService(CameraManager::class.java)
      val cameraId = cameraManager.cameraIdList.first {
        cameraManager.getCameraCharacteristics(it)
          .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
      }
      val file = File(context.cacheDir, "exam_${System.currentTimeMillis()}.mp4")
      outputFile = file

      val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
      } else {
        @Suppress("DEPRECATION")
        MediaRecorder()
      }.apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setVideoSource(MediaRecorder.VideoSource.SURFACE)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setOutputFile(file.absolutePath)
        setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        setVideoSize(320, 240)
        setVideoFrameRate(15)
        setVideoEncodingBitRate(250_000)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        setAudioChannels(1)
        setAudioSamplingRate(16_000)
        setAudioEncodingBitRate(32_000)
        prepare()
      }
      mediaRecorder = recorder

      cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
          cameraDevice = camera
          val surface = recorder.surface
          camera.createCaptureSession(
            listOf(surface),
            object : CameraCaptureSession.StateCallback() {
              override fun onConfigured(session: CameraCaptureSession) {
                try {
                  captureSession = session
                  val request = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
                    addTarget(surface)
                    set(
                      android.hardware.camera2.CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,
                      Range(15, 15),
                    )
                  }.build()
                  session.setRepeatingRequest(request, null, handler)
                  recorder.start()
                  isRecording = true
                  startSignal.complete(true)
                } catch (e: Exception) {
                  failStart(e)
                }
              }

              override fun onConfigureFailed(session: CameraCaptureSession) {
                failStart(IllegalStateException("Camera capture session configuration failed"))
              }
            },
            handler,
          )
        }

        override fun onDisconnected(camera: CameraDevice) {
          camera.close()
          failStart(IllegalStateException("Camera disconnected"))
        }

        override fun onError(camera: CameraDevice, error: Int) {
          camera.close()
          failStart(IllegalStateException("Camera error: $error"))
        }
      }, handler)
    } catch (e: Exception) {
      failStart(e)
    }
  }

  suspend fun stopAndGetFile(): File? {
    if (!isRecording) {
      withTimeoutOrNull(5000) { startSignal.await() }
    }

    val file = outputFile
    var completed = false
    if (isRecording) {
      try {
        mediaRecorder?.stop()
        completed = true
      } catch (e: RuntimeException) {
        Log.e(TAG, "Failed to finalize recording", e)
      }
    }
    release()
    if (!completed) file?.delete()
    return file?.takeIf { completed && it.exists() }
  }

  private fun failStart(error: Exception) {
    Log.e(TAG, "Failed to start recording", error)
    startSignal.complete(false)
    release()
    outputFile?.delete()
  }

  private fun release() {
    isRecording = false
    try {
      captureSession?.stopRepeating()
    } catch (_: Exception) {
    }
    captureSession?.close()
    captureSession = null
    cameraDevice?.close()
    cameraDevice = null
    mediaRecorder?.reset()
    mediaRecorder?.release()
    mediaRecorder = null
    cameraThread?.quitSafely()
    cameraThread = null
  }

  private companion object {
    const val TAG = "ExamVideoRecorder"
  }
}
