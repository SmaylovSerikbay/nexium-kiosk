package com.example

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.util.Range
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

/**
 * Пишет процесс осмотра на фронтальную камеру со звуком, без предпросмотра
 * (только VideoCapture, без Preview use case). Файл отдаётся на отправку после
 * стоп-события. Разрешение RECORD_AUDIO должно быть выдано до вызова start()
 * (см. KioskManager.grantExamVideoPermissionsSilently).
 */
class ExamVideoRecorder(private val context: Context) {
  private var cameraProvider: ProcessCameraProvider? = null
  private var activeRecording: Recording? = null
  private var finalizeSignal = CompletableDeferred<Unit>()

  var outputFile: File? = null
    private set

  @SuppressLint("MissingPermission")
  fun start(lifecycleOwner: LifecycleOwner) {
    if (activeRecording != null) return
    val future = ProcessCameraProvider.getInstance(context)
    future.addListener({
      try {
        val provider = future.get()
        cameraProvider = provider
        provider.unbindAll()

        val recorder = Recorder.Builder()
          .setQualitySelector(QualitySelector.from(Quality.SD))
          .setTargetVideoEncodingBitRate(450_000)
          .build()
        val capture = VideoCapture.Builder(recorder)
          .setTargetFrameRate(Range(15, 15))
          .build()
        provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, capture)

        val file = File(context.cacheDir, "exam_${System.currentTimeMillis()}.mp4")
        outputFile = file
        finalizeSignal = CompletableDeferred()

        activeRecording = capture.output
          .prepareRecording(context, FileOutputOptions.Builder(file).build())
          .withAudioEnabled()
          .start(ContextCompat.getMainExecutor(context)) { event ->
            if (event is VideoRecordEvent.Finalize) {
              if (event.hasError()) {
                Log.e("ExamVideoRecorder", "Recording error: ${event.error}")
              }
              finalizeSignal.complete(Unit)
            }
          }
      } catch (e: Exception) {
        Log.e("ExamVideoRecorder", "Failed to start recording", e)
      }
    }, ContextCompat.getMainExecutor(context))
  }

  // Останавливает запись и ждёт финализации файла на диске, прежде чем вернуть его.
  suspend fun stopAndGetFile(): File? {
    val recording = activeRecording ?: return null
    recording.stop()
    activeRecording = null
    withTimeoutOrNull(5000) { finalizeSignal.await() }
    cameraProvider?.unbindAll()
    cameraProvider = null
    return outputFile
  }
}
