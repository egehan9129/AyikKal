package com.ayikkal.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val tr = Locale("tr", "TR")

fun fmtTime(ms: Long): String = SimpleDateFormat("HH:mm", tr).format(Date(ms))

fun fmtBac(bac: Double): String = String.format(Locale.US, "%.2f", bac)

/** 9900000 -> "2s 45dk" */
fun fmtDuration(ms: Long): String {
    val totalMin = (ms.coerceAtLeast(0) / 60_000).toInt()
    val h = totalMin / 60
    val m = totalMin % 60
    return if (h > 0) "${h}s ${m.toString().padStart(2, '0')}dk" else "${m}dk"
}

/** 95000 -> "01:35" */
fun fmtCountdown(ms: Long): String {
    val s = (ms.coerceAtLeast(0) / 1000).toInt()
    return "%02d:%02d".format(s / 60, s % 60)
}

/** Belirli aralıkla güncellenen "şimdi". Geri sayımlar için. */
@Composable
fun rememberNow(intervalMs: Long = 1_000): Long {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(intervalMs) {
        while (true) {
            now = System.currentTimeMillis()
            delay(intervalMs)
        }
    }
    return now
}

/** Kullanıcı ayarlardan döndüğünde izin durumlarını yeniden okumak için artan sayaç. */
@Composable
fun rememberResumeKey(): Int {
    var key by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { key++ }
    return key
}
