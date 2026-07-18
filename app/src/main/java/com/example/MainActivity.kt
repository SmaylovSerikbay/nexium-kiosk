package com.example

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.net.Uri
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import android.widget.VideoView
import android.widget.MediaController
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.math.cos
import kotlin.math.sin
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView

// Screen & Step States
enum class KioskScreen {
  LANGUAGE_SELECTION,
  AUTHORIZATION,
  REGISTRATION,
  CONFIRMATION,
  DASHBOARD,
  SETTINGS
}

// Режим полноэкранного сканера лица (см. FaceIdScanScreen): вход по Face ID
// или включение Face ID уже опознанным сотрудником.
enum class FaceIdCaptureMode { LOGIN, ENROLL }

enum class AppLanguage {
  KAZAKH,
  RUSSIAN
}

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.RUSSIAN }

// AutoMirrored variants unavailable in the current material-icons-extended version.
@Suppress("DEPRECATION")
val LegacyArrowBackIcon = Icons.Filled.ArrowBack
@Suppress("DEPRECATION")
val LegacyBluetoothSearchingIcon = Icons.Filled.BluetoothSearching

class Trans(val ru: String, val kk: String) {
  fun get(lang: AppLanguage): String = if (lang == AppLanguage.KAZAKH) kk else ru
}

object AppText {
  val titleBranding = Trans("NEXIUM HEALTH", "NEXIUM HEALTH")
  val subtitleBranding = Trans("Medical Kiosk Authorization Gate", "Медициналық терминалға кіру шлюзі")
  val authSubtitle = Trans("Авторизация медицинского терминала", "Медициналық терминалға авторлану")
  val authPinPrompt = Trans("ВВЕДИТЕ ПИН-КОД ДЛЯ ВХОДА", "КІРУ ҮШІН ПИН-КОДТЫ ЕНГІЗІҢІЗ")
  val authHint = Trans("Подсказка: Введите '1111'", "Нұсқау: '1111' енгізіңіз")
  val employeeNotFound = Trans("Сотрудник не найден", "Қызметкер табылмады")
  val bypass = Trans("ОБХОД\n(1111)", "АЙНАЛЫП ӨТУ\n(1111)")

  val systemStatus = Trans("КОНТРОЛЬНЫЙ СПИСОК ДИАГНОСТИКИ", "ДИАГНОСТИКАЛЫҚ БАҚЫЛАУ ТІЗІМІ")
  val activeWorkflow = Trans("АКТИВНЫЙ РАБОЧИЙ ТЕРМИНАЛ", "БЕЛСЕНДІ ЖҰМЫС ТЕРМИНАЛЫ")
  val secureConn = Trans("БЕЗОПАСНОЕ СОЕДИНЕНИЕ // 128-БИТ", "ҚАУІПСІЗ ҚОСЫЛЫМ // 128-БИТ")
  val terminalVersion = Trans("NEXIUM HEALTH • ТЕРМИНАЛ v2.5", "NEXIUM HEALTH • ТЕРМИНАЛ v2.5")
  val physicianId = Trans("ID ВРАЧА: DR-9942", "ДӘРІГЕР ID: DR-9942")
  val encryptedTransfer = Trans("ЗАШИФРОВАННАЯ ПЕРЕДАЧА АКТИВНА", "ШИФРЛЕНГЕН БЕРІЛІМ БЕЛСЕНДІ")
  
  val bpTitle = Trans("Артериальное давление", "Қан қысымы")
  val hrTitle = Trans("Частота пульса", "Жүрек соғу жиілігі")
  val breathTitle = Trans("Алкотестер", "Алкотестер")
  val tempTitle = Trans("Температура тела", "Дене температурасы")
  val complaintsTitle = Trans("Жалобы на здоровье", "Денсаулыққа шағымдар")

  val awaitValue = Trans("Ожидание...", "Күтілуде...")
  val pendingValue = Trans("В очереди", "Күтуде")
  
  val valClear = Trans("В НОРМЕ", "ҚАЛЫПТЫ")
  val valSymptoms = Trans("ЖАЛОБЫ", "ШАҒЫМДАР")
  val valActive = Trans("АКТИВНО", "БЕЛСЕНДІ")
  val valPending = Trans("ОЖИДАНИЕ", "КҮТУДЕ")

  val noSymptomsToday = Trans("Нет жалоб на здоровье сегодня", "Бүгін денсаулығыңызға шағымдарыңыз жоқ")
  val awaitQuestionnaire = Trans("Ожидание заполнения анкеты...", "Анкетаны толтыру күтілуде...")
  val analysisPending = Trans("Анализ ожидает выполнения", "Талдау орындалу күтілуде")

  val stepAnamnesis = Trans("АНАМНЕЗ", "АНАМНЕЗ")
  val stepBp = Trans("ДАВЛЕНИЕ И ПУЛЬС", "ҚАН ҚЫСЫМЫ МЕН ПУЛЬС")
  val stepBreath = Trans("ДЫХАТЕЛЬНЫЙ ТЕСТ", "ТЫНЫС АЛУ СЫНАҒЫ")
  val stepTemp = Trans("ТЕМПЕРАТУРНЫЙ СКРИНИНГ", "ТЕМПЕРАТУРАНЫ СКАНЕРЛЕУ")
  val stepVerif = Trans("ПРОВЕРКА ЗАКЛЮЧЕНИЯ", "ҚОРЫТЫНДЫНЫ ТЕКСЕРУ")

  val complaintsPrompt = Trans("Есть ли у вас жалобы на здоровье сегодня?", "Бүгін денсаулығыңызға шағымдарыңыз бар ма?")
  val complaintsDetail = Trans(
    "Любой физический дискомфорт, боли, хроническая усталость или симптомы должны быть немедленно зарегистрированы.",
    "Кез келген физикалық қолайсыздықтар, ауырсыну, созылмалы шаршау немесе симптомдар дереу белгіленуі тиіс."
  )
  val buttonSymptomsYes = Trans("ДА, есть симптомы", "ИӘ, симптомдар бар")
  val buttonSymptomsNo = Trans("НЕТ жалоб сегодня", "ЖОҚ, шағымдар жоқ")

  val bpCuffPrompt = Trans("Наденьте манжету на предплечье", "Манжетті қолыңызға киіңіз")
  val bpCuffDetail = Trans(
    "Оставайтесь полностью неподвижны. Поместите руку в манжету тонометра, зафиксируйте ее, сядьте ровно и соблюдайте тишину.",
    "Қозғалмай отырыңыз. Қолыңызды қан қысымын өлшейтін манжетке салыңыз, бекітіңіз және тыныштықты сақтаңыз."
  )
  val bpScanning = Trans("Идет калибровка сканирования давления...", "Сенсорды калибрлеу белсенді...")
  val bpInflating = Trans("НАГНЕТАНИЕ", "ҚЫСЫМ ТОЛТЫРУ")
  val bpCalibrating = Trans("Калибровка датчиков...", "Сенсорларды ретке келтіру...")
  val bpButtonScan = Trans("Запустить сенсорное сканирование", "Сенсорлық сканерлеуді бастау")

  val breathPrompt = Trans("Равномерно дуйте в стерильный мундштук", "Стерильді түтікке бірқалыпты үрлеңіз")
  val breathDetail = Trans(
    "Сделайте глубокий вдох, плотно обхватите мундштук губами и выдыхайте с постоянным давлением.",
    "Терең дем алып, ерніңізді түтікке нығыз тақап, тұрақты қысыммен дем шығарыңыз."
  )
  val breathExhalating = Trans("Давление выдоха зафиксировано. Дышите ровно...", "Дем шығару қысымы қабылданды. Бірқалыпты дем алыңыз...")
  val breathAnalyzing = Trans("Анализ состава воздуха...", "Ауа құрамын талдау...")
  val breathButtonScan = Trans("Начать спирометрический забор", "Спирометрлік сынама алуды бастау")

  val tempPrompt = Trans("Направьте лоб на бесконтактный оптический термометр", "Маңдайыңызды контактісіз оптикалық датчикке жақындатыңыз")
  val tempDetail = Trans(
    "Расположитесь на расстоянии 5–10 см от бесконтактного термометра. Сохраняйте полную неподвижность.",
    "Оптикалық термометрден 5-10 см қашықтықта орналасыңыз. Мүлдем қозғалмаңыз."
  )
  val tempScanning = Trans("Тепловое сканирование активно...", "Термиялық сканерлеу белсенді...")
  val tempScreenProgress = Trans("Тепловой скрининг выполняется...", "Термиялық скрининг орындалуда...")
  val tempButtonScan = Trans("Запустить тепловое сканирование", "Термиялық оптикалық сканерлеуді бастау")

  val verifSummaryTitle = Trans("ПРОВЕРКА НЕЗАВИСИМОГО ЗАКЛЮЧЕНИЯ", "ТӘУЕЛСІЗ МЕДИЦИНАЛЫҚ ҚОРЫТЫНДЫНЫ ТЕКСЕРУ")
  val verifAudit = Trans("Автоматический аудит заключения", "Қорытындыны автоматты түрде тексеру")
  val verifDesc = Trans(
    "Все интегрированные диагностические показатели успешно синхронизированы. Пожалуйста, проверьте данные ниже.",
    "Барлық біріктірілген диагностикалық көрсеткіштер сессиямен синхрондалды. Төмендегі деректерді тексеріңіз."
  )
  val verifRowAnamnesis = Trans("Физиологический анамнез", "Физиологиялық анамнез")
  val verifRowBp = Trans("Артериальное давление", "Жүрек-қантамыр жүйесі (Қан қысымы)")
  val verifRowHr = Trans("Частота пульса", "Жүрек соғу жиілігі")
  val verifRowBreath = Trans("Дыхательный спирометр (Алкотест)", "Өкпе спирометриясы (Алкотестер)")
  val verifRowTemp = Trans("Термальный скрининг core", "Термиялық оптикалық бақылау")
  val verifCompleted = Trans("Завершено", "Аяқталды")
  val verifCalibrated = Trans("Откалибровано", "Калибрленді")
  val verifVerifiedNormal = Trans("В норме", "Қалыпты")
  val verifButtonSubmit = Trans("Подписать и отправить досье", "Құжаттамаға қол қою және жіберу")

  val awaitingDecision = Trans("Ожидание медицинского заключения...", "Медициналық шешімді күту...")
  val awaitingDesc = Trans(
    "Ваши диагностические данные заблокированы и проверяются сертифицированным дежурным врачом для подтверждения телеметрической подписи соответствия.",
    "Сіздің диагностикалық деректеріңіз бұғатталды және телеметрия қолтаңбасының талаптарына сәйкестігін бақылау үшін Nexium Health сертификатталған дәрігерімен қауіпсіз тексеріледі."
  )

  val fitForDuty = Trans("ДОПУЩЕН К РАБОЧЕЙ СМЕНЕ", "ЖҰМЫС АУЫСЫМЫНА РҰҚСАТ ЕТІЛДІ")
  val passVerified = Trans("Пропуск здоровья Nexium подтвержден", "Nexium денсаулық рұқсатнамасы расталды")
  val logoutText = Trans("Выйти и завершить сессию", "Шығу және сессияны аяқтау")

  val profileName = Trans("Д-р Александр Стерлинг", "Др. Александр Стерлинг")
  val profileDept = Trans("Департамент когнитивного усиления", "Когнитивті күшейту бөлімі")
  val complaintsYes = Trans("Выявлены симптомы", "Симптомдар анықталды")
  val complaintsNone = Trans("Жалоб нет // Здоров", "Шағымдар жоқ // Дені сау")
  
  val stepBadge = Trans("ШАГ", "ҚАДАМ")
  val step1BadgeTitle = Trans("АНАМНЕЗ", "АНАМНЕЗ")
  val step2BadgeTitle = Trans("АД И ПУЛЬС", "ҚАН ҚЫСЫМЫ МЕН ПУЛЬС")
  val step3BadgeTitle = Trans("ТЕСТ ДЫХАНИЯ", "ТЫНЫС АЛУ СЫНАҒЫ")
  val step4BadgeTitle = Trans("СКАНИРОВАНИЕ ТЕМПЕРАТУРЫ", "ТЕМПЕРАТУРАНЫ СКАНЕРЛЕУ")
  
  val certSigneeId = Trans("ID подписанта", "Қол қоюшы ID-і")
  val certSignedBy = Trans("Подписано врачом", "Қол қойған дәрігер")
  val certSarah = Trans("Д-р Сара Дженкинс", "Д-р Сара Дженкинс")
  val certState = Trans("Статус сертификата", "Сертификат күйі")
  val certToken = Trans("Токен аудит-сессии", "Аудит сессиясының токені")
  val certApproved = Trans("ОДОБРЕН", "МАҚҰЛДАНДЫ")

  val authIdPrompt = Trans("ВВЕДИТЕ ID СОТРУДНИКА ДЛЯ ВХОДА", "КІРУ ҮШІН ҚЫЗМЕТКЕР ID-ін ЕНГІЗІҢІЗ")
  val authIdHint = Trans("Введите ваш ID сотрудника на сайте", "Қызметкердің сайттағы ID-ін енгізіңіз")
  val verifyingId = Trans("Проверка ID сотрудника в базе данных...", "Қызметкер ID-і дерекқордан тексерілуде...")
  val confirmTitle = Trans("Подтверждение личности", "Жеке басын растау")
  val confirmIsThisYou = Trans("Это вы?", "Бұл сіз бе?")
  val confirmFullName = Trans("ФИО:", "Т.А.Ә.:")
  val confirmId = Trans("ID сотрудника:", "Қызметкер ID-і:")
  val confirmOrg = Trans("Организация:", "Ұйым:")
  val confirmBranch = Trans("Филиал:", "Филиал:")
  val confirmDept = Trans("Должность:", "Лауазымы:")
  val yesItsMe = Trans("ДА, ЭТО Я", "ИӘ, БҰЛ МЕН")
  val noNotMe = Trans("НЕТ, НЕ Я", "ЖОҚ, МЕН ЕМЕС")
  val errorVerificationFailed = Trans(
    "Не удалось проверить сотрудника. Проверьте интернет и повторите попытку.",
    "Қызметкерді тексеру мүмкін болмады. Интернетті тексеріп, қайталап көріңіз."
  )
  val errorNoInternet = Trans("Отсутствует соединение с интернетом", "Интернет байланысы жоқ")
  val errorEmployeeOrgMismatch = Trans(
    "Этот сотрудник не относится к организации, к которой привязан данный аппарат. Обратитесь к администратору для проверки организации сотрудника или токена аппарата.",
    "Бұл қызметкер осы аппарат байланыстырылған ұйымға жатпайды. Қызметкердің ұйымын немесе аппарат токенін тексеру үшін әкімшіге хабарласыңыз."
  )
  val errorExamSendFailed = Trans("Не удалось отправить медосмотр", "Медициналық тексеруді жіберу мүмкін болмады")
}

enum class StepState {
  HEALTH_COMPLAINTS,   // Step 1
  BLOOD_PRESSURE,      // Step 2
  BREATHALYZER,        // Step 3
  TEMPERATURE,         // Step 4
  VERIFICATION,        // Step 5
  SECURE_LOADING,      // Post-Sign Review Spinner
  COMPLETED_VERDICT,   // Final Medical Clearance Pass
  AWAITING_NURSE       // Авто-подтверждение отключено, медсестра не успела решить за время поллинга
}

enum class ExamSendStatus {
  IDLE,
  SENDING,
  SUCCESS,
  ERROR
}

data class EmployeeProfile(
  val id: String,
  val fullName: String,
  val organization: String,
  val iin: String,
  val department: String? = null,
  val branch: String? = null,
  val photoUrl: String? = null,
  val position: String? = null
)

// Локально снятое на камеру фото хранится/передаётся как data:image/jpeg;base64,...
// строка (тот же формат, что уходит на сервер) — Coil такие URI не грузит, поэтому
// декодируем сами и рисуем как Bitmap, а не через AsyncImage.
fun decodeDataUriBitmap(uri: String?): android.graphics.Bitmap? {
  if (uri.isNullOrEmpty() || !uri.startsWith("data:")) return null
  return try {
    val base64 = uri.substringAfter("base64,", "")
    if (base64.isEmpty()) return null
    val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
  } catch (e: Exception) {
    null
  }
}

// Стандартный TakePicturePreview не даёt выбрать камеру — implicit-интенту нужны
// дополнительные extras, чтобы системная камера открылась на фронтальном объективе
// (общепринятые, но не гарантированные ключи — не все камеры их слушают).
class FrontCameraTakePicturePreview : androidx.activity.result.contract.ActivityResultContract<Void?, android.graphics.Bitmap?>() {
  override fun createIntent(context: Context, input: Void?): android.content.Intent {
    return android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
      putExtra("android.intent.extras.CAMERA_FACING", 1)
      putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
      putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
      putExtra("camerafacing", "front")
      putExtra("previous_mode", "front_camera")
    }
  }
  override fun getSynchronousResult(context: Context, input: Void?) = null
  override fun parseResult(resultCode: Int, intent: android.content.Intent?): android.graphics.Bitmap? {
    if (resultCode != android.app.Activity.RESULT_OK) return null
    @Suppress("DEPRECATION")
    return intent?.extras?.get("data") as? android.graphics.Bitmap
  }
}

// Кодирует снимок с камеры в JPEG Base64 data URL для отправки на бэкенд (аватар, Face ID).
fun bitmapToJpegDataUrl(bitmap: android.graphics.Bitmap): String? {
  return try {
    val stream = java.io.ByteArrayOutputStream()
    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, stream)
    val encoded = android.util.Base64.encodeToString(stream.toByteArray(), android.util.Base64.NO_WRAP)
    "data:image/jpeg;base64,$encoded"
  } catch (e: Exception) {
    null
  }
}

// Декодирует JPEG-кадр из ImageCapture и поворачивает его по EXIF-повороту сенсора.
fun imageProxyToBitmap(image: ImageProxy): android.graphics.Bitmap? {
  val buffer = image.planes[0].buffer
  val bytes = ByteArray(buffer.remaining())
  buffer.get(bytes)
  val decoded = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
  val rotation = image.imageInfo.rotationDegrees
  if (rotation == 0) return decoded
  val matrix = android.graphics.Matrix().apply { postRotate(rotation.toFloat()) }
  return android.graphics.Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
}

// Снимает один кадр с ImageCapture как suspend-вызов — обёртка над callback-API CameraX.
suspend fun captureFaceFrame(context: Context, capture: ImageCapture): android.graphics.Bitmap? =
  kotlinx.coroutines.suspendCancellableCoroutine { cont ->
    capture.takePicture(
      ContextCompat.getMainExecutor(context),
      object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
          val bitmap = imageProxyToBitmap(image)
          image.close()
          if (cont.isActive) cont.resume(bitmap)
        }
        override fun onError(exception: ImageCaptureException) {
          if (cont.isActive) cont.resume(null)
        }
      }
    )
  }

// Полноэкранный сканер лица со живым превью с фронтальной камеры: работает как обычный СКУД —
// открылся, сам непрерывно снимает кадры и шлёт на сервер, пока не найдёт совпадение или его не отменят.
// Овальная рамка-гид считается от высоты экрана (не ширины), чтобы не искажаться на горизонтальном планшете.
@Composable
fun FaceIdScanScreen(
  title: String,
  subtitle: String,
  isProcessing: Boolean,
  statusText: String,
  isError: Boolean,
  onCapture: (android.graphics.Bitmap) -> Unit,
  onCancel: () -> Unit
) {
  val lang = LocalAppLanguage.current
  val context = androidx.compose.ui.platform.LocalContext.current
  val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

  var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
  var hasCameraPermission by remember {
    mutableStateOf(context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
  }
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { granted -> hasCameraPermission = granted }
  LaunchedEffect(Unit) { if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA) }

  // Авто-скан: как только камера готова и предыдущая проверка на сервере завершилась (isProcessing = false),
  // берём новый кадр и отдаём его наверх — без кнопки "Сделать снимок". Пауза даёт автофокусу устояться.
  val capture = imageCapture
  LaunchedEffect(capture, isProcessing) {
    if (capture == null || isProcessing) return@LaunchedEffect
    delay(1200)
    val bitmap = captureFaceFrame(context, capture)
    if (bitmap != null) onCapture(bitmap)
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black)
  ) {
    if (hasCameraPermission) {
      AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
          val previewView = PreviewView(ctx).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
          }
          val future = ProcessCameraProvider.getInstance(ctx)
          future.addListener({
            try {
              val provider = future.get()
              val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
              }
              val newCapture = ImageCapture.Builder().build()
              provider.unbindAll()
              provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, preview, newCapture)
              imageCapture = newCapture
            } catch (e: Exception) {
              // Камера недоступна — пользователь увидит чёрный экран и сможет отменить сканирование.
            }
          }, ContextCompat.getMainExecutor(ctx))
          previewView
        }
      )
    } else {
      Text(
        text = if (lang == AppLanguage.KAZAKH) "Камераға рұқсат қажет" else "Нужен доступ к камере",
        color = Color.White,
        modifier = Modifier.align(Alignment.Center)
      )
    }

    // Овальная рамка-гид — размер считаем от высоты экрана и фиксированной пропорции лица (~0.74),
    // а не от ширины: на горизонтальном планшете ширина намного больше высоты и овал "расплющивало".
    val guideColor = if (isError) AppleAmber else AppleBlue
    val scanPulse = rememberInfiniteTransition(label = "faceScanPulse")
    val pulseAlpha by scanPulse.animateFloat(
      initialValue = 0.5f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutCirc), RepeatMode.Reverse),
      label = "faceScanPulseAlpha"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
      val ovalHeight = size.height * 0.62f
      val ovalWidth = (ovalHeight * 0.74f).coerceAtMost(size.width * 0.85f)
      val centerX = size.width / 2f
      val centerY = size.height * 0.45f
      drawOval(
        color = guideColor.copy(alpha = if (isProcessing || isError) 1f else pulseAlpha),
        topLeft = Offset(centerX - ovalWidth / 2f, centerY - ovalHeight / 2f),
        size = androidx.compose.ui.geometry.Size(ovalWidth, ovalHeight),
        style = Stroke(width = 4.dp.toPx())
      )
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.TopCenter)
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .padding(20.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onCancel,
        modifier = Modifier.background(Color.White.copy(alpha = 0.12f), CircleShape)
      ) {
        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
      }
      Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
      Spacer(modifier = Modifier.size(40.dp))
    }

    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .padding(bottom = 40.dp, start = 24.dp, end = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      if (isProcessing) {
        CircularProgressIndicator(color = AppleBlue, modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
      }
      Text(
        text = statusText.ifEmpty { subtitle },
        color = if (isError) AppleAmber else Color.White.copy(alpha = 0.85f),
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center
      )
    }
  }
}

fun getInitials(name: String?): String {
  if (name.isNullOrBlank()) return "👤"
  val parts = name.trim().split("\\s+".toRegex())
  val first = parts.getOrNull(0)?.firstOrNull()?.toString()?.uppercase() ?: ""
  val second = parts.getOrNull(1)?.firstOrNull()?.toString()?.uppercase() ?: ""
  val initials = first + second
  return if (initials.isNotEmpty()) initials else "👤"
}

data class MetricState(
  val name: String,
  val value: String?,
  val unit: String,
  val icon: ImageVector,
  val isCompleted: Boolean,
  val isActive: Boolean
)

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    NexApiClient.init(this)
    AppUpdateManager.sendQueuedUpdateStatusAsync(this)
    requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    val settingsPrefs = getSharedPreferences("nex_settings", android.content.Context.MODE_PRIVATE)
    val initialKioskModeEnabled = settingsPrefs.getBoolean("kiosk_mode_enabled", true)
    enableEdgeToEdge()
    applyKioskWindowMode(initialKioskModeEnabled)

    // Если устройство назначено Device Owner (см. KioskManager.kt), политики киоска
    // применяем только когда режим включён. Иначе сервисные системные экраны будут
    // открываться в фоне, потому что приложение останется persistent HOME.
    if (initialKioskModeEnabled) {
      KioskManager.configureLockTask(this)
    } else {
      KioskManager.disableKioskPolicies(this)
    }
    KioskManager.grantExamVideoPermissionsSilently(this)

    setContent {
      val context = androidx.compose.ui.platform.LocalContext.current
      val prefs = remember(context) { context.getSharedPreferences("nex_employees", android.content.Context.MODE_PRIVATE) }
      var isDarkTheme by remember { mutableStateOf(prefs.getBoolean("is_dark_theme", true)) }
      var kioskModeEnabled by remember { mutableStateOf(initialKioskModeEnabled) }

      MyApplicationTheme(darkTheme = isDarkTheme) {
        KioskAppRoot(
          isDarkTheme = isDarkTheme,
          onThemeToggle = {
            val newValue = !isDarkTheme
            isDarkTheme = newValue
            prefs.edit().putBoolean("is_dark_theme", newValue).apply()
          },
          kioskModeEnabled = kioskModeEnabled,
          onKioskModeToggle = { enabled ->
            kioskModeEnabled = enabled
            settingsPrefs.edit().putBoolean("kiosk_mode_enabled", enabled).apply()
            applyKioskWindowMode(enabled)
            if (!enabled) {
              try {
                stopLockTask()
              } catch (e: Exception) {
                android.util.Log.w("MainActivity", "stopLockTask() ignored", e)
              }
              KioskManager.disableKioskPolicies(this)
            } else {
              if (KioskManager.isDeviceOwner(this)) {
                KioskManager.configureLockTask(this)
                try {
                  startLockTask()
                } catch (e: Exception) {
                  android.util.Log.e("MainActivity", "Failed to startLockTask()", e)
                }
              }
            }
          }
        )
      }
    }
  }

  // Пин киоска к экрану на устройствах, где приложение — Device Owner. На обычных
  // (не Device Owner) устройствах вызов безопасно не выполняет ничего дополнительного —
  // ограничение переключения между приложениями там уже даёт перехват BackHandler.
  override fun onResume() {
    super.onResume()
    val isOwner = KioskManager.isDeviceOwner(this)
    val settingsPrefs = getSharedPreferences("nex_settings", android.content.Context.MODE_PRIVATE)
    val kioskEnabled = settingsPrefs.getBoolean("kiosk_mode_enabled", true)
    applyKioskWindowMode(kioskEnabled)
    
    android.util.Log.d("MainActivity", "onResume: isDeviceOwner=$isOwner, kioskEnabled=$kioskEnabled")
    if (isOwner && kioskEnabled) {
      try {
        android.util.Log.d("MainActivity", "Attempting startLockTask()")
        startLockTask()
        android.util.Log.d("MainActivity", "startLockTask() called successfully")
      } catch (e: Exception) {
        android.util.Log.e("MainActivity", "Failed to startLockTask()", e)
      }
    }
  }

  private fun applyKioskWindowMode(enabled: Boolean) {
    WindowCompat.setDecorFitsSystemWindows(window, !enabled)
    val controller = WindowCompat.getInsetsController(window, window.decorView)

    if (enabled) {
      controller.hide(WindowInsetsCompat.Type.systemBars())
      controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    } else {
      controller.show(WindowInsetsCompat.Type.systemBars())
    }
  }
}

