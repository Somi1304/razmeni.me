package me.razmeni

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import me.razmeni.notifications.Notifier
import me.razmeni.ui.theme.RazmeniTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName
        Notifier.createChannel(this)
        setContent {
            RazmeniTheme {
                RazmeniApp()
            }
        }
    }
}
