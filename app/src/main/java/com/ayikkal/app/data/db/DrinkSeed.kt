package com.ayikkal.app.data.db

import com.ayikkal.app.data.db.DrinkCategory.BEER
import com.ayikkal.app.data.db.DrinkCategory.COCKTAIL
import com.ayikkal.app.data.db.DrinkCategory.GIN
import com.ayikkal.app.data.db.DrinkCategory.RAKI
import com.ayikkal.app.data.db.DrinkCategory.TEQUILA
import com.ayikkal.app.data.db.DrinkCategory.VODKA
import com.ayikkal.app.data.db.DrinkCategory.WHISKY
import com.ayikkal.app.data.db.DrinkCategory.WINE

/**
 * İlk kurulumda veritabanına yazılan hazır içki kataloğu.
 * Kokteyllerin alkol oranı, içindeki saf alkol miktarının toplam hacme bölünmesiyle hesaplandı.
 */
object DrinkSeed {
    private fun d(category: DrinkCategory, name: String, serving: String, ml: Int, abv: Double) =
        DrinkType(category = category, name = name, servingLabel = serving, volumeMl = ml, abv = abv)

    val items: List<DrinkType> = listOf(
        d(BEER, "Efes Pilsen", "33cl", 330, 5.0),
        d(BEER, "Efes Pilsen", "50cl", 500, 5.0),
        d(BEER, "Efes Malt", "50cl", 500, 5.0),
        d(BEER, "Tuborg Gold", "50cl", 500, 5.0),
        d(BEER, "Carlsberg", "50cl", 500, 5.0),
        d(BEER, "Bomonti Filtresiz", "50cl", 500, 5.0),
        d(BEER, "Corona", "35.5cl", 355, 4.5),
        d(BEER, "Guinness", "44cl", 440, 4.2),
        d(BEER, "Craft IPA", "33cl", 330, 6.5),
        d(BEER, "Fıçı Bira", "Bardak 40cl", 400, 5.0),

        d(WINE, "Kırmızı Şarap", "Kadeh 15cl", 150, 13.5),
        d(WINE, "Beyaz Şarap", "Kadeh 15cl", 150, 12.0),
        d(WINE, "Roze Şarap", "Kadeh 15cl", 150, 12.0),
        d(WINE, "Köpüklü Şarap", "Kadeh 12cl", 120, 11.5),

        d(RAKI, "Yeni Rakı", "Tek 4cl", 40, 45.0),
        d(RAKI, "Yeni Rakı", "Duble 8cl", 80, 45.0),
        d(RAKI, "Tekirdağ Altın Seri", "Duble 8cl", 80, 45.0),
        d(RAKI, "Efe Gold", "Duble 8cl", 80, 45.0),
        d(RAKI, "Beylerbeyi Göbek", "Duble 8cl", 80, 45.0),

        d(VODKA, "Absolut", "Shot 4cl", 40, 40.0),
        d(VODKA, "Smirnoff", "Shot 4cl", 40, 37.5),
        d(VODKA, "Grey Goose", "Shot 4cl", 40, 40.0),
        d(VODKA, "Votka", "Duble 8cl", 80, 40.0),

        d(WHISKY, "Jameson", "Tek 4cl", 40, 40.0),
        d(WHISKY, "Jack Daniel's", "Double 8cl", 80, 40.0),
        d(WHISKY, "Johnnie Walker Black", "Double 8cl", 80, 40.0),
        d(WHISKY, "Chivas Regal", "Double 8cl", 80, 40.0),

        d(TEQUILA, "José Cuervo", "Shot 4cl", 40, 38.0),
        d(TEQUILA, "Olmeca", "Shot 4cl", 40, 38.0),
        d(TEQUILA, "Jägermeister", "Shot 4cl", 40, 35.0),

        d(GIN, "Gordon's", "Shot 4cl", 40, 37.5),
        d(GIN, "Bombay Sapphire", "Shot 4cl", 40, 40.0),

        d(COCKTAIL, "Cin Tonik", "25cl", 250, 6.0),
        d(COCKTAIL, "Mojito", "25cl", 250, 8.0),
        d(COCKTAIL, "Cuba Libre", "25cl", 250, 8.0),
        d(COCKTAIL, "Aperol Spritz", "20cl", 200, 8.0),
        d(COCKTAIL, "Margarita", "15cl", 150, 17.0),
        d(COCKTAIL, "Long Island", "30cl", 300, 13.0),
        d(COCKTAIL, "Negroni", "9cl", 90, 24.0),
    )
}
