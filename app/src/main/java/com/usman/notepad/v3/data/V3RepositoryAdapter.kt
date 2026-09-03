package com.usman.notepad.v3.data

import android.content.Context
import android.content.Intent
import com.usman.notepad.EditorActivity
import com.usman.notepad.data.NoteRepository
import com.usman.notepad.model.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class V3NoteRow(
    val id: Long,
    val title: String,
    val preview: String,
    val pinned: Boolean,
    val favorite: Boolean,
    val locked: Boolean,
    val quickCopy: Boolean,
    val mode: String,
    val updatedAt: Long,
    val folderId: Long?,
    val tags: List<String>,
    val checklistDone: Int,
    val checklistTotal: Int,
    val deletedAt: Long = 0L
)

data class V3FolderRow(val id: Long, val name: String, val parentId: Long?, val noteCount: Int)

class V3RepositoryAdapter(context: Context) {
    private val appContext = context.applicationContext
    private val repo = NoteRepository(appContext)

    fun listNotes(query: String = "", filter: String = "all"): List<V3NoteRow> {
        val actualFilter = if (filter == "recent") "all" else filter
        return repo.list(query, actualFilter).map(::row)
    }

    fun find(id: Long): Note? = repo.find(id)

    fun create(mode: String = "text", seed: String = ""): Long {
        val n = Note()
        n.mode = mode
        n.body = seed
        repo.save(n, false)
        return n.id
    }

    fun save(id: Long, title: String, body: String, mode: String, snapshot: Boolean = false): Long {
        val n = repo.find(id) ?: Note().also { it.id = id }
        n.title = title
        n.body = body
        n.mode = mode
        return repo.save(n, snapshot)
    }

    fun newScratchId(): Long = repo.getOrCreateScratch().id
    fun dailyId(): Long {
        val day = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return repo.getOrCreateDaily(day).id
    }

    fun convertScratch(id: Long) {
        repo.find(id)?.let { n -> n.scratch = false; if (n.title.isBlank()) n.title = "Quick thought"; repo.save(n, true) }
    }

    fun discardScratch(id: Long) = repo.trash(id)
    fun togglePin(id: Long) = repo.togglePin(id)
    fun toggleFavorite(id: Long) = repo.toggleFavorite(id)
    fun archive(id: Long) = repo.archive(id)
    fun trash(id: Long) = repo.trash(id)
    fun restore(id: Long) = repo.restore(id)
    fun purge(id: Long) = repo.purge(id)

    fun folderRows(): List<V3FolderRow> {
        val allNotes = repo.list("", "all") + repo.list("", "archived")
        return repo.folderRows().mapNotNull { raw ->
            val parts = raw.split("|", limit = 3)
            val id = parts.getOrNull(0)?.toLongOrNull() ?: return@mapNotNull null
            val parent = parts.getOrNull(2)?.takeIf { it.isNotBlank() }?.toLongOrNull()
            V3FolderRow(id, parts.getOrNull(1).orEmpty(), parent, allNotes.count { it.folderId == id })
        }
    }

    fun notesInFolder(folderId: Long): List<V3NoteRow> = repo.list("", "all").filter { it.folderId == folderId }.map(::row)
    fun tags(): List<String> = repo.allTags()
    fun templates(): List<String> = repo.templates()
    fun legacyEditorIntent(context: Context, id: Long): Intent = Intent(context, EditorActivity::class.java).putExtra("note_id", id)

    private fun row(n: Note): V3NoteRow {
        val progress = PreviewFormatter.checklistProgress(if (n.locked) "" else n.body)
        return V3NoteRow(
            id = n.id,
            title = PreviewFormatter.title(n.title),
            preview = PreviewFormatter.preview(n.body, n.locked),
            pinned = n.pinned,
            favorite = n.favorite,
            locked = n.locked,
            quickCopy = n.quickCopy,
            mode = n.mode ?: "text",
            updatedAt = n.updatedAt,
            folderId = n.folderId,
            tags = if (n.locked) emptyList() else repo.tagsFor(n.id).take(2),
            checklistDone = progress.first,
            checklistTotal = progress.second,
            deletedAt = n.deletedAt
        )
    }
}