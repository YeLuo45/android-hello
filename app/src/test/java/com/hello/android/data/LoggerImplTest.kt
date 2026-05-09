package com.hello.android.data

import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import timber.log.Timber

class LoggerImplTest {

    private lateinit var loggerImpl: LoggerImpl

    @Before
    fun setup() {
        mockkObject(Timber)
        loggerImpl = LoggerImpl()
    }

    @After
    fun tearDown() {
        unmockkObject(Timber)
    }

    @Test
    fun `log should call Timber with correct tag and message`() {
        loggerImpl.log("Test message", "CustomTag")

        verify { Timber.tag("CustomTag").d("Test message") }
    }

    @Test
    fun `log should use default tag when not specified`() {
        loggerImpl.log("Test message")

        verify { Timber.tag("HelloApp").d("Test message") }
    }

    @Test
    fun `log can handle empty message`() {
        loggerImpl.log("", "TestTag")

        verify { Timber.tag("TestTag").d("") }
    }
}
