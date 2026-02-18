package com.tungnk123.soundalarm.presentation.home

import app.cash.turbine.test
import com.tungnk123.soundalarm.domain.model.Alarm
import com.tungnk123.soundalarm.domain.usecase.DeleteAlarmUseCase
import com.tungnk123.soundalarm.domain.usecase.GetAlarmsUseCase
import com.tungnk123.soundalarm.domain.usecase.ToggleAlarmUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getAlarmsUseCase: GetAlarmsUseCase
    private lateinit var toggleAlarmUseCase: ToggleAlarmUseCase
    private lateinit var deleteAlarmUseCase: DeleteAlarmUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getAlarmsUseCase = mockk()
        toggleAlarmUseCase = mockk()
        deleteAlarmUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState emits alarms from use case`() = runTest {
        val alarms = listOf(
            Alarm(id = 1, hour = 8, minute = 0, label = "Wake up"),
            Alarm(id = 2, hour = 9, minute = 30, label = "Meeting"),
        )
        every { getAlarmsUseCase() } returns flowOf(alarms)

        val viewModel = HomeViewModel(getAlarmsUseCase, toggleAlarmUseCase, deleteAlarmUseCase)

        viewModel.uiState.test {
            val initial = awaitItem()
            assertEquals(true, initial.isLoading)

            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(2, loaded.alarms.size)
            assertEquals("Wake up", loaded.alarms[0].label)
        }
    }

    @Test
    fun `toggleAlarm calls use case`() = runTest {
        every { getAlarmsUseCase() } returns flowOf(emptyList())
        coEvery { toggleAlarmUseCase(any(), any()) } returns Unit

        val viewModel = HomeViewModel(getAlarmsUseCase, toggleAlarmUseCase, deleteAlarmUseCase)
        viewModel.toggleAlarm(1L, false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { toggleAlarmUseCase(1L, false) }
    }
}
