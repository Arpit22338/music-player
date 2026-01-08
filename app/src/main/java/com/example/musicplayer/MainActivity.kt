package com.example.musicplayer

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private val songs = mutableListOf<Song>()
    private lateinit var adapter: SongAdapter
    private lateinit var hiddenManager: HiddenManager
    private var mediaPlayer: MediaPlayer? = null
    private var shuffle = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        hiddenManager = HiddenManager(this)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val recycler = findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = SongAdapter(songs, hiddenManager, onPlay = { play(it) }, onLongPress = { toggleHide(it) })
        recycler.adapter = adapter

        val spinner = findViewById<Spinner>(R.id.viewSpinner)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("All Songs", "Folders", "Hidden"))
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        val shuffleToggle = findViewById<ToggleButton>(R.id.shuffleToggle)
        shuffleToggle.setOnCheckedChangeListener { _, isChecked -> shuffle = isChecked }

        val search = findViewById<SearchView>(R.id.searchView)

        spinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> { // All
                        updateList(songsAll())
                    }
                    1 -> { // Folders
                        showFoldersDialog()
                    }
                    2 -> { // Hidden
                        updateList(songsHidden())
                    }
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })

        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText ?: "")
                return true
            }
        })

        requestPermissionAndScan()
    }

    private fun requestPermissionAndScan() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) scanSongs()
            }
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else scanSongs()
    }

    private fun scanSongs() {
        songs.clear()
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION)
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!=0"
        val cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null)
        cursor?.use {
            val idIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val dataIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            while (it.moveToNext()) {
                val id = it.getLong(idIdx)
                val title = it.getString(titleIdx) ?: "Unknown"
                val artist = it.getString(artistIdx)
                val data = it.getString(dataIdx) ?: continue
                val dur = it.getLong(durIdx)
                songs.add(Song(id, title, artist, data, dur))
            }
        }
        updateList(songs)
    }

    private fun songsAll(): List<Song> {
        return songs.filter { !hiddenManager.isHidden(it.id) }
    }

    private fun songsHidden(): List<Song> {
        val hidden = hiddenManager.getAllHidden()
        return songs.filter { hidden.contains(it.id) }
    }

    private fun updateList(list: List<Song>) {
        adapter.update(list)
    }

    private fun showFoldersDialog() {
        val map = mutableMapOf<String, MutableList<Song>>()
        for (s in songs) {
            val parent = s.path.substringBeforeLast('/', "")
            if (!hiddenManager.isHidden(s.id)) map.getOrPut(parent) { mutableListOf() }.add(s)
        }
        val folders = map.keys.sorted()
        if (folders.isEmpty()) return
        AlertDialog.Builder(this)
            .setTitle("Choose folder")
            .setItems(folders.toTypedArray()) { _, which ->
                val f = folders[which]
                updateList(map[f] ?: emptyList())
            }
            .show()
    }

    private fun toggleHide(song: Song) {
        if (hiddenManager.isHidden(song.id)) hiddenManager.unhide(song.id) else hiddenManager.hide(song.id)
        // Refresh current view
        updateList(songsAll())
    }

    private fun play(song: Song) {
        if (hiddenManager.isHidden(song.id)) return
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(song.path)
            setOnPreparedListener { start() }
            prepareAsync()
        }
    }

    private fun filter(q: String) {
        val lower = q.lowercase()
        val filtered = songsAll().filter { it.title.lowercase().contains(lower) || (it.artist?.lowercase()?.contains(lower) == true) || it.path.lowercase().contains(lower) }
        updateList(filtered)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}
