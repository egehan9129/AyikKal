package com.ayikkal.app.domain

import java.util.Locale
import kotlin.random.Random

enum class TestMode {
    /** Bildirimle gelen zorunlu kontrol. Başarısızlık = Sarhoş Modu. */
    PERIODIC,
    /** Kullanıcının kendi isteğiyle seans sırasında yaptığı test. Sonuç aynı şekilde bağlayıcı. */
    VOLUNTARY,
    /** Ayıkken yapılan deneme; kişisel refleks referansını kaydeder, ceza yok. */
    PRACTICE,
    /** Sarhoş Modu'ndan erken çıkmak için 5 zor soru. */
    SOBER_UP,
}

enum class ColorName(val label: String, val argb: Long) {
    RED("KIRMIZI", 0xFFEF4444),
    BLUE("MAVİ", 0xFF3B82F6),
    GREEN("YEŞİL", 0xFF10B981),
    YELLOW("SARI", 0xFFFACC15),
    PURPLE("MOR", 0xFFA855F7),
}

sealed interface TestStep {
    val title: String
    val subtitle: String
    val timeLimitSec: Int

    data class Choice(
        override val title: String,
        override val subtitle: String,
        override val timeLimitSec: Int,
        val caption: String,
        val prompt: String,
        /** Stroop sorusunda yazının mürekkep rengi. */
        val promptColor: Long? = null,
        val options: List<String>,
        val correctIndex: Int,
    ) : TestStep

    data class Typing(
        override val title: String,
        override val subtitle: String,
        override val timeLimitSec: Int,
        val sentence: String,
        val allowedErrors: Int,
    ) : TestStep

    data class Reflex(
        override val title: String,
        override val subtitle: String,
        override val timeLimitSec: Int,
        val hitsNeeded: Int,
        val moveIntervalMs: Long,
        val maxMisses: Int,
    ) : TestStep

    data class Balance(
        override val title: String,
        override val subtitle: String,
        override val timeLimitSec: Int,
        val holdSec: Int,
        val maxTiltDeg: Float,
    ) : TestStep

    data class Memory(
        override val title: String,
        override val subtitle: String,
        override val timeLimitSec: Int,
        val digits: String,
        val showMs: Long,
    ) : TestStep
}

object TestGenerator {
    private val tr = Locale("tr", "TR")

    val sentences = listOf(
        "Şu an gayet ayığım ve mantıklı kararlar verebilirim",
        "Eski sevgilimi aramak kesinlikle iyi bir fikir değil",
        "Yarın sabah kendime teşekkür edeceğim",
        "Bir bardak su içip biraz dinlenmek en doğrusu",
        "Patronuma gece yarısı mesaj atmayacağım",
        "Hikâye atmadan önce iki kere düşüneceğim",
    )

    private val hardSentences = listOf(
        "Çığ gibi büyüyen şüphelerimi öğütüp sakin düşünüyorum",
        "Ağır ağır ilerleyen gölgeler, sessiz şehrin üstüne çöktü",
        "Üç küçük güvercin, ıslak çatının üzerinde ötüşüyor",
    )

    /** Türkçe büyük-küçük harf farkını yok sayar ama ğ/g, ı/i gibi harfleri ayırır. */
    fun normalizeTyping(s: String): String = s.trim().replace(Regex("\\s+"), " ").lowercase(tr)

    /** İlk hatalı karakterin indeksi; hata yoksa null. */
    fun firstMismatch(typed: String, target: String): Int? {
        val a = typed.lowercase(tr)
        val b = target.lowercase(tr)
        for (i in a.indices) {
            if (i >= b.length || a[i] != b[i]) return i
        }
        return null
    }

    /** Hızlı yazmayanları cezalandırmamak için süre cümle uzunluğuna göre ölçeklenir. */
    fun typingSeconds(sentence: String, hard: Boolean): Int {
        val perChar = if (hard) 0.55 else 0.65
        return maxOf(15, (sentence.length * perChar).toInt())
    }

    fun build(mode: TestMode, random: Random = Random.Default): List<TestStep> = when (mode) {
        TestMode.SOBER_UP -> soberUp(random)
        else -> periodic(random)
    }

    private fun periodic(random: Random): List<TestStep> {
        val sentence = sentences.random(random)
        return listOf(
            TestStep.Balance(
                title = "El Titreme & Denge",
                subtitle = "Telefonu dik tut, noktayı ortada sabitle",
                timeLimitSec = 12,
                holdSec = 3,
                maxTiltDeg = 9f,
            ),
            mathStep(random, hard = false, index = 1),
            mathStep(random, hard = false, index = 2),
            TestStep.Typing(
                title = "Motor Beceri & Yazım",
                subtitle = "Harfi harfine, hatasız",
                timeLimitSec = typingSeconds(sentence, hard = false),
                sentence = sentence,
                allowedErrors = 1,
            ),
            TestStep.Reflex(
                title = "Refleks",
                subtitle = "Kaçan noktaya 3 kez dokun",
                timeLimitSec = 6,
                hitsNeeded = 3,
                moveIntervalMs = 900,
                maxMisses = 3,
            ),
        )
    }

