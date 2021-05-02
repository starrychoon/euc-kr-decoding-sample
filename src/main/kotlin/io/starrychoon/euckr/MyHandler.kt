package io.starrychoon.euckr

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.*
import kotlinx.coroutines.reactive.*
import org.slf4j.*
import org.springframework.stereotype.*
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.*
import java.net.*
import java.nio.charset.*

/**
 * @author Sungchoon Park
 */
@Component
class MyHandler(
    private val objectMapper: ObjectMapper,
) {

    suspend fun readEucKrTextRequest(request: ServerRequest): ServerResponse {
        val bytes = request.awaitBody<ByteArray>()
        val text = bytes.toString(ExtendedCharsets.EUC_KR)
        logger.info("decoded text: {}", text)

        return ok().buildAndAwait()
    }

    suspend fun readEucKrJsonRequest(request: ServerRequest): ServerResponse {
        val bytes = request.awaitBody<ByteArray>()
        val text = bytes.toString(ExtendedCharsets.EUC_KR)
        val message = objectMapper.readValue<Message>(text)
        logger.info("decoded json: {}", message)

        return ok().buildAndAwait()
    }

    suspend fun readEucKrFormRequest(request: ServerRequest): ServerResponse {
        val formData = request.body(EucKrBodyExtractors.toFormData()).awaitSingle()
        logger.info("decoded form data: {}", formData)

        return ok().buildAndAwait()
    }

    suspend fun readEucKrFormRequest2(request: ServerRequest): ServerResponse {
        // EucKrBodyExtractors를 사용하지 않고 ByteArray를 읽어서 직접 EUC-KR로 디코딩하는것도 가능
        val bytes = request.awaitBody<ByteArray>()
        val text = bytes.toString(ExtendedCharsets.EUC_KR)
        val formData = deserializeForm(text, ExtendedCharsets.EUC_KR)
        logger.info("decoded form data: {}", formData)

        return ok().buildAndAwait()
    }

    private fun deserializeForm(text: String, charset: Charset): FormData {
        val pairs = text.splitToSequence('&')
            .map { it.trim() }
            .filterNot { it.isEmpty() }
            .toList()
        val formData = FormData()

        for (pair in pairs) {
            val (first, second) = pair.split('=').let { it.first() to it.getOrNull(1) }
            val name = URLDecoder.decode(first, charset)
            val value = second?.let { URLDecoder.decode(it, charset) }
            formData.add(name, value)
        }

        return formData
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(MyHandler::class.java)
    }

    private data class Message(val name: String = "unknown", val message: String = "none")
}
