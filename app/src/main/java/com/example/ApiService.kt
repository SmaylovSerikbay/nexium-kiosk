package com.example

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Part
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class VerifyEmployeeRequest(
    @Json(name = "employee_id") val employeeId: String
)

@JsonClass(generateAdapter = true)
data class VerifyEmployeeResponse(
    @Json(name = "employee_id") val employeeId: String,
    @Json(name = "exists") val exists: Boolean,
    @Json(name = "full_name") val fullName: String? = null,
    
    // Organization / Company mapping variants
    @Json(name = "organization_name") val organizationName: String? = null,
    @Json(name = "organization") val organization: String? = null,
    @Json(name = "company_name") val companyName: String? = null,
    @Json(name = "company") val company: String? = null,
    
    // Position / Title mapping variants
    @Json(name = "position_name") val positionName: String? = null,
    @Json(name = "position") val position: String? = null,
    @Json(name = "position_kk") val positionKk: String? = null,
    @Json(name = "job_title") val jobTitle: String? = null,
    @Json(name = "role") val role: String? = null,
    
    // Branch / Filial / Department mapping variants
    @Json(name = "branch_name") val branchName: String? = null,
    @Json(name = "branch") val branch: String? = null,
    @Json(name = "filial_name") val filialName: String? = null,
    @Json(name = "filial") val filial: String? = null,
    @Json(name = "department_name") val departmentName: String? = null,
    @Json(name = "department") val department: String? = null,
    
    // Avatar / Photo URL mapping variants
    @Json(name = "photo_url") val photoUrl: String? = null,
    @Json(name = "photo") val photo: String? = null,
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    @Json(name = "avatar") val avatar: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "image") val image: String? = null,
    @Json(name = "photo_path") val photoPath: String? = null,
    @Json(name = "avatar_path") val avatarPath: String? = null,
    @Json(name = "image_path") val imagePath: String? = null,
    @Json(name = "profile_photo") val profilePhoto: String? = null,
    @Json(name = "profile_picture") val profilePicture: String? = null,
    @Json(name = "picture") val picture: String? = null,
    @Json(name = "pic") val pic: String? = null,
    @Json(name = "face_id_enabled") val faceIdEnabled: Boolean? = null
) {
    // Helper to get the organization name safely
    fun getEffectiveOrganization(): String {
        return organizationName ?: organization ?: companyName ?: company ?: ""
    }
    
    // Helper to get the position/role safely; prefers the Kazakh translation when kazakh=true
    fun getEffectivePosition(kazakh: Boolean = false): String {
        if (kazakh && !positionKk.isNullOrBlank()) return positionKk
        return positionName ?: position ?: jobTitle ?: role ?: ""
    }
    
    // Helper to get the branch/filial safely
    fun getEffectiveBranch(): String {
        return branchName ?: branch ?: filialName ?: filial ?: departmentName ?: department ?: ""
    }
    
    // Helper to get the photo or avatar URL, supporting relative and absolute forms
    fun getEffectivePhotoUrl(): String? {
        val raw = photoUrl ?: photo ?: avatarUrl ?: avatar ?: imageUrl ?: image ?:
                  photoPath ?: avatarPath ?: imagePath ?: profilePhoto ?: profilePicture ?: picture ?: pic
        if (raw.isNullOrEmpty()) return null
        if (raw.startsWith("data:")) return raw

        var resolved = if (raw.startsWith("http://") || raw.startsWith("https://")) {
            raw
        } else {
            val baseUrl = "https://nexium-health.com/"
            if (raw.startsWith("/")) {
                baseUrl + raw.substring(1)
            } else {
                baseUrl + raw
            }
        }
        
        // Force HTTPS to comply with modern secure connection patterns
        if (resolved.startsWith("http://")) {
            resolved = "https://" + resolved.substring(7)
        }
        return resolved
    }
}

@JsonClass(generateAdapter = true)
data class VerifyFaceRequest(
    @Json(name = "face_photo") val facePhoto: String,
    @Json(name = "face_photos") val facePhotos: List<String>
)

@JsonClass(generateAdapter = true)
data class EnrollFaceRequest(
    @Json(name = "employee_id") val employeeId: String,
    @Json(name = "face_photo") val facePhoto: String,
    @Json(name = "face_photos") val facePhotos: List<String>
)

@JsonClass(generateAdapter = true)
data class DisableFaceRequest(
    @Json(name = "employee_id") val employeeId: String
)

