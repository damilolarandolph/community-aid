package com.prog.communityaid.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.prog.communityaid.R
import com.prog.communityaid.data.models.Complaint
import com.prog.communityaid.data.repositories.ComplaintRepository
import com.prog.communityaid.storage.StorageController
import com.prog.communityaid.utils.Filesystem
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Feed : AppCompatActivity() {

    private var storageController = StorageController()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)
        supportActionBar?.hide()
        val listView = findViewById<ListView>(R.id.feedList)
        val complaintRepository = ComplaintRepository()
        val postBar = findViewById<EditText>(R.id.postEditText)
        postBar.isFocusable = false
        postBar.setOnClickListener {
            val intent = Intent(this, CreateComplaint::class.java)
            startActivity(intent)
        }
        GlobalScope.launch {
            val complaints = complaintRepository.findAllAsync().await()
            runOnUiThread {
                listView.adapter = ListViewAdapter(this@Feed, complaints)
            }
        }

    }
}


class ListViewAdapter(_context: AppCompatActivity, _complaints: List<Complaint>) : BaseAdapter() {

    private val context = _context
    private var complaints = _complaints
    private var storageController = StorageController()


    override fun getCount(): Int {
        return complaints.size
    }


    override fun getItem(p0: Int): Any? {
        return null;
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = (context as Activity).layoutInflater;
        var row = inflater.inflate(R.layout.activity_custom_feed_post, parent, false)
        val complaint = complaints[position]
        row.findViewById<TextView>(R.id.header).text = complaint.title
        row.findViewById<TextView>(R.id.description).text = complaint.description
        row.findViewById<TextView>(R.id.userName).text = "loading"
        row.findViewById<TextView>(R.id.commentCount).text = "loading"
        row.findViewById<TextView>(R.id.likeCount).text = "loading"

        val pictureFile = Filesystem.createImageFile(context)

        GlobalScope.launch {
            complaint.getUserAsync().await().name.also {
                this@ListViewAdapter.context.runOnUiThread {
                    row.findViewById<TextView>(R.id.userName).text =
                        it
                }
            }

            complaint.getLikesAsync().await().size.toString().also {
                this@ListViewAdapter.context.runOnUiThread {
                    row.findViewById<TextView>(R.id.likeCount).text =
                        it
                }
            }
            complaint.getCommentsAsync().await().size.toString().also {
                this@ListViewAdapter.context.runOnUiThread {
                    row.findViewById<TextView>(R.id.commentCount).text =
                        it
                }
            }
            StorageController.getFile(complaint.picture, pictureFile).await().also {
                this@ListViewAdapter.context.runOnUiThread {
                    row.findViewById<ImageView>(R.id.image).invalidate()
                    row.findViewById<ImageView>(R.id.image)
                        .setImageDrawable(Drawable.createFromPath(pictureFile.path))
                }
            }
        }

        return row
    }

}