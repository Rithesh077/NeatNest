package com.example.neatnest

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

// demonstrates toolbar menus, context menus, popup menus, and fragment lifecycle
class FileMoverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_mover)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val contextView: TextView = findViewById(R.id.context_view)
        registerForContextMenu(contextView)

        val popupButton: Button = findViewById(R.id.popup_button)
        popupButton.setOnClickListener { showPopupMenu(it) }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LifecycleFragment())
                .commit()
        }
    }

    // options menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                Toast.makeText(this, getString(R.string.search_clicked), Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_settings -> {
                Toast.makeText(this, getString(R.string.settings_clicked), Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // context menu
    override fun onCreateContextMenu(menu: android.view.ContextMenu, v: View, menuInfo: android.view.ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                showDeleteDialog()
                true
            }
            R.id.action_share -> {
                Toast.makeText(this, getString(R.string.share_clicked), Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_dialog_title))
            .setMessage(getString(R.string.delete_dialog_message))
            .setPositiveButton(getString(R.string.dialog_delete)) { _, _ ->
                Toast.makeText(this, getString(R.string.item_deleted), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .show()
    }

    private fun showArchiveDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.archive_dialog_title))
            .setMessage(getString(R.string.archive_dialog_message))
            .setPositiveButton(getString(R.string.dialog_archive)) { _, _ ->
                Toast.makeText(this, getString(R.string.item_archived), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .show()
    }

    // popup menu
    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.popup_menu, popup.menu)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            popup.setForceShowIcon(true)
        }
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_archive -> { showArchiveDialog(); true }
                R.id.action_move -> {
                    Toast.makeText(this, getString(R.string.move_clicked), Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}