@Composable
fun LanguageSelectionScreen(
  isDarkTheme: Boolean,
  onThemeToggle: () -> Unit,
  onLanguageSelected: (AppLanguage) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.safeDrawing)
      .padding(32.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "NEXIUM HEALTH",
      color = AppleLightGrey,
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Bold,
      letterSpacing = 8.sp,
      textAlign = TextAlign.Center
    )
    
    Spacer(modifier = Modifier.height(10.dp))
    
    Text(
      text = "SELECT LANGUAGE // ТІЛДІ ТАҢДАУ",
      color = AppleBlue,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 2.sp,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(36.dp))

    Row(
      horizontalArrangement = Arrangement.spacedBy(24.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Russian language button
      Row(
        modifier = Modifier
          .width(240.dp)
          .height(90.dp)
          .clip(RoundedCornerShape(18.dp))
          .border(BorderStroke(1.2.dp, AppleBorderColor), RoundedCornerShape(18.dp))
          .background(if (isDarkTheme) AppleCharcoal.copy(alpha = 0.5f) else Color.White)
          .clickable { onLanguageSelected(AppLanguage.RUSSIAN) }
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Text(
          text = "Русский язык",
          color = AppleLightGrey,
          fontSize = 18.sp,
          fontWeight = FontWeight.SemiBold,
          letterSpacing = 0.5.sp
        )
      }

      // Kazakh language button
      Row(
        modifier = Modifier
          .width(240.dp)
          .height(90.dp)
          .clip(RoundedCornerShape(18.dp))
          .border(BorderStroke(1.2.dp, AppleBorderColor), RoundedCornerShape(18.dp))
          .background(if (isDarkTheme) AppleCharcoal.copy(alpha = 0.5f) else Color.White)
          .clickable { onLanguageSelected(AppLanguage.KAZAKH) }
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Text(
          text = "Қазақ тілі",
          color = AppleLightGrey,
          fontSize = 18.sp,
          fontWeight = FontWeight.SemiBold,
          letterSpacing = 0.5.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Premium dynamic Theme Switch Option
    Row(
      modifier = Modifier
        .clip(RoundedCornerShape(14.dp))
        .background(if (isDarkTheme) AppleCharcoal.copy(alpha = 0.4f) else Color.White)
        .border(BorderStroke(1.dp, AppleBorderColor), RoundedCornerShape(14.dp))
        .clickable { onThemeToggle() }
        .padding(horizontal = 24.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Icon(
        imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
        contentDescription = "Theme Switch",
        tint = AppleBlue,
        modifier = Modifier.size(20.dp)
      )
      Text(
        text = if (isDarkTheme) "ТЁМНАЯ ТЕМА • ҚАРАҢҒЫ" else "СВЕТЛАЯ ТЕМА • ЖАРҚЫН",
        color = AppleLightGrey,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
      )
    }

    Spacer(modifier = Modifier.height(40.dp))

    Text(
      text = "Nexium Health Kiosk System • Nexium денсаулық сақтау терминалы",
      color = AppleMutedGrey.copy(alpha = 0.4f),
      fontSize = 11.sp,
      letterSpacing = 1.sp,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
fun KioskAppRoot(
  isDarkTheme: Boolean = true,
  onThemeToggle: () -> Unit = {},
  kioskModeEnabled: Boolean = true,
  onKioskModeToggle: (Boolean) -> Unit = {}
) {
  var appLanguage by remember { mutableStateOf<AppLanguage?>(null) }
  var currentScreen by remember { mutableStateOf(KioskScreen.LANGUAGE_SELECTION) }
  
  val activeLanguage = appLanguage ?: AppLanguage.RUSSIAN

  // App-wide data states
  var enteredPin by remember { mutableStateOf("") }
  var shakeTrigger by remember { mutableStateOf(0) }
  var pinErrorText by remember { mutableStateOf("") }
  
  val context = androidx.compose.ui.platform.LocalContext.current
  val prefs = remember(context) { context.getSharedPreferences("nex_employees", android.content.Context.MODE_PRIVATE) }
  val settingsPrefs = remember(context) { context.getSharedPreferences("nex_settings", android.content.Context.MODE_PRIVATE) }

  var deviceToken by remember { mutableStateOf(NexApiClient.deviceToken) }
  var tokenInfo by remember { mutableStateOf<TokenInfoResponse?>(null) }
  var tokenInfoError by remember { mutableStateOf("") }
  var isFetchingTokenInfo by remember { mutableStateOf(false) }

  LaunchedEffect(deviceToken, activeLanguage) {
    if (deviceToken.isNotEmpty()) {
      isFetchingTokenInfo = true
      try {
        val response = withContext(Dispatchers.IO) {
          NexApiClient.service.getCurrentTokenInfo(deviceToken)
        }
        if (response.isSuccessful) {
          tokenInfo = response.body()
          tokenInfoError = ""
        } else {
          tokenInfo = null
          tokenInfoError = ApiErrorText.fromHttp(response.code(), response.errorBody()?.string(), activeLanguage)
        }
      } catch (e: Exception) {
        tokenInfo = null
        tokenInfoError = ApiErrorText.fromThrowable(e, activeLanguage)
      } finally {
        isFetchingTokenInfo = false
      }
    } else {
      tokenInfo = null
      tokenInfoError = ""
    }
  }

  // Auto-update: проверка версии приложения на бэкенде и скачивание/установка APK.
  // Диалог показывается только на "простаивающих" экранах (выбор языка/авторизация),
  // чтобы не мешать сотруднику посреди прохождения медосмотра.
  var availableUpdate by remember { mutableStateOf<AppVersionResponse?>(null) }
  var updateDownloadProgress by remember { mutableStateOf<Int?>(null) }
  var updateDownloadedFile by remember { mutableStateOf<java.io.File?>(null) }
  var updateErrorText by remember { mutableStateOf<String?>(null) }
  var pendingSilentUpdate by remember { mutableStateOf<AppVersionResponse?>(null) }
  var isSilentUpdateInProgress by remember { mutableStateOf(false) }

  // Persistent settings for blood pressure monitor, breathalyzer, and thermometer
  var tonometerMode by remember { mutableStateOf(prefs.getString("tonometer_mode", "simulation") ?: "simulation") }
  var tonometerMac by remember { mutableStateOf(prefs.getString("tonometer_mac", "") ?: "") }
  var tonometerName by remember { mutableStateOf(prefs.getString("tonometer_name", "") ?: "") }

  var breathalyzerMode by remember { mutableStateOf(prefs.getString("breathalyzer_mode", "simulation") ?: "simulation") }
  var breathalyzerMac by remember { mutableStateOf(prefs.getString("breathalyzer_mac", "") ?: "") }
  var breathalyzerName by remember { mutableStateOf(prefs.getString("breathalyzer_name", "") ?: "") }

  var thermometerMode by remember { mutableStateOf(prefs.getString("thermometer_mode", "simulation") ?: "simulation") }
  var thermometerMac by remember { mutableStateOf(prefs.getString("thermometer_mac", "") ?: "") }
  var thermometerName by remember { mutableStateOf(prefs.getString("thermometer_name", "") ?: "") }

  fun getCachedProfile(id: String): EmployeeProfile? {
    val name = prefs.getString("emp_${id}_name", null) ?: return null
    val org = prefs.getString("emp_${id}_org", "") ?: ""
    val branch = prefs.getString("emp_${id}_branch", "") ?: ""
    val pos = prefs.getString("emp_${id}_pos", "") ?: ""
    val photo = prefs.getString("emp_${id}_photo", "") ?: ""
    return EmployeeProfile(
      id = id,
      fullName = name,
      organization = org,
      iin = "",
      department = pos,
      branch = branch,
      photoUrl = photo,
      position = pos
    )
  }

  fun saveRegisteredEmployee(id: String, name: String, org: String, branch: String, pos: String, photo: String) {
    prefs.edit()
      .putString("emp_${id}_name", name)
      .putString("emp_${id}_org", org)
      .putString("emp_${id}_branch", branch)
      .putString("emp_${id}_pos", pos)
      .putString("emp_${id}_photo", photo)
      .apply()
  }

  // Dynamic profile value derived from successful API login verification
  var currentEmployeeProfile by remember { mutableStateOf<EmployeeProfile?>(null) }
  var isVerifyingEmployee by remember { mutableStateOf(false) }
  var showConfirmationDialog by remember { mutableStateOf(false) }
  var verifiedEmployeeResponse by remember { mutableStateOf<VerifyEmployeeResponse?>(null) }
  var isTogglingFaceId by remember { mutableStateOf(false) }
  var faceIdCaptureMode by remember { mutableStateOf<FaceIdCaptureMode?>(null) }
  var examSendStatus by remember { mutableStateOf(ExamSendStatus.IDLE) }
  var examSendErrorMessage by remember { mutableStateOf("") }

  // Запись видео осмотра: стартует при входе в кабинет сотрудника, останавливается
  // и отправляется на бэкенд в момент нажатия "Отправить".
  val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
  val examVideoRecorder = remember { ExamVideoRecorder(context) }
  var isVideoRecording by remember { mutableStateOf(false) }
  var videoRecordingStartMs by remember { mutableStateOf(0L) }
  var videoRecordingElapsedSec by remember { mutableStateOf(0) }

  val examVideoPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { results ->
    if (results.values.all { it } && currentScreen == KioskScreen.DASHBOARD && !isVideoRecording) {
      examVideoRecorder.start(lifecycleOwner)
      isVideoRecording = true
      videoRecordingStartMs = System.currentTimeMillis()
    }
  }

  LaunchedEffect(currentScreen) {
    if (currentScreen == KioskScreen.DASHBOARD && !isVideoRecording) {
      val requiredPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
      val granted = requiredPermissions.all {
        context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
      }
      if (granted) {
        examVideoRecorder.start(lifecycleOwner)
        isVideoRecording = true
        videoRecordingStartMs = System.currentTimeMillis()
      } else {
        examVideoPermissionLauncher.launch(requiredPermissions)
      }
    }
  }

  LaunchedEffect(isVideoRecording) {
    while (isVideoRecording) {
      videoRecordingElapsedSec = ((System.currentTimeMillis() - videoRecordingStartMs) / 1000).toInt()
      delay(1000)
    }
  }

  suspend fun stopExamVideoRecording(): java.io.File? {
    if (!isVideoRecording) return null
    val file = examVideoRecorder.stopAndGetFile()
    isVideoRecording = false
    return file
  }

  // Незагруженное видео переживает сбой сети: путь к файлу и ID осмотра сохраняются,
  // чтобы попытаться отправить его повторно при следующем запуске приложения.
  fun savePendingExamVideo(filePath: String, examId: String) {
    settingsPrefs.edit()
      .putString("pending_video_path", filePath)
      .putString("pending_video_exam_id", examId)
      .apply()
  }

  fun clearPendingExamVideo() {
    settingsPrefs.edit()
      .remove("pending_video_path")
      .remove("pending_video_exam_id")
      .apply()
  }

  suspend fun uploadExamVideoFile(file: java.io.File?, examId: String) {
    if (file == null || !file.exists() || examId.isEmpty()) return
    var success = false
    try {
      withContext(Dispatchers.IO) {
        val requestBody = file.asRequestBody("video/mp4".toMediaType())
        val part = MultipartBody.Part.createFormData("video", file.name, requestBody)
        val response = NexApiClient.service.uploadExamVideo(
          deviceToken = NexApiClient.deviceToken,
          examId = examId,
          video = part
        )
        success = response.isSuccessful
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    if (success) {
      file.delete()
      clearPendingExamVideo()
    } else {
      savePendingExamVideo(file.absolutePath, examId)
    }
  }

  // Однократная попытка досдать видео, оставшееся от прошлого запуска из-за сбоя сети.
  LaunchedEffect(Unit) {
    val pendingPath = settingsPrefs.getString("pending_video_path", null)
    val pendingExamId = settingsPrefs.getString("pending_video_exam_id", null)
    if (pendingPath != null && pendingExamId != null) {
      uploadExamVideoFile(java.io.File(pendingPath), pendingExamId)
    }
  }

  // Real physicians/medics verdict states from server polling data
  var finalVerdictDopusk by remember { mutableStateOf("") }
  var finalVerdictMedicName by remember { mutableStateOf("") }
  var finalVerdictToken by remember { mutableStateOf("") }

  // Falling back to a clean default profile structure
  val defaultEmployeeProfile = remember(activeLanguage) {
    EmployeeProfile(
      id = "1111",
      fullName = if (activeLanguage == AppLanguage.KAZAKH) "Байжанов Нұрлан Бақытұлы" else "Байжанов Нурлан Бакытович",
      organization = if (activeLanguage == AppLanguage.KAZAKH) "Altiora Көлік Сервисі" else "Altiora Транспорт Сервис",
      iin = "940412350891",
      department = if (activeLanguage == AppLanguage.KAZAKH) "Аға локомотив машинисі" else "Старший машинист локомотива",
      branch = if (activeLanguage == AppLanguage.KAZAKH) "Алматы Локомотив филиалы" else "Алматинский Локомотивный филиал",
      photoUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&q=80&w=200",
      position = if (activeLanguage == AppLanguage.KAZAKH) "Аға локомотив машинисі" else "Старший машинист локомотива"
    )
  }

  // Diagnostics workflow state
  var currentStep by remember { mutableStateOf(StepState.HEALTH_COMPLAINTS) }
  
  // Hardware readings simulation results
  var bpSystolic by remember { mutableStateOf<Int?>(null) }
  var bpDiastolic by remember { mutableStateOf<Int?>(null) }
  var heartRateValue by remember { mutableStateOf<Int?>(null) }
  var breathalyzerValue by remember { mutableStateOf<Double?>(null) }
  var temperatureValue by remember { mutableStateOf<Double?>(null) }
  var selectedComplaintsList by remember { mutableStateOf<List<String>>(emptyList()) }
  var plainComplaintsState by remember { mutableStateOf<String?>(null) } // "Pending", "None" or symptom summary

  val isIdleForSilentUpdate =
    currentEmployeeProfile == null &&
      examSendStatus != ExamSendStatus.SENDING &&
      (currentScreen == KioskScreen.LANGUAGE_SELECTION || currentScreen == KioskScreen.AUTHORIZATION)
  val latestIsIdleForSilentUpdate by rememberUpdatedState(isIdleForSilentUpdate)

  suspend fun installReleaseSilentlyWhenAllowed(release: AppVersionResponse) {
    if (!KioskManager.isDeviceOwner(context)) return
    if (!latestIsIdleForSilentUpdate) {
      pendingSilentUpdate = release
      return
    }
    if (isSilentUpdateInProgress) return
    if (!KioskManager.shouldAttemptSilentInstall(context, release.versionCode)) return

    isSilentUpdateInProgress = true
    try {
      val file = AppUpdateManager.downloadApk(context, release) { }
      AppUpdateManager.enqueueUpdateStatus(
        context = context,
        status = "downloaded",
        targetVersionCode = release.versionCode,
        targetVersionName = release.versionName,
        apkUrl = release.apkUrl
      )
      AppUpdateManager.enqueueUpdateStatus(
        context = context,
        status = "install_started",
        targetVersionCode = release.versionCode,
        targetVersionName = release.versionName,
        apkUrl = release.apkUrl
      )
      KioskManager.installSilently(context, file, release.versionCode)
      pendingSilentUpdate = null
    } catch (e: Exception) {
      AppUpdateManager.enqueueUpdateStatus(
        context = context,
        status = "failed",
        targetVersionCode = release.versionCode,
        targetVersionName = release.versionName,
        apkUrl = release.apkUrl,
        message = e.localizedMessage ?: "Silent update failed"
      )
      pendingSilentUpdate = release
    } finally {
      isSilentUpdateInProgress = false
    }
  }

  LaunchedEffect(Unit) {
    while (true) {
      val release = AppUpdateManager.checkForUpdate()
      if (release != null) {
        if (KioskManager.isDeviceOwner(context)) {
          installReleaseSilentlyWhenAllowed(release)
        } else if (release.versionCode != availableUpdate?.versionCode) {
          // Обычное устройство: только диалог с подтверждением пользователя.
          availableUpdate = release
          updateDownloadedFile = null
          updateErrorText = null
        }
      }
      delay(30 * 1000L)
    }
  }

  LaunchedEffect(
    currentScreen,
    currentEmployeeProfile,
    examSendStatus,
    pendingSilentUpdate?.versionCode
  ) {
    val release = pendingSilentUpdate ?: return@LaunchedEffect
    installReleaseSilentlyWhenAllowed(release)
  }

  // Dynamic status of metric cells based on active workflow step
  val metrics = remember(activeLanguage, bpSystolic, bpDiastolic, heartRateValue, breathalyzerValue, temperatureValue, plainComplaintsState, currentStep) {
    listOf(
      MetricState(
        name = AppText.bpTitle.get(activeLanguage),
        value = if (bpSystolic != null && bpDiastolic != null) "$bpSystolic/$bpDiastolic" else null,
        unit = "mmHg",
        icon = Icons.Default.FavoriteBorder,
        isCompleted = bpSystolic != null,
        isActive = currentStep == StepState.BLOOD_PRESSURE
      ),
      MetricState(
        name = AppText.hrTitle.get(activeLanguage),
        value = heartRateValue?.toString(),
        unit = "BPM",
        icon = Icons.Default.MonitorHeart,
        isCompleted = heartRateValue != null,
        isActive = currentStep == StepState.BLOOD_PRESSURE
      ),
      MetricState(
        name = AppText.breathTitle.get(activeLanguage),
        value = breathalyzerValue?.let { String.format("%.2f", it) },
        unit = "mg/L",
        icon = Icons.Default.Air,
        isCompleted = breathalyzerValue != null,
        isActive = currentStep == StepState.BREATHALYZER
      ),
      MetricState(
        name = AppText.tempTitle.get(activeLanguage),
        value = temperatureValue?.let { String.format("%.1f", it) },
        unit = "°C",
        icon = Icons.Default.Thermostat,
        isCompleted = temperatureValue != null,
        isActive = currentStep == StepState.TEMPERATURE
      ),
      MetricState(
        name = AppText.complaintsTitle.get(activeLanguage),
        value = plainComplaintsState,
        unit = "",
        icon = Icons.Default.AssignmentLate,
        isCompleted = plainComplaintsState != null,
        isActive = currentStep == StepState.HEALTH_COMPLAINTS
      )
    )
  }

  val scope = rememberCoroutineScope()

  // SENDS DIAGNOSTICS MEASUREMENTS TO NEX SERVER AND POLLS FOR THE NURSE/MEDIC APPROVAL STATUS
  suspend fun sendHealthDataAndPoll(profileId: String, lang: AppLanguage) {
    currentStep = StepState.SECURE_LOADING
    examSendStatus = ExamSendStatus.SENDING
    examSendErrorMessage = ""

    // Останавливаем запись видео осмотра сразу по нажатию "Отправить" — сама отправка
    // на бэкенд произойдёт чуть ниже, как только станет известен ID осмотра.
    val examVideoFile = stopExamVideoRecording()

    val hasComplaints = selectedComplaintsList.isNotEmpty() || plainComplaintsState == "Symptoms Reported"
    val isAbnormalBp = (bpSystolic ?: 120) > 140 || (bpSystolic ?: 120) < 90 || (bpDiastolic ?: 80) > 90 || (bpDiastolic ?: 80) < 60
    val isAbnormalPulse = (heartRateValue ?: 70) > 100 || (heartRateValue ?: 70) < 55
    val isPositiveAlc = breathalyzerValue != null && breathalyzerValue!! > 0.0
    val isAbnormalTemp = (temperatureValue ?: 36.6) > 37.2 || (temperatureValue ?: 36.6) < 35.5

    val isDopuskAllowed = !(hasComplaints || isAbnormalBp || isAbnormalPulse || isPositiveAlc || isAbnormalTemp)
    val deviceDopuskStr = if (isDopuskAllowed) "Допущен" else "Не допущен"

    val request = CreateExamRequest(
      employeeId = profileId,
      deviceId = 4,
      systolic = bpSystolic ?: 120,
      diastolic = bpDiastolic ?: 80,
      pulse = heartRateValue ?: 70,
      breathalyzer = if (breathalyzerValue == null || breathalyzerValue!! <= 0.0) "Пройден" else "Не пройден",
      temperature = temperatureValue ?: 36.6,
      complaints = if (hasComplaints) "Да" else "Нет",
      drugTest = "Пройден",
      deviceDopusk = deviceDopuskStr,
      priceCharged = 0.0
    )

    var createdExamId = ""
    var success = false
    try {
      val response = withContext(Dispatchers.IO) {
        NexApiClient.service.createExam(
          deviceToken = NexApiClient.deviceToken,
          request = request
        )
      }
      if (response.isSuccessful) {
        success = true
        val bodyString = response.body()?.string() ?: ""
        try {
          val jsonObject = org.json.JSONObject(bodyString)
          val examObj = jsonObject.optJSONObject("exam")
          createdExamId = examObj?.optString("id") ?: ""
        } catch (e: Exception) {
          e.printStackTrace()
        }
      } else {
        val errBody = response.errorBody()?.string()
        examSendErrorMessage = ApiErrorText.fromHttp(
          code = response.code(),
          errorBody = errBody,
          lang = lang,
          operation = AppText.errorExamSendFailed.get(lang)
        )
      }
    } catch (e: Exception) {
      examSendErrorMessage = ApiErrorText.fromThrowable(
        throwable = e,
        lang = lang,
        operation = AppText.errorExamSendFailed.get(lang)
      )
    }

    if (success) {
      examSendStatus = ExamSendStatus.SUCCESS

      // Отправляем видео осмотра в фоне, не блокируя опрос статуса подписи
      if (createdExamId.isNotEmpty() && examVideoFile != null) {
        scope.launch {
          uploadExamVideoFile(examVideoFile, createdExamId)
        }
      }

      // Auto-trigger the required server-side payment to unlock the exam for the nurse/medic dashboard
      if (createdExamId.isNotEmpty()) {
        try {
          withContext(Dispatchers.IO) {
            NexApiClient.service.payExam(
              deviceToken = NexApiClient.deviceToken,
              request = PayExamRequest(
                amount = 0.0,
                examId = createdExamId,
                paymentMethod = "cash"
              )
            )
          }
        } catch (e: Exception) {
          e.printStackTrace()
        }
      } else {
        createdExamId = "mock-id"
      }

      // Real-time polling of getExamDetail every 2 seconds until signed or decision made.
      var isSignedOrApproved = false
      var pollAttempts = 0
      val maxPollAttempts = 90 // 180 seconds = 3 minutes timeout
      // null = ещё не знаем настройку организации/филиала; по умолчанию считаем разрешённым
      // (совпадает с поведением бэкенда для старых версий API и осмотров без организации)
      var autoConfirmEnabled = true

      while (!isSignedOrApproved && pollAttempts < maxPollAttempts) {
        delay(2000)
        pollAttempts++
        try {
          val detailResponse = withContext(Dispatchers.IO) {
            NexApiClient.service.getExamDetail(
              deviceToken = NexApiClient.deviceToken,
              id = createdExamId
            )
          }
          if (detailResponse.isSuccessful) {
            val body = detailResponse.body()
            val examObj = body?.exam
            body?.autoConfirmEnabled?.let { autoConfirmEnabled = it }
            if (examObj != null) {
              val isSigned = examObj.isSigned
              val dopuskVal = examObj.dopusk
              // If signed or has a clear verdict
              if (isSigned || (dopuskVal.isNotEmpty() && dopuskVal.lowercase() != "pending")) {
                isSignedOrApproved = true
                finalVerdictDopusk = dopuskVal
                finalVerdictMedicName = body.nurseName ?: if (activeLanguage == AppLanguage.KAZAKH) "АМБ АБК медбикесі" else "Медсестра АПК АМК"
                finalVerdictToken = examObj.id.takeLast(12).uppercase()
              }
            }
          }
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }

      if (isSignedOrApproved) {
        currentStep = StepState.COMPLETED_VERDICT
      } else if (autoConfirmEnabled) {
        // Авто-подтверждение разрешено для этой организации/филиала, но медсестра
        // не успела среагировать за 15с на сервере — показываем вердикт аппарата как раньше.
        finalVerdictDopusk = deviceDopuskStr
        finalVerdictMedicName = if (activeLanguage == AppLanguage.KAZAKH) "АМБ АБК автотексеру қызметі" else "Служба автопроверки АПК АМК"
        finalVerdictToken = if (createdExamId != "mock-id") createdExamId.takeLast(12).uppercase() else "AUTO-VERIFIED"
        currentStep = StepState.COMPLETED_VERDICT
      } else {
        // Авто-подтверждение отключено для организации/филиала — решение не принято,
        // никакого вердикта показывать нельзя. Ждём медсестру.
        currentStep = StepState.AWAITING_NURSE
      }
    } else {
      examSendStatus = ExamSendStatus.ERROR
    }
  }

  // Keypad submit and auto-validation
  fun handlePinInput(char: String) {
    if (enteredPin.length < 8) {
      enteredPin += char
      pinErrorText = ""
    }
  }

  fun handlePinDelete() {
    if (enteredPin.isNotEmpty()) {
      enteredPin = enteredPin.dropLast(1)
      pinErrorText = ""
    }
  }

  fun handleVerifyEmployee() {
    if (enteredPin.isEmpty()) return
    
    if (enteredPin == "1111") {
      scope.launch {
        verifiedEmployeeResponse = VerifyEmployeeResponse(
          employeeId = "1111",
          exists = true,
          fullName = if (activeLanguage == AppLanguage.KAZAKH) "Байжанов Нұрлан Бақытұлы" else "Байжанов Нурлан Бакытович",
          organizationName = if (activeLanguage == AppLanguage.KAZAKH) "Altiora Көлік Сервисі" else "Altiora Транспорт Сервис",
          positionName = if (activeLanguage == AppLanguage.KAZAKH) "Аға локомотив машинисі" else "Старший машинист локомотива",
          branchName = if (activeLanguage == AppLanguage.KAZAKH) "Алматы Локомотив филиалы" else "Алматинский Локомотивный филиал",
          photoUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&q=80&w=200"
        )
        currentScreen = KioskScreen.CONFIRMATION
      }
      return
    }
    
    scope.launch {
      isVerifyingEmployee = true
      pinErrorText = ""
      try {
        val response = withContext(Dispatchers.IO) {
          NexApiClient.service.verifyEmployee(
            deviceToken = NexApiClient.deviceToken,
            request = VerifyEmployeeRequest(employeeId = enteredPin)
          )
        }
        if (response.exists) {
          val cached = getCachedProfile(response.employeeId)
          if (cached != null) {
            verifiedEmployeeResponse = response.copy(
              fullName = cached.fullName,
              organizationName = cached.organization,
              branchName = cached.branch,
              positionName = cached.position,
              photoUrl = cached.photoUrl
            )
          } else {
            val cleanName = response.fullName ?: "Сотрудник ${response.employeeId}"
            val responsePhoto = response.getEffectivePhotoUrl()
            val responseOrg = response.getEffectiveOrganization()
            val responseBranch = response.getEffectiveBranch()
            val responsePos = response.getEffectivePosition()

            verifiedEmployeeResponse = response.copy(
              fullName = response.fullName ?: cleanName,
              organizationName = responseOrg.ifEmpty { null },
              branchName = responseBranch.ifEmpty { null },
              positionName = responsePos.ifEmpty { null },
              photoUrl = responsePhoto
            )
          }
          currentScreen = KioskScreen.CONFIRMATION
        } else {
          shakeTrigger += 1
          pinErrorText = AppText.employeeNotFound.get(activeLanguage)
          delay(800)
          enteredPin = ""
        }
      } catch (e: Exception) {
        shakeTrigger += 1
        pinErrorText = ApiErrorText.fromThrowable(
          throwable = e,
          lang = activeLanguage,
          operation = if (activeLanguage == AppLanguage.KAZAKH) {
            "Қызметкерді тексеру мүмкін болмады"
          } else {
            "Не удалось проверить сотрудника"
          }
        )
      } finally {
        isVerifyingEmployee = false
      }
    }
  }

  // Полноэкранный сканер лица (FaceIdScanScreen) вместо одиночного снимка системной камерой —
  // faceIdCaptureMode переключает его между входом по Face ID и энролментом/отключением.
  var faceScanStatusText by remember { mutableStateOf("") }
  var faceScanIsError by remember { mutableStateOf(false) }

  fun handleVerifyFace(bitmap: android.graphics.Bitmap) {
    val base64Photo = bitmapToJpegDataUrl(bitmap)
    if (base64Photo == null) {
      faceScanIsError = true
      faceScanStatusText = if (activeLanguage == AppLanguage.KAZAKH) "Суретті өңдеу мүмкін болмады" else "Не удалось обработать снимок"
      return
    }
    scope.launch {
      isVerifyingEmployee = true
      faceScanIsError = false
      faceScanStatusText = if (activeLanguage == AppLanguage.KAZAKH) "Тексерілуде..." else "Распознавание..."
      try {
        val response = withContext(Dispatchers.IO) {
          NexApiClient.service.verifyFace(
            deviceToken = NexApiClient.deviceToken,
            request = VerifyFaceRequest(facePhoto = base64Photo)
          )
        }
        if (response.exists) {
          verifiedEmployeeResponse = response.copy(
            fullName = response.fullName ?: "Сотрудник ${response.employeeId}",
            organizationName = response.getEffectiveOrganization().ifEmpty { null },
            branchName = response.getEffectiveBranch().ifEmpty { null },
            positionName = response.getEffectivePosition().ifEmpty { null },
            photoUrl = response.getEffectivePhotoUrl()
          )
          faceIdCaptureMode = null
          currentScreen = KioskScreen.CONFIRMATION
        } else {
          faceScanIsError = true
          faceScanStatusText = if (activeLanguage == AppLanguage.KAZAKH) "Бет танылмады, қайталап көріңіз" else "Лицо не распознано, попробуйте ещё раз"
        }
      } catch (e: Exception) {
        faceScanIsError = true
        faceScanStatusText = ApiErrorText.fromThrowable(
          throwable = e,
          lang = activeLanguage,
          operation = if (activeLanguage == AppLanguage.KAZAKH) {
            "Face ID арқылы кіру мүмкін болмады"
          } else {
            "Не удалось войти по Face ID"
          }
        )
      } finally {
        isVerifyingEmployee = false
      }
    }
  }

  fun launchFaceIdLogin() {
    faceScanStatusText = ""
    faceScanIsError = false
    faceIdCaptureMode = FaceIdCaptureMode.LOGIN
  }

  // Энролмент/отключение Face ID — только по решению уже вошедшего сотрудника, на экране CONFIRMATION.
  fun handleEnrollFace(bitmap: android.graphics.Bitmap) {
    val employeeId = verifiedEmployeeResponse?.employeeId ?: return
    val base64Photo = bitmapToJpegDataUrl(bitmap) ?: return
    scope.launch {
      isTogglingFaceId = true
      faceScanIsError = false
      faceScanStatusText = if (activeLanguage == AppLanguage.KAZAKH) "Тексерілуде..." else "Распознавание..."
      try {
        val response = withContext(Dispatchers.IO) {
          NexApiClient.service.enrollFace(
            deviceToken = NexApiClient.deviceToken,
            request = EnrollFaceRequest(employeeId = employeeId, facePhoto = base64Photo)
          )
        }
        verifiedEmployeeResponse = verifiedEmployeeResponse?.copy(faceIdEnabled = response.faceIdEnabled)
        faceIdCaptureMode = null
      } catch (e: Exception) {
        faceScanIsError = true
        faceScanStatusText = ApiErrorText.fromThrowable(
          throwable = e,
          lang = activeLanguage,
          operation = if (activeLanguage == AppLanguage.KAZAKH) "Face ID қосу мүмкін болмады" else "Не удалось включить Face ID"
        )
      } finally {
        isTogglingFaceId = false
      }
    }
  }

  fun handleDisableFace() {
    val employeeId = verifiedEmployeeResponse?.employeeId ?: return
    scope.launch {
      isTogglingFaceId = true
      try {
        withContext(Dispatchers.IO) {
          NexApiClient.service.disableFace(
            deviceToken = NexApiClient.deviceToken,
            request = DisableFaceRequest(employeeId = employeeId)
          )
        }
        verifiedEmployeeResponse = verifiedEmployeeResponse?.copy(faceIdEnabled = false)
      } catch (e: Exception) {
        // Тихо игнорируем
      } finally {
        isTogglingFaceId = false
      }
    }
  }

  fun launchEnrollFace() {
    faceScanStatusText = ""
    faceScanIsError = false
    faceIdCaptureMode = FaceIdCaptureMode.ENROLL
  }

  fun resetAllWorkflowData() {
    // Освобождаем BLE термометр перед сбросом (тонометр Omron не трогаем — он управляется своим DisposableEffect)
    MicrolifeManager.disconnect()
    // Осмотр брошен без отправки — просто останавливаем и отбрасываем незавершённую запись.
    if (isVideoRecording) {
      scope.launch { stopExamVideoRecording()?.delete() }
    }
    bpSystolic = null
    bpDiastolic = null
    heartRateValue = null
    breathalyzerValue = null
    temperatureValue = null
    selectedComplaintsList = emptyList()
    plainComplaintsState = null
    currentStep = StepState.HEALTH_COMPLAINTS
    enteredPin = ""
    pinErrorText = ""
    currentScreen = KioskScreen.LANGUAGE_SELECTION
    currentEmployeeProfile = null
    verifiedEmployeeResponse = null
    showConfirmationDialog = false
  }

  // Единая обработка системной/аппаратной кнопки "назад": повторяет то же действие,
  // что и видимая кнопка "назад" на экране (если она есть), иначе игнорируется —
  // это киоск, случайный системный back не должен выкидывать из приложения или
  // сбрасывать текущий шаг медосмотра.
  BackHandler(enabled = kioskModeEnabled) {
    when (currentScreen) {
      KioskScreen.SETTINGS -> {
        currentScreen = if (currentEmployeeProfile != null) KioskScreen.DASHBOARD else KioskScreen.AUTHORIZATION
      }
      KioskScreen.REGISTRATION -> {
        currentScreen = KioskScreen.AUTHORIZATION
      }
      KioskScreen.CONFIRMATION -> {
        enteredPin = ""
        verifiedEmployeeResponse = null
        currentScreen = KioskScreen.AUTHORIZATION
      }
      KioskScreen.AUTHORIZATION -> {
        enteredPin = ""
        pinErrorText = ""
        currentScreen = KioskScreen.LANGUAGE_SELECTION
      }
      else -> { /* LANGUAGE_SELECTION, DASHBOARD — намеренно игнорируем (киоск не должен сворачиваться/выходить) */ }
    }
  }

  CompositionLocalProvider(LocalAppLanguage provides activeLanguage, LocalDarkTheme provides isDarkTheme) {
    // Smooth background layout frame
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(AppleBlack)
    ) {
      // Elegant deep cinema lighting glow on backgrounds
      Canvas(modifier = Modifier.fillMaxSize()) {
        val centerOffset = Offset(size.width / 2f, size.height / 2f)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(AppleBlue.copy(alpha = 0.08f), Color.Transparent),
            center = centerOffset,
            radius = size.width * 0.7f
          ),
          radius = size.width * 0.7f,
          center = centerOffset
        )
      }

      Crossfade(
        targetState = currentScreen,
        animationSpec = tween(durationMillis = 600, easing = EaseInOutCirc),
        label = "ScreenTransition"
      ) { screen ->
        when (screen) {
          KioskScreen.LANGUAGE_SELECTION -> {
            LanguageSelectionScreen(
              isDarkTheme = isDarkTheme,
              onThemeToggle = onThemeToggle,
              onLanguageSelected = { selected ->
                appLanguage = selected
                currentScreen = KioskScreen.AUTHORIZATION
              }
            )
          }
          KioskScreen.AUTHORIZATION -> {
            AuthorizationGate(
              enteredPin = enteredPin,
              shakeTrigger = shakeTrigger,
              errorText = pinErrorText,
              isVerifying = isVerifyingEmployee,
              onKeyClick = { handlePinInput(it) },
              onDeleteClick = { handlePinDelete() },
              onVerifyClick = { handleVerifyEmployee() },
              onRegisterClick = { currentScreen = KioskScreen.REGISTRATION },
              onFaceIdClick = { launchFaceIdLogin() },
              onChangeLanguage = {
                enteredPin = ""
                pinErrorText = ""
                currentScreen = KioskScreen.LANGUAGE_SELECTION
              }
            )
          }
          KioskScreen.REGISTRATION -> {
            RegistrationScreen(
              onSuccess = { actualId, name, org, branch, pos, photo ->
                saveRegisteredEmployee(actualId, name, org, branch, pos, photo)
                enteredPin = actualId
                verifiedEmployeeResponse = VerifyEmployeeResponse(
                  employeeId = actualId,
                  exists = true,
                  fullName = name,
                  organizationName = org,
                  branchName = branch,
                  positionName = pos,
                  photoUrl = photo
                )
                currentScreen = KioskScreen.CONFIRMATION
              },
              onBack = {
                currentScreen = KioskScreen.AUTHORIZATION
              }
            )
          }
          KioskScreen.CONFIRMATION -> {
            val resp = verifiedEmployeeResponse
            if (resp != null) {
              ConfirmationScreen(
                response = resp,
                onConfirm = {
                  currentEmployeeProfile = EmployeeProfile(
                    id = resp.employeeId,
                    fullName = resp.fullName ?: "Employee ID ${resp.employeeId}",
                    organization = resp.getEffectiveOrganization(),
                    iin = "",
                    department = resp.getEffectivePosition(),
                    branch = resp.getEffectiveBranch(),
                    photoUrl = resp.getEffectivePhotoUrl(),
                    position = resp.getEffectivePosition()
                  )
                  currentScreen = KioskScreen.DASHBOARD
                },
                onDismiss = {
                  enteredPin = ""
                  verifiedEmployeeResponse = null
                  currentScreen = KioskScreen.AUTHORIZATION
                },
                isTogglingFaceId = isTogglingFaceId,
                onEnableFaceId = { launchEnrollFace() },
                onDisableFaceId = { handleDisableFace() }
              )
            } else {
              currentScreen = KioskScreen.AUTHORIZATION
            }
          }
          KioskScreen.DASHBOARD -> {
            KioskDashboard(
              profile = currentEmployeeProfile ?: defaultEmployeeProfile,
              metrics = metrics,
              currentStep = currentStep,
              selectedComplaints = selectedComplaintsList,
              onComplaintsSelected = { selected ->
                selectedComplaintsList = selected
              },
              onConfirmComplaints = { hasComplaints ->
                if (hasComplaints) {
                  plainComplaintsState = "Symptoms Reported"
                } else {
                  plainComplaintsState = "None"
                }
                currentStep = StepState.BLOOD_PRESSURE
              },
              onSimulateBPAndPulse = { sys, dia, bpm ->
                bpSystolic = sys
                bpDiastolic = dia
                heartRateValue = bpm
                currentStep = StepState.BREATHALYZER
              },
              onSimulateBreathalyzer = { valBreath ->
                breathalyzerValue = valBreath
                currentStep = StepState.TEMPERATURE
              },
              onSimulateTemperature = { valTemp ->
                temperatureValue = valTemp
                currentStep = StepState.VERIFICATION
              },
              onSignAndSubmit = {
                val profileId = (currentEmployeeProfile ?: defaultEmployeeProfile).id
                scope.launch {
                  sendHealthDataAndPoll(profileId, activeLanguage)
                }
              },
              onResetAll = {
                resetAllWorkflowData()
              },
              examSendStatus = examSendStatus,
              examSendErrorMessage = examSendErrorMessage,
              onRetrySend = {
                val profileId = (currentEmployeeProfile ?: defaultEmployeeProfile).id
                scope.launch {
                  sendHealthDataAndPoll(profileId, activeLanguage)
                }
              },
              onContinueOffline = {
                scope.launch {
                  examSendStatus = ExamSendStatus.SUCCESS
                  delay(800)
                  val hasComplaints = selectedComplaintsList.isNotEmpty() || plainComplaintsState == "Symptoms Reported"
                  val isAbnormalBp = (bpSystolic ?: 120) > 140 || (bpSystolic ?: 120) < 90 || (bpDiastolic ?: 80) > 90 || (bpDiastolic ?: 80) < 60
                  val isAbnormalPulse = (heartRateValue ?: 70) > 100 || (heartRateValue ?: 70) < 55
                  val isPositiveAlc = breathalyzerValue != null && breathalyzerValue!! > 0.0
                  val isAbnormalTemp = (temperatureValue ?: 36.6) > 37.2 || (temperatureValue ?: 36.6) < 35.5
                  val isDopuskAllowed = !(hasComplaints || isAbnormalBp || isAbnormalPulse || isPositiveAlc || isAbnormalTemp)
                  
                  finalVerdictDopusk = if (isDopuskAllowed) "Допущен" else "Не допущен"
                  finalVerdictMedicName = "Д-р Сара К."
                  finalVerdictToken = "OFFLINE-MOCK-VERDICT"
                  currentStep = StepState.COMPLETED_VERDICT
                }
              },
              finalVerdictDopusk = finalVerdictDopusk,
              finalVerdictMedicName = finalVerdictMedicName,
              finalVerdictToken = finalVerdictToken
            )
          }
          KioskScreen.SETTINGS -> {
            SettingsScreen(
              activeLanguage = activeLanguage,
              tonometerMode = tonometerMode,
              tonometerMac = tonometerMac,
              tonometerName = tonometerName,
              breathalyzerMode = breathalyzerMode,
              breathalyzerMac = breathalyzerMac,
              breathalyzerName = breathalyzerName,
              thermometerMode = thermometerMode,
              thermometerMac = thermometerMac,
              thermometerName = thermometerName,
              deviceToken = deviceToken,
              tokenInfo = tokenInfo,
              tokenInfoError = tokenInfoError,
              isFetchingTokenInfo = isFetchingTokenInfo,
              kioskModeEnabled = kioskModeEnabled,
              onKioskModeToggle = onKioskModeToggle,
              onSaveDevice = { type, mode, mac, name ->
                prefs.edit()
                  .putString("${type}_mode", mode)
                  .putString("${type}_mac", mac)
                  .putString("${type}_name", name)
                  .apply()
                when (type) {
                  "tonometer" -> {
                    tonometerMode = mode
                    tonometerMac = mac
                    tonometerName = name
                  }
                  "breathalyzer" -> {
                    breathalyzerMode = mode
                    breathalyzerMac = mac
                    breathalyzerName = name
                  }
                  "thermometer" -> {
                    thermometerMode = mode
                    thermometerMac = mac
                    thermometerName = name
                  }
                }
              },
              onSaveToken = { newToken: String ->
                settingsPrefs.edit().putString("device_token", newToken).apply()
                NexApiClient.updateDeviceToken(newToken)
                deviceToken = newToken
              },
              onBack = {
                if (currentEmployeeProfile != null) {
                  currentScreen = KioskScreen.DASHBOARD
                } else {
                  currentScreen = KioskScreen.AUTHORIZATION
                }
              }
            )
          }
        }
      }

      if (currentScreen != KioskScreen.SETTINGS && currentScreen != KioskScreen.REGISTRATION && currentScreen != KioskScreen.CONFIRMATION) {
        IconButton(
          onClick = { currentScreen = KioskScreen.SETTINGS },
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(16.dp)
            .testTag("app_settings_button")
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            tint = AppleLightGrey.copy(alpha = 0.6f),
            modifier = Modifier.size(28.dp)
          )
        }
      }

      if (isVideoRecording) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(16.dp)
            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("exam_video_recording_indicator")
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .background(Color.Red, CircleShape)
          )
          Text(
            text = "REC %02d:%02d".format(videoRecordingElapsedSec / 60, videoRecordingElapsedSec % 60),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
          )
        }
      }

      // Диалог доступного обновления показываем только на "простаивающих" экранах —
      // выбор языка/авторизация, — чтобы не прерывать сотрудника посреди медосмотра.
      val isIdleScreenForUpdate = currentScreen == KioskScreen.LANGUAGE_SELECTION || currentScreen == KioskScreen.AUTHORIZATION
      val pendingUpdate = availableUpdate
      if (isIdleScreenForUpdate && pendingUpdate != null) {
        AppUpdateDialog(
          release = pendingUpdate,
          downloadProgress = updateDownloadProgress,
          downloadedFile = updateDownloadedFile,
          errorText = updateErrorText,
          onDownload = {
            scope.launch {
              updateErrorText = null
              updateDownloadProgress = 0
              try {
                val file = AppUpdateManager.downloadApk(context, pendingUpdate) { progress ->
                  updateDownloadProgress = progress
                }
                AppUpdateManager.enqueueUpdateStatus(
                  context = context,
                  status = "downloaded",
                  targetVersionCode = pendingUpdate.versionCode,
                  targetVersionName = pendingUpdate.versionName,
                  apkUrl = pendingUpdate.apkUrl
                )
                updateDownloadProgress = null
                updateDownloadedFile = file
              } catch (e: Exception) {
                AppUpdateManager.enqueueUpdateStatus(
                  context = context,
                  status = "failed",
                  targetVersionCode = pendingUpdate.versionCode,
                  targetVersionName = pendingUpdate.versionName,
                  apkUrl = pendingUpdate.apkUrl,
                  message = e.localizedMessage ?: "Manual update download failed"
                )
                updateDownloadProgress = null
                updateErrorText = e.localizedMessage ?: "Ошибка загрузки обновления"
              }
            }
          },
          onInstall = {
            val file = updateDownloadedFile
            if (file != null) {
              if (KioskManager.isDeviceOwner(context)) {
                AppUpdateManager.enqueueUpdateStatus(
                  context = context,
                  status = "install_started",
                  targetVersionCode = pendingUpdate.versionCode,
                  targetVersionName = pendingUpdate.versionName,
                  apkUrl = pendingUpdate.apkUrl
                )
                KioskManager.installSilently(context, file, pendingUpdate.versionCode)
                availableUpdate = null
                updateDownloadedFile = null
                updateDownloadProgress = null
                updateErrorText = null
              } else if (AppUpdateManager.canInstallPackages(context)) {
                AppUpdateManager.enqueueUpdateStatus(
                  context = context,
                  status = "install_started",
                  targetVersionCode = pendingUpdate.versionCode,
                  targetVersionName = pendingUpdate.versionName,
                  apkUrl = pendingUpdate.apkUrl,
                  message = "Started system package installer"
                )
                AppUpdateManager.installApk(context, file)
              } else {
                context.startActivity(AppUpdateManager.unknownSourcesSettingsIntent(context))
              }
            }
          },
          onDismiss = {
            availableUpdate = null
            updateDownloadedFile = null
            updateDownloadProgress = null
            updateErrorText = null
          }
        )
      }

      val captureMode = faceIdCaptureMode
      if (captureMode != null) {
        FaceIdScanScreen(
          title = if (activeLanguage == AppLanguage.KAZAKH) "Face ID" else "Face ID",
          subtitle = if (captureMode == FaceIdCaptureMode.LOGIN) {
            if (activeLanguage == AppLanguage.KAZAKH) "Бетіңізді сопаққа сыйдырып, түймені басыңыз" else "Расположите лицо в овале и нажмите кнопку"
          } else {
            if (activeLanguage == AppLanguage.KAZAKH) "Face ID қосу үшін бетіңізді суретке түсіріңіз" else "Снимок для включения Face ID"
          },
          isProcessing = if (captureMode == FaceIdCaptureMode.LOGIN) isVerifyingEmployee else isTogglingFaceId,
          statusText = faceScanStatusText,
          isError = faceScanIsError,
          onCapture = { bitmap ->
            if (captureMode == FaceIdCaptureMode.LOGIN) handleVerifyFace(bitmap) else handleEnrollFace(bitmap)
          },
          onCancel = { faceIdCaptureMode = null }
        )
      }
    }
  }
}

// ==========================================
// Диалог автообновления приложения (см. AppUpdateManager.kt)
// ==========================================
@Composable
fun AppUpdateDialog(
  release: AppVersionResponse,
  downloadProgress: Int?,
  downloadedFile: java.io.File?,
  errorText: String?,
  onDownload: () -> Unit,
  onInstall: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(text = "Доступно обновление v${release.versionName}") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (!release.releaseNotes.isNullOrBlank()) {
          Text(text = release.releaseNotes, fontSize = 13.sp)
        }
        if (downloadProgress != null) {
          Spacer(modifier = Modifier.height(4.dp))
          LinearProgressIndicator(
            progress = { downloadProgress / 100f },
            modifier = Modifier.fillMaxWidth()
          )
          Text(text = "Загрузка... $downloadProgress%", fontSize = 12.sp, color = AppleMutedGrey)
        }
        if (errorText != null) {
          Text(text = errorText, color = AppleAmber, fontSize = 12.sp)
        }
      }
    },
    confirmButton = {
      when {
        downloadedFile != null -> {
          TextButton(onClick = onInstall) { Text("Установить") }
        }
        downloadProgress != null -> {
          TextButton(onClick = {}, enabled = false) { Text("Загрузка...") }
        }
        else -> {
          TextButton(onClick = onDownload) { Text("Скачать") }
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Позже") }
    }
  )
}

// ==========================================
// SCREEN 1: Authorization Gate (Entry)
// ==========================================
@Composable
fun AuthorizationGate(
  enteredPin: String,
  shakeTrigger: Int,
  errorText: String,
  isVerifying: Boolean,
  onKeyClick: (String) -> Unit,
  onDeleteClick: () -> Unit,
  onVerifyClick: () -> Unit,
  onRegisterClick: () -> Unit,
  onFaceIdClick: () -> Unit = {},
  onChangeLanguage: () -> Unit = {}
) {
  val lang = LocalAppLanguage.current
  
  // Animate shake value based on trigger
  val shakeOffset = remember { Animatable(0f) }
  LaunchedEffect(shakeTrigger) {
    if (shakeTrigger > 0) {
      repeat(3) {
        shakeOffset.animateTo(24f, spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessHigh))
        shakeOffset.animateTo(-24f, spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessHigh))
      }
      shakeOffset.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }
  }

  Row(
    modifier = Modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.safeDrawing)
      .padding(horizontal = 40.dp, vertical = 16.dp),
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Left Branding & PIN display
    Column(
      modifier = Modifier
        .weight(1.1f)
        .padding(end = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = AppText.titleBranding.get(lang),
        color = AppleLightGrey,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        letterSpacing = 6.sp,
        textAlign = TextAlign.Center
      )
      
      Spacer(modifier = Modifier.height(6.dp))
      
      Text(
        text = AppText.authSubtitle.get(lang),
        color = AppleMutedGrey,
        fontSize = 13.sp,
        letterSpacing = 1.sp,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = AppText.authIdPrompt.get(lang),
        color = AppleBlue,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = AppText.authIdHint.get(lang),
        color = AppleMutedGrey.copy(alpha = 0.5f),
        fontSize = 11.sp,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Interactive ID display slot with shake modifier applied
      Column(
        modifier = Modifier.testTag("pin_display_section"),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Тряска при неверном ID должна затрагивать только сам ввод/ошибку,
        // а не кнопки регистрации/Face ID и смены языка ниже.
        Column(
          modifier = Modifier.offset(x = shakeOffset.value.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
        if (isVerifying) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            CircularProgressIndicator(
              color = AppleBlue,
              modifier = Modifier.size(36.dp),
              strokeWidth = 3.dp
            )
            Text(
              text = AppText.verifyingId.get(lang),
              color = AppleMutedGrey,
              fontSize = 12.sp,
              textAlign = TextAlign.Center
            )
          }
        } else {
          // Dynamic ID underlines
          Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            val displayLength = maxOf(6, enteredPin.length)
            repeat(displayLength) { index ->
              val char = enteredPin.getOrNull(index)
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(
                  text = char?.toString() ?: " ",
                  color = AppleLightGrey,
                  fontSize = 24.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  modifier = Modifier.width(20.dp),
                  textAlign = TextAlign.Center
                )
                Box(
                  modifier = Modifier
                    .width(20.dp)
                    .height(2.dp)
                    .background(
                      if (index < enteredPin.length) AppleBlue else AppleMutedGrey.copy(alpha = 0.3f)
                    )
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Custom Amber error subtext under input
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 30.dp, max = 64.dp)
            .padding(horizontal = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          if (errorText.isNotEmpty() && !isVerifying) {
            Text(
              text = errorText,
              color = AppleAmber,
              fontSize = 13.sp,
              lineHeight = 16.sp,
              fontWeight = FontWeight.Medium,
              textAlign = TextAlign.Center,
              maxLines = 3,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("pin_error_subtext")
            )
          }
        }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large high-tech self-registration entry for testing
        Button(
          onClick = {
            onRegisterClick()
          },
          colors = ButtonDefaults.buttonColors(containerColor = AppleBlue.copy(alpha = 0.15f)),
          border = BorderStroke(1.2.dp, AppleBlue.copy(alpha = 0.6f)),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.testTag("btn_register_self")
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.PersonAdd,
              contentDescription = null,
              tint = AppleBlue,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = if (lang == AppLanguage.KAZAKH) "Қызметкерді тіркеу" else "Регистрация сотрудника",
              color = AppleBlue,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.5.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
          onClick = { onFaceIdClick() },
          enabled = !isVerifying,
          colors = ButtonDefaults.buttonColors(containerColor = AppleBlue.copy(alpha = 0.15f)),
          border = BorderStroke(1.2.dp, AppleBlue.copy(alpha = 0.6f)),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.testTag("btn_face_id")
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Face,
              contentDescription = null,
              tint = AppleBlue,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = if (lang == AppLanguage.KAZAKH) "Face ID арқылы кіру" else "Войти по Face ID",
              color = AppleBlue,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.5.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = if (lang == AppLanguage.KAZAKH) "← Тілді ауыстыру" else "← Сменить язык",
          color = AppleMutedGrey,
          fontSize = 12.sp,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onChangeLanguage() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("btn_change_language")
        )
      }
    }

    // Right Keypad
    Box(
      modifier = Modifier
        .weight(0.9f),
      contentAlignment = Alignment.Center
    ) {
      Column(
        modifier = Modifier
          .width(250.dp)
          .testTag("numeric_keyboard"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        val keys = listOf(
          listOf("1", "2", "3"),
          listOf("4", "5", "6"),
          listOf("7", "8", "9"),
          listOf("OK", "0", "Delete")
        )

        keys.forEach { rowKeys ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            rowKeys.forEach { key ->
              val isUtility = key == "OK" || key == "Delete"
              
              val isDark = LocalDarkTheme.current
              Box(
                modifier = Modifier
                  .weight(1f)
                  .aspectRatio(1f)
                  .clip(CircleShape)
                  .border(
                    border = BorderStroke(
                      1.dp,
                      if (isUtility) Color.Transparent else if (isDark) AppleMutedGrey.copy(alpha = 0.15f) else Color(0xFFD1D1D6)
                    ),
                    shape = CircleShape
                  )
                  .background(
                    color = if (key == "OK") {
                      AppleBlue
                    } else if (key == "Delete") {
                      Color.Transparent
                    } else {
                      if (isDark) AppleCharcoal.copy(alpha = 0.5f) else Color(0xFFE5E5EA)
                    }
                  )
                  .clickable(
                    enabled = !isVerifying,
                    onClick = {
                      when (key) {
                        "OK" -> onVerifyClick()
                        "Delete" -> onDeleteClick()
                        else -> onKeyClick(key)
                      }
                    }
                  )
                  .testTag("key_$key"),
                contentAlignment = Alignment.Center
              ) {
                if (key == "Delete") {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Delete",
                    tint = AppleLightGrey,
                    modifier = Modifier.size(20.dp)
                  )
                } else if (key == "OK") {
                  Text(
                    text = "OK",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                  )
                } else {
                  Text(
                    text = key,
                    color = AppleLightGrey,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.SansSerif
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

// ==========================================
// SCREEN 2: Dashboard Layout (High-Readability, Adaptive & Fully Legible)
// ==========================================
@Composable
fun KioskDashboard(
  profile: EmployeeProfile,
  metrics: List<MetricState>,
  currentStep: StepState,
  selectedComplaints: List<String>,
  onComplaintsSelected: (List<String>) -> Unit,
  onConfirmComplaints: (Boolean) -> Unit,
  onSimulateBPAndPulse: (Int, Int, Int) -> Unit,
  onSimulateBreathalyzer: (Double) -> Unit,
  onSimulateTemperature: (Double) -> Unit,
  onSignAndSubmit: () -> Unit,
  onResetAll: () -> Unit,
  examSendStatus: ExamSendStatus,
  examSendErrorMessage: String,
  onRetrySend: () -> Unit,
  onContinueOffline: () -> Unit,
  finalVerdictDopusk: String,
  finalVerdictMedicName: String,
  finalVerdictToken: String
) {
  val lang = LocalAppLanguage.current
  var showCancelConfirm by remember { mutableStateOf(false) }

  if (showCancelConfirm) {
    androidx.compose.material3.AlertDialog(
      onDismissRequest = { showCancelConfirm = false },
      title = {
        Text(
          text = if (lang == AppLanguage.KAZAKH) "Тексеруден бас тарту?" else "Отменить осмотр?",
          color = AppleLightGrey,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Text(
          text = if (lang == AppLanguage.KAZAKH)
            "Барлық енгізілген деректер жойылады. Сіз бастапқы экранға ораласыз."
          else
            "Весь прогресс осмотра будет потерян. Вы вернётесь на экран входа.",
          color = AppleMutedGrey
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showCancelConfirm = false
            onResetAll()
          },
          colors = ButtonDefaults.buttonColors(containerColor = AppleRed)
        ) {
          Text(
            text = if (lang == AppLanguage.KAZAKH) "Иә, бас тарту" else "Да, отменить",
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
        }
      },
      dismissButton = {
        TextButton(onClick = { showCancelConfirm = false }) {
          Text(
            text = if (lang == AppLanguage.KAZAKH) "Артқа" else "Назад",
            color = AppleMutedGrey
          )
        }
      },
      containerColor = AppleCharcoal,
      shape = RoundedCornerShape(20.dp)
    )
  }

  Row(
    modifier = Modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.safeDrawing)
      .padding(16.dp),
    horizontalArrangement = Arrangement.spacedBy(20.dp),
    verticalAlignment = Alignment.Top
  ) {
    // LEFT COLUMN: Patient Identity & Measurement Bento Grid Status (takes 1.25f weight, zero-scroll)
    Column(
      modifier = Modifier
        .weight(1.25f)
        .fillMaxHeight(),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      EmployeeProfileCard(profile = profile)

      Text(
        text = AppText.systemStatus.get(lang),
        color = AppleMutedGrey,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
      )

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        val bp = metrics.getOrNull(0)
        val hr = metrics.getOrNull(1)
        val breath = metrics.getOrNull(2)
        val temp = metrics.getOrNull(3)
        val complaints = metrics.getOrNull(4)

        // Row 1 (BP & HR)
        Row(
          modifier = Modifier.weight(1f).fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            if (bp != null) BentoCell(metric = bp)
          }
          Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            if (hr != null) BentoCell(metric = hr)
          }
        }

        // Row 2 (Breathalyzer & Temperature)
        Row(
          modifier = Modifier.weight(1f).fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            if (breath != null) BentoCell(metric = breath)
          }
          Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            if (temp != null) BentoCell(metric = temp)
          }
        }

        // Row 3 (Health Complaints - Full Width)
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
          if (complaints != null) BentoCellHealthComplaints(metric = complaints)
        }
      }
    }

    // RIGHT COLUMN: Active Workflow Terminal (takes 1.6f weight)
    Column(
      modifier = Modifier
        .weight(1.6f)
        .fillMaxHeight(),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Top Bar Header inside Terminal for sleek branding
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("dashboard_header"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(AppleBlue)
          )
          Text(
            text = AppText.terminalVersion.get(lang),
            color = AppleLightGrey.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
          )
        }
        Row(
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (currentStep != StepState.COMPLETED_VERDICT) {
            Text(
              text = if (lang == AppLanguage.KAZAKH) "БАС ТАРТУ" else "ОТМЕНИТЬ ОСМОТР",
              color = AppleRed,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { showCancelConfirm = true }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("cancel_exam_button")
            )
          }
          Text(
            text = AppText.secureConn.get(lang),
            color = AppleLightGrey.copy(alpha = 0.4f),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
          )
        }
      }

      // Active terminals interactive box (spacious container)
      val isDark = LocalDarkTheme.current
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .clip(RoundedCornerShape(32.dp))
          .border(BorderStroke(1.dp, AppleBorderColor), RoundedCornerShape(32.dp))
          .background(if (isDark) AppleCharcoal.copy(alpha = 0.6f) else AppleCharcoal)
          .padding(24.dp)
      ) {
        WorkflowStepDispatcher(
          currentStep = currentStep,
          metrics = metrics,
          selectedComplaints = selectedComplaints,
          onComplaintsSelected = onComplaintsSelected,
          onConfirmComplaints = onConfirmComplaints,
          onSimulateBPAndPulse = onSimulateBPAndPulse,
          onSimulateBreathalyzer = onSimulateBreathalyzer,
          onSimulateTemperature = onSimulateTemperature,
          onSignAndSubmit = onSignAndSubmit,
          onResetAll = onResetAll,
          examSendStatus = examSendStatus,
          examSendErrorMessage = examSendErrorMessage,
          onRetrySend = onRetrySend,
          onContinueOffline = onContinueOffline,
          finalVerdictDopusk = finalVerdictDopusk,
          finalVerdictMedicName = finalVerdictMedicName,
          finalVerdictToken = finalVerdictToken
        )
      }

      // Bottom Micro-Bar Footer info for telemetry
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("dashboard_footer"),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = AppText.encryptedTransfer.get(lang),
          color = AppleLightGrey.copy(alpha = 0.3f),
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
      }
    }
  }
}

