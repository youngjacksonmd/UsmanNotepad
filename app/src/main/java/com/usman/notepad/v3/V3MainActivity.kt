package com.usman.notepad.v3

import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.usman.notepad.FoldersActivity
import com.usman.notepad.GraphActivity
import com.usman.notepad.SettingsActivity
import com.usman.notepad.TagsActivity
import com.usman.notepad.TemplatesActivity
import com.usman.notepad.data.NoteRepository
import com.usman.notepad.security.AppLock
import com.usman.notepad.v3.data.AppearancePrefs
import com.usman.notepad.v3.data.AppearanceStore
import com.usman.notepad.v3.data.V3Accent
import com.usman.notepad.v3.data.V3Density
import com.usman.notepad.v3.data.V3FolderRow
import com.usman.notepad.v3.data.V3NoteRow
import com.usman.notepad.v3.data.V3RepositoryAdapter
import com.usman.notepad.v3.data.V3ThemeMode
import com.usman.notepad.v3.theme.LocalUsmanMetrics
import com.usman.notepad.v3.theme.UsmanMotion
import com.usman.notepad.v3.theme.UsmanNotepadTheme
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class V3MainActivity : ComponentActivity() {
    private val refreshPulse = mutableIntStateOf(0)
    private val widgetAction = mutableStateOf<String?>(null)
    private val unlocked = mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        widgetAction.value = intent.getStringExtra("widget_action")
        val needsLock = AppLock.appLockEnabled(this) && AppLock.hasPin(this)
        unlocked.value = !needsLock
        setContent { V3Root() }
        if (needsLock) {
            window.decorView.post {
                AppLock.promptPin(this, "Welcome back") {
                    unlocked.value = true
                    window.decorView.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPulse.intValue++
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        widgetAction.value = intent.getStringExtra("widget_action")
        refreshPulse.intValue++
    }

    @Composable
    private fun V3Root() {
        var appearance by remember { mutableStateOf(AppearanceStore.load(this)) }
        UsmanNotepadTheme(appearance) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                if (!unlocked.value) {
                    LockSurface()
                } else {
                    V3App(
                        appearance = appearance,
                        onAppearance = {
                            appearance = it
                            AppearanceStore.save(this, it)
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun LockSurface() {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(76.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp)) }
            }
            Spacer(Modifier.height(24.dp))
            Text("Welcome back", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("Unlock to continue to your notes.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    private sealed interface Route {
        data class Notes(val initialFilter: String = "all") : Route
        data object Folders : Route
        data object More : Route
        data object Search : Route
        data class Editor(val id: Long) : Route
        data object Appearance : Route
        data object Trash : Route
        data class Scratch(val id: Long) : Route
        data object About : Route
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun V3App(appearance: AppearancePrefs, onAppearance: (AppearancePrefs) -> Unit) {
        val context = LocalContext.current
        val adapter = remember { V3RepositoryAdapter(context) }
        var route by remember { mutableStateOf<Route>(Route.Notes()) }
        val refresh = refreshPulse.intValue

        fun openNote(id: Long) {
            val note = adapter.find(id)
            if (note?.locked == true || (note?.unlockAt ?: 0L) > System.currentTimeMillis()) {
                startActivity(adapter.legacyEditorIntent(this, id))
            } else route = Route.Editor(id)
        }

        fun newNote(mode: String = "text", seed: String = "") {
            openNote(adapter.create(mode, seed))
            performSoftHaptic()
        }

        LaunchedEffect(widgetAction.value) {
            when (widgetAction.value) {
                "new" -> newNote()
                "scratch" -> route = Route.Scratch(adapter.newScratchId())
                "daily" -> openNote(adapter.dailyId())
            }
            widgetAction.value = null
        }

        val showBottom = route is Route.Notes || route is Route.Folders || route is Route.More
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (showBottom) {
                    V3BottomBar(
                        route = route,
                        onNotes = { route = Route.Notes() },
                        onFolders = { route = Route.Folders },
                        onMore = { route = Route.More }
                    )
                }
            }
        ) { padding ->
            when (val r = route) {
                is Route.Notes -> HomeScreen(
                    modifier = Modifier.padding(padding),
                    adapter = adapter,
                    refresh = refresh,
                    initialFilter = r.initialFilter,
                    onOpen = ::openNote,
                    onSearch = { route = Route.Search },
                    onNew = ::newNote,
                    onScratch = { route = Route.Scratch(adapter.newScratchId()) },
                    onDaily = { openNote(adapter.dailyId()) },
                    onChanged = { refreshPulse.intValue++ }
                )
                Route.Folders -> FoldersScreen(Modifier.padding(padding), adapter, refresh, ::openNote)
                Route.More -> MoreScreen(
                    Modifier.padding(padding),
                    onFavorites = { route = Route.Notes("favorites") },
                    onArchive = { route = Route.Notes("archived") },
                    onTrash = { route = Route.Trash },
                    onAppearance = { route = Route.Appearance },
                    onAbout = { route = Route.About }
                )
                Route.Search -> SearchScreen(adapter, refresh, onBack = { route = Route.Notes() }, onOpen = ::openNote)
                is Route.Editor -> EditorScreen(adapter, r.id, onBack = { route = Route.Notes(); refreshPulse.intValue++ })
                Route.Appearance -> AppearanceScreen(appearance, onAppearance, onBack = { route = Route.More })
                Route.Trash -> TrashScreen(adapter, refresh, onBack = { route = Route.More }, onChanged = { refreshPulse.intValue++ })
                is Route.Scratch -> ScratchScreen(adapter, r.id, onBack = { route = Route.Notes(); refreshPulse.intValue++ }, onOpen = ::openNote)
                Route.About -> AboutScreen { route = Route.More }
            }
        }
    }

    private fun performSoftHaptic() {
        window.decorView.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    @Composable
    private fun V3BottomBar(route: Route, onNotes: () -> Unit, onFolders: () -> Unit, onMore: () -> Unit) {
        Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = .97f), tonalElevation = 2.dp) {
            NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp, modifier = Modifier.navigationBarsPadding()) {
                val colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                NavigationBarItem(route is Route.Notes, onNotes, { Icon(Icons.Default.Description, null) }, label = { Text("Notes") }, colors = colors)
                NavigationBarItem(route is Route.Folders, onFolders, { Icon(Icons.Default.Folder, null) }, label = { Text("Folders") }, colors = colors)
                NavigationBarItem(route is Route.More, onMore, { Icon(Icons.Default.MoreHoriz, null) }, label = { Text("More") }, colors = colors)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun HomeScreen(
        modifier: Modifier,
        adapter: V3RepositoryAdapter,
        refresh: Int,
        initialFilter: String,
        onOpen: (Long) -> Unit,
        onSearch: () -> Unit,
        onNew: (String, String) -> Unit,
        onScratch: () -> Unit,
        onDaily: () -> Unit,
        onChanged: () -> Unit
    ) {
        val m = LocalUsmanMetrics.current
        var filter by remember(initialFilter) { mutableStateOf(initialFilter) }
        var createSheet by remember { mutableStateOf(false) }
        val notes = remember(refresh, filter) { adapter.listNotes("", filter) }
        val all = remember(refresh) { adapter.listNotes() }
        val pinned = all.filter { it.pinned }.take(8)
        val greeting = remember { greeting() }

        Box(modifier.fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(start = m.pagePadding, end = m.pagePadding, top = 20.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(m.itemGap)
            ) {
                item {
                    Text(greeting, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text("Your thoughts,\nbeautifully organized.", style = MaterialTheme.typography.displaySmall)
                    Spacer(Modifier.height(12.dp))
                    Text("${all.size} notes · ${all.count { it.pinned }} pinned", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item { Spacer(Modifier.height(6.dp)) }
                item { FloatingSearchBar(onSearch) }
                item {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("all" to "All", "pinned" to "Pinned", "favorites" to "Favorites", "recent" to "Recent").forEach { (key, label) ->
                            FilterPill(label, filter == key) { filter = key }
                        }
                    }
                }
                if (pinned.isNotEmpty() && filter == "all") {
                    item { Spacer(Modifier.height(8.dp)); SectionHeader("Pinned", "Keep important thoughts close") }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(end = 12.dp)) {
                            items(pinned, key = { it.id }) { n -> PinnedCard(n, { onOpen(n.id) }, onChanged, adapter) }
                        }
                    }
                }
                item { Spacer(Modifier.height(8.dp)); SectionHeader(if (filter == "all") "Recent" else filter.replaceFirstChar { it.uppercase() }, null) }
                if (notes.isEmpty()) {
                    item { EmptyState("No notes yet", "Your first thought can start here.", "Create a note") { createSheet = true } }
                } else {
                    items(notes, key = { it.id }) { n -> EditorialNoteRow(n, { onOpen(n.id) }, adapter, onChanged) }
                }
            }
            ExtendedFloatingActionButton(
                onClick = { createSheet = true; performSoftHaptic() },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("New note", fontWeight = FontWeight.SemiBold) },
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 20.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }

        if (createSheet) {
            ModalBottomSheet(onDismissRequest = { createSheet = false }, containerColor = MaterialTheme.colorScheme.surface) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Create something", style = MaterialTheme.typography.headlineMedium)
                    Text("Start with exactly what you need.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    CreateRow(Icons.Default.Description, "Text", "Start with a blank page") { createSheet = false; onNew("text", "") }
                    CreateRow(Icons.Default.CheckBox, "Checklist", "Perfect for tasks and lists") { createSheet = false; onNew("checklist", "☐ ") }
                    CreateRow(Icons.Default.AutoAwesome, "Scratch Pad", "Capture something instantly") { createSheet = false; onScratch() }
                    CreateRow(Icons.Default.NoteAdd, "Daily note", "Open today’s page") { createSheet = false; onDaily() }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    @Composable
    private fun FloatingSearchBar(onClick: () -> Unit) {
        Surface(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .72f),
            tonalElevation = 0.dp
        ) {
            Row(Modifier.padding(horizontal = 18.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(12.dp))
                Text("Search anything…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    @Composable
    private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
        val target by animateColorAsState(
            if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            tween(UsmanMotion.Micro), label = "filter"
        )
        Surface(shape = RoundedCornerShape(14.dp), color = target, modifier = Modifier.clickable(onClick = onClick)) {
            Text(label, modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp), style = MaterialTheme.typography.labelMedium, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    @Composable
    private fun SectionHeader(title: String, subtitle: String?) {
        Column {
            Text(title, style = MaterialTheme.typography.titleLarge)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    @Composable
    private fun PinnedCard(n: V3NoteRow, onOpen: () -> Unit, onChanged: () -> Unit, adapter: V3RepositoryAdapter) {
        Surface(
            modifier = Modifier.width(244.dp).height(162.dp).clip(RoundedCornerShape(24.dp)).clickable(onClick = onOpen),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .62f)
        ) {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PushPin, null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("PINNED", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(12.dp))
                Text(n.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(7.dp))
                Text(n.preview.ifBlank { "A quiet page waiting for more." }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
    }

    @Composable
    private fun EditorialNoteRow(n: V3NoteRow, onOpen: () -> Unit, adapter: V3RepositoryAdapter, onChanged: () -> Unit) {
        var menu by remember { mutableStateOf(false) }
        Column(Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(vertical = 9.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (n.locked) { Icon(Icons.Default.Lock, null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(6.dp)) }
                        Text(n.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (n.preview.isNotBlank()) {
                        Spacer(Modifier.height(5.dp))
                        Text(n.preview, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    if (n.checklistTotal > 0 && !n.locked) {
                        Spacer(Modifier.height(7.dp))
                        Text("${n.checklistDone} of ${n.checklistTotal} complete", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        n.tags.take(2).forEach { TagPill(it) }
                        if (n.pinned) Icon(Icons.Default.PushPin, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        if (n.favorite) Icon(Icons.Default.Star, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(relativeDate(n.updatedAt), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, "Note actions", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text(if (n.pinned) "Unpin" else "Pin") }, onClick = { menu = false; adapter.togglePin(n.id); onChanged(); performSoftHaptic() })
                        DropdownMenuItem(text = { Text(if (n.favorite) "Remove favorite" else "Favorite") }, onClick = { menu = false; adapter.toggleFavorite(n.id); onChanged(); performSoftHaptic() })
                        DropdownMenuItem(text = { Text("Archive") }, onClick = { menu = false; adapter.archive(n.id); onChanged() })
                        DropdownMenuItem(text = { Text("Move to Trash") }, onClick = { menu = false; adapter.trash(n.id); onChanged() })
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .28f), thickness = .7.dp)
        }
    }

    @Composable
    private fun TagPill(tag: String) {
        Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .7f)) {
            Text(tag, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }

    @Composable
    private fun CreateRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(vertical = 13.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(46.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }

    @Composable
    private fun EmptyState(title: String, subtitle: String, action: String? = null, onAction: (() -> Unit)? = null) {
        Column(Modifier.fillMaxWidth().padding(vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(58.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Description, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) } }
            Spacer(Modifier.height(18.dp)); Text(title, style = MaterialTheme.typography.titleLarge); Spacer(Modifier.height(6.dp)); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (action != null && onAction != null) { Spacer(Modifier.height(16.dp)); FilledTonalButton(onClick = onAction) { Text(action) } }
        }
    }

    @Composable
    private fun SearchScreen(adapter: V3RepositoryAdapter, refresh: Int, onBack: () -> Unit, onOpen: (Long) -> Unit) {
        var query by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        val results = remember(query, refresh) { if (query.isBlank()) emptyList() else adapter.listNotes(query, "all") }
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .75f), modifier = Modifier.weight(1f)) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(10.dp))
                        BasicTextField(
                            value = query, onValueChange = { query = it }, singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            modifier = Modifier.weight(1f).focusRequester(focusRequester),
                            decorationBox = { inner -> if (query.isEmpty()) Text("Search notes…", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge); inner() }
                        )
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
            when {
                query.isBlank() -> EmptyState("Find anything", "Search titles, words, folders and tags.")
                results.isEmpty() -> EmptyState("No results", "Nothing matched “$query”.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) { items(results, key = { it.id }) { EditorialNoteRow(it, { onOpen(it.id) }, adapter) {} } }
            }
        }
        LaunchedEffect(Unit) { delay(180); focusRequester.requestFocus() }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun EditorScreen(adapter: V3RepositoryAdapter, id: Long, onBack: () -> Unit) {
        val context = LocalContext.current
        val note = remember(id) { adapter.find(id) }
        if (note == null) { EmptyState("Note unavailable", "This note could not be opened.", "Back", onBack); return }
        var title by remember(id) { mutableStateOf(note.title ?: "") }
        var body by remember(id) { mutableStateOf(note.body ?: "") }
        var mode by remember(id) { mutableStateOf(note.mode ?: "text") }
        var saveState by remember { mutableStateOf("✓ Saved") }
        var initialized by remember { mutableStateOf(false) }
        var moreSheet by remember { mutableStateOf(false) }
        var insertSheet by remember { mutableStateOf(false) }
        var focusMode by remember { mutableStateOf(false) }
        val lifecycleOwner = LocalLifecycleOwner.current

        fun persist(snapshot: Boolean = false) { adapter.save(id, title, body, mode, snapshot); saveState = "✓ Saved" }
        LaunchedEffect(Unit) { initialized = true }
        LaunchedEffect(title, body, mode) {
            if (initialized) { saveState = "Saving…"; delay(700); persist(false) }
        }
        DisposableEffect(lifecycleOwner, title, body, mode) {
            val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_PAUSE) persist(false) }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
                Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { persist(false); onBack() }) { Icon(Icons.Default.ArrowBack, "Back") }
                    Spacer(Modifier.weight(1f))
                    AnimatedVisibility(!focusMode, enter = fadeIn(tween(UsmanMotion.Small)), exit = fadeOut(tween(UsmanMotion.Small))) {
                        Text(saveState, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { adapter.togglePin(id); performSoftHaptic() }) { Icon(Icons.Default.PushPin, "Pin", tint = if (note.pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
                    IconButton(onClick = { moreSheet = true }) { Icon(Icons.Default.MoreVert, "More") }
                }
                BasicTextField(
                    value = title, onValueChange = { title = it }, singleLine = false,
                    textStyle = MaterialTheme.typography.displaySmall.copy(color = MaterialTheme.colorScheme.onBackground, textDirection = TextDirection.Content),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    decorationBox = { inner -> if (title.isBlank()) Text("A beautiful idea", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .45f)); inner() }
                )
                Spacer(Modifier.height(20.dp))
                BasicTextField(
                    value = body, onValueChange = { body = it },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground, textDirection = TextDirection.Content),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    decorationBox = { inner -> Box { if (body.isBlank()) Text("Start writing…", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .48f)); inner() } }
                )
                Spacer(Modifier.height(82.dp))
            }
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .96f),
                tonalElevation = 3.dp,
                modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 20.dp, vertical = 14.dp).navigationBarsPadding().imePadding()
            ) {
                Row(Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    EditorTool("Aa") { body = if (body.endsWith("\n") || body.isBlank()) body + "# " else body + "\n# " }
                    EditorTool("✓") { mode = "checklist"; body += if (body.isBlank() || body.endsWith("\n")) "☐ " else "\n☐ " }
                    EditorTool("•") { body += if (body.isBlank() || body.endsWith("\n")) "• " else "\n• " }
                    IconButton(onClick = { insertSheet = true }) { Icon(Icons.Default.Add, "Insert", tint = MaterialTheme.colorScheme.primary) }
                }
            }
        }

        if (moreSheet) ModalBottomSheet(onDismissRequest = { moreSheet = false }) {
            Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Note options", style = MaterialTheme.typography.headlineMedium)
                CreateRow(Icons.Default.Settings, "Advanced tools", "History, tags, reminders, privacy, attachments, export") { moreSheet = false; persist(false); context.startActivity(adapter.legacyEditorIntent(context, id)) }
                CreateRow(Icons.Default.TextFields, if (focusMode) "Exit focus mode" else "Focus mode", "Quiet the interface while you write") { focusMode = !focusMode; moreSheet = false }
                CreateRow(Icons.Default.DeleteOutline, "Move to Trash", "You can restore it for 30 days") { adapter.trash(id); moreSheet = false; onBack() }
                Spacer(Modifier.height(20.dp))
            }
        }
        if (insertSheet) ModalBottomSheet(onDismissRequest = { insertSheet = false }) {
            Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Insert", style = MaterialTheme.typography.headlineMedium)
                CreateRow(Icons.Default.CheckBox, "Checklist", "Insert a task") { body += if (body.endsWith("\n") || body.isBlank()) "☐ " else "\n☐ "; insertSheet = false }
                CreateRow(Icons.Default.Description, "Divider", "Separate ideas") { body += "\n────────────\n"; insertSheet = false }
                CreateRow(Icons.Default.TextFields, "Quote", "Start a quoted thought") { body += if (body.endsWith("\n") || body.isBlank()) "> " else "\n> "; insertSheet = false }
                CreateRow(Icons.Default.NoteAdd, "Date", "Insert today’s date") { body += if (body.endsWith("\n") || body.isBlank()) DateFormat.getDateInstance().format(Date()) else "\n" + DateFormat.getDateInstance().format(Date()); insertSheet = false }
                CreateRow(Icons.Default.MoreHoriz, "More insert tools", "Image, reminder, linked note and drawing") { insertSheet = false; persist(false); context.startActivity(adapter.legacyEditorIntent(context, id)) }
                Spacer(Modifier.height(20.dp))
            }
        }
    }

    @Composable
    private fun EditorTool(label: String, onClick: () -> Unit) {
        TextButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 15.dp, vertical = 10.dp)) { Text(label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface) }
    }

    @Composable
    private fun FoldersScreen(modifier: Modifier, adapter: V3RepositoryAdapter, refresh: Int, onOpen: (Long) -> Unit) {
        val context = LocalContext.current
        val folders = remember(refresh) { adapter.folderRows() }
        var selected by remember { mutableStateOf<V3FolderRow?>(null) }
        if (selected != null) {
            val folder = selected!!
            val notes = remember(refresh, folder.id) { adapter.notesInFolder(folder.id) }
            Column(modifier.fillMaxSize().padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = { selected = null }) { Icon(Icons.Default.ArrowBack, "Back") }; Column { Text(folder.name, style = MaterialTheme.typography.headlineMedium); Text("${folder.noteCount} notes", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                Spacer(Modifier.height(18.dp))
                if (notes.isEmpty()) EmptyState("Nothing here yet", "Move a note into this folder to see it here.") else LazyColumn { items(notes, key = { it.id }) { EditorialNoteRow(it, { onOpen(it.id) }, adapter) {} } }
            }
            return
        }
        LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp, 22.dp, 20.dp, 100.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Text("Folders", style = MaterialTheme.typography.displaySmall); Spacer(Modifier.height(6.dp)); Text("Organize notes your way.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(18.dp)) }
            if (folders.isEmpty()) item { EmptyState("No folders yet", "Create a calm place for related notes.", "Manage folders") { context.startActivity(Intent(context, FoldersActivity::class.java)) } }
            items(folders, key = { it.id }) { f ->
                val depth = folderDepth(f, folders)
                Row(Modifier.fillMaxWidth().clickable { selected = f }.padding(start = (depth * 18).dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(44.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.primary) } }
                    Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text(f.name, style = MaterialTheme.typography.titleMedium); Text("${f.noteCount} notes", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item { Spacer(Modifier.height(14.dp)); OutlinedButton(onClick = { context.startActivity(Intent(context, FoldersActivity::class.java)) }) { Text("Manage folders") } }
        }
    }

    private fun folderDepth(folder: V3FolderRow, all: List<V3FolderRow>): Int {
        var depth = 0; var parent = folder.parentId; val seen = mutableSetOf<Long>()
        while (parent != null && seen.add(parent) && depth < 3) { depth++; parent = all.firstOrNull { it.id == parent }?.parentId }
        return depth
    }

    @Composable
    private fun MoreScreen(modifier: Modifier, onFavorites: () -> Unit, onArchive: () -> Unit, onTrash: () -> Unit, onAppearance: () -> Unit, onAbout: () -> Unit) {
        val context = LocalContext.current
        LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp, 22.dp, 20.dp, 100.dp)) {
            item { Text("More", style = MaterialTheme.typography.displaySmall); Spacer(Modifier.height(6.dp)); Text("Your workspace, your way.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(26.dp)) }
            item { GroupLabel("ORGANIZE") }
            item { SettingsRow(Icons.Default.Favorite, "Favorites", null, onFavorites) }
            item { SettingsRow(Icons.Default.Tag, "Tags", null) { context.startActivity(Intent(context, TagsActivity::class.java)) } }
            item { SettingsRow(Icons.Default.Description, "Templates", null) { context.startActivity(Intent(context, TemplatesActivity::class.java)) } }
            item { SettingsRow(Icons.Default.Restore, "Archive", null, onArchive) }
            item { SettingsRow(Icons.Default.DeleteOutline, "Trash", null, onTrash) }
            item { Spacer(Modifier.height(24.dp)); GroupLabel("PERSONALIZE") }
            item { SettingsRow(Icons.Default.Palette, "Appearance", "Theme, accent & writing style", onAppearance) }
            item { Spacer(Modifier.height(24.dp)); GroupLabel("PRIVACY") }
            item { SettingsRow(Icons.Default.Lock, "Privacy & Lock", "PIN, device credential & private titles") { context.startActivity(Intent(context, SettingsActivity::class.java)) } }
            item { Spacer(Modifier.height(24.dp)); GroupLabel("DATA") }
            item { SettingsRow(Icons.Default.Settings, "Backup & Export", "Local backup, restore & import") { context.startActivity(Intent(context, SettingsActivity::class.java)) } }
            item { SettingsRow(Icons.Default.GraphicEq, "Note Graph", "Explore linked notes") { context.startActivity(Intent(context, GraphActivity::class.java)) } }
            item { Spacer(Modifier.height(24.dp)); GroupLabel("ABOUT") }
            item { SettingsRow(Icons.Default.Description, "About UsmanNotepad", "Version 3.0.0", onAbout) }
        }
    }

    @Composable private fun GroupLabel(text: String) { Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp)) }

    @Composable
    private fun SettingsRow(icon: ImageVector, title: String, value: String?, onClick: () -> Unit) {
        Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(21.dp)); Spacer(Modifier.width(15.dp)); Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleMedium); if (value != null) Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun AppearanceScreen(prefs: AppearancePrefs, onPrefs: (AppearancePrefs) -> Unit, onBack: () -> Unit) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp, 10.dp, 20.dp, 30.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }; Column { Text("Appearance", style = MaterialTheme.typography.headlineMedium); Text("Make UsmanNotepad feel like yours.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
            item { ThemePreview() }
            item { Text("Theme", style = MaterialTheme.typography.titleLarge); FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { V3ThemeMode.entries.forEach { mode -> ChoiceButton(mode.name.lowercase().replaceFirstChar { it.uppercase() }, prefs.themeMode == mode) { onPrefs(prefs.copy(themeMode = mode)) } } } }
            item { Text("Accent", style = MaterialTheme.typography.titleLarge); FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { V3Accent.entries.forEach { accent -> AccentDot(accent, prefs.accent == accent) { onPrefs(prefs.copy(accent = accent)); performSoftHaptic() } } } }
            item { Text("Writing style", style = MaterialTheme.typography.titleLarge); Column { V3Density.entries.forEach { density -> DensityRow(density, prefs.density == density) { onPrefs(prefs.copy(density = density)) } } } }
            item { Text("Text size", style = MaterialTheme.typography.titleLarge); Row(verticalAlignment = Alignment.CenterVertically) { Text("A", style = MaterialTheme.typography.labelMedium); Slider(value = prefs.textScale, onValueChange = { onPrefs(prefs.copy(textScale = it.coerceIn(.85f, 1.25f))) }, valueRange = .85f..1.25f, modifier = Modifier.weight(1f).padding(horizontal = 12.dp)); Text("A", style = MaterialTheme.typography.titleLarge) } }
        }
    }

    @Composable
    private fun ThemePreview() {
        Surface(shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) { Text("A quiet place for ideas", style = MaterialTheme.typography.titleLarge); Spacer(Modifier.height(8.dp)); Text("Beautiful writing begins when the interface knows when to disappear.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(16.dp)); Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { TagPill("idea"); TagPill("personal") } }
        }
    }

    @Composable
    private fun ChoiceButton(text: String, selected: Boolean, onClick: () -> Unit) {
        if (selected) Button(onClick = onClick, shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)) { Text(text) }
        else OutlinedButton(onClick = onClick, shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)) { Text(text) }
    }

    @Composable
    private fun AccentDot(accent: V3Accent, selected: Boolean, onClick: () -> Unit) {
        val color = when (accent) { V3Accent.INDIGO -> Color(0xFF5A5BD6); V3Accent.VIOLET -> Color(0xFF7656C9); V3Accent.SAGE -> Color(0xFF5E7764); V3Accent.ROSE -> Color(0xFFA85F6E); V3Accent.SKY -> Color(0xFF4D7395); V3Accent.GRAPHITE -> Color(0xFF5D6269) }
        Surface(shape = CircleShape, color = color, modifier = Modifier.size(if (selected) 42.dp else 36.dp).clickable(onClick = onClick), border = if (selected) androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.onBackground) else null) {}
    }

    @Composable
    private fun DensityRow(density: V3Density, selected: Boolean, onClick: () -> Unit) {
        val (title, sub) = when (density) { V3Density.CALM -> "Calm" to "Spacious and relaxed"; V3Density.MODERN -> "Modern" to "Balanced and polished"; V3Density.COMPACT -> "Compact" to "More content on screen" }
        Surface(shape = RoundedCornerShape(18.dp), color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(sub, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }; if (selected) Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) } }
    }

    @Composable
    private fun TrashScreen(adapter: V3RepositoryAdapter, refresh: Int, onBack: () -> Unit, onChanged: () -> Unit) {
        val notes = remember(refresh) { adapter.listNotes("", "trash") }
        var confirm by remember { mutableStateOf<V3NoteRow?>(null) }
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }; Column { Text("Trash", style = MaterialTheme.typography.headlineMedium); Text("Deleted notes stay here for 30 days.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            Spacer(Modifier.height(18.dp))
            if (notes.isEmpty()) EmptyState("Trash is empty", "Nothing here needs your attention.") else LazyColumn { items(notes, key = { it.id }) { n -> Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(n.title, style = MaterialTheme.typography.titleMedium); Text(if (n.deletedAt > 0) "Deleted ${relativeDate(n.deletedAt)}" else "Deleted", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }; TextButton(onClick = { adapter.restore(n.id); onChanged() }) { Text("Restore") }; IconButton(onClick = { confirm = n }) { Icon(Icons.Default.DeleteOutline, "Delete forever") } }; HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .25f)) } }
        }
        if (confirm != null) AlertDialog(onDismissRequest = { confirm = null }, title = { Text("Delete forever?") }, text = { Text("This cannot be undone.") }, confirmButton = { TextButton(onClick = { adapter.purge(confirm!!.id); confirm = null; onChanged() }) { Text("Delete forever", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = { confirm = null }) { Text("Cancel") } })
    }

    @Composable
    private fun ScratchScreen(adapter: V3RepositoryAdapter, id: Long, onBack: () -> Unit, onOpen: (Long) -> Unit) {
        val note = remember(id) { adapter.find(id) }
        var body by remember(id) { mutableStateOf(note?.body.orEmpty()) }
        var saved by remember { mutableStateOf("Saved") }
        val focus = remember { FocusRequester() }
        LaunchedEffect(body) { saved = "Saving…"; delay(500); adapter.save(id, "Scratch", body, "text", false); saved = "Saved" }
        LaunchedEffect(Unit) { delay(150); focus.requestFocus() }
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }; Text("Scratch", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary); Spacer(Modifier.weight(1f)); Text(saved, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Spacer(Modifier.height(28.dp))
            BasicTextField(value = body, onValueChange = { body = it }, textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground, textDirection = TextDirection.Content), cursorBrush = SolidColor(MaterialTheme.colorScheme.primary), modifier = Modifier.fillMaxWidth().weight(1f).focusRequester(focus), decorationBox = { inner -> Box { if (body.isBlank()) Text("What’s on your mind?", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .5f)); inner() } })
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.End) { TextButton(onClick = { adapter.discardScratch(id); onBack() }) { Text("Discard", color = MaterialTheme.colorScheme.onSurfaceVariant) }; Spacer(Modifier.width(8.dp)); Button(onClick = { adapter.save(id, "Scratch", body, "text", true); adapter.convertScratch(id); onOpen(id) }, shape = RoundedCornerShape(14.dp)) { Text("Convert to note") } }
        }
    }

    @Composable
    private fun AboutScreen(onBack: () -> Unit) {
        Column(Modifier.fillMaxSize().padding(24.dp)) { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }; Spacer(Modifier.height(32.dp)); Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(72.dp)) { Box(contentAlignment = Alignment.Center) { Text("U", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) } }; Spacer(Modifier.height(22.dp)); Text("UsmanNotepad", style = MaterialTheme.typography.displaySmall); Text("Version 3.0.0", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(24.dp)); Text("Your thoughts belong here.", style = MaterialTheme.typography.titleLarge); Spacer(Modifier.height(10.dp)); Text("Private, local-first notes with no account, no analytics and no external AI API required.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }

    private fun greeting(): String {
        return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) { in 5..11 -> "Good morning"; in 12..16 -> "Good afternoon"; else -> "Good evening" }
    }

    private fun relativeDate(time: Long): String {
        if (time <= 0L) return ""
        val now = System.currentTimeMillis(); val delta = now - time
        return when { delta < 60_000L -> "Just now"; delta < 3_600_000L -> "${delta / 60_000L}m ago"; delta < 86_400_000L -> "${delta / 3_600_000L}h ago"; delta < 172_800_000L -> "Yesterday"; else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(time)) }
    }
}