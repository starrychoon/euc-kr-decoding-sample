package io.starrychoon.euckr

import com.fasterxml.jackson.module.kotlin.*
import org.springframework.boot.*
import org.springframework.boot.autoconfigure.*
import org.springframework.boot.web.codec.*
import org.springframework.context.annotation.*
import org.springframework.web.reactive.function.server.*

@SpringBootApplication
class MyApplication {

    @Bean
    fun kotlinModule() = KotlinModule.Builder()
        .nullIsSameAsDefault(true)
        .nullToEmptyCollection(true)
        .nullToEmptyMap(true)
        .build()

    @Bean
    fun eucKrCodecCustomizer() = CodecCustomizer {
        val custom = it.customCodecs()
        custom.registerWithDefaultConfig(EucKrFormHttpMessageReader())
    }

    @Bean
    fun myRouter(myHandler: MyHandler) = coRouter {
        "/euc-kr".nest {
            POST("/text", myHandler::readEucKrTextRequest)
            POST("/json", myHandler::readEucKrJsonRequest)
            POST("/form", myHandler::readEucKrFormRequest)
        }
    }
}

fun main(args: Array<String>) {
    runApplication<MyApplication>(*args)
}
