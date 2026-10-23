package com.ayikkal.app.domain

import kotlin.math.max
import kotlin.math.min

data class UserProfile(
    val male: Boolean = true,
    val weightKg: Int = 75,
    val heightCm: Int = 175,
    val age: Int = 25,
    val fullStomach: Boolean = true,
)

data class Dose(val at: Long, val volumeMl: Int, val abv: Double)

data class BacPoint(val at: Long, val bac: Double)

enum class BacLevel(val label: String, val min: Double) {
    SOBER("Ayık", 0.0),
    LIGHT("Hafif Çakırkeyif", 0.2),
    TIPSY("Çakırkeyif", 0.5),
    DRUNK("Sarhoş", 0.8),
    VERY_DRUNK("Çok Sarhoş", 1.5),
    DANGER("Tehlikeli", 2.5);

    companion object {
        fun of(bac: Double): BacLevel = entries.last { bac >= it.min }
    }
}

/**
 * Widmark formülünün Watson toplam vücut suyu düzeltmeli hali.
 * Boy ve yaş, kişiye özgü dağılım katsayısını (r) hesaplamak için kullanılır.
 * Sonuç promil (‰ ≈ g/kg) cinsindendir ve yalnızca tahmindir.
 */
object BacCalculator {
    private const val ETHANOL_DENSITY = 0.789
    /** Saatte vücuttan atılan ortalama alkol (‰). */
    const val ELIMINATION_PER_HOUR = 0.15
    /** Türkiye'de hususi araç sürücüleri için yasal sınır. */
    const val LEGAL_DRIVING_LIMIT = 0.50
    private const val STEP_MS = 60_000L
    private const val HOUR_MS = 3_600_000.0

    fun grams(volumeMl: Int, abv: Double): Double = volumeMl * (abv / 100.0) * ETHANOL_DENSITY

    /** 10 gram saf alkol = 1 standart içki. */
    fun standardDrinks(volumeMl: Int, abv: Double): Double = grams(volumeMl, abv) / 10.0

    fun distributionFactor(p: UserProfile): Double {
        val w = p.weightKg.toDouble()
        val h = p.heightCm.toDouble()
        val tbw = if (p.male) {
            2.447 - 0.09516 * p.age + 0.1074 * h + 0.3362 * w
        } else {
            -2.097 + 0.1069 * h + 0.2466 * w
        }
        return (tbw / (0.8 * w)).coerceIn(0.45, 0.85)
    }

    /** Aç karnına alkol ~30 dk'da, tok karnına ~60 dk'da kana tamamen karışır. */
    fun absorptionMs(p: UserProfile): Long = if (p.fullStomach) 60 * 60_000L else 30 * 60_000L

    /** Seans başından [until] anına kadar dakikalık promil eğrisi. */
    fun curve(doses: List<Dose>, p: UserProfile, until: Long): List<BacPoint> {
        if (doses.isEmpty()) return emptyList()
        val start = doses.minOf { it.at }
        val r = distributionFactor(p)
        val absorb = absorptionMs(p).toDouble()
        val elimPerStep = ELIMINATION_PER_HOUR * STEP_MS / HOUR_MS
        val points = ArrayList<BacPoint>()
        var bac = 0.0
        var prevAbsorbed = 0.0
        var t = start
        while (t <= until) {
            val absorbed = doses.sumOf { d ->
                if (t <= d.at) 0.0 else grams(d.volumeMl, d.abv) * min(1.0, (t - d.at) / absorb)
            }
            bac += (absorbed - prevAbsorbed) / (r * p.weightKg)
            prevAbsorbed = absorbed
            bac = max(0.0, bac - elimPerStep)
            points += BacPoint(t, bac)
            t += STEP_MS
        }
        return points
    }

    fun current(doses: List<Dose>, p: UserProfile, now: Long): Double =
        curve(doses, p, now).lastOrNull()?.bac ?: 0.0

    /** Şu ana kadar içilenlerin tamamı emildiğinde ulaşılacak en yüksek değer. */
    fun projectedPeak(doses: List<Dose>, p: UserProfile, now: Long): Double {
        val horizon = now + absorptionMs(p)
        return curve(doses, p, horizon).filter { it.at >= now }.maxOfOrNull { it.bac } ?: 0.0
    }

    /** Promilin sıfıra ineceği tahmini an. İçki yoksa null. */
    fun soberAt(doses: List<Dose>, p: UserProfile, now: Long): Long? {
        if (doses.isEmpty()) return null
        val lastAbsorbed = doses.maxOf { it.at } + absorptionMs(p)
        val pts = curve(doses, p, max(now, lastAbsorbed) + 36 * HOUR_MS.toLong())
        return pts.firstOrNull { it.at >= max(now, lastAbsorbed) && it.bac <= 0.0 }?.at
    }
}