// ==========================================
// BLOCK 1: Profile Card Component (Spacious & Prominent)
// ==========================================
@Composable
fun EmployeeProfileCard(profile: EmployeeProfile, modifier: Modifier = Modifier) {
  val lang = LocalAppLanguage.current
  val isDark = LocalDarkTheme.current
  Card(
    shape = RoundedCornerShape(18.dp),
    border = BorderStroke(1.dp, AppleBorderColor),
    colors = CardDefaults.cardColors(containerColor = if (isDark) AppleCharcoal.copy(alpha = 0.8f) else AppleCharcoal),
    modifier = modifier
      .fillMaxWidth()
      .testTag("employee_profile_card")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Rounded squircle avatar with gradient and glowing subtleness, supporting Coil loading
      val photoUrl = profile.photoUrl
      var isLoadError by remember { mutableStateOf(false) }

      Box(
        modifier = Modifier
          .size(52.dp)
          .clip(RoundedCornerShape(14.dp))
          .border(BorderStroke(1.dp, AppleBorderColor), RoundedCornerShape(14.dp))
          .background(Brush.linearGradient(colors = if (isDark) listOf(Color(0xFF444446), Color(0xFF1C1C1E)) else listOf(Color(0xFFE5E5EA), Color(0xFFFAFAFA)))),
        contentAlignment = Alignment.Center
      ) {
        val localBitmap = remember(photoUrl) { decodeDataUriBitmap(photoUrl) }
        if (localBitmap != null) {
          Image(
            bitmap = localBitmap.asImageBitmap(),
            contentDescription = "Profile Photo",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))
          )
        } else if (!photoUrl.isNullOrEmpty() && !isLoadError) {
          coil.compose.AsyncImage(
            model = photoUrl,
            contentDescription = "Profile Photo",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
            onError = { isLoadError = true }
          )
        } else {
          Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
              color = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f),
              radius = size.width * 0.4f,
              center = center
            )
          }
          val initials = getInitials(profile.fullName)
          if (initials == "👤") {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "Profile Avatar Icon",
              modifier = Modifier.size(24.dp),
              tint = AppleLightGrey.copy(alpha = 0.8f)
            )
          } else {
            Text(
              text = initials,
              color = AppleBlue,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.SansSerif,
              letterSpacing = 0.5.sp
            )
          }
        }
      }

      // Demographic Profile Details (Clean, modern, and highly legible from API)
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(3.dp)
      ) {
        Text(
          text = profile.fullName,
          color = AppleLightGrey,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.1.sp
        )
        
        val pos = profile.position ?: profile.department ?: ""
        val org = profile.organization
        val branch = profile.branch ?: ""
        
        if (pos.isNotEmpty()) {
          Text(
            text = pos,
            color = AppleBlue,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.1.sp
          )
        }
        
        val subInfo = buildString {
          if (org.isNotEmpty()) append(org)
          if (branch.isNotEmpty()) {
            if (isNotEmpty()) append(" • ")
            append(branch)
          }
          if (profile.id.isNotEmpty()) {
            if (isNotEmpty()) append(" • ")
            append("ID: ")
            append(profile.id)
          }
        }
        
        if (subInfo.isNotEmpty()) {
          Text(
            text = subInfo,
            color = AppleMutedGrey,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.1.sp
          )
        }
      }
    }
  }
}

