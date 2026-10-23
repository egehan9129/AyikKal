package com.ayikkal.app.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ayikkal.app.AyikKalApp
import com.ayikkal.app.domain.TestGenerator
import com.ayikkal.app.domain.TestMode
import com.ayikkal.app.domain.TestStep
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TestResult(val passed: Boolean, val reason: String, val avgReactionMs: Int?)

data class TestRunState(
    val steps: List<TestStep>,
    val index: Int = 0,
    val remainingMs: Long = 0,
    /** Seçmeli sorularda kısa geri bildirim: seçilen şık ve doğru mu. */
    val feedback: Pair<Int, Boolean>? = null,
    val result: TestResult? = null,
) {
    val step: TestStep? get() = steps.getOrNull(index)
}

class TestRunViewModel(app: Application, val mode: TestMode) : AndroidViewModel(app) {
    private val repo = (app as AyikKalApp).repository
    private val _state = MutableStateFlow(TestRunState(TestGenerator.build(mode)))
    val state: StateFlow<TestRunState> = _state.asStateFlow()
    private var timer: Job? = null
    private val reactions = mutableListOf<Long>()
    val baselineReactionMs: Int get() = repo.prefs.current.baselineReactionMs

    init {
        viewModelScope.launch { repo.onTestStarted(mode) }
        startTimer()
    }

    private fun startTimer() {
        timer?.cancel()
        val step = _state.value.step ?: return
        val end = System.currentTimeMillis() + step.timeLimitSec * 1000L
        timer = viewModelScope.launch {
            while (true) {
                val left = end - System.currentTimeMillis()
                _state.update { it.copy(remainingMs = left.coerceAtLeast(0)) }
                if (left <= 0) {
                    fail("Süre doldu: ${step.title}")
                    break
                }
                delay(100)
            }
        }
    }

    fun answer(option: Int) {
        val s = _state.value
        val step = s.step as? TestStep.Choice ?: return
        if (s.feedback != null || s.result != null) return
        val correct = option == step.correctIndex
        timer?.cancel()
        _state.update { it.copy(feedback = option to correct) }
        viewModelScope.launch {
            delay(450)
            _state.update { it.copy(feedback = null) }
            if (correct) pass() else fail("Yanlış cevap: ${step.title}")
        }
    }

    fun addReaction(ms: Long) {
        reactions += ms
    }

    val averageReaction: Int? get() = if (reactions.isEmpty()) null else reactions.average().toInt()

    /** Kişisel referans varsa, refleks iki katına yakın yavaşladıysa sarhoşluk işareti sayılır. */
    fun reflexTooSlow(): Boolean {
        val avg = averageReaction ?: return false
        val base = baselineReactionMs
        if (mode == TestMode.PRACTICE || base <= 0) return false
        return avg > maxOf(base * 1.8, 650.0)
    }

    fun pass() {
        if (_state.value.result != null) return
        val next = _state.value.index + 1
        if (next >= _state.value.steps.size) {
            finish(TestResult(true, "Tüm adımlar geçildi", averageReaction))
        } else {
            _state.update { it.copy(index = next) }
            startTimer()
        }
    }

    fun fail(reason: String) {
        if (_state.value.result != null) return
        finish(TestResult(false, reason, averageReaction))
    }

    private fun finish(result: TestResult) {
        timer?.cancel()
        _state.update { it.copy(result = result) }
        val summary = buildString {
            append(result.reason)
            result.avgReactionMs?.let { append(" · refleks ${it}ms") }
        }
        viewModelScope.launch { repo.onTestFinished(mode, result.passed, summary, result.avgReactionMs) }
    }

    class Factory(private val app: Application, private val mode: TestMode) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = TestRunViewModel(app, mode) as T
    }
}
