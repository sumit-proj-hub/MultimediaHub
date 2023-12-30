package com.example.multimediahub.pdfviewer

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.multimediahub.R
import com.example.multimediahub.getUriAndNameFromIntent
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle


class PDFViewerActivity : AppCompatActivity() {
    private lateinit var pdfView: PDFView
    private var toggleUiMode: MenuItem? = null
    private var nightMode = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdfviewer)
        val (uri, fileName) = getUriAndNameFromIntent(this, intent)
        if (uri == null) {
            Toast.makeText(this, "Failed to load PDF", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = fileName
        pdfView = findViewById(R.id.pdfView)
        pdfView.fromUri(uri)
            .spacing(8)
            .scrollHandle(DefaultScrollHandle(this))
            .load()
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            R.id.nightModeBtn -> {
                nightMode = !nightMode
                toggleUiMode?.setIcon(
                    if (nightMode) R.drawable.baseline_light_mode_24
                    else R.drawable.baseline_dark_mode_24
                )
                pdfView.setNightMode(nightMode)
                pdfView.invalidate()
                true
            }

            R.id.pdfJump -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Set Page Number (1 - ${pdfView.pageCount})")
                val editText = EditText(this)
                editText.inputType = InputType.TYPE_CLASS_NUMBER
                editText.filters = arrayOf(object : InputFilter {
                    override fun filter(
                        source: CharSequence?,
                        start: Int,
                        end: Int,
                        dest: Spanned?,
                        dstart: Int,
                        dend: Int
                    ): CharSequence? {
                        val input = (dest.toString() + source.toString()).toIntOrNull() ?: 1
                        if (input in 1..pdfView.pageCount)
                            return null
                        return ""
                    }

                })
                builder.setView(editText)
                builder.setPositiveButton("GO") { dialog, _ ->
                    val gotoPage = editText.text.toString().toIntOrNull()
                    if (gotoPage == null) dialog.cancel()
                    else pdfView.jumpTo(gotoPage - 1)
                }
                builder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                builder.show()
                true
            }

            else -> super.onOptionsItemSelected(menuItem)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.pdf_menu, menu)
        toggleUiMode = menu?.findItem(R.id.nightModeBtn)
        return true
    }
}