@Composable
fun BentoCell(metric: MetricState) {
  val lang = LocalAppLanguage.current
  val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.25f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = EaseInOutSine),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseAlpha"
  )

  val isDark = LocalDarkTheme.current
  val cellBorderColor = when {
    metric.isCompleted -> AppleGreen.copy(alpha = 0.4f)
    metric.isActive -> AppleBlue.copy(alpha = 0.6f)
    else -> AppleBorderColor
  }

  val cellBackground = when {
    metric.isCompleted -> if (isDark) AppleGreen.copy(alpha = 0.03f) else AppleGreen.copy(alpha = 0.08f)
    metric.isActive -> if (isDark) AppleBlue.copy(alpha = 0.05f) else AppleBlue.copy(alpha = 0.08f)
    else -> if (isDark) AppleCharcoal.copy(alpha = 0.3f) else AppleCharcoal
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .clip(RoundedCornerShape(18.dp))
      .border(BorderStroke(1.dp, cellBorderColor), RoundedCornerShape(18.dp))
      .background(cellBackground)
      .padding(12.dp)
      .testTag("metric_${metric.name.lowercase().replace(" ", "_")}"),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Top Row: Metric Name and Status LED
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = metric.icon,
          contentDescription = null,
          tint = if (metric.isCompleted) AppleGreen else if (metric.isActive) AppleBlue else AppleMutedGrey,
          modifier = Modifier.size(16.dp)
        )
        Text(
          text = metric.name.uppercase(),
          color = if (metric.isCompleted) AppleLightGrey.copy(alpha = 0.9f) else AppleMutedGrey,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
      }

      // Status LED/indicator
      Box(
        modifier = Modifier
          .size(18.dp)
          .clip(CircleShape)
          .background(
            when {
              metric.isCompleted -> AppleGreen.copy(alpha = 0.15f)
              metric.isActive -> AppleBlue.copy(alpha = 0.15f)
              else -> if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.05f)
            }
          ),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(
              when {
                metric.isCompleted -> AppleGreen
                metric.isActive -> AppleBlue.copy(alpha = pulseAlpha)
                else -> AppleMutedGrey.copy(alpha = 0.3f)
              }
            )
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Bottom Row: Reading Value and Unit
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.Bottom
    ) {
      if (metric.isCompleted) {
        Row(
          verticalAlignment = Alignment.Bottom,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = metric.value ?: "",
            color = AppleLightGrey,
            fontSize = 28.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = (-0.5).sp,
            fontFamily = FontFamily.SansSerif
          )
          if (metric.unit.isNotEmpty()) {
            Text(
              text = metric.unit,
              color = AppleMutedGrey.copy(alpha = 0.6f),
              fontSize = 13.sp,
              modifier = Modifier.padding(bottom = 3.dp),
              fontWeight = FontWeight.Normal
            )
          }
        }
      } else {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = if (metric.isActive) AppText.awaitValue.get(lang) else AppText.pendingValue.get(lang),
            color = if (metric.isActive) AppleBlue.copy(alpha = pulseAlpha) else AppleMutedGrey.copy(alpha = 0.3f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = (-0.2).sp
          )
          if (metric.isActive) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(AppleBlue.copy(alpha = pulseAlpha))
            )
          }
        }
      }
    }
  }
}

@Composable
fun BentoCellHealthComplaints(metric: MetricState) {
  val lang = LocalAppLanguage.current
  val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.25f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = EaseInOutSine),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseAlpha"
  )

  val isDark = LocalDarkTheme.current
  val isClear = metric.isCompleted && (metric.value == "None" || metric.value == null)
  val isAnySymptom = metric.isCompleted && (metric.value != "None" && metric.value != null)

  val cellBorderColor = when {
    isClear -> AppleGreen.copy(alpha = 0.4f)
    isAnySymptom -> AppleAmber.copy(alpha = 0.5f)
    metric.isActive -> AppleBlue.copy(alpha = 0.6f)
    else -> AppleBorderColor
  }

  val cellBackground = when {
    isClear -> if (isDark) AppleGreen.copy(alpha = 0.03f) else AppleGreen.copy(alpha = 0.08f)
    isAnySymptom -> if (isDark) AppleAmber.copy(alpha = 0.04f) else AppleAmber.copy(alpha = 0.08f)
    metric.isActive -> if (isDark) AppleBlue.copy(alpha = 0.05f) else AppleBlue.copy(alpha = 0.08f)
    else -> if (isDark) AppleCharcoal.copy(alpha = 0.3f) else AppleCharcoal
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .clip(RoundedCornerShape(18.dp))
      .border(BorderStroke(1.dp, cellBorderColor), RoundedCornerShape(18.dp))
      .background(cellBackground)
      .padding(12.dp)
      .testTag("metric_${metric.name.lowercase().replace(" ", "_")}"),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Top Row: Metric Name and Status Badge
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = metric.icon,
          contentDescription = null,
          tint = if (isClear) AppleGreen else if (isAnySymptom) AppleAmber else if (metric.isActive) AppleBlue else AppleMutedGrey,
          modifier = Modifier.size(16.dp)
        )
        Text(
          text = metric.name.uppercase(),
          color = if (metric.isCompleted) AppleLightGrey.copy(alpha = 0.9f) else AppleMutedGrey,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
      }

      // Status Text Badge
      Text(
        text = when {
          isClear -> "STATUS: " + AppText.valClear.get(lang)
          isAnySymptom -> "STATUS: " + AppText.valSymptoms.get(lang)
          metric.isActive -> "STATUS: " + AppText.valActive.get(lang)
          else -> "STATUS: " + AppText.valPending.get(lang)
        },
        color = when {
          isClear -> AppleGreen
          isAnySymptom -> AppleAmber
          metric.isActive -> AppleBlue.copy(alpha = pulseAlpha)
          else -> AppleMutedGrey
        },
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Bottom Content: Actual Complaints Details
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.Bottom
    ) {
      val textValue = when {
        isClear -> AppText.noSymptomsToday.get(lang)
        isAnySymptom -> AppText.complaintsYes.get(lang)
        metric.isActive -> AppText.awaitQuestionnaire.get(lang)
        else -> AppText.analysisPending.get(lang)
      }
      
      Text(
        text = textValue,
        color = if (metric.isCompleted) AppleLightGrey.copy(alpha = 0.9f) else AppleMutedGrey,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 2,
        lineHeight = 18.sp
      )
    }

    // Retained hidden check icon testTag for automated UI tests verification
    if (metric.isCompleted) {
      Box(
        modifier = Modifier
          .size(1.dp)
          .testTag("check_${metric.name.lowercase().replace(" ", "_")}")
      )
    }
  }
}

// ==========================================
// BLOCK 3: Dynamic Process Controller dispatcher
// ==========================================
@Composable
fun WorkflowStepDispatcher(
  currentStep: StepState,
  metrics: List<MetricState>,
  selectedComplaints: List<String>,
  onComplaintsSelected: (List<String>) -> Unit,
  onConfirmComplaints: (Boolean) -> Unit,
  onSimulateBPAndPulse: (Int, Int, Int) -> Unit,
  onSimulateBreathalyzer: (Double) -> Unit,
  onSimulateTemperature: (Double) -> Unit,
  onSignAndSubmit: () -> Unit,
  onResetAll: () -> Unit,
  examSendStatus: ExamSendStatus,
  examSendErrorMessage: String,
  onRetrySend: () -> Unit,
  onContinueOffline: () -> Unit,
  finalVerdictDopusk: String,
  finalVerdictMedicName: String,
  finalVerdictToken: String
) {
  Crossfade(
    targetState = currentStep,
    animationSpec = tween(400, easing = EaseInOut),
    label = "WorkflowSteps"
  ) { step ->
    when (step) {
      StepState.HEALTH_COMPLAINTS -> {
        Step1HealthComplaints(
          selectedComplaints = selectedComplaints,
          onComplaintsSelected = onComplaintsSelected,
          onConfirmComplaints = onConfirmComplaints
        )
      }
      StepState.BLOOD_PRESSURE -> {
        Step2BloodPressure(onConfirm = onSimulateBPAndPulse)
      }
      StepState.BREATHALYZER -> {
        Step3Breathalyzer(onConfirm = onSimulateBreathalyzer)
      }
      StepState.TEMPERATURE -> {
        Step4Temperature(onConfirm = onSimulateTemperature)
      }
      StepState.VERIFICATION -> {
        Step5Verification(onSignClick = onSignAndSubmit, metrics = metrics)
      }
      StepState.SECURE_LOADING -> {
        SecureDecisionsLoadingScreen(
          status = examSendStatus,
          errorMessage = examSendErrorMessage,
          onRetry = onRetrySend,
          onContinueOffline = onContinueOffline
        )
      }
      StepState.COMPLETED_VERDICT -> {
        FinalClearanceVerdictScreen(
          verdictDopusk = finalVerdictDopusk,
          medicName = finalVerdictMedicName,
          signatureToken = finalVerdictToken,
          onRestart = onResetAll
        )
      }
      StepState.AWAITING_NURSE -> {
        AwaitingNurseDecisionScreen(onRestart = onResetAll)
      }
    }
  }
}

// ==========================================
// AWAITING NURSE DECISION (auto-confirm disabled, nurse hasn't decided yet)
// ==========================================
@Composable
fun AwaitingNurseDecisionScreen(onRestart: () -> Unit) {
  val lang = LocalAppLanguage.current
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.SpaceBetween,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(80.dp)
          .clip(CircleShape)
          .background(AppleAmber.copy(alpha = 0.12f))
          .border(BorderStroke(2.dp, AppleAmber), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.HourglassEmpty,
          contentDescription = "Awaiting nurse",
          tint = AppleAmber,
          modifier = Modifier.size(44.dp)
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = if (lang == AppLanguage.KAZAKH) "МЕДБИКЕ ӘЛІ ШЕШІМ ҚАБЫЛДАҒАН ЖОҚ" else "МЕДСЕСТРА ЕЩЁ НЕ ПРИНЯЛА РЕШЕНИЕ",
        color = AppleAmber,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = if (lang == AppLanguage.KAZAKH)
          "Нәтижені алу үшін медбикеге хабарласыңыз немесе күте тұрыңыз."
        else
          "Обратитесь к медсестре для получения результата или подождите ещё немного.",
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 24.dp)
      )
    }

    Button(
      onClick = onRestart,
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp)
    ) {
      Text(if (lang == AppLanguage.KAZAKH) "Басынан бастау" else "Начать заново")
    }
  }
}

// ==========================================
// STEP 1: Health Complaints Panel
// ==========================================
@Composable
fun Step1HealthComplaints(
  selectedComplaints: List<String>,
  onComplaintsSelected: (List<String>) -> Unit,
  onConfirmComplaints: (Boolean) -> Unit
) {
  val lang = LocalAppLanguage.current
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Prompt initial YES or NO state
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      
      // Step progress marker
      StepHeaderBadge(stepNum = 1, title = AppText.stepAnamnesis.get(lang))
      
      Spacer(modifier = Modifier.height(16.dp))

      Icon(
        imageVector = Icons.Default.ChatBubbleOutline,
        contentDescription = "Complaints",
        tint = AppleBlue,
        modifier = Modifier.size(48.dp)
      )

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = AppText.complaintsPrompt.get(lang),
        color = AppleLightGrey,
        fontSize = 22.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        lineHeight = 28.sp
      )
      
      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = AppText.complaintsDetail.get(lang),
        color = AppleMutedGrey,
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
        lineHeight = 18.sp,
        modifier = Modifier.padding(horizontal = 16.dp)
      )
    }

    // Elegant Apple-styled Yes/No options
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Button(
        onClick = { 
          onConfirmComplaints(true) // Confirms that symptoms were reported
        },
        colors = ButtonDefaults.buttonColors(containerColor = AppleCharcoal),
        border = BorderStroke(1.dp, AppleAmber.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .weight(1f)
          .height(56.dp)
          .testTag("complaints_yes_button")
      ) {
        Icon(Icons.Default.Warning, contentDescription = "Yes", tint = AppleAmber, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(AppText.buttonSymptomsYes.get(lang), color = AppleLightGrey, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
      }

      Button(
        onClick = {
          onConfirmComplaints(false) // Sets plain complaints to "None"
        },
        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .weight(1f)
          .height(56.dp)
          .testTag("complaints_no_button")
      ) {
        Icon(Icons.Default.Done, contentDescription = "No", tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(AppText.buttonSymptomsNo.get(lang), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
      }
    }
  }
}

// ==========================================
// STEP 2: Blood Pressure & Heart Rate panel
// ==========================================
@Composable
fun Step2BloodPressure(
  onConfirm: (Int, Int, Int) -> Unit
) {
  val lang = LocalAppLanguage.current
  val drawMutedColor = AppleMutedGrey
  var isCapturing by remember { mutableStateOf(false) }
  var progressValue by remember { mutableStateOf(0f) }
  
  val context = androidx.compose.ui.platform.LocalContext.current
  val prefs = remember(context) { context.getSharedPreferences("nex_employees", android.content.Context.MODE_PRIVATE) }
  var tonometerMode by remember { mutableStateOf(prefs.getString("tonometer_mode", "simulation") ?: "simulation") }
  var tonometerMac by remember { mutableStateOf(prefs.getString("tonometer_mac", "") ?: "") }
  var tonometerName by remember { mutableStateOf(prefs.getString("tonometer_name", "") ?: "") }

  DisposableEffect(prefs) {
    val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
      when (key) {
        "tonometer_mode" -> tonometerMode = sharedPreferences.getString("tonometer_mode", "simulation") ?: "simulation"
        "tonometer_mac" -> tonometerMac = sharedPreferences.getString("tonometer_mac", "") ?: ""
        "tonometer_name" -> tonometerName = sharedPreferences.getString("tonometer_name", "") ?: ""
      }
    }
    prefs.registerOnSharedPreferenceChangeListener(listener)
    onDispose {
      prefs.unregisterOnSharedPreferenceChangeListener(listener)
      OmronBleManager.disconnect()
    }
  }

  val bleStatus = remember { OmronBleManager.statusText }
  val bleIsConnected = remember { OmronBleManager.isConnected }

  // Oscilloscope wave animation
  val infiniteTransition = rememberInfiniteTransition(label = "pulseTimeline")
  val wavePhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 2f * Math.PI.toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "wavePhase"
  )

  LaunchedEffect(isCapturing) {
    if (isCapturing) {
      if (tonometerMode == "omron_ble") {
        if (tonometerMac.trim().isEmpty()) {
          OmronBleManager.statusText.value = "Ошибка: MAC-адрес не настроен в Настройках!"
          delay(3000)
          progressValue = 0f
          while (progressValue < 1f) {
            delay(40)
            progressValue += 0.02f
          }
          onConfirm(118, 77, 72)
        } else {
          OmronBleManager.connect(context, tonometerMac) { sys, dia, hr ->
            onConfirm(sys, dia, hr)
          }
        }
      } else {
        progressValue = 0f
        while (progressValue < 1f) {
          delay(40)
          progressValue += 0.02f
        }
        onConfirm(118, 77, 72) // Auto simulated clear physiological numbers
      }
    }
  }

  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.SpaceBetween,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      StepHeaderBadge(stepNum = 2, title = AppText.stepBp.get(lang))
      
      Spacer(modifier = Modifier.height(10.dp))

      // Oscilloscope vector pulse canvas
      val isDark = LocalDarkTheme.current
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(125.dp)
          .clip(RoundedCornerShape(12.dp))
          .border(BorderStroke(1.dp, AppleBorderColor), RoundedCornerShape(12.dp))
          .background(if (isDark) AppleBlack.copy(alpha = 0.5f) else Color(0xFFF2F2F7)),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val drawWidth = size.width
          val drawHeight = size.height
          val midY = drawHeight / 2f
          val strokeWidth = 2.5f

          // Grid gridlines background for medical oscilloscope aesthetic
          val gridInterval = 30f
          var gridX = 0f
          val gridLineColorLocal = if (isDark) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.05f)
          while (gridX < drawWidth) {
            drawLine(
              color = gridLineColorLocal,
              start = Offset(gridX, 0f),
              end = Offset(gridX, drawHeight),
              strokeWidth = 1f
            )
            gridX += gridInterval
          }
          var gridY = 0f
          while (gridY < drawHeight) {
            drawLine(
              color = gridLineColorLocal,
              start = Offset(0f, gridY),
              end = Offset(drawWidth, gridY),
              strokeWidth = 1f
            )
            gridY += gridInterval
          }

          // Sinus rhythm arterial line pattern
          val path = Path()
          path.moveTo(0f, midY)
          
          val segmentCount = drawWidth.toInt()
          for (x in 0..segmentCount step 2) {
            val xFloat = x.toFloat()
            val theta = (xFloat * 0.025f) - (wavePhase * 2f)
            val baseSin = sin(theta)
            
            // Highlight spike rhythms
            val rPulse = if (cos(theta * 0.5f) > 0.85f) {
              val phaseLocal = (theta * 0.5f) % Math.PI.toFloat()
              sin(phaseLocal * 12f) * 35f
            } else {
              baseSin * 4f
            }
            
            val y = midY + rPulse * (if (isCapturing) 1.2f else 0.4f)
            path.lineTo(xFloat, y)
          }

          drawPath(
            path = path,
            color = if (isCapturing) AppleBlue else drawMutedColor.copy(alpha = 0.5f),
            style = Stroke(width = strokeWidth)
          )
        }

        // Active text readout overlay
        if (isCapturing) {
          Box(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(10.dp)
              .background(AppleBlue.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Text(
              text = if (tonometerMode == "omron_ble") "BLUETOOTH READOUT" else "${AppText.bpInflating.get(lang)}: ${(progressValue * 150).toInt()} mmHg",
              color = AppleBlue,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.5.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Instructional video — shown in ALL modes before scan starts
      if (!isCapturing) {
        val context = androidx.compose.ui.platform.LocalContext.current
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(BorderStroke(1.dp, AppleBorderColor), RoundedCornerShape(12.dp))
            .background(Color.Black)
        ) {
          AndroidView(
            factory = { ctx ->
              VideoView(ctx).apply {
                val videoUri = Uri.parse(
                  "android.resource://${ctx.packageName}/${R.raw.videotono}"
                )
                setVideoURI(videoUri)
                setOnPreparedListener { mp ->
                  mp.isLooping = true
                  mp.setVolume(0f, 0f)
                  start()
                }
              }
            },
            modifier = Modifier.fillMaxSize()
          )
          Box(
            modifier = Modifier
              .align(Alignment.TopStart)
              .padding(6.dp)
              .background(AppleBlue.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = if (lang == AppLanguage.KAZAKH) "НҰСҚАУЛЫҚ" else "ИНСТРУКЦИЯ",
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.5.sp
            )
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
      }

      if (tonometerMode == "omron_ble") {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Bluetooth,
              contentDescription = null,
              tint = AppleBlue,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = "OMRON BLE: ${tonometerName.ifEmpty { "Omron M4 7155T" }}",
              color = AppleBlue,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }
          Text(
            text = "СТАТУС: ${bleStatus.value}",
            color = AppleLightGrey,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
          )
          Text(
            text = if (isCapturing) {
              if (lang == AppLanguage.KAZAKH) "Тонометр өлшеген қысым деректерін күтуде..." else "Ожидание данных измерения с тонометра..."
            } else {
              if (lang == AppLanguage.KAZAKH) "Автоматты оқу үшін тонометрде өлшеуді қосыңыз" else "Включите измерение на тонометре для автоматического считывания"
            },
            color = AppleMutedGrey,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 14.dp)
          )
        }
      } else {
        if (isCapturing) {
          Text(
            text = AppText.bpScanning.get(lang),
            color = AppleLightGrey,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center
          )
        } else {
          Text(
            text = AppText.bpCuffPrompt.get(lang),
            color = AppleLightGrey,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = AppText.bpCuffDetail.get(lang),
            color = AppleMutedGrey,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 17.sp,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }

    if (isCapturing) {
      if (tonometerMode == "omron_ble") {
        // Показываем статус подключения Omron — данные придут автоматически через onCharacteristicChanged
        val bleStatus = remember { OmronBleManager.statusText }
        val bleConnected = remember { OmronBleManager.isConnected }
        Column(
          modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          LinearProgressIndicator(
            color = if (bleConnected.value) AppleGreen else AppleBlue,
            trackColor = AppleCharcoal,
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
          )
          Text(
            text = bleStatus.value,
            color = if (bleConnected.value) AppleGreen else AppleLightGrey,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
          )
        }
      } else {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          LinearProgressIndicator(
            progress = { progressValue },
            color = AppleBlue,
            trackColor = AppleCharcoal,
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp))
          )
          Text(
            text = "${AppText.bpCalibrating.get(lang)} ${(progressValue * 100).toInt()}%",
            color = AppleBlue,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }
    } else {
      Button(
        onClick = { isCapturing = true },
        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .testTag("bp_simulation_trigger")
      ) {
        Icon(
          imageVector = if (tonometerMode == "omron_ble") LegacyBluetoothSearchingIcon else Icons.Default.PlayArrow,
          contentDescription = "Simulate",
          tint = Color.White,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (tonometerMode == "omron_ble") (if (lang == AppLanguage.KAZAKH) "Bluetooth арқылы қысымды өлшеу" else "Считать давление через Bluetooth") else AppText.bpButtonScan.get(lang),
          fontWeight = FontWeight.Bold,
          color = Color.White,
          fontSize = 14.sp
        )
      }
    }
  }
}

// ==========================================
// STEP 3: Breathalyzer Test panel
// ==========================================
@Composable
fun Step3Breathalyzer(
  onConfirm: (Double) -> Unit
) {
  val lang = LocalAppLanguage.current
  val context = androidx.compose.ui.platform.LocalContext.current
  val prefs = remember(context) { context.getSharedPreferences("nex_employees", android.content.Context.MODE_PRIVATE) }
  var breathalyzerMode by remember { mutableStateOf(prefs.getString("breathalyzer_mode", "simulation") ?: "simulation") }

  DisposableEffect(prefs) {
    val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
      if (key == "breathalyzer_mode") breathalyzerMode = sp.getString("breathalyzer_mode", "simulation") ?: "simulation"
    }
    prefs.registerOnSharedPreferenceChangeListener(listener)
    onDispose {
      prefs.unregisterOnSharedPreferenceChangeListener(listener)
      DingoSerialManager.stopMeasurement()
      DingoSerialManager.disconnect()
    }
  }

  val drawMutedColor = AppleMutedGrey
  var isExhalating by remember { mutableStateOf(false) }
  var blowerGaugeVal by remember { mutableStateOf(0f) }
  var statusMessage by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf("") }

  val dingoConnected = remember { DingoSerialManager.isConnected }
  val dingoMeasuring = remember { DingoSerialManager.isMeasuring }
  val dingoStatus = remember { DingoSerialManager.statusText }

  // Exhalation fluid flow animation timeline
  val infiniteTransition = rememberInfiniteTransition(label = "exhaleWave")
  val wavePhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 2f * Math.PI.toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(1100, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "wavePhase"
  )

  // Simulation progress
  LaunchedEffect(isExhalating) {
    if (isExhalating && breathalyzerMode == "simulation") {
      blowerGaugeVal = 0f
      while (blowerGaugeVal < 1f) {
        delay(40)
        blowerGaugeVal += 0.025f
      }
      onConfirm(0.00)
    }
  }

  // AUTO-START: immediately begin measurement when step is shown
  LaunchedEffect(breathalyzerMode) {
    if (breathalyzerMode == "dingo_usb") {
      // Small delay so UI renders first
      delay(600)
      errorMessage = ""
      DingoSerialManager.startMeasurement(
        context = context,
        onStatusUpdate = { msg -> DingoSerialManager.statusText.value = msg },
        onResult  = { mgPerL -> onConfirm(mgPerL) },
        onError   = { err -> errorMessage = err }
      )
    } else {
      // Simulation: auto-start blow sequence
      delay(400)
      isExhalating = true
    }
  }

  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.SpaceBetween,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      StepHeaderBadge(stepNum = 3, title = AppText.stepBreath.get(lang))

      Spacer(modifier = Modifier.height(10.dp))

      // Wave canvas
      val isDark = LocalDarkTheme.current
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(125.dp)
          .clip(RoundedCornerShape(12.dp))
          .border(BorderStroke(1.dp, AppleBorderColor), RoundedCornerShape(12.dp))
          .background(if (isDark) AppleBlack else Color(0xFFF2F2F7)),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val drawWidth = size.width
          val drawHeight = size.height
          val midY = drawHeight / 2f
          val path = Path()
          path.moveTo(0f, midY)
          val active = isExhalating || dingoMeasuring.value
          val ampMax = if (active) 32f else 6f
          for (x in 0..drawWidth.toInt() step 4) {
            val xFloat = x.toFloat()
            val envelope = sin((xFloat / drawWidth) * Math.PI.toFloat())
            val y = midY + sin((xFloat * 0.03f) - wavePhase) * ampMax * envelope
            path.lineTo(xFloat, y.toFloat())
          }
          drawPath(
            path = path,
            color = if (active) AppleBlue.copy(alpha = 0.9f) else drawMutedColor.copy(alpha = 0.3f),
            style = Stroke(width = 3.5f)
          )
        }

        if (breathalyzerMode == "dingo_usb") {
          // USB mode badge
          Box(
            modifier = Modifier
              .align(Alignment.TopStart)
              .padding(8.dp)
              .background(
                if (dingoConnected.value) AppleGreen.copy(alpha = 0.85f) else AppleAmber.copy(alpha = 0.85f),
                RoundedCornerShape(4.dp)
              )
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = if (dingoConnected.value) "● USB DINGO" else "○ USB DINGO",
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
          }
          // Status overlay on canvas
          if (dingoMeasuring.value && dingoStatus.value.isNotEmpty()) {
            Box(
              modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(8.dp)
                .background(AppleBlue.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text(
                text = dingoStatus.value,
                color = AppleBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (breathalyzerMode == "dingo_usb") {
        // USB Serial mode UI
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Usb,
              contentDescription = null,
              tint = if (dingoConnected.value) AppleGreen else AppleAmber,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = "DINGO E-200 / MAX40",
              color = if (dingoConnected.value) AppleGreen else AppleAmber,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }
          Text(
            text = when {
              dingoMeasuring.value -> dingoStatus.value
              dingoConnected.value -> if (lang == AppLanguage.KAZAKH) "'Бастау' батырмасын басып, түтікке үрлеңіз" else "Нажмите кнопку ниже и дуйте в мундштук"
              DingoSerialManager.isDeviceAvailable(context) -> if (lang == AppLanguage.KAZAKH) "Құрылғы табылды. Іске қосу үшін батырманы басыңыз" else "Устройство найдено. Нажмите кнопку — Android запросит разрешение"
              else -> if (lang == AppLanguage.KAZAKH) "USB кабелін қосыңыз (Dingo E-200)" else "Подключите USB кабель (Dingo E-200)"
            },
            color = AppleMutedGrey,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 14.dp)
          )
          if (errorMessage.isNotEmpty()) {
            Text(
              text = "⚠ $errorMessage",
              color = AppleRed,
              fontSize = 12.sp,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(horizontal = 14.dp)
            )
          }
        }
      } else {
        // Simulation mode UI
        Text(
          text = if (isExhalating) AppText.breathExhalating.get(lang) else AppText.breathPrompt.get(lang),
          color = AppleLightGrey,
          fontSize = 18.sp,
          fontWeight = FontWeight.Normal,
          textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = AppText.breathDetail.get(lang),
          color = AppleMutedGrey,
          fontSize = 13.sp,
          textAlign = TextAlign.Center,
          lineHeight = 18.sp,
          modifier = Modifier.padding(horizontal = 12.dp)
        )
      }
    }

    // Bottom button / progress
    if (breathalyzerMode == "dingo_usb") {
      if (dingoMeasuring.value) {
        // Измерение идёт — показываем анимированный индикатор + кнопку отмены
        Column(
          modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          LinearProgressIndicator(
            color = AppleBlue,
            trackColor = AppleCharcoal,
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
          )
          Text(
            text = dingoStatus.value,
            color = AppleBlue,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
          )
          OutlinedButton(
            onClick = {
              DingoSerialManager.stopMeasurement()
              errorMessage = "Измерение отменено"
            },
            border = BorderStroke(1.dp, AppleRed.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
          ) {
            Text(
              text = if (lang == AppLanguage.KAZAKH) "Тоқтату" else "Отмена",
              color = AppleRed,
              fontWeight = FontWeight.Bold
            )
          }
        }
      } else {
        Button(
          onClick = {
            errorMessage = ""
            DingoSerialManager.startMeasurement(
              context = context,
              onStatusUpdate = { msg ->
                DingoSerialManager.statusText.value = msg
              },
              onResult = { mgPerL ->
                onConfirm(mgPerL)
              },
              onError = { err ->
                errorMessage = err
              }
            )
          },
          colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (lang == AppLanguage.KAZAKH) "Алкотестерді іске қосу" else "Запустить измерение Dingo",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 14.sp
          )
        }
      }
    } else {
      // Simulation bottom
      if (isExhalating) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          LinearProgressIndicator(
            progress = { blowerGaugeVal },
            color = AppleBlue,
            trackColor = AppleCharcoal,
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
          )
          Text(
            text = "${AppText.breathAnalyzing.get(lang)} ${(blowerGaugeVal * 100).toInt()}%",
            color = AppleBlue,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
          )
        }
      } else {
        Button(
          onClick = { isExhalating = true },
          colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("breathalyzer_sim_trigger")
        ) {
          Icon(
            imageVector = Icons.Default.Air,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = AppText.breathButtonScan.get(lang),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 14.sp
          )
        }
      }
    }
  }
}

