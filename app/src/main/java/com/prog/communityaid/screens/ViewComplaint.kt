package com.prog.communityaid.screens

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.prog.communityaid.R
import com.prog.communityaid.auth.Auth
import com.prog.communityaid.data.models.Comment
import com.prog.communityaid.data.models.Complaint
import com.prog.communityaid.data.repositories.CommentRepository
import com.prog.communityaid.data.repositories.ComplaintRepository
import com.prog.communityaid.storage.StorageController
import com.prog.communityaid.utils.Filesystem
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ViewComplaint : AppCompatActivity() {
    private var complaintRepo = ComplaintRepository()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more)

        val moreTabs = findViewById<TabLayout>(R.id.moreTabs)
        val morePager = findViewById<ViewPager2>(R.id.morePager)

        val solveButton = findViewById<Button>(R.id.solveButton)
        if (Auth.user!!.userType == "user") {
            solveButton.visibility = View.GONE
        } else {
            solveButton.visibility = View.VISIBLE
        }
        supportActionBar!!.hide()
        GlobalScope.launch {
            val complaints =
                complaintRepo.findByIdAsync(intent.getStringExtra("complaintId")!!)
                    .await()

            runOnUiThread {
                if (complaints.solved) {
                    solveButton.visibility = View.GONE
                } else {
                    solveButton.visibility = View.VISIBLE
                }

                solveButton.setOnClickListener {
                    complaints.solved = true;
                    GlobalScope.launch {
                        complaintRepo.saveAsync(complaints).await()
                        val intent = Intent(this@ViewComplaint, ViewComplaint::class.java)
                        intent.putExtra("complaintId", complaints.id)
                        this@ViewComplaint.finish()
                        this@ViewComplaint.overridePendingTransition(0, 0)
                        startActivity(intent)
                        this@ViewComplaint.overridePendingTransition(0, 0)
                    }

                }
                morePager.adapter =
                    TabAdapter2(
                        complaints,
                        3,
                        supportFragmentManager,
                        lifecycle,
                        this@ViewComplaint
                    )
                morePager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        moreTabs.setScrollPosition(position, 0f, true)
                    }
                })
                moreTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        morePager.currentItem = tab!!.position
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {
                    }

                    override fun onTabReselected(tab: TabLayout.Tab?) {
                    }

                })

            }
        }
    }
}


class TabAdapter2(
    _complaint: Complaint,
    _itemCount: Int,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private var app: AppCompatActivity
) :
    androidx.viewpager2.adapter.FragmentStateAdapter(fragmentManager, lifecycle) {

    private var complaint = _complaint
    private var itemCount = _itemCount
    override fun getItemCount(): Int {
        return itemCount
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                FirstPage(complaint, app)
            }
            1 -> {
                SecondFragment(complaint)
            }
            else -> {
                ThirdPage(complaint, app)
            }
        }
    }

}

class FirstPage(private var complaint: Complaint, private var app: AppCompatActivity) : Fragment() {

    private var commentRepo = CommentRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_more_first, container, false)
        val recycler = view.findViewById<RecyclerView>(R.id.recyler)
        val commentBox = view.findViewById<EditText>(R.id.commentBox)
        val sendButton = view.findViewById<ImageButton>(R.id.sendComment)

        sendButton.setOnClickListener {
            app.findViewById<ConstraintLayout>(R.id.loader).visibility = View.VISIBLE
            val comment = Comment()
            comment.body = commentBox.text.toString()
            comment.userId = Auth.user!!.id
            comment.complaintId = complaint.id
            GlobalScope.async {
                commentRepo.saveAsync(comment).await()
                app.runOnUiThread {
                    app.findViewById<ConstraintLayout>(R.id.loader).visibility = View.GONE
                    val intent = Intent(app, ViewComplaint::class.java)
                    intent.putExtra("complaintId", complaint.id)
                    app.finish()
                    app.overridePendingTransition(0, 0)
                    startActivity(intent)
                    app.overridePendingTransition(0, 0)
                }
            }
        }


        GlobalScope.launch {
            val comments = complaint.getCommentsAsync().await()
            app.runOnUiThread {
                recycler.adapter = CustomAdapter(comments, app)
                recycler.layoutManager = LinearLayoutManager(app)
            }
        }


        return view
    }
}


class ThirdPage(private var complaint: Complaint, private var app: AppCompatActivity) :
    Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_more_third, container, false);
        val pictureFile = Filesystem.createImageFile(app);
        val videoFile = Filesystem.createVideoFile(app);
        val videView = view.findViewById<VideoView>(R.id.videoMore)
        val imageView = view.findViewById<ImageView>(R.id.imageMore)
        videView.setOnPreparedListener { m ->
            m.isLooping = true
        }

        GlobalScope.launch {
            StorageController.getFile(complaint.picture, pictureFile).await().also {
                app.runOnUiThread {
                    imageView.invalidate()
                    imageView.setImageDrawable(Drawable.createFromPath(pictureFile.path))
                    Toast.makeText(app, "Done loading image", Toast.LENGTH_LONG).show()
                }
            }

            StorageController.getFile(complaint.video, videoFile).await().also {
                app.runOnUiThread {
                    videView.invalidate()
                    videView.setVideoPath(videoFile.path)
                    videView.start()
                    Toast.makeText(app, "Done loading video", Toast.LENGTH_LONG).show()
                }
            }
        }



        return view
    }
}

class CustomAdapter(private val dataSet: List<Comment>, private val app: AppCompatActivity) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName: TextView
        val content: TextView

        init {
            // Define click listener for the ViewHolder's View.
            userName = view.findViewById(R.id.commentHeader)
            content = view.findViewById(R.id.commentText)

        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.activity_comment, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.content.setText(dataSet[position].body)
        GlobalScope.launch {
            dataSet[position].getUser().await().also {
                app.runOnUiThread {
                    viewHolder.userName.setText(it.name)
                }
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}



