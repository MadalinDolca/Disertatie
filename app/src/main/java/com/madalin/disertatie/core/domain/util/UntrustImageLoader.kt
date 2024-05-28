package com.madalin.disertatie.core.domain.util

import android.annotation.SuppressLint
import android.content.Context
import coil.ImageLoader
import okhttp3.OkHttpClient
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Initializes and returns an [ImageLoader] that bypasses SSL/TLS certificate validation.
 *
 * This function creates and configures an [ImageLoader] that uses an [OkHttpClient] with an
 * all-trusting [X509TrustManager], which means that it does not validate certificate chains.
 * This is generally unsafe and should only be used in a controlled environment where you
 * explicitly trust the source of the images being loaded.
 */
@SuppressLint("CustomX509TrustManager")
fun initUntrustImageLoader(context: Context): ImageLoader {
    // Create a trust manager that does not validate certificate chains
    val trustAllCerts = arrayOf<TrustManager>(
        object : X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
    )

    // Install the all-trusting trust manager
    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null, trustAllCerts, java.security.SecureRandom())

    // Create an ssl socket factory with our all-trusting manager
    val sslSocketFactory = sslContext.socketFactory

    val client = OkHttpClient.Builder()
        .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
        .hostnameVerifier { _, _ -> true }.build()

    return ImageLoader.Builder(context)
        .okHttpClient(client)
        .build()
}