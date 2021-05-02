package io.starrychoon.euckr

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.*
import kotlinx.coroutines.reactive.*
import org.slf4j.*
import org.springframework.stereotype.*
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.*

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

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(MyHandler::class.java)
    }

    private data class Message(val name: String = "unknown", val message: String = "none")
}
