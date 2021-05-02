package io.starrychoon.euckr

import org.springframework.core.*
import org.springframework.http.*
import org.springframework.http.codec.*
import org.springframework.http.server.reactive.*
import org.springframework.util.*
import org.springframework.web.reactive.function.*
import reactor.core.publisher.*
import java.nio.charset.*

/**
 * [application/x-www-form-urlencoded][MediaType.APPLICATION_FORM_URLENCODED] form data type
 *
 * @author Sungchoon Park
 */
typealias FormData = MultiValueMap<String, String?>

fun FormData(): FormData {
    return LinkedMultiValueMap()
}

fun FormData(size: Int): FormData {
    return LinkedMultiValueMap(size)
}

/**
 * @author Sungchoon Park
 */
object ExtendedCharsets {
    @JvmField
    val EUC_KR: Charset = charset("EUC-KR")
}

/**
 * marker type for ecu-kr encoded form data
 *
 * @author Sungchoon Park
 */
private object EucKrFormDataType

/**
 * [Content-Type][MediaType]에 charset 파라미터도 없이 euc-kr로 인코딩된 리퀘스트를 받아야 하거나,
 * euc-kr로 인코딩된 리스폰스를 읽어야 하는 답도 없는 경우를 위한 [BodyExtractor] 구현체
 *
 * 구현 방식은 [BodyExtractors]를 참고함
 *
 * @author Sungchoon Park
 */
object EucKrBodyExtractors {

    @JvmStatic
    private val EUC_KR_FORM_DATA_TYPE: ResolvableType = ResolvableType.forClass(EucKrFormDataType::class.java)

    @JvmStatic
    private val DEFAULT_FORM_READER: FormHttpMessageReader = EucKrFormHttpMessageReader()

    @JvmStatic
    private val EMPTY_FORM_DATA: Mono<FormData> =
        Mono.just<FormData>(CollectionUtils.unmodifiableMultiValueMap(FormData(0))).cache()

    /**
     * form data를 [euc-kr][ExtendedCharsets.EUC_KR]로 디코딩해 [FormData]로 변환하는 [BodyExtractor]를 반환한다
     *
     * [ServerRequest.formData][org.springframework.web.reactive.function.server.ServerRequest.formData]와 다르게
     * 한번 읽은 form data를 캐싱하지 않는다
     */
    fun toFormData(): BodyExtractor<Mono<FormData>, ReactiveHttpInputMessage> = BodyExtractor { message, context ->
        val elementType = EUC_KR_FORM_DATA_TYPE
        val mediaType = MediaType.APPLICATION_FORM_URLENCODED

        if (mediaType.isCompatibleWith(message.headers.contentType)) {
            val reader = findReader<FormData>(elementType, mediaType, context) ?: DEFAULT_FORM_READER
            readToMono(message, context, elementType, reader).switchIfEmpty(EMPTY_FORM_DATA)
        } else {
            EMPTY_FORM_DATA
        }
    }

    private fun <BODY> findReader(
        elementType: ResolvableType,
        mediaType: MediaType,
        context: BodyExtractor.Context,
    ): HttpMessageReader<BODY>? {
        return context.messageReaders()
            .find { it.canRead(elementType, mediaType) }
            ?.let { cast(it) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <BODY> cast(reader: HttpMessageReader<*>): HttpMessageReader<BODY> {
        return reader as HttpMessageReader<BODY>
    }

    private fun <BODY> readToMono(
        message: ReactiveHttpInputMessage,
        context: BodyExtractor.Context,
        type: ResolvableType,
        reader: HttpMessageReader<BODY>,
    ): Mono<BODY> {
        return context.serverResponse()
            .map { reader.readMono(type, type, message as ServerHttpRequest, it, context.hints()) }
            .orElseGet { reader.readMono(type, message, context.hints()) }
    }
}

/**
 * [EUC-KR][ExtendedCharsets.EUC_KR]을 [defaultCharset]으로 사용하는 [FormHttpMessageReader]
 *
 * @author Sungchoon Park
 */
class EucKrFormHttpMessageReader : FormHttpMessageReader() {

    init {
        super.setDefaultCharset(ExtendedCharsets.EUC_KR)
    }

    /**
     * [BaseDefaultCodecs.getTypedReaders][org.springframework.http.codec.support.BaseDefaultCodecs.getTypedReaders]에서 기본으로
     * 등록하는 form reader와 충돌되지 않도록 [elementType]은 [EucKrFormDataType]만 지원한다
     */
    override fun canRead(elementType: ResolvableType, mediaType: MediaType?): Boolean {
        return EUC_KR_FORM_DATA_TYPE.isAssignableFrom(elementType) &&
                (mediaType == null || MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType))
    }

    companion object {
        @JvmStatic
        private val EUC_KR_FORM_DATA_TYPE: ResolvableType = ResolvableType.forClass(EucKrFormDataType::class.java)
    }
}