@JsonClass(generateAdapter = true)
data class CreateExamRequest(
    @Json(name = "exam_id") val examId: String = "",
    @Json(name = "employee_id") val employeeId: String,
    @Json(name = "device_id") val deviceId: Int,
    @Json(name = "type_status") val typeStatus: String,
    @Json(name = "systolic") val systolic: Int,
    @Json(name = "diastolic") val diastolic: Int,
    @Json(name = "pulse") val pulse: Int,
    @Json(name = "breathalyzer") val breathalyzer: String,
    @Json(name = "temperature") val temperature: Double,
    @Json(name = "complaints") val complaints: String,
    @Json(name = "drug_test") val drugTest: String,
    @Json(name = "device_dopusk") val deviceDopusk: String,
    @Json(name = "price_charged") val priceCharged: Double
)

@JsonClass(generateAdapter = true)
data class RegisterPatientRequest(
    @Json(name = "employee_id") val employeeId: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "avatar_photo") val avatarPhoto: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "preferred_language") val preferredLanguage: String? = "ru",
    @Json(name = "organization_id") val organizationId: Int? = null,
    @Json(name = "branch_id") val branchId: Int? = null,
    @Json(name = "position_id") val positionId: Int? = null
)

@JsonClass(generateAdapter = true)
data class RegisteredEmployeeInfo(
    @Json(name = "id") val id: Int?,
    @Json(name = "employee_id") val employeeId: String?,
    @Json(name = "full_name") val fullName: String?,
    @Json(name = "phone") val phone: String?,
    @Json(name = "preferred_language") val preferredLanguage: String?
)

@JsonClass(generateAdapter = true)
data class PatientRegisterResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String?,
    @Json(name = "employee") val employee: RegisteredEmployeeInfo?
)

@JsonClass(generateAdapter = true)
data class OrganizationRef(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class BranchRef(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "organization_id") val organizationId: Int
)

@JsonClass(generateAdapter = true)
data class PositionRef(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "name_kk") val nameKk: String? = null
)

@JsonClass(generateAdapter = true)
data class PayExamRequest(
    @Json(name = "amount") val amount: Double,
    @Json(name = "exam_id") val examId: String,
    @Json(name = "payment_method") val paymentMethod: String,
    @Json(name = "external_tx_id") val externalTxId: String? = null
)

@JsonClass(generateAdapter = true)
data class ExamInfo(
    @Json(name = "id") val id: String,
    @Json(name = "employee_id") val employeeId: String,
    @Json(name = "dopusk") val dopusk: String,
    @Json(name = "is_signed") val isSigned: Boolean,
    @Json(name = "payment_status") val paymentStatus: String,
    @Json(name = "pulse") val pulse: Int?,
    @Json(name = "systolic") val systolic: Int?,
    @Json(name = "diastolic") val diastolic: Int?
)

@JsonClass(generateAdapter = true)
data class AppVersionResponse(
    @Json(name = "version_code") val versionCode: Int,
    @Json(name = "version_name") val versionName: String,
    @Json(name = "apk_url") val apkUrl: String,
    @Json(name = "release_notes") val releaseNotes: String? = null
)

@JsonClass(generateAdapter = true)
data class AppUpdateStatusRequest(
    @Json(name = "status") val status: String,
    @Json(name = "target_version_code") val targetVersionCode: Int,
    @Json(name = "target_version_name") val targetVersionName: String? = null,
    @Json(name = "installed_version_code") val installedVersionCode: Int,
    @Json(name = "installed_version_name") val installedVersionName: String,
    @Json(name = "apk_url") val apkUrl: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "package_name") val packageName: String,
    @Json(name = "device_manufacturer") val deviceManufacturer: String,
    @Json(name = "device_model") val deviceModel: String,
    @Json(name = "android_sdk") val androidSdk: Int,
    @Json(name = "reported_at") val reportedAt: Long
)

@JsonClass(generateAdapter = true)
data class ExamDetailResponse(
    @Json(name = "exam") val exam: ExamInfo?,
    @Json(name = "nurse_name") val nurseName: String?,
    @Json(name = "success") val success: Boolean?,
    // null = старый бэкенд без этого поля — считаем разрешённым, как раньше
    @Json(name = "auto_confirm_enabled") val autoConfirmEnabled: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class TokenInfoResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "device_name") val deviceName: String? = null,
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "expires_at") val expiresAt: String? = null
)

interface NexApiService {
    @POST("exams/verify-employee")
    suspend fun verifyEmployee(
        @Header("X-Device-Token") deviceToken: String,
        @Body request: VerifyEmployeeRequest
    ): VerifyEmployeeResponse

