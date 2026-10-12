package ir.idontconnect.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import ir.idontconnect.ui.IdontConnectApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IdontConnectApp(
                onLanguageChanged = { languageTag ->
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(languageTag),
                    )
                },
            )
        }
    }
}