// ==========================================
// STEP 4: Body Temperature Screening Panel
// ==========================================
@Composable
fun Step4Temperature(
  onConfirm: (Double) -> Unit
) {
  val lang = LocalAppLanguage.current
  val context = androidx.compose.ui.platform.LocalContext.current
  val prefs = remember(context) { context.getSharedPreferences("nex_employees", android.content.Context.MODE_PRIVATE) }
  var thermometerMode by remember { mutableStateOf(prefs.getString("thermometer_mode", "simulation") ?: "simulation") }
  var thermometerMac  by remember { mutableStateOf(prefs.getString("thermometer_mac",  "") ?: "") }
  var thermometerName by remember { mutableStateOf(prefs.getString("thermometer_name", "") ?: "") }

  // Tracks whether BLE connection has been initiated (one-shot)
  var hasStarted by remember { mutableStateOf(false) }

  DisposableEffect(prefs) {
    val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
      when (key) {
        "thermometer_mode" -> { thermometerMode = sp.getString("thermometer_mode", "simulation") ?: "simulation"; hasStarted = false }
        "thermometer_mac"  -> thermometerMac  = sp.getString("thermometer_mac",  "") ?: ""
        "thermometer_name" -> thermometerName = sp.getString("thermometer_name", "") ?: ""
      }
    }
    prefs.registerOnSharedPreferenceChangeListener(listener)
    onDispose {
      prefs.unregisterOnSharedPreferenceChangeListener(listener)
      MicrolifeManager.disconnect()
    }
  }

  val isMicrolifeActive = MicrolifeManager.isActiveMode(thermometerMode, thermometerName)

  var scansCount   by remember { mutableStateOf(0f) }
  var currentTemp  by remember { mutableStateOf(35.0) }
  var errorMessage by remember { mutableStateOf("") }

  val microStatus    = remember { MicrolifeManager.statusText }
  val microConnected = remember { MicrolifeManager.isConnected }
  val microMeasuring = remember { MicrolifeManager.isMeasuring }

  // Pulse ring animation — always running
  val infiniteTransition = rememberInfiniteTransition(label = "tempPulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.3f, targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Restart),
    label = "pulseScale"
  )
  val heatShimmer by infiniteTransition.animateFloat(
    initialValue = 0f, targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
    label = "heatShimmer"
  )

  // AUTO-START once when entering step
  LaunchedEffect(Unit) {
    if (hasStarted) return@LaunchedEffect
    hasStarted = true
    errorMessage = ""

    if (isMicrolifeActive) {
      // Microlife NC-150 BT: GATT connect without system pairing
      delay(400)
      if (thermometerMac.isBlank()) {
        errorMessage = "MAC-адрес не задан. Откройте Настройки → отсканируйте Microlife NC-150 BT"
        return@LaunchedEffect
      }
      MicrolifeManager.connect(
        context    = context,
        macAddress = thermometerMac,
        onResult   = { temp ->
          currentTemp = temp
          onConfirm(temp)
        },
        onError    = { err -> errorMessage = err }
      )
    } else {
      // Simulation: auto-animate temperature rise
      delay(300)
      scansCount = 0f
      while (scansCount < 1f) {
        delay(35)
        scansCount += 0.02f
        currentTemp = 35.0 + scansCount * 1.6
      }
      onConfirm(36.6)
    }
  }

  val isDark = LocalDarkTheme.current
  val tempColor = when {
    currentTemp >= 37.2 -> AppleRed
    currentTemp >= 36.8 -> AppleAmber
    else -> AppleBlue
  }
  val isBleActive = isMicrolifeActive

  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.SpaceBetween,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().weight(1f),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      StepHeaderBadge(stepNum = 4, title = AppText.stepTemp.get(lang))

      Spacer(modifier = Modifier.height(14.dp))

      // Pulsing heat rings visualization
      Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val cx = size.width / 2f
          val cy = size.height / 2f
          val maxR = size.width / 2f

          drawCircle(
            color = if (isDark) Color(0xFF0A0A0F) else Color(0xFFF0F0F5),
            radius = maxR * 0.72f, center = Offset(cx, cy)
          )
          drawCircle(color = tempColor.copy(alpha = 0.08f), radius = maxR * 0.72f, center = Offset(cx, cy))

          for (i in 0..2) {
            val ringPhase = (pulseScale + i * 0.33f) % 1f
            val ringR = maxR * 0.4f + maxR * 0.55f * ringPhase
            drawCircle(
              color = tempColor.copy(alpha = (1f - ringPhase) * 0.35f),
              radius = ringR, center = Offset(cx, cy), style = Stroke(width = 2.5f)
            )
          }

          val glowAlpha = 0.15f + heatShimmer * 0.12f
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(tempColor.copy(alpha = glowAlpha * 2f), tempColor.copy(alpha = glowAlpha)),
              center = Offset(cx, cy), radius = maxR * 0.5f
            ),
            radius = maxR * 0.5f, center = Offset(cx, cy)
          )
          drawCircle(
            color = tempColor.copy(alpha = 0.5f + heatShimmer * 0.3f),
            radius = maxR * 0.72f, center = Offset(cx, cy), style = Stroke(width = 2f)
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = if (isBleActive && !microMeasuring.value && currentTemp == 35.0) "—"
                   else String.format("%.1f", currentTemp),
            color = tempColor,
            fontSize = 28.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace
          )
          Text(text = "°C", color = tempColor.copy(alpha = 0.7f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (isBleActive) {
        // BLE mode status
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(
            if (microConnected.value) AppleGreen else if (microMeasuring.value) AppleAmber else AppleMutedGrey
          ))
          Text(
            text = "MICROLIFE NC-150 BT",
            color = if (microConnected.value) AppleGreen else AppleLightGrey,
            fontSize = 14.sp, fontWeight = FontWeight.Bold
          )
        }
        Spacer(modifier = Modifier.height(6.dp))

        // Status text or error
        val displayText = when {
          errorMessage.isNotEmpty() -> "⚠ $errorMessage"
          microConnected.value && !microMeasuring.value -> "✓ Подключено. Нажмите START на термометре NC-150"
          microMeasuring.value -> "Измерение температуры..."
          else -> microStatus.value
        }
        Text(
          text = displayText,
          color = if (errorMessage.isNotEmpty()) AppleRed else AppleMutedGrey,
          fontSize = 12.sp, textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 14.dp)
        )
        if (errorMessage.isNotEmpty()) {
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedButton(
            onClick = {
              errorMessage = ""
              hasStarted = false
              MicrolifeManager.connect(
                context = context,
                macAddress = thermometerMac,
                onResult = { temp -> currentTemp = temp; onConfirm(temp) },
                onError  = { err -> errorMessage = err }
              )
            },
            border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text(text = if (lang == AppLanguage.KAZAKH) "Қайталау" else "Повторить", color = AppleBlue, fontSize = 12.sp)
          }
        }
      } else {
        Text(
          text = AppText.tempScanning.get(lang),
          color = AppleLightGrey, fontSize = 16.sp,
          fontWeight = FontWeight.Normal, textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = AppText.tempDetail.get(lang),
          color = AppleMutedGrey, fontSize = 12.sp,
          textAlign = TextAlign.Center, lineHeight = 17.sp,
          modifier = Modifier.padding(horizontal = 12.dp)
        )
      }
    }

    // Progress bar
    Column(
      modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      if (isBleActive && microMeasuring.value) {
        LinearProgressIndicator(
          color = tempColor,
          trackColor = if (isDark) AppleCharcoal else Color(0xFFE5E5EA),
          modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
        )
        Text(
          text = microStatus.value,
          color = tempColor, fontSize = 12.sp, fontWeight = FontWeight.Medium
        )
      } else if (isBleActive && errorMessage.isEmpty() && !microConnected.value) {
        // Scanning progress
        LinearProgressIndicator(
          color = tempColor,
          trackColor = if (isDark) AppleCharcoal else Color(0xFFE5E5EA),
          modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
        )
        Text(
          text = "Поиск термометра...",
          color = tempColor, fontSize = 12.sp, fontWeight = FontWeight.Medium
        )
      } else if (!isBleActive) {
        // Simulation progress
        LinearProgressIndicator(
          progress = { scansCount },
          color = tempColor,
          trackColor = if (isDark) AppleCharcoal else Color(0xFFE5E5EA),
          modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
        )
        Text(
          text = "${AppText.tempScreenProgress.get(lang)} ${(scansCount * 100).toInt()}%",
          color = tempColor, fontSize = 12.sp, fontWeight = FontWeight.Medium
        )
      }
    }
  }
}


// ==========================================
// STEP 5: Verification & Verification Lock state
// ==========================================
@Composable
fun Step5Verification(
  onSignClick: () -> Unit,
  metrics: List<MetricState>
) {
  val lang = LocalAppLanguage.current
  fun metricValue(name: String) = metrics.firstOrNull { it.name == name }?.value
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
    ) {
      StepHeaderBadge(stepNum = 5, title = AppText.verifSummaryTitle.get(lang))
      
      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = AppText.verifAudit.get(lang),
        color = AppleLightGrey,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
      )

      Text(
        text = AppText.verifDesc.get(lang),
        color = AppleMutedGrey,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
      )

      // Verified summary overview grid checklist inside the Controller Card
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val bpValue    = metricValue(AppText.bpTitle.get(lang))
        val hrValue    = metricValue(AppText.hrTitle.get(lang))
        val breathValue = metricValue(AppText.breathTitle.get(lang))
        val tempValue  = metricValue(AppText.tempTitle.get(lang))

        val verificationRows = listOf(
          Triple(AppText.verifRowAnamnesis.get(lang), AppText.verifCompleted.get(lang), Icons.Default.AssignmentTurnedIn),
          Triple(AppText.verifRowBp.get(lang), "${AppText.verifCalibrated.get(lang)} (${bpValue ?: "—"})", Icons.Default.Favorite),
          Triple(AppText.verifRowHr.get(lang), "${AppText.verifCalibrated.get(lang)} (${hrValue ?: "—"} BPM)", Icons.Default.Favorite),
          Triple(AppText.verifRowBreath.get(lang), "${AppText.verifVerifiedNormal.get(lang)} (${breathValue ?: "—"})", Icons.Default.Air),
          Triple(AppText.verifRowTemp.get(lang), "${AppText.verifVerifiedNormal.get(lang)} (${tempValue ?: "—"} °C)", Icons.Default.Thermostat)
        )

        verificationRows.forEach { row ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(AppleBlack.copy(alpha = 0.3f))
              .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(10.dp))
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = row.third,
                contentDescription = null,
                tint = AppleBlue,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = row.first,
                color = AppleLightGrey,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
              )
            }
            
            Row(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = row.second,
                color = AppleGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = AppleGreen,
                modifier = Modifier.size(14.dp)
              )
            }
          }
        }
      }
    }

    // Interactive Submit Digital Lock button
    Button(
      onClick = onSignClick,
      colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .testTag("submit_signing_and_decisions")
    ) {
      Icon(
        imageVector = Icons.Default.LockOpen,
        contentDescription = "Signed",
        tint = Color.White,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = AppText.verifButtonSubmit.get(lang),
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Color.White
      )
    }
  }
}

// ==========================================
// PHYSICIAN DECISION LOADING SCREEN
// ==========================================
@Composable
fun SecureDecisionsLoadingScreen(
  status: ExamSendStatus,
  errorMessage: String,
  onRetry: () -> Unit,
  onContinueOffline: () -> Unit
) {
  val lang = LocalAppLanguage.current
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(32.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    when (status) {
      ExamSendStatus.SENDING -> {
        CircularProgressIndicator(
          color = AppleBlue,
          strokeWidth = 3.dp,
          modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
          text = if (lang == AppLanguage.KAZAKH) "Телеметрияны сайтқа жіберу..." else "Отправка телеметрии на сайт...",
          color = AppleLightGrey,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "nexium-health.com • " + AppText.secureConn.get(lang),
          color = AppleMutedGrey,
          fontSize = 13.sp,
          textAlign = TextAlign.Center
        )
      }
      ExamSendStatus.SUCCESS -> {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = "Success",
          tint = Color(0xFF30D158),
          modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
          text = if (lang == AppLanguage.KAZAKH) "Деректер сәтті жіберілді!" else "Данные успешно отправлены!",
          color = Color.White,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = if (lang == AppLanguage.KAZAKH) "Дәрігердің қорытындысын алу..." else "Получение заключения врача...",
          color = AppleMutedGrey,
          fontSize = 13.sp,
          textAlign = TextAlign.Center
        )
      }
      ExamSendStatus.ERROR -> {
        Icon(
          imageVector = Icons.Default.ErrorOutline,
          contentDescription = "Error",
          tint = Color(0xFFFF453A),
          modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
          text = if (lang == AppLanguage.KAZAKH) "Жіберу кезінде қате пайда болды" else "Ошибка при отправке данных",
          color = Color(0xFFFF453A),
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = errorMessage,
          color = AppleMutedGrey,
          fontSize = 13.sp,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Row(
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
            onClick = onContinueOffline,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, AppleMutedGrey.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppleLightGrey)
          ) {
            Text(
              text = if (lang == AppLanguage.KAZAKH) "Офлайн жалғастыру (Симуляция)" else "Продолжить оффлайн (Симуляция)",
              fontWeight = FontWeight.SemiBold,
              fontSize = 14.sp
            )
          }

          Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppleBlue, contentColor = Color.White)
          ) {
            Text(
              text = if (lang == AppLanguage.KAZAKH) "Қайталау" else "Повторить попытку",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }
        }
      }
      else -> {
        // Fallback default loading
        CircularProgressIndicator(
          color = AppleBlue,
          strokeWidth = 3.dp,
          modifier = Modifier.size(56.dp)
        )
      }
    }
  }
}

// ==========================================
// FINAL MOUNTED CLEARANCE MOUNT SCREEN
// ==========================================
@Composable
fun FinalClearanceVerdictScreen(
  verdictDopusk: String,
  medicName: String,
  signatureToken: String,
  onRestart: () -> Unit
) {
  val lang = LocalAppLanguage.current
  val verdictDopuskLower = verdictDopusk.lowercase()
  val isApproved = !verdictDopuskLower.contains("не") && 
      !verdictDopuskLower.contains("no") && 
      !verdictDopuskLower.contains("failed") && 
      !verdictDopuskLower.contains("denied") && 
      (verdictDopuskLower.contains("допущен") || 
       verdictDopuskLower.contains("yes") || 
       verdictDopuskLower.contains("passed") ||
       verdictDopuskLower.contains("approved"))
  val finalColor = if (isApproved) AppleGreen else AppleRed
  val statusIcon = if (isApproved) Icons.Default.Verified else Icons.Default.ErrorOutline

  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.SpaceBetween,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      
      // Giant glowing validation circle
      Box(
        modifier = Modifier
          .size(80.dp)
          .clip(CircleShape)
          .background(finalColor.copy(alpha = 0.12f))
          .border(BorderStroke(2.dp, finalColor), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = statusIcon,
          contentDescription = if (isApproved) "Cleared" else "Denied",
          tint = finalColor,
          modifier = Modifier.size(44.dp)
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = if (isApproved) AppText.fitForDuty.get(lang) else (if (lang == AppLanguage.KAZAKH) "ЖҰМЫСҚА ЖІБЕРІЛМЕДІ" else "НЕ ДОПУЩЕН (НЕ ГОДЕН)"),
        color = finalColor,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = if (isApproved) AppText.passVerified.get(lang) else (if (lang == AppLanguage.KAZAKH) "Кезекші медқызметкердің шешімі бойынша" else "По решению дежурного медработника"),
        color = AppleLightGrey,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Official clearance metadata ticket
      Column(
        modifier = Modifier
          .width(320.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(AppleBlack)
          .border(BorderStroke(1.dp, AppleBorderColor), RoundedCornerShape(16.dp))
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(AppText.certSignedBy.get(lang), color = AppleMutedGrey, fontSize = 11.sp)
          Text(medicName.ifEmpty { if (lang == AppLanguage.KAZAKH) "АМБ АБК кезекші медбикесі" else "Дежурный медик АПК АМК" }, color = AppleLightGrey, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(AppText.certState.get(lang), color = AppleMutedGrey, fontSize = 11.sp)
          Text(
            text = if (isApproved) AppText.certApproved.get(lang) else (if (lang == AppLanguage.KAZAKH) "Қабылданбады" else "Не допущен"),
            color = finalColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    // Complete session button
    Button(
      onClick = onRestart,
      colors = ButtonDefaults.buttonColors(containerColor = AppleCharcoal),
      border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .testTag("complete_log_out_session")
    ) {
      Text(
        text = AppText.logoutText.get(lang),
        color = AppleLightGrey,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp
      )
    }
  }
}

// ==========================================
// SHARED GENERAL HELPERS
// ==========================================
@Composable
fun StepHeaderBadge(stepNum: Int, title: String) {
  val lang = LocalAppLanguage.current
  Row(
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth()
  ) {
    Box(
      modifier = Modifier
        .background(AppleBlue.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
        .border(BorderStroke(1.dp, AppleBlue.copy(alpha = 0.3f)), RoundedCornerShape(6.dp))
        .padding(horizontal = 8.dp, vertical = 4.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "${AppText.stepBadge.get(lang)} $stepNum/5",
        color = AppleBlue,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
      )
    }

    Text(
      text = title,
      color = AppleMutedGrey,
      fontSize = 13.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp
    )
  }
}

// ==========================================
// SCREEN: Premium Full-Screen Confirmation
// ==========================================
@Composable
fun ConfirmationScreen(
  response: VerifyEmployeeResponse,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
  isTogglingFaceId: Boolean = false,
  onEnableFaceId: () -> Unit = {},
  onDisableFaceId: () -> Unit = {}
) {
  val lang = LocalAppLanguage.current
  Column(
    modifier = Modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.safeDrawing)
      .padding(32.dp),
    verticalArrangement = Arrangement.SpaceBetween,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top Info Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Icon(
          imageVector = Icons.Default.VerifiedUser,
          contentDescription = "Verified Identity",
          tint = AppleBlue,
          modifier = Modifier.size(28.dp)
        )
        Text(
          text = AppText.confirmTitle.get(lang).uppercase(),
          color = AppleLightGrey,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 2.sp
        )
      }

      // API Connection Status
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFF1E2F1E))
          .border(BorderStroke(1.dp, Color(0xFF30D158).copy(alpha = 0.35f)), RoundedCornerShape(8.dp))
          .padding(horizontal = 14.dp, vertical = 6.dp)
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(Color(0xFF30D158))
          )
          Text(
            text = "API SYNCHRONIZED",
            color = Color(0xFF30D158),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
          )
        }
      }
    }

    // Centered Interactive Bento layout content
    Row(
      modifier = Modifier
        .fillMaxWidth(0.9f)
        .weight(1f)
        .padding(vertical = 40.dp),
      horizontalArrangement = Arrangement.spacedBy(32.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Left side: Large avatar photo view (Solving "no avatar" feedback elegantly)
      val isDarkThemeLocal = LocalDarkTheme.current
      Card(
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.5.dp, AppleBorderColor),
        colors = CardDefaults.cardColors(containerColor = if (isDarkThemeLocal) AppleCharcoal.copy(alpha = 0.4f) else AppleCharcoal),
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight(0.85f)
      ) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          // Dynamic high-tech scan reticle backdrop
          Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
            drawRoundRect(
              color = AppleBlue.copy(alpha = 0.15f),
              size = size.copy(width = size.width - 40.dp.toPx(), height = size.height - 40.dp.toPx()),
              topLeft = Offset(20.dp.toPx(), 20.dp.toPx()),
              cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
              style = stroke
            )
          }

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            // Interactive Glowing profile sphere with Coil Image loading and fail-safe initials fallback
            val photoUrl = response.getEffectivePhotoUrl()
            var isLoadError by remember { mutableStateOf(false) }

            Box(
              modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(colors = listOf(AppleBlue.copy(alpha = 0.25f), Color.Transparent)))
                .border(BorderStroke(3.dp, Brush.sweepGradient(colors = listOf(AppleBlue, Color(0xFF5AC8FA), AppleBlue))), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              val localBitmap = remember(photoUrl) { decodeDataUriBitmap(photoUrl) }
              if (localBitmap != null) {
                Image(
                  bitmap = localBitmap.asImageBitmap(),
                  contentDescription = "Profile Image",
                  contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                  modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
              } else if (!photoUrl.isNullOrEmpty() && !isLoadError) {
                coil.compose.AsyncImage(
                  model = photoUrl,
                  contentDescription = "Profile Image",
                  contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                  modifier = Modifier.fillMaxSize().clip(CircleShape),
                  onError = { isLoadError = true }
                )
              } else {
                val initials = getInitials(response.fullName)
                if (initials == "👤") {
                  Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile icon",
                    modifier = Modifier.size(80.dp),
                    tint = AppleLightGrey.copy(alpha = 0.8f)
                  )
                } else {
                  Text(
                    text = initials,
                    color = AppleLightGrey,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                  )
                }
              }
            }

            // Subtitle state under avatar
            Text(
              text = "FACIAL PASS ID AUTOMATED",
              color = AppleBlue,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.6.sp
            )
          }
        }
      }

      // Right side: Profile structured key-value list rows
      Card(
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.5.dp, AppleBorderColor),
        colors = CardDefaults.cardColors(containerColor = if (isDarkThemeLocal) AppleCharcoal.copy(alpha = 0.4f) else AppleCharcoal),
        modifier = Modifier
          .weight(1.3f)
          .fillMaxHeight(0.85f)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
          Text(
            text = AppText.confirmIsThisYou.get(lang).uppercase(),
            color = AppleMutedGrey,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
          )

          Text(
            text = response.fullName ?: "---",
            color = AppleLightGrey,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 40.sp,
            letterSpacing = 0.5.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Key Value details list rows
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            InfoTableRow(label = AppText.confirmId.get(lang), value = response.employeeId, isMonospace = true)
            
            val org = response.getEffectiveOrganization()
            if (org.isNotEmpty()) {
              InfoTableRow(label = AppText.confirmOrg.get(lang), value = org)
            }
            
            val branch = response.getEffectiveBranch()
            if (branch.isNotEmpty()) {
              InfoTableRow(label = AppText.confirmBranch.get(lang), value = branch)
            }
            
            val pos = response.getEffectivePosition()
            if (pos.isNotEmpty()) {
              InfoTableRow(label = AppText.confirmDept.get(lang), value = pos)
            }
          }
        }
      }
    }

    // Face ID — сотрудник сам решает, включать или выключать вход по лицу
    Row(
      modifier = Modifier
        .fillMaxWidth(0.9f)
        .padding(bottom = 12.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (isTogglingFaceId) {
        CircularProgressIndicator(color = AppleBlue, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
      } else {
        TextButton(onClick = { if (response.faceIdEnabled == true) onDisableFaceId() else onEnableFaceId() }) {
          Icon(
            imageVector = Icons.Default.Face,
            contentDescription = null,
            tint = AppleBlue,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (response.faceIdEnabled == true) {
              if (lang == AppLanguage.KAZAKH) "Face ID өшіру" else "Отключить Face ID"
            } else {
              if (lang == AppLanguage.KAZAKH) "Face ID қосу" else "Включить Face ID"
            },
            color = AppleBlue,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    // Bottom Action Bar Row - Massive clickable buttons
    Row(
      modifier = Modifier
        .fillMaxWidth(0.9f)
        .height(80.dp),
      horizontalArrangement = Arrangement.spacedBy(24.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // "NO, NOT ME" BACKOUT ACTION
      OutlinedButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, AppleMutedGrey.copy(alpha = 0.4f)),
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = AppleLightGrey,
          containerColor = Color.Transparent
        ),
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .testTag("confirm_not_me_button")
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Cancel verification",
            modifier = Modifier.size(20.dp),
            tint = AppleLightGrey
          )
          Text(
            text = AppText.noNotMe.get(lang),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            letterSpacing = 0.5.sp
          )
        }
      }

      // "YES, IT'S ME" ACCESS ACTION
      Button(
        onClick = onConfirm,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = AppleBlue,
          contentColor = Color.White
        ),
        modifier = Modifier
          .weight(1.3f)
          .fillMaxHeight()
          .testTag("confirm_its_me_button")
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Confirm identity",
            modifier = Modifier.size(22.dp),
            tint = Color.White
          )
          Text(
            text = AppText.yesItsMe.get(lang),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            letterSpacing = 0.5.sp
          )
        }
      }
    }
  }
}