    @POST("exams/verify-face")
    suspend fun verifyFace(
        @Header("X-Device-Token") deviceToken: String,
        @Body request: VerifyFaceRequest
    ): VerifyEmployeeResponse

    @POST("exams/enroll-face")
    suspend fun enrollFace(
        @Header("X-Device-Token") deviceToken: String,
        @Body request: EnrollFaceRequest
    ): VerifyEmployeeResponse

    @POST("exams/disable-face")
    suspend fun disableFace(
        @Header("X-Device-Token") deviceToken: String,
        @Body request: DisableFaceRequest
    ): Response<ResponseBody>

    @POST("exams/create")
    suspend fun createExam(
        @Header("X-Device-Token") deviceToken: String,
        @Body request: CreateExamRequest
    ): Response<ResponseBody>

    @POST("exams/pay")
    suspend fun payExam(
        @Header("X-Device-Token") deviceToken: String,
        @Body request: PayExamRequest
    ): Response<ResponseBody>

    @GET("exams/{id}")
    suspend fun getExamDetail(
        @Header("X-Device-Token") deviceToken: String,
        @Path("id") id: String
    ): Response<ExamDetailResponse>

    @POST("patients/register")
    suspend fun registerPatient(
        @Header("X-Device-Token") deviceToken: String,
        @Body request: RegisterPatientRequest
    ): Response<PatientRegisterResponse>

    @GET("reference/organizations")
    suspend fun getOrganizations(
        @Header("X-Device-Token") deviceToken: String
    ): List<OrganizationRef>

    @GET("reference/branches")
    suspend fun getBranches(
        @Header("X-Device-Token") deviceToken: String
    ): List<BranchRef>

    @GET("reference/positions")
    suspend fun getPositions(
        @Header("X-Device-Token") deviceToken: String
    ): List<PositionRef>

    // 200 с данными активного релиза, либо 204 если ничего не опубликовано —
    // поэтому оборачиваем в Response, а не возвращаем AppVersionResponse напрямую
    // (Moshi не может распарсить пустое тело 204 ответа).
    @GET("app/latest-version")
    suspend fun getLatestAppVersion(
        @Header("X-Device-Token") deviceToken: String,
        @Header("X-App-Version-Code") versionCode: Int,
        @Header("X-App-Version-Name") versionName: String
    ): Response<AppVersionResponse>

    @POST("app/update-status")
    suspend fun reportAppUpdateStatus(
        @Header("X-Device-Token") deviceToken: String,
        @Body request: AppUpdateStatusRequest
    ): Response<ResponseBody>

    @GET("tokens/me")
    suspend fun getCurrentTokenInfo(
        @Header("X-Device-Token") deviceToken: String
    ): Response<TokenInfoResponse>

    @Multipart
    @POST("exams/{id}/video")
    suspend fun uploadExamVideo(
        @Header("X-Device-Token") deviceToken: String,
        @Path("id") examId: String,
        @Part video: MultipartBody.Part
    ): Response<ResponseBody>
}

object NexApiClient {
    const val DEFAULT_BASE_URL = "https://nexium-health.com/api/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.MINUTES)
        .writeTimeout(5, TimeUnit.MINUTES)
        .build()

    private fun buildService(url: String): NexApiService {
        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(NexApiService::class.java)
    }

    /** Строит отдельный сервис на кандидатный URL, не трогая текущий [service] — для проверки перед сохранением. */
    fun buildTemporaryService(url: String): NexApiService = buildService(normalizeBaseUrl(url))

    private fun normalizeBaseUrl(url: String): String = if (url.endsWith("/")) url else "$url/"

    var baseUrl: String = try {
        BuildConfig.NEX_API_BASE_URL
    } catch (e: Throwable) {
        DEFAULT_BASE_URL
    }
        private set

    var service: NexApiService = buildService(baseUrl)
        private set

    fun setBaseUrl(url: String) {
        baseUrl = normalizeBaseUrl(url)
        service = buildService(baseUrl)
    }

    var deviceToken: String = ""
        private set

    fun updateDeviceToken(token: String) {
        deviceToken = token
    }

    fun init(context: android.content.Context) {
        val prefs = context.getSharedPreferences("nex_settings", android.content.Context.MODE_PRIVATE)
        val savedToken = prefs.getString("device_token", null)
        if (!savedToken.isNullOrEmpty()) {
            deviceToken = savedToken
        }
        val savedBaseUrl = prefs.getString("api_base_url", null)
        if (!savedBaseUrl.isNullOrEmpty()) {
            setBaseUrl(savedBaseUrl)
        }
    }
}
