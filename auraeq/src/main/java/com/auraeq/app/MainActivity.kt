package com.auraeq.app

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private var player: MediaPlayer? = null
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AuraEqApp() }
    }

    override fun onDestroy() {
        releaseAudio()
        super.onDestroy()
    }

    private fun releaseAudio() {
        runCatching { equalizer?.release() }
        runCatching { bassBoost?.release() }
        runCatching { player?.release() }
        equalizer = null
        bassBoost = null
        player = null
    }

    private fun loadTrack(
        uri: Uri,
        onReady: (EngineState) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        releaseAudio()
        val newPlayer = MediaPlayer()
        player = newPlayer
        runCatching {
            newPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            newPlayer.setDataSource(this, uri)
            newPlayer.setOnPreparedListener { mp ->
                val engine = setupEffects(mp.audioSessionId)
                onReady(engine.copy(durationMs = mp.duration.coerceAtLeast(1)))
                mp.start()
            }
            newPlayer.setOnCompletionListener { onComplete() }
            newPlayer.setOnErrorListener { _, what, extra ->
                onError("Playback error $what/$extra")
                true
            }
            newPlayer.prepareAsync()
        }.onFailure { onError(it.message ?: "Unable to open audio") }
    }

    private fun setupEffects(audioSessionId: Int): EngineState {
        runCatching { equalizer?.release() }
        runCatching { bassBoost?.release() }

        val eq = runCatching { Equalizer(0, audioSessionId) }.getOrNull()
        equalizer = eq
        if (eq == null) return EngineState(message = "Equalizer not supported on this device")

        eq.enabled = true
        val range = eq.bandLevelRange
        val minDb = range[0] / 100f
        val maxDb = range[1] / 100f
        val labels = (0 until eq.numberOfBands.toInt()).map { i ->
            formatFrequency(eq.getCenterFreq(i.toShort()) / 1000f)
        }
        val levels = (0 until eq.numberOfBands.toInt()).map { i ->
            eq.getBandLevel(i.toShort()) / 100f
        }

        val bass = runCatching { BassBoost(0, audioSessionId) }.getOrNull()
        bassBoost = bass
        bass?.enabled = true

        return EngineState(
            labels = labels,
            levels = levels,
            minDb = minDb,
            maxDb = maxDb,
            bassSupported = bass?.strengthSupported == true,
            message = "${labels.size}-band device equalizer active"
        )
    }

    private fun setBand(index: Int, db: Float) {
        val eq = equalizer ?: return
        val r = eq.bandLevelRange
        val mb = (db * 100).roundToInt().coerceIn(r[0].toInt(), r[1].toInt()).toShort()
        runCatching { eq.setBandLevel(index.toShort(), mb) }
    }

    private fun applyPreset(name: String): List<Float> {
        val eq = equalizer ?: return emptyList()
        val r = eq.bandLevelRange
        val minDb = r[0] / 100f
        val maxDb = r[1] / 100f
        return (0 until eq.numberOfBands.toInt()).map { i ->
            val hz = eq.getCenterFreq(i.toShort()) / 1000f
            val target = when (name) {
                "Bass" -> when { hz < 180f -> 7f; hz < 500f -> 3f; hz > 6000f -> 1.5f; else -> 0f }
                "Vocal" -> when { hz in 700f..4000f -> 5f; hz < 180f -> -2f; else -> 0f }
                "Rock" -> when { hz < 180f -> 5f; hz in 700f..2500f -> 2f; hz > 5000f -> 4f; else -> 0f }
                else -> 0f
            }.coerceIn(minDb, maxDb)
            setBand(i, target)
            target
        }
    }

    private fun setBass(value: Float) {
        val bass = bassBoost ?: return
        runCatching { bass.setStrength(value.roundToInt().coerceIn(0, 1000).toShort()) }
    }

    private fun displayName(context: Context, uri: Uri): String {
        var name: String? = null
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (i >= 0) name = c.getString(i)
            }
        }
        return name ?: uri.lastPathSegment ?: "Selected audio"
    }

    @Composable
    private fun AuraEqApp() {
        val scheme = darkColorScheme(
            primary = Color(0xFF65E6FF),
            secondary = Color(0xFFB27CFF),
            background = Color(0xFF090B12),
            surface = Color(0xFF121624)
        )

        var trackName by remember { mutableStateOf("No track selected") }
        var status by remember { mutableStateOf("Choose a local audio file to begin") }
        var isPlaying by remember { mutableStateOf(false) }
        var eqEnabled by remember { mutableStateOf(true) }
        var labels by remember { mutableStateOf(emptyList<String>()) }
        var levels by remember { mutableStateOf(emptyList<Float>()) }
        var minDb by remember { mutableStateOf(-15f) }
        var maxDb by remember { mutableStateOf(15f) }
        var bassSupported by remember { mutableStateOf(false) }
        var bass by remember { mutableStateOf(0f) }
        var duration by remember { mutableStateOf(1) }
        var position by remember { mutableStateOf(0f) }

        val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                trackName = displayName(this, uri)
                status = "Loading audio…"
                position = 0f
                loadTrack(
                    uri,
                    onReady = { e ->
                        labels = e.labels
                        levels = e.levels
                        minDb = e.minDb
                        maxDb = e.maxDb
                        bassSupported = e.bassSupported
                        duration = e.durationMs
                        status = e.message
                        isPlaying = true
                        eqEnabled = true
                    },
                    onComplete = { isPlaying = false; position = duration.toFloat() },
                    onError = { status = it; isPlaying = false }
                )
            }
        }

        LaunchedEffect(isPlaying, duration) {
            while (isPlaying) {
                player?.let { mp -> position = runCatching { mp.currentPosition.toFloat() }.getOrDefault(position) }
                delay(400)
            }
        }

        MaterialTheme(colorScheme = scheme) {
            Surface(Modifier.fillMaxSize(), color = Color.Transparent) {
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color(0xFF080A11), Color(0xFF10152A), Color(0xFF090B12)))
                    )
                ) {
                    Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Equalizer, null, tint = scheme.primary)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("AuraEQ", fontSize = 30.sp, fontWeight = FontWeight.Black)
                                Text("Native Audio Equalizer", color = Color(0xFF9BA7C7))
                            }
                        }

                        GlassCard {
                            Text(trackName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(status, color = Color(0xFFAAB4D2), fontSize = 13.sp)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { picker.launch(arrayOf("audio/*")) }, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.FolderOpen, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Choose audio file")
                            }
                            if (player != null) {
                                Slider(
                                    value = position.coerceIn(0f, duration.toFloat()),
                                    onValueChange = {
                                        position = it
                                        runCatching { player?.seekTo(it.roundToInt()) }
                                    },
                                    valueRange = 0f..duration.toFloat().coerceAtLeast(1f)
                                )
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(formatTime(position.roundToInt()), color = Color(0xFFAAB4D2))
                                    Button(
                                        onClick = {
                                            val mp = player ?: return@Button
                                            if (runCatching { mp.isPlaying }.getOrDefault(false)) {
                                                mp.pause(); isPlaying = false
                                            } else {
                                                mp.start(); isPlaying = true
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = scheme.secondary)
                                    ) {
                                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null)
                                        Spacer(Modifier.width(6.dp))
                                        Text(if (isPlaying) "Pause" else "Play")
                                    }
                                    Text(formatTime(duration), color = Color(0xFFAAB4D2))
                                }
                            }
                        }

                        if (labels.isNotEmpty()) {
                            GlassCard {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Equalizer", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                        Text("Actual device bands", color = Color(0xFFAAB4D2), fontSize = 12.sp)
                                    }
                                    Switch(checked = eqEnabled, onCheckedChange = {
                                        eqEnabled = it
                                        runCatching { equalizer?.enabled = it }
                                    })
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Flat", "Bass", "Vocal", "Rock").forEach { preset ->
                                        Button(
                                            onClick = { levels = applyPreset(preset) },
                                            modifier = Modifier.weight(1f),
                                            contentPadding = ButtonDefaults.ContentPadding
                                        ) { Text(preset, fontSize = 11.sp) }
                                    }
                                }
                                labels.forEachIndexed { index, label ->
                                    val value = levels.getOrElse(index) { 0f }.coerceIn(minDb, maxDb)
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text(label, Modifier.width(64.dp), fontWeight = FontWeight.SemiBold)
                                        Slider(
                                            value,
                                            onValueChange = { newValue ->
                                                levels = levels.toMutableList().also { it[index] = newValue }
                                                setBand(index, newValue)
                                            },
                                            valueRange = minDb..maxDb,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text("${"%.1f".format(value)} dB", Modifier.width(68.dp), fontSize = 12.sp)
                                    }
                                }
                            }

                            GlassCard {
                                Text("Bass Boost", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                if (bassSupported) {
                                    Text("${(bass / 10).roundToInt()}%", color = scheme.primary)
                                    Slider(bass, onValueChange = { bass = it; setBass(it) }, valueRange = 0f..1000f)
                                } else {
                                    Text("Not supported by this device/audio path", color = Color(0xFFAAB4D2))
                                }
                            }

                            Text(
                                "AuraEQ processes audio played inside this app. Android does not guarantee system-wide EQ control for other apps.",
                                color = Color(0xFF7F8AA8),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun GlassCard(content: @Composable ColumnScope.() -> Unit) {
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xCC151A2B))
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
        }
    }

    private fun formatFrequency(hz: Float): String =
        if (hz >= 1000f) "${"%.1f".format(hz / 1000f)}k" else "${hz.roundToInt()}Hz"

    private fun formatTime(ms: Int): String {
        val total = ms.coerceAtLeast(0) / 1000
        return "%d:%02d".format(total / 60, total % 60)
    }

    private data class EngineState(
        val labels: List<String> = emptyList(),
        val levels: List<Float> = emptyList(),
        val minDb: Float = -15f,
        val maxDb: Float = 15f,
        val bassSupported: Boolean = false,
        val durationMs: Int = 1,
        val message: String = "Ready"
    )
}