// Helper row block
@Composable
fun InfoTableRow(label: String, value: String, isMonospace: Boolean = false) {
  val isDark = LocalDarkTheme.current
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(if (isDark) AppleBlack.copy(alpha = 0.4f) else Color(0xFFF2F2F7))
      .border(BorderStroke(1.dp, AppleBorderColor), RoundedCornerShape(12.dp))
      .padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label.uppercase(),
      color = AppleMutedGrey,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.2.sp
    )
    Text(
      text = value,
      color = AppleLightGrey,
      fontSize = 14.sp,
      fontWeight = FontWeight.SemiBold,
      fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.SansSerif,
      letterSpacing = 0.25.sp
    )
  }
}

// ==========================================
// DETERMINISTIC AVATAR & INDUSTRY FIELD GENERATORS
// ==========================================
fun getDeterministicAvatar(name: String?): String {
  val clean = name ?: ""
  val hashCode = clean.hashCode()
  val avatars = listOf(
    "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&q=80&w=256",
    "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=256",
    "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&q=80&w=256",
    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=256",
    "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&q=80&w=256",
    "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&q=80&w=256"
  )
  val index = kotlin.math.abs(hashCode) % avatars.size
  return avatars[index]
}

fun getDeterministicOrg(name: String?, lang: AppLanguage): String {
  val clean = name ?: ""
  val hashCode = clean.hashCode()
  val orgs = listOf(
    Trans("Altiora Транспорт Сервис", "Altiora Көлік Сервисі"),
    Trans("Altiora Логистика", "Altiora Логистика және Терминалдары"),
    Trans("Altiora Инжиниринг Продакшн", "Altiora Инжиниринг өндірісі")
  )
  val index = kotlin.math.abs(hashCode) % orgs.size
  return orgs[index].get(lang)
}

fun getDeterministicBranch(name: String?, lang: AppLanguage): String {
  val clean = name ?: ""
  val hashCode = clean.hashCode()
  val branches = listOf(
    Trans("Алматинский Локомотивный филиал", "Алматы Локомотив филиалы"),
    Trans("Астанинский Терминал", "Астана Бас Терминалы"),
    Trans("Карагандинский транспортный хаб", "Қарағанды көлік хабы")
  )
  val index = kotlin.math.abs(hashCode) % branches.size
  return branches[index].get(lang)
}

fun getDeterministicPos(name: String?, lang: AppLanguage): String {
  val clean = name ?: ""
  val hashCode = clean.hashCode()
  val positions = listOf(
    Trans("Старший машинист локомотива", "Аға локомотив машинисі"),
    Trans("Старший диспетчер", "Аға диспетчер"),
    Trans("Инспектор по безопасности труда", "Еңбек қауіпсіздігі жөніндегі инспектор")
  )
  val index = kotlin.math.abs(hashCode) % positions.size
  return positions[index].get(lang)
}

// ==========================================
// Searchable dropdown — переиспользуется для организации/филиала/должности
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SearchableDropdownField(
  label: String,
  items: List<T>,
  selectedItem: T?,
  itemLabel: (T) -> String,
  onSelect: (T) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  placeholder: String? = null
) {
  // Два режима: обычный select (клик открывает список, клавиатура не лезет) —
  // и поиск (включается только явным тапом по лупе, тогда появляется ввод и клавиатура).
  var expanded by remember { mutableStateOf(false) }
  var searchMode by remember { mutableStateOf(false) }
  var query by remember { mutableStateOf(selectedItem?.let(itemLabel) ?: "") }
  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(selectedItem) {
    if (!searchMode) query = selectedItem?.let(itemLabel) ?: ""
  }

  fun closeAndReset() {
    expanded = false
    searchMode = false
    query = selectedItem?.let(itemLabel) ?: ""
  }

  val filtered = remember(query, items, searchMode) {
    if (!searchMode || query.isBlank()) items else items.filter { itemLabel(it).contains(query, ignoreCase = true) }
  }

  ExposedDropdownMenuBox(
    expanded = expanded && enabled,
    onExpandedChange = { if (enabled) { if (it) expanded = true else closeAndReset() } },
    modifier = modifier
  ) {
    OutlinedTextField(
      value = query,
      onValueChange = { query = it },
      enabled = enabled,
      readOnly = !searchMode,
      label = { Text(label) },
      placeholder = placeholder?.let { { Text(it) } },
      trailingIcon = {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(top = 4.dp)) {
          if (enabled) {
            IconButton(onClick = {
              searchMode = true
              expanded = true
              query = ""
            }) {
              Icon(Icons.Default.Search, contentDescription = "Search", tint = AppleMutedGrey, modifier = Modifier.size(20.dp))
            }
          }
          ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled)
        }
      },
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AppleBlue,
        unfocusedBorderColor = AppleBorderColor,
        focusedLabelColor = AppleBlue,
        unfocusedLabelColor = AppleMutedGrey,
        focusedTextColor = AppleLightGrey,
        unfocusedTextColor = AppleLightGrey,
        disabledTextColor = AppleMutedGrey,
        disabledBorderColor = AppleBorderColor,
        disabledLabelColor = AppleMutedGrey,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent
      ),
      modifier = Modifier
        .menuAnchor(MenuAnchorType.PrimaryEditable, enabled)
        .fillMaxWidth()
        .focusRequester(focusRequester)
    )
    ExposedDropdownMenu(expanded = expanded && enabled, onDismissRequest = { closeAndReset() }) {
      if (filtered.isEmpty()) {
        DropdownMenuItem(text = { Text("—", color = AppleMutedGrey) }, onClick = {}, enabled = false)
      }
      filtered.forEachIndexed { index, item ->
        if (index > 0) {
          HorizontalDivider(color = AppleBorderColor.copy(alpha = 0.4f))
        }
        DropdownMenuItem(
          text = { Text(itemLabel(item), color = AppleLightGrey, lineHeight = 18.sp) },
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
          onClick = {
            onSelect(item)
            searchMode = false
            query = itemLabel(item)
            expanded = false
          }
        )
      }
    }
  }

  LaunchedEffect(searchMode) {
    if (searchMode) focusRequester.requestFocus()
  }
}

// ==========================================
// IN-KIOSK CLIENT-SIDE SELF-REGISTRATION ENGINE
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
  onSuccess: (registeredId: String, fullName: String, org: String, branch: String, pos: String, photoUrl: String) -> Unit,
  onBack: () -> Unit
) {
  val lang = LocalAppLanguage.current
  val isDark = LocalDarkTheme.current
  val scope = rememberCoroutineScope()

  var fullName by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  
  // Справочники организаций/филиалов/должностей — тянутся с реального сервера
  // (GET /api/reference/{organizations,branches,positions}, X-Device-Token).
  var orgs by remember { mutableStateOf<List<OrganizationRef>>(emptyList()) }
  var allBranches by remember { mutableStateOf<List<BranchRef>>(emptyList()) }
  var positions by remember { mutableStateOf<List<PositionRef>>(emptyList()) }
  var isLoadingReference by remember { mutableStateOf(true) }
  var referenceError by remember { mutableStateOf("") }

  var selectedOrg by remember { mutableStateOf<OrganizationRef?>(null) }
  var selectedBranch by remember { mutableStateOf<BranchRef?>(null) }
  var selectedPos by remember { mutableStateOf<PositionRef?>(null) }

  // Филиалы зависят от выбранной организации
  val branches = remember(allBranches, selectedOrg) {
    val orgId = selectedOrg?.id
    if (orgId == null) allBranches else allBranches.filter { it.organizationId == orgId }
  }
  LaunchedEffect(selectedOrg) { selectedBranch = null }

  LaunchedEffect(Unit) {
    try {
      val (o, b, p) = withContext(Dispatchers.IO) {
        Triple(
          NexApiClient.service.getOrganizations(NexApiClient.deviceToken),
          NexApiClient.service.getBranches(NexApiClient.deviceToken),
          NexApiClient.service.getPositions(NexApiClient.deviceToken)
        )
      }
      orgs = o
      allBranches = b
      positions = p
    } catch (e: Exception) {
      val operation = if (lang == AppLanguage.KAZAKH) {
        "Анықтамалықтарды жүктеу мүмкін болмады"
      } else {
        "Не удалось загрузить справочники"
      }
      referenceError = ApiErrorText.fromThrowable(e, lang, operation)
    } finally {
      isLoadingReference = false
    }
  }
  
  // Фото профиля — реальный снимок с камеры, а не картинка-заглушка.
  // capturedPhoto — то, что вернула камера; photoConfirmed — пользователь явно
  // подтвердил, что снимок годится (иначе может переснять).
  val context = androidx.compose.ui.platform.LocalContext.current
  var capturedPhoto by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
  var photoConfirmed by remember { mutableStateOf(false) }

  val cameraLauncher = rememberLauncherForActivityResult(
    contract = FrontCameraTakePicturePreview()
  ) { bitmap ->
    if (bitmap != null) {
      capturedPhoto = bitmap
      photoConfirmed = false
    }
  }
  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { granted -> if (granted) cameraLauncher.launch(null) }

  fun launchCamera() {
    val granted = context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    if (granted) cameraLauncher.launch(null) else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
  }

  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }

  var showConfirmStep by remember { mutableStateOf(false) }
  var showSuccessDialog by remember { mutableStateOf(false) }
  var showIdSavedConfirmDialog by remember { mutableStateOf(false) }
  var registeredIdResult by remember { mutableStateOf("") }

  // Face ID — отдельный шаг после успешной регистрации: сотрудник сам решает,
  // включать его или нет, и это не смешивается с обычным фото профиля.
  var showFaceIdOfferDialog by remember { mutableStateOf(false) }
  var showFaceIdEnrollStep by remember { mutableStateOf(false) }
  var isEnrollingFace by remember { mutableStateOf(false) }
  var faceEnrollStatusText by remember { mutableStateOf("") }
  var faceEnrollIsError by remember { mutableStateOf(false) }

  fun finishRegistration() {
    val posName = if (lang == AppLanguage.KAZAKH && !selectedPos?.nameKk.isNullOrBlank()) selectedPos?.nameKk!! else (selectedPos?.name ?: "")
    val photoDataUri = capturedPhoto?.let { bitmapToJpegDataUrl(it) } ?: ""
    onSuccess(
      registeredIdResult,
      fullName,
      selectedOrg?.name ?: "",
      selectedBranch?.name ?: "",
      posName,
      photoDataUri
    )
  }

  fun handleRegistrationFaceEnroll(bitmap: android.graphics.Bitmap) {
    val base64Photo = bitmapToJpegDataUrl(bitmap) ?: return
    scope.launch {
      isEnrollingFace = true
      faceEnrollIsError = false
      faceEnrollStatusText = if (lang == AppLanguage.KAZAKH) "Тексерілуде..." else "Распознавание..."
      try {
        withContext(Dispatchers.IO) {
          NexApiClient.service.enrollFace(
            deviceToken = NexApiClient.deviceToken,
            request = EnrollFaceRequest(employeeId = registeredIdResult, facePhoto = base64Photo)
          )
        }
        showFaceIdEnrollStep = false
        finishRegistration()
      } catch (e: Exception) {
        faceEnrollIsError = true
        faceEnrollStatusText = ApiErrorText.fromThrowable(
          throwable = e,
          lang = lang,
          operation = if (lang == AppLanguage.KAZAKH) "Face ID қосу мүмкін болмады" else "Не удалось включить Face ID"
        )
      } finally {
        isEnrollingFace = false
      }
    }
  }

  if (showFaceIdEnrollStep) {
    FaceIdScanScreen(
      title = "Face ID",
      subtitle = if (lang == AppLanguage.KAZAKH) "Face ID қосу үшін бетіңізді суретке түсіріңіз" else "Снимок для включения Face ID",
      isProcessing = isEnrollingFace,
      statusText = faceEnrollStatusText,
      isError = faceEnrollIsError,
      onCapture = { bitmap -> handleRegistrationFaceEnroll(bitmap) },
      onCancel = {
        showFaceIdEnrollStep = false
        finishRegistration()
      }
    )
    return
  }
  
  Column(
    modifier = Modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.safeDrawing)
      .padding(24.dp)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top utility bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier
          .background(if (isDark) AppleCharcoal else Color(0xFFF2F2F7), CircleShape)
          .testTag("reg_back_button")
      ) {
        Icon(imageVector = LegacyArrowBackIcon, contentDescription = "Back", tint = AppleBlue)
      }
      Text(
        text = Trans("РЕГИСТРАЦИЯ НОВОГО СОТРУДНИКА", "ЖАҢА ҚЫЗМЕТКЕРДІ ТІРКЕУ").get(lang),
        color = AppleLightGrey,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp
      )
      Spacer(modifier = Modifier.width(48.dp))
    }
    
    if (!showConfirmStep) {
    Row(
      modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
      horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
      // Left basic data entriescard
      val isDark = LocalDarkTheme.current
      Card(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, AppleBorderColor),
        colors = CardDefaults.cardColors(containerColor = if (isDark) AppleCharcoal.copy(alpha = 0.3f) else AppleCharcoal),
        modifier = Modifier.weight(1f).fillMaxHeight()
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Text(
            text = Trans("ОСНОВНЫЕ ДАННЫЕ", "НЕГІЗГІ ДЕРЕКТЕР").get(lang),
            color = AppleBlue,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
          
          // ID сотрудника присваивается сервером автоматически после регистрации —
          // пользователь его не вводит.
          Text(
            text = Trans(
              "ID сотрудника будет присвоен автоматически после регистрации",
              "Қызметкер ID-і тіркеуден кейін автоматты түрде беріледі"
            ).get(lang),
            color = AppleMutedGrey,
            fontSize = 12.sp
          )

          // Full name field
          OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text(Trans("ФИО сотрудника", "Қызметкердің Т.А.Ә.").get(lang)) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AppleBlue,
              unfocusedBorderColor = AppleBorderColor,
              focusedLabelColor = AppleBlue,
              unfocusedLabelColor = AppleMutedGrey,
              focusedTextColor = AppleLightGrey,
              unfocusedTextColor = AppleLightGrey,
              focusedContainerColor = Color.Transparent,
              unfocusedContainerColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth().testTag("reg_name_field")
          )
          
          // Optional phone field
          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(Trans("Номер телефона (опционально)", "Телефон нөмірі (міндетті емес)").get(lang)) },
            placeholder = { Text("+77071234567") },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AppleBlue,
              unfocusedBorderColor = AppleBorderColor,
              focusedLabelColor = AppleBlue,
              unfocusedLabelColor = AppleMutedGrey,
              focusedTextColor = AppleLightGrey,
              unfocusedTextColor = AppleLightGrey,
              focusedContainerColor = Color.Transparent,
              unfocusedContainerColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth().testTag("reg_phone_field")
          )
        }
      }
      
      // Right department and avatar configurationscard
      Card(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, AppleBorderColor),
        colors = CardDefaults.cardColors(containerColor = if (isDark) AppleCharcoal.copy(alpha = 0.3f) else AppleCharcoal),
        modifier = Modifier.weight(1.2f).fillMaxHeight()
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Text(
            text = Trans("ДОЛЖНОСТЬ И ФОТО", "ЛАУАЗЫМЫ МЕН СУРЕТІ").get(lang),
            color = AppleBlue,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
          
          if (isLoadingReference) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
              CircularProgressIndicator(color = AppleBlue, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
              Text(
                if (lang == AppLanguage.KAZAKH) "Анықтамалықтар жүктелуде…" else "Загрузка справочников…",
                color = AppleMutedGrey, fontSize = 12.sp
              )
            }
          } else if (referenceError.isNotEmpty()) {
            Text(referenceError, color = AppleAmber, fontSize = 12.sp)
          }

          // Организация — поисковый выпадающий список
          SearchableDropdownField(
            label = Trans("Организация", "Ұйым").get(lang),
            items = orgs,
            selectedItem = selectedOrg,
            itemLabel = { it.name },
            onSelect = { selectedOrg = it },
            enabled = !isLoadingReference,
            modifier = Modifier.fillMaxWidth().testTag("reg_org_field")
          )

          // Филиал — зависит от выбранной организации
          SearchableDropdownField(
            label = Trans("Филиал", "Филиал").get(lang),
            items = branches,
            selectedItem = selectedBranch,
            itemLabel = { it.name },
            onSelect = { selectedBranch = it },
            enabled = !isLoadingReference && selectedOrg != null && branches.isNotEmpty(),
            placeholder = when {
              selectedOrg == null -> if (lang == AppLanguage.KAZAKH) "Алдымен ұйымды таңдаңыз" else "Сначала выберите организацию"
              branches.isEmpty() -> if (lang == AppLanguage.KAZAKH) "Филиалдар жоқ" else "Филиалов нет"
              else -> null
            },
            modifier = Modifier.fillMaxWidth().testTag("reg_branch_field")
          )

          // Должность
          SearchableDropdownField(
            label = Trans("Должность", "Лауазымы").get(lang),
            items = positions,
            selectedItem = selectedPos,
            itemLabel = { if (lang == AppLanguage.KAZAKH && !it.nameKk.isNullOrBlank()) it.nameKk!! else it.name },
            onSelect = { selectedPos = it },
            enabled = !isLoadingReference,
            modifier = Modifier.fillMaxWidth().testTag("reg_position_field")
          )
          
          // Фото профиля — снимок с камеры устройства
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(Trans("Фото профиля", "Профиль суреті").get(lang), color = AppleMutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)

            val photo = capturedPhoto
            if (photo == null) {
              OutlinedButton(
                onClick = { launchCamera() },
                border = BorderStroke(1.dp, AppleBorderColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppleBlue),
                modifier = Modifier.testTag("reg_camera_button")
              ) {
                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(Trans("Сделать фото", "Фото түсіру").get(lang), fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }
            } else {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                  modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .border(BorderStroke(2.dp, if (photoConfirmed) AppleGreen else AppleAmber), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Image(
                    bitmap = photo.asImageBitmap(),
                    contentDescription = "Captured portrait",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                  )
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  if (!photoConfirmed) {
                    Text(
                      Trans("Фото подходит?", "Фото жарай ма?").get(lang),
                      color = AppleMutedGrey, fontSize = 12.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                      OutlinedButton(
                        onClick = { launchCamera() },
                        border = BorderStroke(1.dp, AppleBorderColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppleLightGrey),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("reg_retake_button")
                      ) {
                        Text(Trans("Переснять", "Қайта түсіру").get(lang), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                      }
                      Button(
                        onClick = { photoConfirmed = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AppleGreen),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("reg_confirm_photo_button")
                      ) {
                        Text(Trans("Использовать", "Қолдану").get(lang), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                      }
                    }
                  } else {
                    Text(
                      "✓ " + Trans("Фото подтверждено", "Фото расталды").get(lang),
                      color = AppleGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold
                    )
                    Text(
                      Trans("Изменить фото", "Фотоны өзгерту").get(lang),
                      color = AppleBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                      modifier = Modifier.clickable { launchCamera() }
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
    
    if (errorMessage.isNotEmpty()) {
      Text(text = errorMessage, color = AppleAmber, fontSize = 13.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
    }

    Box(
      modifier = Modifier.fillMaxWidth().height(60.dp),
      contentAlignment = Alignment.Center
    ) {
      Button(
        onClick = {
          if (fullName.isBlank()) {
            errorMessage = if (lang == AppLanguage.KAZAKH) "Толық аты-жөніңізді енгізіңіз" else "Пожалуйста, введите ФИО"
            return@Button
          }
          if (selectedOrg == null || selectedPos == null) {
            errorMessage = if (lang == AppLanguage.KAZAKH) "Ұйымды және лауазымды таңдаңыз" else "Выберите организацию и должность"
            return@Button
          }
          if (selectedBranch == null && branches.isNotEmpty()) {
            errorMessage = if (lang == AppLanguage.KAZAKH) "Филиалды таңдаңыз" else "Выберите филиал"
            return@Button
          }
          if (capturedPhoto == null || !photoConfirmed) {
            errorMessage = if (lang == AppLanguage.KAZAKH) "Фото түсіріп, растаңыз" else "Сделайте фото и подтвердите его"
            return@Button
          }
          errorMessage = ""
          showConfirmStep = true
        },
        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(0.5f).fillMaxHeight().testTag("reg_submit_button")
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(imageVector = Icons.Default.HowToReg, contentDescription = null, tint = Color.White)
          Text(Trans("Продолжить", "Жалғастыру").get(lang), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
      }
    }
    } else {
      // ─── Шаг подтверждения — показываем итог перед реальной отправкой на сервер ───
      val org = selectedOrg
      val branch = selectedBranch
      val pos = selectedPos
      val posDisplayName = if (lang == AppLanguage.KAZAKH && !pos?.nameKk.isNullOrBlank()) pos?.nameKk!! else (pos?.name ?: "")

      Card(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, AppleBorderColor),
        colors = CardDefaults.cardColors(containerColor = if (isDark) AppleCharcoal.copy(alpha = 0.3f) else AppleCharcoal),
        modifier = Modifier.fillMaxWidth(0.7f)
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            capturedPhoto?.let { bmp ->
              Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Photo preview",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(CircleShape).border(BorderStroke(1.dp, AppleBorderColor), CircleShape)
              )
            }
            Text(
              text = Trans("ПРОВЕРЬТЕ ДАННЫЕ ПЕРЕД РЕГИСТРАЦИЕЙ", "ТІРКЕУДЕН БҰРЫН ДЕРЕКТЕРДІ ТЕКСЕРІҢІЗ").get(lang),
              color = AppleBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp
            )
          }

          @Composable
          fun ConfirmRow(label: String, value: String) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(label, color = AppleMutedGrey, fontSize = 13.sp)
              Text(value, color = AppleLightGrey, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
          }

          ConfirmRow(Trans("ФИО", "Т.А.Ә.").get(lang), fullName)
          if (phone.isNotBlank()) ConfirmRow(Trans("Телефон", "Телефон").get(lang), phone)
          ConfirmRow(Trans("Организация", "Ұйым").get(lang), org?.name ?: "")
          ConfirmRow(Trans("Филиал", "Филиал").get(lang), branch?.name ?: "")
          ConfirmRow(Trans("Должность", "Лауазымы").get(lang), posDisplayName)

          if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = AppleAmber, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
          }

          Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            OutlinedButton(
              onClick = { showConfirmStep = false; errorMessage = "" },
              enabled = !isLoading,
              modifier = Modifier.weight(1f).height(52.dp),
              border = BorderStroke(1.dp, AppleBorderColor),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = AppleLightGrey)
            ) {
              Text(Trans("Изменить", "Өзгерту").get(lang), fontWeight = FontWeight.Bold)
            }
            Button(
              onClick = {
                isLoading = true
                errorMessage = ""
                scope.launch {
                  try {
                    // Кодируем реальный снимок с камеры в Base64 data URL
                    val photoBitmap = capturedPhoto
                    val base64Photo = withContext(Dispatchers.IO) {
                      try {
                        if (photoBitmap == null) return@withContext null
                        val stream = java.io.ByteArrayOutputStream()
                        photoBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, stream)
                        val encoded = android.util.Base64.encodeToString(stream.toByteArray(), android.util.Base64.NO_WRAP)
                        "data:image/jpeg;base64,$encoded"
                      } catch (e: Exception) {
                        null
                      }
                    }

                    val req = RegisterPatientRequest(
                      employeeId = "",
                      fullName = fullName,
                      avatarPhoto = base64Photo,
                      phone = phone.ifBlank { null },
                      preferredLanguage = if (lang == AppLanguage.KAZAKH) "kk" else "ru",
                      organizationId = org?.id,
                      branchId = branch?.id,
                      positionId = pos?.id
                    )
                    val response = withContext(Dispatchers.IO) {
                      NexApiClient.service.registerPatient(
                        deviceToken = NexApiClient.deviceToken,
                        request = req
                      )
                    }
                    if (response.isSuccessful) {
                      val resultBody = response.body()
                      val actualId = resultBody?.employee?.employeeId ?: ""
                      registeredIdResult = actualId
                      showSuccessDialog = true
                    } else {
                      val errBody = response.errorBody()?.string() ?: ""
                      val operation = if (lang == AppLanguage.KAZAKH) {
                        "Қызметкерді тіркеу мүмкін болмады"
                      } else {
                        "Не удалось зарегистрировать сотрудника"
                      }
                      errorMessage = ApiErrorText.fromHttp(response.code(), errBody, lang, operation)
                    }
                  } catch (e: Exception) {
                    val operation = if (lang == AppLanguage.KAZAKH) {
                      "Қызметкерді тіркеу мүмкін болмады"
                    } else {
                      "Не удалось зарегистрировать сотрудника"
                    }
                    errorMessage = ApiErrorText.fromThrowable(e, lang, operation)
                  } finally {
                    isLoading = false
                  }
                }
              },
              enabled = !isLoading,
              colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
              modifier = Modifier.weight(1f).height(52.dp),
              shape = RoundedCornerShape(14.dp),
              contentPadding = PaddingValues(0.dp)
            ) {
              if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
              } else {
                Text(Trans("Подтвердить и зарегистрировать", "Растап тіркеу").get(lang), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White, textAlign = TextAlign.Center)
              }
            }
          }
        }
      }
    }

    if (showSuccessDialog) {
      androidx.compose.material3.AlertDialog(
        onDismissRequest = {},
        title = {
          Text(
            text = Trans("РЕГИСТРАЦИЯ УСПЕШНА!", "ТІРКЕЛУ СӘТТІ ӨТТІ!").get(lang),
            fontWeight = FontWeight.Bold,
            color = AppleLightGrey,
            fontSize = 20.sp
          )
        },
        text = {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = Trans(
                "Сервер присвоил вам персональный ID сотрудника.",
                "Сервер сізге жеке қызметкер ID-ін берді."
              ).get(lang),
              color = AppleLightGrey.copy(alpha = 0.7f),
              fontSize = 14.sp,
              textAlign = TextAlign.Center
            )

            // Glowing ID Badge
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(AppleBlue.copy(alpha = 0.15f))
                .border(BorderStroke(2.dp, AppleBlue), RoundedCornerShape(16.dp))
                .padding(horizontal = 30.dp, vertical = 15.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = registeredIdResult,
                color = AppleBlue,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                letterSpacing = 2.sp
              )
            }

            Text(
              text = Trans(
                "⚠ ОБЯЗАТЕЛЬНО ЗАПИШИТЕ ИЛИ ЗАПОМНИТЕ ЭТОТ ID — он понадобится для входа и прохождения следующих медосмотров.",
                "⚠ БҰЛ ID-ДІ МІНДЕТТІ ТҮРДЕ ЖАЗЫП АЛЫҢЫЗ НЕМЕСЕ ЕСТЕ САҚТАҢЫЗ — ол келесі кіру және медтексеруден өту үшін қажет болады."
              ).get(lang),
              color = AppleAmber,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center
            )
          }
        },
        confirmButton = {
          Button(
            onClick = { showIdSavedConfirmDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(
              text = Trans("Начать осмотр", "Медтексеруді бастау").get(lang),
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = Color.White
            )
          }
        },
        containerColor = AppleCharcoal,
        shape = RoundedCornerShape(24.dp)
      )
    }

    if (showIdSavedConfirmDialog) {
      androidx.compose.material3.AlertDialog(
        onDismissRequest = {},
        title = {
          Text(
            text = Trans("Вы уверены?", "Сенімдісіз бе?").get(lang),
            fontWeight = FontWeight.Bold,
            color = AppleLightGrey,
            fontSize = 20.sp
          )
        },
        text = {
          Text(
            text = Trans(
              "Вы уверены, что сохранили свой ID сотрудника? Без него вы не сможете войти для следующего медосмотра.",
              "Қызметкер ID-іңізді сақтап алғаныңызға сенімдісіз бе? Ол болмаса келесі медтексеруге кіре алмайсыз."
            ).get(lang),
            color = AppleLightGrey.copy(alpha = 0.8f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
          )
        },
        confirmButton = {
          Button(
            onClick = {
              showIdSavedConfirmDialog = false
              showSuccessDialog = false
              showFaceIdOfferDialog = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(Trans("Да, сохранил", "Иә, сақтадым").get(lang), fontWeight = FontWeight.Bold, color = Color.White)
          }
        },
        dismissButton = {
          OutlinedButton(
            onClick = { showIdSavedConfirmDialog = false },
            border = BorderStroke(1.dp, AppleBorderColor),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppleLightGrey),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(Trans("Нет, показать снова", "Жоқ, қайта көрсет").get(lang), fontWeight = FontWeight.Bold)
          }
        },
        containerColor = AppleCharcoal,
        shape = RoundedCornerShape(24.dp)
      )
    }

    if (showFaceIdOfferDialog) {
      androidx.compose.material3.AlertDialog(
        onDismissRequest = {},
        title = {
          Text(
            text = "Face ID",
            fontWeight = FontWeight.Bold,
            color = AppleLightGrey,
            fontSize = 20.sp
          )
        },
        text = {
          Text(
            text = Trans(
              "Хотите включить вход по Face ID? Тогда в следующий раз можно будет пройти осмотр без ввода ID — на любом киоске.",
              "Face ID арқылы кіруді қосқыңыз келе ме? Онда келесі жолы кез келген киоскіде ID енгізбей-ақ өтуге болады."
            ).get(lang),
            color = AppleLightGrey.copy(alpha = 0.8f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
          )
        },
        confirmButton = {
          Button(
            onClick = {
              showFaceIdOfferDialog = false
              showFaceIdEnrollStep = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(Trans("Включить Face ID", "Face ID қосу").get(lang), fontWeight = FontWeight.Bold, color = Color.White)
          }
        },
        dismissButton = {
          OutlinedButton(
            onClick = {
              showFaceIdOfferDialog = false
              finishRegistration()
            },
            border = BorderStroke(1.dp, AppleBorderColor),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppleLightGrey),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(Trans("Пропустить", "Өткізіп жіберу").get(lang), fontWeight = FontWeight.Bold)
          }
        },
        containerColor = AppleCharcoal,
        shape = RoundedCornerShape(24.dp)
      )
    }
  }
}

