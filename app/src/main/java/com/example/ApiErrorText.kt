package com.example

import org.json.JSONObject
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

object ApiErrorText {
    private val inactiveToken = Trans(
        "Токен аппарата отключён. Обратитесь к администратору.",
        "Аппарат токені өшірілген. Әкімшіге хабарласыңыз."
    )
    private val employeeOrgMismatch = Trans(
        "Сотрудник не относится к организации этого аппарата. Обратитесь к администратору.",
        "Қызметкер осы аппарат ұйымына жатпайды. Әкімшіге хабарласыңыз."
    )
    private val noInternet = Trans(
        "Нет соединения с интернетом. Проверьте Wi-Fi или мобильную сеть на планшете.",
        "Интернет байланысы жоқ. Планшеттегі Wi-Fi немесе мобильді желіні тексеріңіз."
    )
    private val timeout = Trans(
        "Сервер не ответил вовремя. Проверьте интернет и повторите попытку.",
        "Сервер уақытында жауап бермеді. Интернетті тексеріп, қайталап көріңіз."
    )
    private val secureConnection = Trans(
        "Не удалось установить безопасное соединение с сервером. Проверьте дату, время и интернет на планшете.",
        "Сервермен қауіпсіз байланыс орнату мүмкін болмады. Планшеттегі күнді, уақытты және интернетті тексеріңіз."
    )
    private val accessDenied = Trans(
        "Доступ запрещён. У этого аппарата нет прав для выполнения операции.",
        "Кіруге тыйым салынған. Бұл аппараттың осы әрекетті орындауға құқығы жоқ."
    )
    private val notFound = Trans(
        "Запрошенные данные не найдены на сервере.",
        "Сұралған деректер серверден табылмады."
    )
    private val validation = Trans(
        "Сервер отклонил данные. Проверьте заполненные поля и повторите попытку.",
        "Сервер деректерді қабылдамады. Толтырылған өрістерді тексеріп, қайталап көріңіз."
    )
    private val server = Trans(
        "На сервере произошла ошибка. Повторите попытку позже или обратитесь к администратору.",
        "Серверде қате пайда болды. Кейінірек қайталап көріңіз немесе әкімшіге хабарласыңыз."
    )
    private val connection = Trans(
        "Ошибка соединения с сервером. Проверьте интернет и повторите попытку.",
        "Сервермен байланысу қатесі. Интернетті тексеріп, қайталап көріңіз."
    )
    private val unknown = Trans(
        "Не удалось выполнить запрос к серверу.",
        "Серверге сұранысты орындау мүмкін болмады."
    )

    fun fromHttp(
        code: Int,
        errorBody: String?,
        lang: AppLanguage,
        operation: String? = null
    ): String {
        val serverMessage = extractServerMessage(errorBody)
        val normalized = serverMessage.lowercase()
        val text = when {
            code == 401 || normalized.contains("неактивный api-токен") ||
                normalized.contains("неверный или неактивный") ||
                normalized.contains("invalid") && normalized.contains("token") ||
                normalized.contains("inactive") && normalized.contains("token") ||
                normalized.contains("unauthorized") -> inactiveToken.get(lang)

            normalized.contains("сотрудник не принадлежит организации аппарата") ||
                normalized.contains("не относится к организации") -> employeeOrgMismatch.get(lang)

            code == 403 -> accessDenied.get(lang)
            code == 404 -> notFound.get(lang)
            code == 400 || code == 422 -> validation.get(lang)
            code >= 500 -> server.get(lang)
            else -> unknown.get(lang)
        }

        val result = appendSafeServerMessage(text, serverMessage)
        return withOperation(operation, result)
    }

    fun fromThrowable(
        throwable: Throwable,
        lang: AppLanguage,
        operation: String? = null
    ): String {
        val text = when (throwable) {
            is HttpException -> {
                val body = throwable.response()?.errorBody()?.string()
                return fromHttp(throwable.code(), body, lang, operation)
            }
            is UnknownHostException -> noInternet.get(lang)
            is SocketTimeoutException -> timeout.get(lang)
            is ConnectException -> connection.get(lang)
            is SSLException -> secureConnection.get(lang)
            else -> {
                val cause = throwable.cause
                if (cause != null && cause !== throwable) {
                    return fromThrowable(cause, lang, operation)
                }
                val detail = throwable.localizedMessage ?: throwable.message
                appendSafeServerMessage(connection.get(lang), detail.orEmpty())
            }
        }
        return withOperation(operation, text)
    }

    private fun withOperation(operation: String?, message: String): String {
        if (operation.isNullOrBlank()) return message
        if (message.startsWith(operation, ignoreCase = true)) return message
        return "$operation. $message"
    }

    private fun extractServerMessage(errorBody: String?): String {
        val body = errorBody?.trim().orEmpty()
        if (body.isEmpty() || body.startsWith("<")) return ""
        return try {
            val json = JSONObject(body)
            listOf("message", "error", "detail", "description")
                .asSequence()
                .map { json.optString(it).trim() }
                .firstOrNull { it.isNotEmpty() }
                .orEmpty()
        } catch (_: Exception) {
            body
        }
    }

    private fun appendSafeServerMessage(base: String, serverMessage: String): String {
        val clean = serverMessage.trim()
        if (clean.isEmpty()) return base
        if (clean.length > 180 || clean.startsWith("<")) return base
        if (base.contains(clean, ignoreCase = true)) return base
        return "$base ($clean)"
    }
}
