# 🍻 Proje Planı: "AyıkKal" (DrunkGuard) - Android Uygulaması

> Bu, projenin ilk planıdır; uygulamanın kendisi için [README](../README.md)'ye bak.
>
> **Durum:** Faz 1–4 ve V2 bonus özelliklerinin tamamı kodlandı (`app/`). Plan uygulanırken mantıksız veya daha iyi yapılabilecek bulunan noktalar bu dokümanda düzeltildi; her düzeltme **🔧** ile, plana eklenen yeni özellikler **✨** ile işaretli. Ayrıntılı liste [Bölüm 7](#7-plandan-sapmalar-ve-gerekçeleri)'de.

## 1. Proje Özeti ve Amacı
**AyıkKal**, kullanıcıların alkol tüketimini takip eden, belirli aralıklarla yaptığı bilişsel/motor testlerle kullanıcının sarhoşluk seviyesini ölçen ve kritik eşik aşıldığında kullanıcının pişman olacağı eylemleri (eski sevgiliyi aramak, sosyal medyada hikaye atmak, patrona mesaj yazmak vb.) engelleyen Android tabanlı bir dijital güvenlik uygulamasıdır.

🔧 **Sorumluluk sınırı:** Promil değeri bir *tahmindir*, alkolmetre değildir. Uygulama araç kullanma kararı için asla kullanılmamalı; ilk açılışta bu uyarı ve 18+ onayı alınır. 112 her koşulda aranabilir.

---

## 2. Temel Özellikler (Core Features)

### A. Ön Hazırlık (Ayık Modu - Kurulum)
Kullanıcı içmeye başlamadan önce (veya uygulamayı ilk indirdiğinde) güvenlik duvarını kurar:
* **Kara Liste (Kişiler):** Sarhoşken aranması veya mesaj atılması kesinlikle yasak olan kişiler (Rehberden seçilir veya elle girilir). 🔧 Rehber seçici kullanıldığı için `READ_CONTACTS` izni gerekmez.
* **Kilitlenecek Uygulamalar:** WhatsApp, Instagram, X (Twitter), Tinder, Telefon/Rehber uygulaması vb. ✨ "Önerilenler" tuşu telefonda yüklü olan riskli uygulamaları (WhatsApp, Instagram, X, Tinder, Bumble, Telegram, Snapchat, Gmail, Slack, Teams…) tek dokunuşla ekler.
* **Kişisel Bilgiler (Promil Hesabı İçin):** Boy, kilo, cinsiyet, 🔧 **yaş** ve açlık durumu. (Boy ancak yaşla birlikte işe yarar: Watson toplam vücut suyu formülü ikisini de kullanır.)
* **Güvenli Kişi (Acil Durum):** Çok sarhoş olunduğunda tek tuşla konum atılacak veya aranacak yakın arkadaş.
* ✨ **Ev adresi:** Taksi ve eve rota butonları için.
* ✨ **Koruma kuralları:** Kilidin açılacağı sabah saati, test aralığı (30/45/60/90 dk), otomatik kilit eşiği (Kapalı / 1.0 / 1.5 / 2.0‰), "Sarhoşken yalnızca güvenli kişi aranabilsin" ve su hatırlatıcısı.

### B. İçme Modu (Aktif Takip)
* **"İçmeye Başladım" Butonu:** Ana ekranda büyük, nabız gibi atan bir başlatma butonu. (İlk kadeh eklendiğinde seans zaten kendiliğinden başlar.)
* **İçki Seçimi ve Sayacı:**
  * **Tür:** Bira, Şarap, Rakı, Votka, Viski, Tekila, Cin, Kokteyl.
  * **Marka ve Hacim/Alkol Oranı:** ~40 hazır içki (Efes, Tuborg, Yeni Rakı tek/duble, Jameson, Mojito…). ✨ Her içkide "kaç standart içki" (10 g saf alkol) olduğu gösterilir. ✨ Listede olmayan içki için *Özel* içki (ad + ml + %) eklenip kataloğa kaydedilir.
  * **Canlı Sayaç:** İçki başına `−  2  +` sayaç ve "Yeni Kadeh / Shot Ekle".
* **Tahmini Promil Göstergesi:** Canlı promil halkası + seviye etiketi (Ayık → Tehlikeli), ✨ tahmini zirve, ✨ tahmini ayılma saati ve 0.50‰ yasal sürüş sınırı uyarısı.
* ✨ **Zehirlenme uyarısı:** Promil 2.5‰'ü geçerse ekranda belirtiler ve tek tuşla 112 kartı çıkar.

### C. Periyodik Sarhoşluk Testi (Tespit Mekanizması)
🔧 **Tetiklenme kuralı netleştirildi** (orijinal metinde "her yeni bardakta / her 30-45 dk'da" çelişiyordu):
* İlk test seans başından **test aralığı** kadar sonra (varsayılan 45 dk).
* Her **3. kadehte** ek test (Kullanıcı Akışı 5. madde ile uyumlu).
* Promil yükseldikçe aralık kısalır: ≥0.5‰ → en fazla 30 dk, ≥1.0‰ → en fazla 20 dk.

Bildirim tam ekran alarm gibi gelir; **2 dakika** içinde teste başlanmalıdır. Testin adımları:
1. **Refleks / Denge Testi (Jiroskop):** 🔧 İvmeölçerle telefonun sağ-sol eğimi ve el titremesi ölçülür; nokta yeşil alanda 3 sn sabit tutulmalı. (Telefonu "düz tutmak" yerine "dik tutmak" seçildi: masada yatan telefon testi hileyle geçerdi.)
2. **Bilişsel Test (Matematik):** Elde gerektiren 2 hızlı işlem (her biri 12 sn, 4 şık). 🔧 *"Şu an saat kaç?"* sorusu çıkarıldı; saat durum çubuğunda göründüğü için hiçbir şey ölçmüyordu.
3. **Motor Beceri / Yazım Testi:** Ekrandaki cümleyi harfi harfine yazma. 🔧 Sabit 15 sn yerine süre cümle uzunluğuna göre ölçeklenir (≥15 sn, ~0.65 sn/harf); 50 harflik bir cümleyi ayıkken bile telefonda 15 sn'de hatasız yazmak çoğu kişi için mümkün değil. Otomatik düzeltme kapalı, yapıştırma ve klavye önerisiyle kelime ekleme engelli, büyük/küçük harf farkı önemsiz ama `ğ/g`, `ı/i` ayrımı kesin. 1 düzeltme hakkı vardır.
4. **Refleks (Kaçan Nokta):** Kaçan noktaya 6 sn içinde 3 kez dokunma.
   ✨ **Kişisel referans:** Ayıkken yapılan cezasız *Deneme Testi* ortalama refleks süreni kaydeder; gece refleksin normalinin ~1.8 katını aşarsa test başarısız sayılır. Herkesin refleksi farklı olduğu için sabit eşikten daha adildir.

### D. Koruma Kalkanı ("Sarhoş Modu" / Kilit Sistemi)
🔧 Tetikleme koşulları netleştirildi. Uygulama **Sarhoş Modu**'na şu durumlarda geçer:
* Testin herhangi bir adımında **başarısız olursa** veya süresi biterse,
* **Başladığı testi yarıda bırakırsa** (test 3 dk içinde bitmezse başarısız sayılır, yoksa uygulamayı kapatıp kaçmak mümkün olurdu),
* Test **bildirimini 2 kez yok sayarsa** (her biri 2 dk; ilki "son uyarı" bildirimiyle tekrar gelir),
* ✨ "Pes Ettim, Sarhoş Modunu Aç" tuşuna basarsa veya Kalkan ekranında **"Şimdi Kilitle"** ile gönüllü kilitlerse ("kendimi tanıyorum"),
* ✨ (İsteğe bağlı) tahmini promil **otomatik kilit eşiğini** geçerse.

Sarhoş Modu'nda:
* **Uygulama Kilidi:** Seçilen uygulamalar açıldığı an tam sayfa engel ekranı gelir ("Şu an sarhoşsun. … sabaha kadar kapalı").
* **Arama Engeli:** 🔧 Kara listedeki numaralara giden aramalar *Telefon uygulaması kilitlenmeden* anında kesilir (bkz. Bölüm 3). ✨ Kara listedeki kişi seni ararsa çağrı sessizce reddedilir. İstenirse güvenli kişi dışındaki tüm giden aramalar kesilir. 112 sistem gereği hiçbir zaman engellenmez.
* **Kilit Açma Şartı:** Kilit sabah belirlenen saatte (örn. 08:00) otomatik açılır ya da çok zorlu **Ayılma Testi** (5 soru) geçilirse açılır.
  * 🔧 Kilit her durumda **en az 2 saat** sürer (07:30'da kilitlenen biri 08:00'de kurtulmasın).
  * 🔧 Ayılma testi kilitten **30 dk sonra** açılır ve başarısızlıktan sonra **30 dk** bekleme vardır (aksi halde sınırsız deneme ile şansla geçilebilirdi). Her başarısızlık kilidi **1 saat uzatır**.
  * ✨ Ayılma testi soruları: zor matematik (×, büyük çıkarma), sayı örüntüsü, **Stroop testi** (kelimeyi değil rengini seç — dikkat ve inhibisyon ölçer), 6 haneli **kısa süreli hafıza**, hatasız zor yazım (`ç, ğ, ı, ö, ş, ü` dolu cümle).
* ✨ **Kaçış koruması:** Sarhoş Modu'nda Ayarlar'da veya kaldırma ekranında "AyıkKal" görünürse (erişilebilirliği kapatma, uygulamayı kaldırma, verileri silme, durmaya zorlama) ekran kapatılır: *"Güzel deneme 😏"*. Ayarlar ekranı da kilitlidir: kısıtlama eklenebilir ama kaldırılamaz/gevşetilemez. Seans bitirilemez.
* ✨ **Güvenli çıkışlar** (engel ekranında da her zaman açık): güvenli kişiyi ara, canlı konumu SMS ile gönder, BiTaksi (ev adresi panoya kopyalanır), Uber (ev adresine otomatik rota), toplu taşıma rotası, 112. Bu eylemler kilitli bir uygulamayı (ör. Telefon) gerekiyorsa birkaç dakikalığına açar; aramaya devam ederken arama ekranı hiçbir zaman kapatılmaz.

---

## 3. Teknik Mimari ve Android Gereksinimleri

### Kullanılan Teknolojiler
* **Dil:** Kotlin 2.0
* **UI Framework:** Jetpack Compose (Material 3) — "Nocturnal Clarity" tasarım sistemi (`designs/`), Plus Jakarta Sans fontu, yalnızca karanlık tema.
* **Yerel Veritabanı:** Room (içki kataloğu, bardak geçmişi, gece olay kaydı, kara liste, kilitli uygulamalar). Anlık koruma durumu SharedPreferences'ta (erişilebilirlik servisinin senkron okuması için).
* **Arka Plan İşlemleri:** 🔧 Yalnızca **`AlarmManager`** (`setExactAndAllowWhileIdle`). README'de alternatif olarak geçen `WorkManager` 15 dk altı ve dakika hassasiyetli tetiklemeyi garanti etmez; "2 dakikan var" gibi kesin süreler için uygun değil. Telefon yeniden başlarsa `BootReceiver` alarmları ve gecikmiş durumları (örn. kilit saati geçtiyse açma) toparlar.
* **Minimum Android:** 10 (API 29) — arama yönlendirme rolü bu sürümle geldi. Hedef: Android 15 (API 35).

### Kritik Android İzinleri ve Servisleri
1. **Uygulamaları Kilitlemek İçin:**
   * `AccessibilityService`: Ön plana gelen uygulamayı algılar.
   * 🔧 ~~`SYSTEM_ALERT_WINDOW`~~ **gerekmez.** Erişilebilirlik servisleri arka plandan aktivite başlatma kısıtlamasından muaf olduğu için engel ekranı normal bir tam ekran Activity olarak açılır. Bir izin daha az = daha az korkutucu kurulum ve Play Store incelemesinde daha az soru.
   * 🔧 ~~`PACKAGE_USAGE_STATS`~~ kullanılmadı: periyodik sorgulama gerektirir (pil) ve gecikmelidir; erişilebilirlik olayları anlıktır.
2. **Aramaları Engellemek İçin:** 🔧 README'deki iki yöntemden daha iyi bir üçüncü yol seçildi:
   * **`CallRedirectionService` + `RoleManager.ROLE_CALL_REDIRECTION` (Android 10+):** Kullanıcı tek bir sistem diyaloğuyla onaylar; her giden aramada sistem önce bize sorar, kara listedeyse `cancelCall()` ile arama başlamadan kesilir. Kullanıcının kendi Telefon uygulaması aynen çalışır (Yöntem 1'deki gibi Telefon'u komple kilitlemeye gerek yok), varsayılan arama uygulaması olmaya da gerek yok (Yöntem 2'deki gibi koca bir dialer yazmak gerekmez). Acil numaralar bu servise hiç gelmez.
   * ✨ **`CallScreeningService` + `ROLE_CALL_SCREENING`:** Gelen aramalarda kara listedeki kişiyi sessizce reddeder.
   * Yöntem 1 hâlâ mümkün: Telefon uygulaması kilit listesine eklenirse yalnızca uygulama içi güvenli çıkışlardan arama yapılabilir.
3. **Sensörler:** `SensorManager` ivmeölçer (eğim + titreme). Sensörü olmayan cihazda adım atlanır.
4. **Diğer:** `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `USE_FULL_SCREEN_INTENT` (kilit ekranında test alarmı), `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, `CALL_PHONE` (güvenli kişiyi numara çevirmeden arama), `SEND_SMS` + `ACCESS_FINE_LOCATION` (tek tuşla konum). Uygulama içinde "Sistem İzinleri" ekranı hepsinin durumunu gösterir ve tek tek ister.
5. ✨ **Gizlilik:** Manifest'te **`INTERNET` izni yok.** Hiçbir veri cihazdan çıkamaz; tüm kayıtlar yalnızca telefonda.

> ⚠️ **Android 13+ "Kısıtlanmış ayar":** APK Play Store dışından yüklenirse Erişilebilirlik anahtarı gri görünür. Çözüm: *Ayarlar → Uygulamalar → AyıkKal → ⋮ → Kısıtlanmış ayarlara izin ver.* (İzin ekranında da yazıyor.)

---

## 4. Kullanıcı Akışı (User Flow)

1. **Hazırlık:** Kullanıcı evden çıkmadan uygulamayı açar → Kara listeye eski sevgilisini ekler → WhatsApp ve Instagram'ı kilit listesine alır. ✨ Ayıkken bir kez *Deneme Testi* yaparak refleks referansını kaydeder.
2. **Başlangıç:** Mekana oturur → **"İçmeye Başladım"** butonuna basar.
3. **Kayıt:** "Bira → Efes Pilsen → 50cl" seçer ve 1. bardağı ekler.
4. **Süreç:** İlerleyen saatlerde "+" butonuna basmaya devam eder (uygulama arkada tahmini promili hesaplar, her 2 kadehte su hatırlatır).
5. **Tetiklenme:** 3. bardaktan sonra (veya test aralığı dolunca) telefon titrer: *"Hey! Kontrol zamanı. Testi çözmek için 2 dakikan var."*
6. **Karar Anı:**
   * *Senaryo A (Başarılı):* Testi geçer → "İyi eğlenceler! Bir sonraki kontrolde görüşürüz." Sonraki test promile göre planlanır.
   * *Senaryo B (Başarısız):* Yazım hatası yapar veya soruyu bilemez → **ALARM! Sarhoş Modu Aktif!**
7. **Koruma:** Kullanıcı gece 02:00'de WhatsApp'a girmeye çalışır → Ekranda *"Git su iç ve uyu bro, yarın bana teşekkür edeceksin"* yazısı çıkar ve uygulama açılmaz. Eski sevgiliyi Telefon uygulamasından ararsa arama başlamadan kesilir.
8. ✨ **Sabah:** 08:00'de kilit açılır, seans kapanır, "Günaydın! Kilitler açıldı" bildirimi gelir ve **Hangover Raporu** hazırdır.

---

## 5. Geliştirme Aşamaları (Roadmap)

* **Faz 1: Temel UI ve Sayaç Mantığı** ✅
  * Ana ekran, içki seçimi, bardak ekleme sayacı ve Room veritabanı.
  * Promil hesabı: 🔧 basit Widmark yerine **Widmark + Watson** (boy/kilo/yaş/cinsiyetten kişisel dağılım katsayısı), dakikalık emilim modeli (aç karnına ~30 dk, tok ~60 dk) ve saatte 0.15‰ atılım.
* **Faz 2: Bildirim ve Test Motoru** ✅
  * `AlarmManager` ile periyodik ve son-süre alarmları, tam ekran test bildirimi.
  * Denge, matematik, yazım, refleks; ayılma testi için örüntü, Stroop, hafıza.
* **Faz 3: Kilit ve Engelleme Mekanizması** ✅
  * `AccessibilityService` ile uygulama kilidi ve kaçış koruması.
  * `CallRedirectionService` / `CallScreeningService` ile arama engeli.
* **Faz 4: Cilalama ve Test** ✅ (kısmen)
  * Karanlık mod tasarımı (uygulama yalnızca karanlık temada çalışır).
  * Birim testleri (promil, numara eşleştirme, soru üretimi, kilit saati).
  * ⏳ Kalan: gerçek cihazlarda (Samsung/Xiaomi gibi agresif pil yöneticileri) pil optimizasyonu testleri, Play Store hazırlığı (Erişilebilirlik API kullanım beyanı, SMS izni beyanı ya da SMS'in yalnızca hazır mesaj açmaya indirilmesi).

---

## 6. Gelecek Özellikler (V2.0 Bonus Fikirler) — hepsi V1'e alındı ✅
* **💧 Su İçme Hatırlatıcısı:** Her 2 kadehte bir *"Baş ağrısı çekmemek için şimdi 1 bardak su iç"* bildirimi; Takip ekranında "su borcu" ve Su Ekle.
* **📊 "Dün Gece Ne Oldu?" Özeti (Hangover Raporu):** Sabah kilit açılınca: toplam alkol, zirve promil ve saati, test karnesi ve refleks, su skoru, **gece promil eğrisi** (kadehler ve engellenen girişimler işaretli), **engellenen krizler** (hangi uygulama kaç kez, hangi kişi kaç kez arandı/aradı, kalkanı kapatma girişimleri), baş ağrısı risk tahmini ve toparlanma kontrol listesi. ✨ Anonim karne paylaşma, ✨ güvenli kişiye "İyiyim" mesajı, ✨ geçmiş gecelerin arşivi.
* **🚕 Eve Dönüş Kısayolu:** Sarhoş modu ekranında BiTaksi / Uber / toplu taşıma rotası ve güvenli kişiye canlı konum.

---

## 7. Plandan Sapmalar ve Gerekçeleri
| Orijinal plan | Uygulanan | Neden |
|---|---|---|
| `SYSTEM_ALERT_WINDOW` ile overlay | Erişilebilirlik servisinden tam ekran Activity | İzin gereksiz; servis arka plan başlatma kısıtından muaf |
| Telefon'u kilitle **veya** varsayılan arama uygulaması ol | `CallRedirectionService` rolü | Telefon uygulaması çalışmaya devam eder, yalnızca kara liste kesilir |
| `WorkManager` veya `AlarmManager` | Yalnızca `AlarmManager` | WorkManager dakika hassasiyeti garanti etmez |
| Boy, kilo, cinsiyet, açlık | + yaş | Watson formülü boyu yaşla birlikte kullanır |
| "Şu an saat kaç?" sorusu | Kaldırıldı; Stroop/örüntü/hafıza eklendi | Saat ekranda görünüyor, hiçbir şey ölçmüyor |
| Yazım: sabit 15 sn | Cümle uzunluğuna göre (≥15 sn) | Uzun cümle 15 sn'de ayıkken bile yazılamaz |
| Telefonu "düz" tutma | "Dik" tutma + titreme ölçümü | Masaya bırakılan telefon testi geçerdi |
| "Her bardakta veya 30-45 dk'da" | Aralık + her 3. kadeh + promile göre sıklaşma | Çelişkili kural netleşti |
| "Süreyi kaçırırsa / 2 kez yok sayarsa" | 2 dk yanıt süresi, 2 kaçırma, yarıda bırakılan test = başarısız | Kaçış yolları kapatıldı |
| Ayılma testi sınırsız deneme | 30 dk bekleme + başarısızlıkta +1 saat | Şansla geçmeyi önler |
| Kilit sabah saatinde açılır | + en az 2 saat kilit | 07:30 kilidi 08:00'de açılmasın |
| Tasarımdaki "Göz Takibi Aktif / Nabız 104 BPM" kartları | Çıkarıldı | Telefonda böyle bir sensör yok; sahte veri güveni zedeler |
| — | Kaçış koruması, ayarların kilitlenmesi | Sarhoş kullanıcı uygulamayı kapatıp kalkanı delemesin |
| — | Gönüllü kilit, otomatik promil eşiği | "Kendimi tanıyorum" kullanıcıları için |
| — | INTERNET izni yok | Hassas veriler (kişiler, içki geçmişi) cihazda kalır |

---

## 8. Derleme ve Çalıştırma
Gereksinimler: JDK 17, Android SDK (platform 35). Proje Gradle Wrapper ile gelir.

```bash
./gradlew :app:assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. Birim testleri:

```bash
./gradlew :app:testDebugUnitTest
```

İlk açılışta: tanıtım → uyarılar (18+) → Ayarlar (kara liste, uygulamalar, güvenli kişi, profil) → Sistem İzinleri (en az Erişilebilirlik, Bildirim, Tam zamanlı alarm).

### Proje Yapısı
```
app/src/main/java/com/ayikkal/app/
├── AyikKalApp.kt, MainActivity.kt
├── data/            Room (Entities, Daos, DrinkSeed), Prefs, Repository (tüm iş kuralları)
├── domain/          Bac.kt (Widmark+Watson), SobrietyTests.kt (soru üretimi), PhoneNumbers.kt
├── system/          AlarmScheduler/Receiver, Notifier, GuardAccessibilityService,
│                    CallGuards (yönlendirme + filtreleme), SafeActions, Permissions, AppCatalog
└── ui/              theme, components, screens (Takip, Test, Kalkan, Rapor, Ayarlar,
                     Onboarding, İzinler), block/BlockActivity
designs/             Stitch tasarım ekranları ve DESIGN.md (Nocturnal Clarity)
```