    private fun soberUp(random: Random): List<TestStep> {
        val sentence = hardSentences.random(random)
        return listOf(
            mathStep(random, hard = true, index = 1),
            sequenceStep(random),
            stroopStep(random),
            TestStep.Memory(
                title = "Kısa Süreli Hafıza",
                subtitle = "Sayıyı aklında tut",
                timeLimitSec = 15,
                digits = (1..6).map { random.nextInt(0, 10) }.joinToString(""),
                showMs = 3_000,
            ),
            TestStep.Typing(
                title = "Zor Yazım",
                subtitle = "Tek bir hata hakkın yok",
                timeLimitSec = typingSeconds(sentence, hard = true),
                sentence = sentence,
                allowedErrors = 0,
            ),
        )
    }

    fun mathStep(random: Random, hard: Boolean, index: Int): TestStep.Choice {
        val (prompt, answer) = if (hard) {
            when (random.nextInt(3)) {
                0 -> { val a = random.nextInt(12, 20); val b = random.nextInt(6, 10); "$a × $b" to a * b }
                1 -> { val a = random.nextInt(120, 200); val b = random.nextInt(47, 99); "$a − $b" to a - b }
                else -> { val a = random.nextInt(25, 60); val b = random.nextInt(25, 60); val c = random.nextInt(3, 9); "$a + $b − $c" to a + b - c }
            }
        } else {
            if (random.nextBoolean()) {
                // Elde gerektiren toplama: birler basamağı toplamı 10'u geçsin.
                val a = random.nextInt(2, 9) * 10 + random.nextInt(5, 10)
                val b = random.nextInt(1, 6) * 10 + random.nextInt(5, 10)
                "$a + $b" to a + b
            } else {
                val a = random.nextInt(6, 10) * 10 + random.nextInt(0, 5)
                val b = random.nextInt(1, 5) * 10 + random.nextInt(5, 10)
                "$a − $b" to a - b
            }
        }
        val (options, correct) = optionsAround(answer, random)
        return TestStep.Choice(
            title = if (hard) "Zor Matematik" else "Bilişsel Matematik",
            subtitle = if (hard) "Kafadan hesapla" else "Soru $index: Hızlı odak",
            timeLimitSec = if (hard) 20 else 12,
            caption = "İŞLEM SONUCUNU SEÇ",
            prompt = "$prompt = ?",
            options = options,
            correctIndex = correct,
        )
    }

    private fun sequenceStep(random: Random): TestStep.Choice {
        val start = random.nextInt(2, 7)
        val seq: List<Int>
        val next: Int
        when (random.nextInt(3)) {
            0 -> { val k = random.nextInt(3, 9); seq = List(4) { start + k * it }; next = start + k * 4 }
            1 -> { seq = List(4) { start * pow(2, it) }; next = start * 16 }
            else -> {
                val s = mutableListOf(start)
                repeat(3) { s += s.last() * 2 + 1 }
                seq = s; next = s.last() * 2 + 1
            }
        }
        val (options, correct) = optionsAround(next, random)
        return TestStep.Choice(
            title = "Örüntü",
            subtitle = "Diziyi tamamla",
            timeLimitSec = 20,
            caption = "SIRADAKİ SAYI HANGİSİ?",
            prompt = seq.joinToString(", ") + ", ?",
            options = options,
            correctIndex = correct,
        )
    }

    private fun stroopStep(random: Random): TestStep.Choice {
        val colors = ColorName.entries.shuffled(random)
        val word = colors[0]
        val ink = colors[1]
        val options = colors.take(4).shuffled(random)
        return TestStep.Choice(
            title = "Stroop Testi",
            subtitle = "Kelimeyi değil, RENGİ seç",
            timeLimitSec = 8,
            caption = "YAZININ RENGİ NE?",
            prompt = word.label,
            promptColor = ink.argb,
            options = options.map { it.label },
            correctIndex = options.indexOf(ink),
        )
    }

    private fun optionsAround(answer: Int, random: Random): Pair<List<String>, Int> {
        val candidates = linkedSetOf(answer)
        val deltas = listOf(-10, 10, -1, 1, -2, 2, 7, -9, 11).shuffled(random)
        for (d in deltas) {
            if (candidates.size == 4) break
            if (answer + d > 0) candidates += answer + d
        }
        val list = candidates.toList().shuffled(random)
        return list.map { it.toString() } to list.indexOf(answer)
    }

    private fun pow(base: Int, exp: Int): Int {
        var r = 1
        repeat(exp) { r *= base }
        return r
    }
}