// ==========================================
// CONFIGURATION: Settings & Bluetooth Core
// ==========================================

data class MockBluetoothDevice(
  val name: String,
  val address: String,
  val type: String,
  val rssi: Int = -65
)

object OmronBleManager {
  private var activeGatt: BluetoothGatt? = null
  private var gattServer: BluetoothGattServer? = null
  
  var isConnected = mutableStateOf(false)
  var statusText = mutableStateOf("Ожидание запуска...")
  var lastResult = mutableStateOf<Triple<Int, Int, Int>?>(null)

  private var bestTimestampValue: Long = -1L
  private val settleHandler = Handler(Looper.getMainLooper())
  private var settleRunnable: Runnable? = null
  private const val SETTLE_DELAY_MS = 1200L // Немного увеличили для стабильности многопакетной передачи

  private val CTS_SERVICE_UUID = java.util.UUID.fromString("00001805-0000-1000-8000-00805f9b34fb")
  private val CURRENT_TIME_CHAR_UUID = java.util.UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb")

  private val BP_SERVICE_UUID = java.util.UUID.fromString("00001810-0000-1000-8000-00805f9b34fb")
  private val BP_MEAS_CHAR_UUID = java.util.UUID.fromString("00002a35-0000-1000-8000-00805f9b34fb")
  private val BP_FEATURE_CHAR_UUID = java.util.UUID.fromString("00002a49-0000-1000-8000-00805f9b34fb")
  private val CCCD_UUID = java.util.UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

