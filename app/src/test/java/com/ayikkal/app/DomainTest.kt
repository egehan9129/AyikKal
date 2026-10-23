package com.ayikkal.app

import com.ayikkal.app.data.Repository
import com.ayikkal.app.domain.BacCalculator
import com.ayikkal.app.domain.BacLevel
import com.ayikkal.app.domain.Dose
import com.ayikkal.app.domain.PhoneNumbers
import com.ayikkal.app.domain.TestGenerator
import com.ayikkal.app.domain.TestMode
import com.ayikkal.app.domain.TestStep
import com.ayikkal.app.domain.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import kotlin.random.Random

class BacCalculatorTest {
    private val man = UserProfile(male = true, weightKg = 78, heightCm = 182, age = 25, fullStomach = false)
    private val hour = 3_600_000L

    @Test
    fun noDrinksMeansZero() {
        assertEquals(0.0, BacCalculator.current(emptyList(), man, 0), 0.0)
        assertNull(BacCalculator.soberAt(emptyList(), man, 0))
    }

    @Test
    fun twoBeersOnEmptyStomachPeakInRealisticRange() {
        // 2 x 50cl %5 bira, aç karnına, 78 kg erkek: literatürde ~0.5‰ civarı zirve beklenir.
        val doses = listOf(Dose(0, 500, 5.0), Dose(0, 500, 5.0))
        val peak = BacCalculator.curve(doses, man, 3 * hour).maxOf { it.bac }
        assertTrue("peak=$peak", peak in 0.45..0.75)
    }

    @Test
    fun fullStomachLowersPeak() {
        val doses = listOf(Dose(0, 80, 45.0), Dose(0, 80, 45.0))
        val empty = BacCalculator.curve(doses, man, 4 * hour).maxOf { it.bac }
        val full = BacCalculator.curve(doses, man.copy(fullStomach = true), 4 * hour).maxOf { it.bac }
        assertTrue(full < empty)
    }

    @Test
    fun womanSameWeightGetsHigherBac() {
        val doses = listOf(Dose(0, 150, 13.5), Dose(0, 150, 13.5))
        val woman = man.copy(male = false, heightCm = 168)
        val m = BacCalculator.curve(doses, man, 3 * hour).maxOf { it.bac }
        val w = BacCalculator.curve(doses, woman, 3 * hour).maxOf { it.bac }
        assertTrue(w > m)
    }

    @Test
    fun eliminatesOverTimeAndPredictsSoberTime() {
        val doses = listOf(Dose(0, 500, 5.0))
        val soberAt = BacCalculator.soberAt(doses, man, 0)!!
        assertTrue(soberAt in hour..5 * hour)
        assertEquals(0.0, BacCalculator.current(doses, man, soberAt + hour), 0.0)
    }

    @Test
    fun levels() {
        assertEquals(BacLevel.SOBER, BacLevel.of(0.1))
        assertEquals(BacLevel.TIPSY, BacLevel.of(0.78))
        assertEquals(BacLevel.DANGER, BacLevel.of(3.0))
    }
}

class PhoneNumbersTest {
    @Test
    fun formatsMatch() {
        assertTrue(PhoneNumbers.matches("+90 532 111 22 33", "0532 111 2233"))
        assertTrue(PhoneNumbers.matches("5321112233", "+905321112233"))
        assertTrue(!PhoneNumbers.matches("05321112233", "05321112234"))
        assertTrue(!PhoneNumbers.matches("112", "112"))
    }

    @Test
    fun masks() {
        assertEquals("0532 *** ** 33", PhoneNumbers.mask("+90 532 111 22 33"))
    }
}

class TestGeneratorTest {
    @Test
    fun choiceStepsAlwaysContainCorrectAnswer() {
        repeat(200) { seed ->
            val r = Random(seed)
            listOf(TestMode.PERIODIC, TestMode.SOBER_UP).forEach { mode ->
                TestGenerator.build(mode, r).filterIsInstance<TestStep.Choice>().forEach { s ->
                    assertEquals(4, s.options.size)
                    assertEquals(4, s.options.toSet().size)
                    assertTrue(s.correctIndex in 0..3)
                }
            }
        }
    }

    @Test
    fun mathAnswerIsActuallyCorrect() {
        repeat(100) { seed ->
            val s = TestGenerator.mathStep(Random(seed), hard = false, index = 1)
            val expr = s.prompt.removeSuffix(" = ?")
            val parts = expr.split(" ")
            val a = parts[0].toInt()
            val b = parts[2].toInt()
            val expected = if (parts[1] == "+") a + b else a - b
            assertEquals(expected.toString(), s.options[s.correctIndex])
        }
    }

    @Test
    fun periodicHasSoberUpFiveSteps() {
        assertEquals(5, TestGenerator.build(TestMode.SOBER_UP).size)
        assertEquals(5, TestGenerator.build(TestMode.PERIODIC).size)
    }

    @Test
    fun typingIsCaseInsensitiveButLetterStrict() {
        val target = "Şu an gayet ayığım"
        assertEquals(TestGenerator.normalizeTyping(target), TestGenerator.normalizeTyping("şu an gayet ayığım "))
        assertEquals(9, TestGenerator.firstMismatch("şu an gayet", "Şu an gayit"))
        assertEquals(14, TestGenerator.firstMismatch("şu an gayet ayig", target))
        assertEquals(15, TestGenerator.firstMismatch("şu an gayet ayıg", target))
        assertNull(TestGenerator.firstMismatch("ŞU AN", target))
    }
}

class UnlockTimeTest {
    private fun at(h: Int, m: Int, dayOffset: Int = 0): Long = Calendar.getInstance().apply {
        set(2026, Calendar.OCTOBER, 24 + dayOffset, h, m, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    @Test
    fun lockAtNightUnlocksNextMorning() {
        assertEquals(at(8, 0, 1), Repository.computeUnlockAt(at(23, 30), 8, 0))
        assertEquals(at(8, 0), Repository.computeUnlockAt(at(2, 45), 8, 0))
    }

    @Test
    fun alwaysAtLeastTwoHours() {
        val now = at(7, 30)
        assertEquals(now + 2 * 3_600_000L, Repository.computeUnlockAt(now, 8, 0))
    }
}
