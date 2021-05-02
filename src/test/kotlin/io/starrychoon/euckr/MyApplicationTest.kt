package io.starrychoon.euckr

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.*
import org.springframework.beans.factory.annotation.*
import org.springframework.boot.test.autoconfigure.web.reactive.*
import org.springframework.boot.test.context.*
import org.springframework.boot.test.system.*
import org.springframework.http.*
import org.springframework.test.web.reactive.server.*
import java.net.*
import java.nio.charset.*

/**
 * @author Sungchoon Park
 */
@SpringBootTest
@AutoConfigureWebTestClient
@ExtendWith(OutputCaptureExtension::class)
class MyApplicationTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `EUC-KR로 인코딩된 폼 요청도 서버에서는 유니코드로 출력되어야한다`(output: CapturedOutput) {
        val expected1 = "Hello World"
        val expected2 = "한글텍스트"
        val formData = FormData(2)
        formData.add("text_en", expected1)
        formData.add("text_kr", expected2)
        val bytes = serializeForm(formData, ExtendedCharsets.EUC_KR).toByteArray(ExtendedCharsets.EUC_KR)

        webTestClient.post()
            .uri("http://localhost:8080/euc-kr/form")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(bytes)
            .exchange()
            .expectStatus().isOk
            .expectBody().isEmpty

        val stdout = output.out
        assertTrue(expected1 in stdout)
        assertTrue(expected2 in stdout)
    }

    private fun serializeForm(formData: FormData, charset: Charset): String = buildString {
        formData.forEach { (name, values) ->
            values.forEach {
                if (isNotEmpty()) append('&')
                append(URLEncoder.encode(name, charset))
                if (it != null) {
                    append('=')
                    append(URLEncoder.encode(it, charset))
                }
            }
        }
    }
}