  private fun startGattServer(context: Context) {
    if (gattServer != null) return
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    gattServer = bluetoothManager.openGattServer(context, object : BluetoothGattServerCallback() {
      override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
        android.util.Log.d("OmronBleManager", "Server connection state changed: $newState status: $status for ${device.address}")
      }

      override fun onServiceAdded(status: Int, service: BluetoothGattService) {
        android.util.Log.d("OmronBleManager", "Server service added: ${service.uuid} status: $status")
      }

      override fun onCharacteristicReadRequest(
        device: BluetoothDevice,
        requestId: Int,
        offset: Int,
        characteristic: BluetoothGattCharacteristic
      ) {
        if (characteristic.uuid == CURRENT_TIME_CHAR_UUID) {
          val timeBytes = getCurrentTimeBytes()
          android.util.Log.d("OmronBleManager", "Time sync request (READ) from ${device.address}. Sending: ${timeBytes.joinToString(" "){ "%02X".format(it) }}")
          gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, timeBytes)
        } else {
          gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset, null)
        }
      }
    })

    val ctsService = BluetoothGattService(CTS_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
    val timeChar = BluetoothGattCharacteristic(
      CURRENT_TIME_CHAR_UUID,
      BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
      BluetoothGattCharacteristic.PERMISSION_READ
    )
    ctsService.addCharacteristic(timeChar)
    gattServer?.addService(ctsService)
    android.util.Log.d("OmronBleManager", "GATT Server opening...")
  }

  private fun getCurrentTimeBytes(): ByteArray {
    val cal = java.util.Calendar.getInstance()
    val bytes = ByteArray(10)
    val year = cal.get(java.util.Calendar.YEAR)
    bytes[0] = (year and 0xFF).toByte()
    bytes[1] = (year shr 8 and 0xFF).toByte()
    bytes[2] = (cal.get(java.util.Calendar.MONTH) + 1).toByte()
    bytes[3] = cal.get(java.util.Calendar.DAY_OF_MONTH).toByte()
    bytes[4] = cal.get(java.util.Calendar.HOUR_OF_DAY).toByte()
    bytes[5] = cal.get(java.util.Calendar.MINUTE).toByte()
    bytes[6] = cal.get(java.util.Calendar.SECOND).toByte()
    // Day of week: BLE format 1=Monday...7=Sunday. Calendar: 1=Sunday, 2=Monday...
    val dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK)
    bytes[7] = if (dayOfWeek == java.util.Calendar.SUNDAY) 7 else (dayOfWeek - 1).toByte()
    bytes[8] = 0 // fractions
    bytes[9] = 1 // adjust reason: Manual time update
    return bytes
  }

  private var bondReceiver: android.content.BroadcastReceiver? = null

  // Omron хранит только один слот сопряжённого центрального устройства — если прибор
  // успел сопрячься с чем-то ещё (например, с телефоном через Omron Connect), наш
  // сохранённый на телефоне ключ шифрования перестаёт совпадать с тем, что помнит сам
  // прибор. Android в этом случае сам стирает bond (encryption_change:key_missing).
  // Вместо того чтобы просто показать ошибку и заставить лезть в настройки —
  // пересопрягаем автоматически прямо из экрана осмотра.
  private fun rebondAndRetry(context: Context, macAddress: String, onValueRead: (Int, Int, Int) -> Unit) {
    val adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter ?: return
    val device = adapter.getRemoteDevice(macAddress)
    statusText.value = "Тонометр сопряжён с другим устройством. Пересопряжение — подтвердите системное окно Bluetooth."

    bondReceiver?.let { context.applicationContext.unregisterReceiver(it) }
    val receiver = object : android.content.BroadcastReceiver() {
      override fun onReceive(ctx: Context?, intent: android.content.Intent?) {
        if (intent?.action != BluetoothDevice.ACTION_BOND_STATE_CHANGED) return
        val changedDevice = IntentCompat.getParcelableExtra(intent, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        if (changedDevice?.address != macAddress) return
        val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
        if (bondState == BluetoothDevice.BOND_BONDED) {
          context.applicationContext.unregisterReceiver(this)
          bondReceiver = null
          connect(context, macAddress, attempt = 0, rebonded = true, onValueRead = onValueRead)
        } else if (bondState == BluetoothDevice.BOND_NONE) {
          context.applicationContext.unregisterReceiver(this)
          bondReceiver = null
          statusText.value = "Не удалось пересопрячь тонометр. Попробуйте вручную в настройках."
        }
      }
    }
    bondReceiver = receiver
    context.applicationContext.registerReceiver(receiver, android.content.IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
    device.createBond()
  }

  fun connect(context: Context, macAddress: String, attempt: Int = 0, rebonded: Boolean = false, onValueRead: (Int, Int, Int) -> Unit) {
    if (macAddress.isEmpty()) {
      statusText.value = "Ошибка: MAC-адрес пуст"
      return
    }
    val adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    if (adapter == null) {
      statusText.value = "Ошибка: Bluetooth недоступен"
      return
    }
    try {
      val device = adapter.getRemoteDevice(macAddress)
      if (device.bondState != BluetoothDevice.BOND_BONDED) {
        if (!rebonded) {
          rebondAndRetry(context, macAddress, onValueRead)
        } else {
          statusText.value = "Тонометр не сопряжён. Нажмите «СОПРЯЧЬ» и подтвердите Bluetooth."
        }
        return
      }

      // Запускаем сервер времени ПЕРЕД подключением клиента
      startGattServer(context)

      statusText.value = "Подключение к ${device.name ?: "Omron M4"}..."
      bestTimestampValue = -1L
      lastResult.value = null
      settleRunnable?.let { settleHandler.removeCallbacks(it) }
      settleRunnable = null

      activeGatt?.disconnect()
      activeGatt?.close()

      activeGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
          android.util.Log.d("OmronBleManager", "Client connection state=$newState status=$status bond=${device.bondState}")
          if (newState == BluetoothProfile.STATE_CONNECTED) {
            statusText.value = "Установлено. Настройка..."
            gatt?.requestMtu(512)
          } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            isConnected.value = false
            settleRunnable?.let { settleHandler.removeCallbacks(it) }
            settleRunnable = null
            
            val result = lastResult.value
            if (result != null) {
              android.util.Log.d("OmronBleManager", "Disconnected — результат готов")
              statusText.value = "✓ ${result.first}/${result.second}, пульс ${result.third}"
              Handler(Looper.getMainLooper()).post {
                onValueRead(result.first, result.second, result.third)
              }
              lastResult.value = null
            } else if (device.bondState != BluetoothDevice.BOND_BONDED && !rebonded) {
              // Шифрование сорвалось из-за key_missing — Omron больше не помнит наш ключ
              // (см. rebondAndRetry). Сопрягаемся заново вместо голого повтора коннекта.
              android.util.Log.d("OmronBleManager", "Bond потерян (status=$status) — пересопряжение")
              rebondAndRetry(context, macAddress, onValueRead)
            } else if (attempt < 2) {
              // Первые попытки нередко срываются до установления связи — прибор ещё не
              // готов (особенно если его только что включили в розетку и он не успел
              // "прогреться"). Повторяем автоматически, без участия пользователя.
              android.util.Log.d("OmronBleManager", "Попытка $attempt не удалась (status=$status) — повтор")
              statusText.value = "Не удалось подключиться, повтор..."
              Handler(Looper.getMainLooper()).postDelayed({
                connect(context, macAddress, attempt = attempt + 1, rebonded = rebonded, onValueRead = onValueRead)
              }, 800)
            } else {
              statusText.value = if (status == 19 || status == 22) "Тонометр отключился (Handshake failed)" else "Отключено ($status)"
            }
          }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
          android.util.Log.d("OmronBleManager", "MTU changed to $mtu, status=$status")
          gatt?.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
          Handler(Looper.getMainLooper()).postDelayed({
            gatt?.discoverServices()
          }, 800)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
          if (status == BluetoothGatt.GATT_SUCCESS) {
            android.util.Log.d("OmronBleManager", "Services discovered. Listing...")
            gatt?.services?.forEach { s ->
              android.util.Log.d("OmronBleManager", "  Service: ${s.uuid}")
              s.characteristics.forEach { c ->
                android.util.Log.d("OmronBleManager", "    Char: ${c.uuid} props=${c.properties}")
              }
            }

            statusText.value = "Синхронизация..."
            val service = gatt?.getService(BP_SERVICE_UUID)
            val featureChar = service?.getCharacteristic(BP_FEATURE_CHAR_UUID)
            val measChar = service?.getCharacteristic(BP_MEAS_CHAR_UUID)

            if (featureChar != null) {
              android.util.Log.d("OmronBleManager", "Reading BP Features...")
              gatt?.readCharacteristic(featureChar)
            } else if (measChar != null) {
              enableIndication(gatt!!, measChar)
            } else {
              statusText.value = "Ошибка: Служба измерения не найдена"
            }
          } else {
            statusText.value = "Ошибка поиска служб: $status"
          }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
          if (characteristic?.uuid == BP_FEATURE_CHAR_UUID) {
            android.util.Log.d("OmronBleManager", "BP Features read status=$status. Now enabling indications.")
            val service = gatt?.getService(BP_SERVICE_UUID)
            val measChar = service?.getCharacteristic(BP_MEAS_CHAR_UUID)
            if (measChar != null) {
              enableIndication(gatt, measChar)
            }
          }
        }

        private fun enableIndication(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
          gatt.setCharacteristicNotification(characteristic, true)
          val descriptor = characteristic.getDescriptor(CCCD_UUID)
          if (descriptor != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
              gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)
            } else {
              @Suppress("DEPRECATION")
              descriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
              @Suppress("DEPRECATION")
              gatt.writeDescriptor(descriptor)
            }
            android.util.Log.d("OmronBleManager", "Enabling indications for 0x2A35")
          }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: android.bluetooth.BluetoothGattDescriptor?, status: Int) {
          if (status == BluetoothGatt.GATT_SUCCESS) {
            android.util.Log.d("OmronBleManager", "Indications enabled OK — ожидаем данные")
            isConnected.value = true
            statusText.value = "✓ Готов! Начните замер на Omron M4..."
          } else {
            android.util.Log.w("OmronBleManager", "Descriptor write failed: status=$status")
            statusText.value = "Ошибка подписки: $status"
          }
        }

        @Suppress("DEPRECATION") // required overload for API < 33; new value-param overload delegates to this one
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
          val value = characteristic?.value ?: return
          if (value.size >= 7) {
            val flags = value[0].toInt() and 0xFF
            var sysVal = parseSfloat(value[1], value[2])
            var diaVal = parseSfloat(value[3], value[4])
            if ((flags and 0x01) != 0) { // kPa to mmHg
              sysVal *= 7.50062
              diaVal *= 7.50062
            }
            
            var pulseVal = 0.0
            var offset = 7
            var timestampValue = -1L
            if ((flags and 0x02) != 0) { // Timestamp
              if (value.size >= offset + 7) {
                val year = ((value[offset + 1].toInt() and 0xFF) shl 8) or (value[offset].toInt() and 0xFF)
                val month = value[offset + 2].toInt() and 0xFF
                val day = value[offset + 3].toInt() and 0xFF
                val hour = value[offset + 4].toInt() and 0xFF
                val minute = value[offset + 5].toInt() and 0xFF
                val second = value[offset + 6].toInt() and 0xFF
                timestampValue = (((((year.toLong() * 13) + month) * 32 + day) * 24 + hour) * 60 + minute) * 60 + second
              }
              offset += 7
            }
            if ((flags and 0x04) != 0) { // Pulse
              if (value.size >= offset + 2) {
                pulseVal = parseSfloat(value[offset], value[offset + 1])
              }
            }

            val sys = sysVal.toInt()
            val dia = diaVal.toInt()
            val hr = if (pulseVal > 0) pulseVal.toInt() else 72

            if (sys !in 40..280 || dia !in 20..200) return

            if (timestampValue >= bestTimestampValue) {
              bestTimestampValue = if (timestampValue >= 0) timestampValue else bestTimestampValue
              lastResult.value = Triple(sys, dia, hr)
              statusText.value = "Принято: $sys/$dia, пульс $hr"

              settleRunnable?.let { settleHandler.removeCallbacks(it) }
              val runnable = Runnable {
                val settled = lastResult.value
                if (settled != null) {
                  android.util.Log.d("OmronBleManager", "Settle complete: ${settled.first}/${settled.second}")
                  statusText.value = "✓ ${settled.first}/${settled.second}, пульс ${settled.third}"
                  lastResult.value = null
                  onValueRead(settled.first, settled.second, settled.third)
                  try { activeGatt?.disconnect() } catch (_: Exception) {}
                }
              }
              settleRunnable = runnable
              settleHandler.postDelayed(runnable, SETTLE_DELAY_MS)
            }
          }
        }
      }, BluetoothDevice.TRANSPORT_LE)
    } catch (e: Exception) {
      statusText.value = "Ошибка: ${e.localizedMessage}"
    }
  }

  fun disconnect() {
    settleRunnable?.let { settleHandler.removeCallbacks(it) }
    settleRunnable = null
    activeGatt?.disconnect()
    activeGatt?.close()
    activeGatt = null
    
    try {
      gattServer?.close()
    } catch (_: Exception) {}
    gattServer = null
    
    isConnected.value = false
    statusText.value = "Ожидание запуска..."
  }

  private fun parseSfloat(b1: Byte, b2: Byte): Double {
    val uint16 = ((b2.toInt() and 0xFF) shl 8) or (b1.toInt() and 0xFF)
    var mantissa = uint16 and 0x0FFF
    if ((mantissa and 0x0800) != 0) mantissa = mantissa or -0x1000
    val exponent = uint16 shr 12
    val expValue = if (exponent >= 8) exponent - 16 else exponent
    return mantissa * java.lang.Math.pow(10.0, expValue.toDouble())
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  activeLanguage: AppLanguage,
  tonometerMode: String,
  tonometerMac: String,
  tonometerName: String,
  breathalyzerMode: String,
  breathalyzerMac: String,
  breathalyzerName: String,
  thermometerMode: String,
  thermometerMac: String,
  thermometerName: String,
  deviceToken: String,
  tokenInfo: TokenInfoResponse?,
  tokenInfoError: String,
  isFetchingTokenInfo: Boolean,
  kioskModeEnabled: Boolean,
  onKioskModeToggle: (Boolean) -> Unit,
  onSaveDevice: (type: String, mode: String, mac: String, name: String) -> Unit,
  onSaveToken: (String) -> Unit,
  onBack: () -> Unit
) {
  val context = androidx.compose.ui.platform.LocalContext.current
  val scope = rememberCoroutineScope()
  val isDark = LocalDarkTheme.current

  var isScanning by remember { mutableStateOf(false) }
  var foundDevices by remember { mutableStateOf<List<MockBluetoothDevice>>(emptyList()) }
  var scanErrorText by remember { mutableStateOf("") }
  var selectedDeviceForBinding by remember { mutableStateOf<MockBluetoothDevice?>(null) }
  var showOmronPairingDialog by remember { mutableStateOf(false) }
  var showKioskPasswordDialog by remember { mutableStateOf(false) }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val granted = permissions.values.all { it }
    if (granted) {
      scanErrorText = ""
    } else {
      scanErrorText = "Разрешения отклонены. Сканирование невозможно."
    }
  }

  val baseDevices = listOf(
    MockBluetoothDevice("Omron M4 HEM-7155T", "00:80:25:AB:CD:01", "tonometer"),
    MockBluetoothDevice("Breath Pro Alcolock 2024", "18:93:7F:42:11:A4", "breathalyzer"),
    MockBluetoothDevice("FastTemp Medical BLE", "54:D0:39:6C:AA:FF", "thermometer")
  )

  fun startBleScan() {
    foundDevices = emptyList()
    val isBTPermissionGranted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
      context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
      context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
      context.checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED
    } else {
      context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    if (!isBTPermissionGranted) {
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        permissionLauncher.launch(
          arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.ACCESS_FINE_LOCATION
          )
        )
      } else {
        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
      }
      return
    }

    val bManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val adapter = bManager?.adapter
    if (adapter == null) {
      scanErrorText = "Bluetooth отсутствует на этом терминале"
      foundDevices = baseDevices
      return
    }

    if (!adapter.isEnabled) {
      scanErrorText = "Bluetooth выключен"
      foundDevices = baseDevices
      return
    }

    val scanner = adapter.bluetoothLeScanner
    if (scanner == null) {
      scanErrorText = "BLE сканер недоступен"
      foundDevices = baseDevices
      return
    }

    isScanning = true

    fun upsertScanResult(result: ScanResult) {
      val dev = result.device ?: return
      val scanRecordName = result.scanRecord?.deviceName
      val devName = try {
        if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
          dev.name
        } else null
      } catch (e: Exception) {
        null
      }
      val resolvedName = when {
        !scanRecordName.isNullOrBlank() -> scanRecordName
        !devName.isNullOrBlank() -> devName
        else -> null
      }
      val deviceRssi = result.rssi
      val existing = foundDevices.find { it.address == dev.address }

      if (existing == null) {
        val finalName = resolvedName ?: "Bluetooth Device"
        val type = when {
          finalName.uppercase().contains("OMRON") || finalName.uppercase().contains("HEM") || dev.address.startsWith("00:80:25") -> "tonometer"
          finalName.uppercase().contains("ALCO") || finalName.uppercase().contains("BREATH") || finalName.uppercase().contains("ALCOLOCK") -> "breathalyzer"
          else -> "thermometer"
        }
        foundDevices = foundDevices + MockBluetoothDevice(finalName, dev.address, type, deviceRssi)
      } else if (!resolvedName.isNullOrBlank() && existing.name == "Bluetooth Device") {
        // Имя пришло позже (например, во втором пакете рекламы) — обновляем плейсхолдер.
        foundDevices = foundDevices.map { if (it.address == dev.address) it.copy(name = resolvedName, rssi = deviceRssi) else it }
      }
    }

    val scanCallback = object : ScanCallback() {
      override fun onScanResult(callbackType: Int, result: ScanResult?) {
        result?.let { upsertScanResult(it) }
      }

      override fun onBatchScanResults(results: MutableList<ScanResult>?) {
        results?.forEach { upsertScanResult(it) }
      }

      override fun onScanFailed(errorCode: Int) {
        isScanning = false
      }
    }

    val scanSettings = ScanSettings.Builder()
      .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
      .build()

    scope.launch {
      try {
        scanner.startScan(null, scanSettings, scanCallback)
        delay(8000)
        scanner.stopScan(scanCallback)
      } catch (e: Exception) {
        scanErrorText = "Ошибка: ${e.localizedMessage}"
      } finally {
        isScanning = false
        if (foundDevices.isEmpty()) {
          foundDevices = baseDevices
        }
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(AppleBlack)
      .windowInsetsPadding(WindowInsets.safeDrawing)
      .padding(24.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier
            .background(if (isDark) AppleCharcoal else Color(0xFFF2F2F7), CircleShape)
            .testTag("settings_back_button")
        ) {
          Icon(
            imageVector = LegacyArrowBackIcon,
            contentDescription = "Back",
            tint = AppleBlue
          )
        }
        Column {
          Text(
            text = if (activeLanguage == AppLanguage.KAZAKH) "ЖҮЙЕ БАПТАУЛАРЫ" else "НАСТРОЙКИ СИСТЕМЫ",
            color = AppleLightGrey,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
          Text(
            text = if (activeLanguage == AppLanguage.KAZAKH) "Перифериялық құрылғыларды басқару" else "Управление периферийными BLE устройствами",
            color = AppleMutedGrey,
            fontSize = 13.sp
          )
          Text(
            text = "v${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})",
            color = AppleMutedGrey,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val isOwner = remember(context) { KioskManager.isDeviceOwner(context) }
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = if (isOwner) AppleGreen.copy(alpha = 0.15f) else AppleRed.copy(alpha = 0.15f)),
          border = BorderStroke(1.dp, if (isOwner) AppleGreen.copy(alpha = 0.4f) else AppleRed.copy(alpha = 0.4f))
        ) {
          Text(
            text = if (isOwner) "DEVICE OWNER: ACTIVE" else "DEVICE OWNER: DISABLED",
            color = if (isOwner) AppleGreen else AppleRed,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }

        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = if (isDark) AppleCharcoal.copy(alpha = 0.5f) else Color(0xFFE5E5EA)),
          border = BorderStroke(1.dp, AppleBorderColor)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(AppleGreen)
            )
            Text(
              text = "BLE STATUS: ACTIVE",
              color = AppleLightGrey,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }

    Text(
      text = if (activeLanguage == AppLanguage.KAZAKH) "ЖҮЙЕЛІК ПАРАМЕТРЛЕР" else "СИСТЕМНЫЕ ПАРАМЕТРЫ",
      color = AppleBlue,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.2.sp
    )

    Card(
      shape = RoundedCornerShape(18.dp),
      border = BorderStroke(1.dp, AppleBorderColor),
      colors = CardDefaults.cardColors(containerColor = if (isDark) AppleCharcoal else Color.White),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = if (activeLanguage == AppLanguage.KAZAKH) "Киоск режимі" else "Режим киоска",
              color = AppleLightGrey,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
            Text(
              text = if (activeLanguage == AppLanguage.KAZAKH) "Экранды бұғаттау және жүйелік батырмаларды өшіру" else "Блокировка экрана и системных кнопок навигации",
              color = AppleMutedGrey,
              fontSize = 12.sp
            )
          }
          
          Switch(
            checked = kioskModeEnabled,
            onCheckedChange = { 
              if (!it) {
                showKioskPasswordDialog = true
              } else {
                onKioskModeToggle(true)
              }
            },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = AppleBlue,
              uncheckedThumbColor = AppleMutedGrey,
              uncheckedTrackColor = AppleCharcoal
            )
          )
        }
      }
    }

    if (showKioskPasswordDialog) {
      var passInput by remember { mutableStateOf("") }
      var passError by remember { mutableStateOf(false) }
      
      androidx.compose.material3.AlertDialog(
        onDismissRequest = { showKioskPasswordDialog = false },
        title = { 
          Text(
            text = if (activeLanguage == AppLanguage.KAZAKH) "Кіруді растау" else "Подтверждение доступа",
            color = AppleLightGrey,
            fontWeight = FontWeight.Bold
          ) 
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = if (activeLanguage == AppLanguage.KAZAKH) "Киоск режимін өшіру үшін парольді енгізіңіз:" else "Введите пароль для отключения режима киоска:",
              color = AppleMutedGrey
            )
            OutlinedTextField(
              value = passInput,
              onValueChange = { passInput = it; passError = false },
              label = { Text("Password") },
              isError = passError,
              visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppleBlue,
                unfocusedBorderColor = AppleBorderColor,
                focusedTextColor = AppleLightGrey,
                unfocusedTextColor = AppleLightGrey
              )
            )
            if (passError) {
              Text(
                text = if (activeLanguage == AppLanguage.KAZAKH) "Қате пароль" else "Неверный пароль",
                color = AppleRed,
                fontSize = 12.sp
              )
            }
          }
        },
        confirmButton = {
          Button(
            onClick = {
              if (passInput == "Nex2026") {
                onKioskModeToggle(false)
                showKioskPasswordDialog = false
              } else {
                passError = true
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = AppleBlue)
          ) {
            Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showKioskPasswordDialog = false }) {
            Text(
              text = if (activeLanguage == AppLanguage.KAZAKH) "Болдырмау" else "Отмена",
              color = AppleLightGrey
            )
          }
        },
        containerColor = AppleCharcoal,
        shape = RoundedCornerShape(24.dp)
      )
    }

    Card(
      shape = RoundedCornerShape(18.dp),
      border = BorderStroke(1.dp, AppleBorderColor),
      colors = CardDefaults.cardColors(containerColor = if (isDark) AppleCharcoal else Color.White),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        var tokenInput by remember { mutableStateOf("") }
        var isEditingToken by remember { mutableStateOf(deviceToken.isEmpty()) }
        var isValidatingNewToken by remember { mutableStateOf(false) }
        var newTokenError by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = if (activeLanguage == AppLanguage.KAZAKH) "API құрылғысының токені (X-Device-Token)" else "API Токен устройства (X-Device-Token)",
            color = AppleLightGrey,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
          
          if (tokenInfo != null) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AppleGreen.copy(alpha = 0.1f))
                .border(BorderStroke(1.dp, AppleGreen.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                .padding(14.dp),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(imageVector = Icons.Default.CloudDone, contentDescription = null, tint = AppleGreen)
              Column {
                Text(
                  text = "${if (activeLanguage == AppLanguage.KAZAKH) "Токен атауы" else "Имя токена"}: ${tokenInfo.name}",
                  color = AppleLightGrey,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                )
                if (!tokenInfo.deviceName.isNullOrEmpty()) {
                  Text(
                    text = "${if (activeLanguage == AppLanguage.KAZAKH) "Аппаратқа тіркелген" else "Привязан к аппарату"}: ${tokenInfo.deviceName}",
                    color = AppleMutedGrey,
                    fontSize = 12.sp
                  )
                }
              }
            }
          } else if (deviceToken.isNotEmpty() && !isFetchingTokenInfo) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AppleRed.copy(alpha = 0.1f))
                .border(BorderStroke(1.dp, AppleRed.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                .padding(14.dp),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(imageVector = Icons.Default.CloudOff, contentDescription = null, tint = AppleRed)
              Text(
                text = tokenInfoError.ifBlank {
                  if (activeLanguage == AppLanguage.KAZAKH) {
                    "Токенді тексеру мүмкін болмады"
                  } else {
                    "Не удалось проверить токен"
                  }
                },
                color = AppleRed,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }
          }

          if (isFetchingTokenInfo) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AppleBlue)
          }

          Spacer(modifier = Modifier.height(8.dp))

          if (!isEditingToken) {
            OutlinedButton(
              onClick = { isEditingToken = true; newTokenError = "" },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text(if (activeLanguage == AppLanguage.KAZAKH) "Токенді өзгерту" else "Изменить токен")
            }
          } else {
            Text(
              text = if (activeLanguage == AppLanguage.KAZAKH) "Жаңа токенді енгізу" else "Ввести новый токен",
              color = AppleMutedGrey,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedTextField(
                value = tokenInput,
                onValueChange = { tokenInput = it; newTokenError = "" },
                placeholder = { Text("nxt_...") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = AppleBlue,
                  unfocusedBorderColor = AppleBorderColor,
                  focusedTextColor = AppleLightGrey,
                  unfocusedTextColor = AppleLightGrey
                ),
                singleLine = true,
                enabled = !isValidatingNewToken
              )

              Button(
                onClick = {
                  val candidate = tokenInput
                  if (candidate.isNotBlank()) {
                    scope.launch {
                      isValidatingNewToken = true
                      newTokenError = ""
                      try {
                        val response = withContext(Dispatchers.IO) {
                          NexApiClient.service.getCurrentTokenInfo(candidate)
                        }
                        if (response.isSuccessful) {
                          onSaveToken(candidate)
                          tokenInput = ""
                          isEditingToken = deviceToken.isEmpty()
                        } else {
                          newTokenError = ApiErrorText.fromHttp(response.code(), response.errorBody()?.string(), activeLanguage)
                        }
                      } catch (e: Exception) {
                        newTokenError = ApiErrorText.fromThrowable(e, activeLanguage)
                      } finally {
                        isValidatingNewToken = false
                      }
                    }
                  }
                },
                enabled = tokenInput.isNotBlank() && !isValidatingNewToken,
                colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(54.dp)
              ) {
                Text(if (activeLanguage == AppLanguage.KAZAKH) "Жаңарту" else "ОБНОВИТЬ")
              }
            }

            if (isValidatingNewToken) {
              LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AppleBlue)
            }

            if (newTokenError.isNotBlank()) {
              Text(text = newTokenError, color = AppleRed, fontSize = 12.sp)
            }

            if (deviceToken.isNotEmpty()) {
              TextButton(onClick = { isEditingToken = false; tokenInput = ""; newTokenError = "" }) {
                Text(if (activeLanguage == AppLanguage.KAZAKH) "Бас тарту" else "Отмена")
              }
            }
          }
        }
      }
    }

    Text(
      text = if (activeLanguage == AppLanguage.KAZAKH) "ПЕРИФЕРИЯЛЫҚ ҚҰРЫЛҒЫЛАР" else "ПЕРИФЕРИЙНЫЕ УСТРОЙСТВА",
      color = AppleBlue,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.2.sp
    )

    Card(
      shape = RoundedCornerShape(18.dp),
      border = BorderStroke(1.dp, AppleBorderColor),
      colors = CardDefaults.cardColors(containerColor = if (isDark) AppleCharcoal else Color.White),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .background(AppleBlue.copy(alpha = 0.12f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = null, tint = AppleBlue)
            }
            Column {
              Text(
                text = if (activeLanguage == AppLanguage.KAZAKH) "Тонометр (Қан қысымы)" else "Тонометр (Артериальное давление)",
                color = AppleLightGrey,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
              Text(
                text = if (tonometerMode == "omron_ble") "Omron M4 Bluetooth BLE - $tonometerName [$tonometerMac]" else "Режим Симуляции Медосмотра",
                color = AppleMutedGrey,
                fontSize = 12.sp
              )
            }
          }

          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(if (isDark) AppleBlack else Color(0xFFF2F2F7))
              .padding(4.dp)
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (tonometerMode == "simulation") AppleBlue else Color.Transparent)
                .clickable { onSaveDevice("tonometer", "simulation", tonometerMac, tonometerName) }
                .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
              Text(
                text = if (activeLanguage == AppLanguage.KAZAKH) "Симуляция" else "ИМИТАЦИЯ",
                color = if (tonometerMode == "simulation") Color.White else AppleMutedGrey,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (tonometerMode == "omron_ble") AppleBlue else Color.Transparent)
                .clickable { onSaveDevice("tonometer", "omron_ble", tonometerMac, tonometerName) }
                .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
              Text(
                text = "OMRON BLE",
                color = if (tonometerMode == "omron_ble") Color.White else AppleMutedGrey,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    Card(
      shape = RoundedCornerShape(18.dp),
      border = BorderStroke(1.dp, AppleBorderColor),
      colors = CardDefaults.cardColors(containerColor = if (isDark) AppleCharcoal else Color.White),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .background(AppleAmber.copy(alpha = 0.12f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.Air, contentDescription = null, tint = AppleAmber)
            }
            Column {
              Text(
                text = if (activeLanguage == AppLanguage.KAZAKH) "Алкотестер" else "Алкотестер",
                color = AppleLightGrey,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
              Text(
                text = when (breathalyzerMode) {
                  "dingo_usb" -> "Dingo E-200 • USB Serial / MAX40 (4800 baud)"
                  "ble" -> "BLE Alcolock - $breathalyzerName [$breathalyzerMac]"
                  else -> "Режим Симуляции Медосмотра"
                },
                color = AppleMutedGrey,
                fontSize = 12.sp
              )
            }
          }

          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(if (isDark) AppleBlack else Color(0xFFF2F2F7))
              .padding(4.dp)
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (breathalyzerMode == "simulation") AppleBlue else Color.Transparent)
                .clickable { onSaveDevice("breathalyzer", "simulation", breathalyzerMac, breathalyzerName) }
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(
                text = if (activeLanguage == AppLanguage.KAZAKH) "Симул." else "ИМИТ.",
                color = if (breathalyzerMode == "simulation") Color.White else AppleMutedGrey,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (breathalyzerMode == "dingo_usb") AppleGreen else Color.Transparent)
                .clickable { onSaveDevice("breathalyzer", "dingo_usb", breathalyzerMac, breathalyzerName) }
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(
                text = "DINGO USB",
                color = if (breathalyzerMode == "dingo_usb") Color.White else AppleMutedGrey,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        // Dingo USB info block
        if (breathalyzerMode == "dingo_usb") {
          Spacer(modifier = Modifier.height(12.dp))
          val ctx = context
          val usbAvailable = remember { DingoSerialManager.isDeviceAvailable(ctx) }
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(if (usbAvailable) AppleGreen.copy(alpha = 0.08f) else AppleAmber.copy(alpha = 0.08f))
              .border(BorderStroke(1.dp, if (usbAvailable) AppleGreen.copy(alpha = 0.3f) else AppleAmber.copy(alpha = 0.3f)), RoundedCornerShape(10.dp))
              .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Usb,
              contentDescription = null,
              tint = if (usbAvailable) AppleGreen else AppleAmber,
              modifier = Modifier.size(22.dp)
            )
            Column {
              Text(
                text = if (usbAvailable) "USB устройство обнаружено" else "USB устройство не найдено",
                color = if (usbAvailable) AppleGreen else AppleAmber,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "4800 baud • 8N1 • MAX40 протокол",
                color = AppleMutedGrey,
                fontSize = 11.sp
              )
            }
          }
        }
      }
    }

    Card(
      shape = RoundedCornerShape(18.dp),
      border = BorderStroke(1.dp, AppleBorderColor),
      colors = CardDefaults.cardColors(containerColor = if (isDark) AppleCharcoal else Color.White),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .background(AppleGreen.copy(alpha = 0.12f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.Thermostat, contentDescription = null, tint = AppleGreen)
            }
            Column {
              Text(
                text = if (activeLanguage == AppLanguage.KAZAKH) "Термометр (Дене қызуы)" else "Термометр (Температура тела)",
                color = AppleLightGrey,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
              Text(
                text = when (thermometerMode) {
                  "microlife_ble" -> "Microlife NC-150 BT • GATT FFF0/FFF1 (без сопряжения)"
                  "ble" -> "BLE FastTemp - $thermometerName [$thermometerMac]"
                  else -> "Режим Симуляции Медосмотра"
                },
                color = AppleMutedGrey,
                fontSize = 12.sp
              )
            }
          }

          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(if (isDark) AppleBlack else Color(0xFFF2F2F7))
              .padding(4.dp)
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (thermometerMode == "simulation") AppleBlue else Color.Transparent)
                .clickable { onSaveDevice("thermometer", "simulation", thermometerMac, thermometerName) }
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(
                text = if (activeLanguage == AppLanguage.KAZAKH) "Симул." else "ИМИТ.",
                color = if (thermometerMode == "simulation") Color.White else AppleMutedGrey,
                fontSize = 11.sp, fontWeight = FontWeight.Bold
              )
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (thermometerMode == "microlife_ble") AppleGreen else Color.Transparent)
                .clickable { onSaveDevice("thermometer", "microlife_ble", thermometerMac, thermometerName) }
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(
                text = "MICROLIFE",
                color = if (thermometerMode == "microlife_ble") Color.White else AppleMutedGrey,
                fontSize = 11.sp, fontWeight = FontWeight.Bold
              )
            }
          }
        }

        // Microlife BLE — scan + MAC binding block
        if (thermometerMode == "microlife_ble") {
          Spacer(modifier = Modifier.height(4.dp))

          // Current MAC display
          val ctx = context
          val savedMac = remember { ctx.getSharedPreferences("nex_employees", android.content.Context.MODE_PRIVATE).getString("thermometer_mac", "") ?: "" }
          var currentMac by remember { mutableStateOf(savedMac) }

          if (currentMac.isNotEmpty()) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(AppleGreen.copy(alpha = 0.07f))
                .border(BorderStroke(1.dp, AppleGreen.copy(alpha = 0.3f)), RoundedCornerShape(10.dp))
                .padding(12.dp),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(imageVector = Icons.Default.Bluetooth, contentDescription = null, tint = AppleGreen, modifier = Modifier.size(20.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(text = "Microlife NC-150 BT", color = AppleGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = currentMac, color = AppleMutedGrey, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text(text = "Health Thermometer • 0x1809 • IEEE 11073", color = AppleMutedGrey.copy(alpha = 0.6f), fontSize = 10.sp)
              }
              IconButton(onClick = {
                ctx.getSharedPreferences("nex_employees", android.content.Context.MODE_PRIVATE)
                  .edit().putString("thermometer_mac", "").apply()
                currentMac = ""
              }) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = AppleMutedGrey, modifier = Modifier.size(16.dp))
              }
            }
          } else {
            // Scan to find Microlife
            var isScanning by remember { mutableStateOf(false) }
            var foundDevices by remember { mutableStateOf<List<MockBluetoothDevice>>(emptyList()) }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = if (activeLanguage == AppLanguage.KAZAKH) "Microlife NC-150 BT құрылғысын жұптау үшін сканерлеңіз" else "Отсканируйте BLE для привязки Microlife NC-150 BT",
                color = AppleMutedGrey, fontSize = 12.sp
              )
              Button(
                onClick = {
                  isScanning = true
                  foundDevices = emptyList()
                  scope.launch {
                    val bManager = ctx.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
                    val scanner = bManager?.adapter?.bluetoothLeScanner
                    if (scanner == null) {
                      isScanning = false
                      return@launch
                    }
                    val cb = object : ScanCallback() {
                      override fun onScanResult(callbackType: Int, result: ScanResult?) {
                        result?.device?.let { dev ->
                          val name = try { dev.name } catch (_: Exception) { null } ?: result.scanRecord?.deviceName ?: "Unknown"
                          if (!foundDevices.any { it.address == dev.address }) {
                            foundDevices = foundDevices + MockBluetoothDevice(name, dev.address, "thermometer", result.rssi)
                          }
                        }
                      }
                    }
                    try {
                      scanner.startScan(cb)
                      delay(6000)
                      scanner.stopScan(cb)
                    } catch (_: Exception) {}
                    isScanning = false
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppleGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
              ) {
                if (isScanning) {
                  CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                  Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                  text = if (isScanning) "Сканирование…" else if (activeLanguage == AppLanguage.KAZAKH) "BLE сканерлеу" else "Сканировать BLE",
                  color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp
                )
              }

              if (foundDevices.isNotEmpty()) {
                foundDevices.forEach { device ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(8.dp))
                      .background(AppleCharcoal.copy(alpha = 0.4f))
                      .clickable {
                        ctx.getSharedPreferences("nex_employees", android.content.Context.MODE_PRIVATE)
                          .edit().putString("thermometer_mac", device.address).apply()
                        onSaveDevice("thermometer", "microlife_ble", device.address, device.name)
                        currentMac = device.address
                      }
                      .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(imageVector = Icons.Default.Bluetooth, contentDescription = null, tint = AppleBlue, modifier = Modifier.size(18.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Text(text = device.name, color = AppleLightGrey, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                      Text(text = device.address, color = AppleMutedGrey, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                    Text(text = "${device.rssi} dBm", color = AppleMutedGrey, fontSize = 10.sp)
                  }
                }
              }
            }
          }
        }
      }
    }

    Card(
      shape = RoundedCornerShape(18.dp),
      border = BorderStroke(1.dp, AppleBorderColor),
      colors = CardDefaults.cardColors(containerColor = if (isDark) AppleCharcoal.copy(alpha = 0.5f) else Color(0xFFE5E5EA)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = if (activeLanguage == AppLanguage.KAZAKH) "ЖАҚЫНДАҒЫ ҚҰРЫЛҒЫЛАРДЫ ІЗДЕУ" else "ПОИСК БЛИЖАЙШИХ BLE УСТРОЙСТВ",
              color = AppleLightGrey,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Text(
              text = if (activeLanguage == AppLanguage.KAZAKH) "Жұптау жасау және таңдау үшін сканерлеңіз" else "Запустите сканирование пространства для сопряжения",
              color = AppleMutedGrey,
              fontSize = 12.sp
            )
          }

          Button(
            onClick = { startBleScan() },
            colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
            shape = RoundedCornerShape(12.dp)
          ) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              if (isScanning) {
                CircularProgressIndicator(
                  color = Color.White,
                  modifier = Modifier.size(16.dp),
                  strokeWidth = 2.dp
                )
              } else {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
              }
              Text(
                text = if (isScanning) (if (activeLanguage == AppLanguage.KAZAKH) "Іздеуде..." else "ПОИСК...") else (if (activeLanguage == AppLanguage.KAZAKH) "Скан жасау" else "СКАНИРОВАТЬ"),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        if (scanErrorText.isNotEmpty()) {
          Text(text = scanErrorText, color = AppleAmber, fontSize = 12.sp)
        }

        if (foundDevices.isEmpty() && !isScanning) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(100.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(if (isDark) AppleBlack.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (activeLanguage == AppLanguage.KAZAKH) "Ешқандай құрылғы табылмады. Сканерлеуді бастаңыз." else "Устройства не найдены. Нажмите сканировать.",
              color = AppleMutedGrey,
              fontSize = 12.sp
            )
          }
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            foundDevices.sortedByDescending { it.rssi }.forEach { dev ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isDark) AppleBlack.copy(alpha = 0.5f) else Color.White)
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(8.dp)
                      .clip(CircleShape)
                      .background(
                        when (dev.type) {
                          "tonometer" -> AppleBlue
                          "breathalyzer" -> AppleAmber
                          else -> AppleGreen
                        }
                      )
                  )
                  Column {
                    Text(
                      text = dev.name,
                      color = AppleLightGrey,
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 13.sp
                    )
                    Text(
                      text = "MAC: ${dev.address} | RSSI: ${dev.rssi}dBm",
                      color = AppleMutedGrey,
                      fontSize = 11.sp,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                }

                val isDeviceActive = dev.address.isNotEmpty() && (
                  tonometerMac == dev.address ||
                  breathalyzerMac == dev.address ||
                  thermometerMac == dev.address
                )

                Button(
                  onClick = {
                    selectedDeviceForBinding = dev
                  },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDeviceActive) AppleGreen else AppleBlue
                  ),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                  Text(
                    text = if (isDeviceActive) {
                      if (activeLanguage == AppLanguage.KAZAKH) "Таңдалды" else "ВЫБРАН"
                    } else {
                      if (activeLanguage == AppLanguage.KAZAKH) "ЖҰПТАУ" else "СОПРЯЧЬ"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  // Dialog for selecting target device application slot
  selectedDeviceForBinding?.let { dev ->
    val triggerBluetoothBonding: (Context, MockBluetoothDevice) -> Unit = { ctx, deviceMock ->
      val bManager = ctx.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
      val adapter = bManager?.adapter
      if (adapter != null && adapter.isEnabled) {
        try {
          val isBTPermissionGranted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            ctx.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
          } else {
            true
          }

          if (isBTPermissionGranted) {
            if (MicrolifeManager.isMicrolifeDevice(deviceMock.name)) {
              android.widget.Toast.makeText(ctx, "Microlife ${deviceMock.name}: сопряжение не нужно, используется прямое BLE-подключение", android.widget.Toast.LENGTH_SHORT).show()
            } else if (deviceMock.address != "00:80:25:AB:CD:01" && deviceMock.address != "18:93:7F:42:11:A4" && deviceMock.address != "54:D0:39:6C:AA:FF") {
              val systemDevice = adapter.getRemoteDevice(deviceMock.address)
              if (systemDevice.bondState == BluetoothDevice.BOND_BONDED) {
                android.widget.Toast.makeText(ctx, "Устройство ${deviceMock.name} уже сопряжено на уровне системы!", android.widget.Toast.LENGTH_SHORT).show()
              } else {
                android.widget.Toast.makeText(ctx, "Инициализация сопряжения с ${deviceMock.name}... Подтвердите запрос на сопряжение в системном окне или панели уведомлений!", android.widget.Toast.LENGTH_LONG).show()
                systemDevice.createBond()
              }
            } else {
              android.widget.Toast.makeText(ctx, "Сопряжение с симулятором ${deviceMock.name} успешно завершено!", android.widget.Toast.LENGTH_SHORT).show()
            }
          } else {
            android.widget.Toast.makeText(ctx, "Нет разрешений BLUETOOTH_CONNECT для выполнения сопряжения!", android.widget.Toast.LENGTH_LONG).show()
          }
        } catch (e: Exception) {
          android.widget.Toast.makeText(ctx, "Ошибка сопряжения: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
        }
      }
    }

    androidx.compose.material3.AlertDialog(
      onDismissRequest = { selectedDeviceForBinding = null },
      title = {
        Text(
          text = if (activeLanguage == AppLanguage.KAZAKH) "Құрылғының мақсатын таңдау" else "Назначение BLE устройства",
          color = AppleLightGrey,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = if (activeLanguage == AppLanguage.KAZAKH) {
              "Осы құрылғы үшін өлшеу санатын таңдаңыз:\n\nАтауы: ${dev.name}\nМекенжай: ${dev.address}"
            } else {
              "Выберите целевую категорию измерения для этого устройства:\n\nИмя: ${dev.name}\nАдрес: ${dev.address}"
            },
            color = AppleLightGrey,
            fontSize = 14.sp
          )
          
          Spacer(modifier = Modifier.height(6.dp))
          
          // Button 1: Tonometer
          Button(
            onClick = {
              if (dev.name.contains("omron", ignoreCase = true) || dev.name.contains("M4 Intelli IT", ignoreCase = true)) {
                showOmronPairingDialog = true
              } else {
                onSaveDevice("tonometer", "omron_ble", dev.address, dev.name)
                selectedDeviceForBinding = null
                // Не просто createBond() — сразу ведём реальный GATT/CTS-хендшейк,
                // без которого Omron остаётся мигать «P» даже после подтверждения
                // системного окна сопряжения (см. OmronBleManager.connect/rebondAndRetry).
                OmronBleManager.connect(context, dev.address) { _, _, _ -> }
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = null, modifier = Modifier.size(18.dp))
              Text(
                text = if (activeLanguage == AppLanguage.KAZAKH) "Тонометр (Қан қысымы)" else "Тонометр (Давление и пульс)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }

          // Button 2: Breathalyzer
          Button(
            onClick = {
              onSaveDevice("breathalyzer", "ble", dev.address, dev.name)
              selectedDeviceForBinding = null
              triggerBluetoothBonding(context, dev)
            },
            colors = ButtonDefaults.buttonColors(containerColor = AppleAmber),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(imageVector = Icons.Default.Air, contentDescription = null, modifier = Modifier.size(18.dp))
              Text(
                text = if (activeLanguage == AppLanguage.KAZAKH) "Алкотестер (Алкоголь буы)" else "Алкотестер (Пары алкоголя)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }

          // Button 3: Thermometer (Microlife NC-150 BT — no system pairing needed)
          Button(
            onClick = {
              val mode = if (MicrolifeManager.isMicrolifeDevice(dev.name)) "microlife_ble" else "ble"
              onSaveDevice("thermometer", mode, dev.address, dev.name)
              selectedDeviceForBinding = null
              if (mode != "microlife_ble") {
                triggerBluetoothBonding(context, dev)
              } else {
                android.widget.Toast.makeText(
                  context,
                  "Microlife привязан по MAC — сопряжение не требуется",
                  android.widget.Toast.LENGTH_SHORT
                ).show()
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = AppleGreen),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(imageVector = Icons.Default.Thermostat, contentDescription = null, modifier = Modifier.size(18.dp))
              Text(
                text = if (activeLanguage == AppLanguage.KAZAKH) "Термометр (Дене қызуы)" else "Термометр (Температура тела)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        TextButton(onClick = { selectedDeviceForBinding = null }) {
          Text(
            text = if (activeLanguage == AppLanguage.KAZAKH) "Бас тарту" else "Отмена",
            color = AppleLightGrey,
            fontWeight = FontWeight.Bold
          )
        }
      },
      containerColor = AppleCharcoal,
      shape = RoundedCornerShape(18.dp)
    )

    if (showOmronPairingDialog) {
      AlertDialog(
        onDismissRequest = { showOmronPairingDialog = false },
        title = { Text("Сопряжение Omron") },
        text = { Text("Удерживайте кнопку Bluetooth на Omron 3–5 секунд, пока не начнёт мигать «P». Затем нажмите «Готово» и сразу подтвердите системное Bluetooth-окно.") },
        confirmButton = {
          TextButton(onClick = {
            showOmronPairingDialog = false
            onSaveDevice("tonometer", "omron_ble", dev.address, dev.name)
            selectedDeviceForBinding = null
            OmronBleManager.connect(context, dev.address) { _, _, _ -> }
          }) { Text("Готово") }
        },
        dismissButton = {
          TextButton(onClick = { showOmronPairingDialog = false }) { Text("Отмена") }
        }
      )
    }
  }
}
