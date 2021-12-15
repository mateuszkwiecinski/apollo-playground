package com.example

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.QueueDispatcher
import okhttp3.mockwebserver.RecordedRequest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.concurrent.TimeUnit

class MockWebServerTestRule : BeforeEachCallback, AfterEachCallback {

  lateinit var mockWebServer: MockWebServer
    private set

  fun enqueue(@Language("JSON") response: String) =
    mockWebServer.enqueue(MockResponse().setBody(response).setBodyDelay(10, TimeUnit.MILLISECONDS))

  override fun beforeEach(context: ExtensionContext?) {
    println("Starting web server")
    mockWebServer = MockWebServer()
    mockWebServer.dispatcher = object : QueueDispatcher() {
      init {
        setFailFast(true)
      }

      override fun dispatch(request: RecordedRequest): MockResponse {
        println("Dispatching ${request.getHeader("X-APOLLO-OPERATION-NAME")}")
        return super.dispatch(request)
      }
    }
    mockWebServer.start()
  }

  override fun afterEach(context: ExtensionContext?) {
    println("Closing web server")
    mockWebServer.close()
  }
}
