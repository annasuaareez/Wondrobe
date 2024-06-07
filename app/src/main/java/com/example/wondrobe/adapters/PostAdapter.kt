import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wondrobe.R
import com.example.wondrobe.data.Post
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class PostAdapter(
    private val context: Context,
    private var posts: List<Post>,
    private val listener: OnPostClickListener,
    private val isPostSaved: (String, (Boolean) -> Unit) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    interface OnPostClickListener {
        fun onSaveClick(post: Post, saveIconView: ImageView)
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage: ImageView = itemView.findViewById(R.id.post_image)
        val postUsernameView: TextView = itemView.findViewById(R.id.post_username)
        val postTitle: TextView = itemView.findViewById(R.id.post_title)
        val postDescription: TextView = itemView.findViewById(R.id.post_description)
        val dateView: TextView = itemView.findViewById(R.id.post_date) // Declaring dateView here
        val saveIcon: ImageView = itemView.findViewById(R.id.saveIconPost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.postTitle.text = post.title
        Glide.with(context).load(post.imageUrl).into(holder.postImage)

        // Set username
        post.userId?.let { userId ->
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val username = document.getString("username")
                    holder.postUsernameView.text = username ?: ""
                }
                .addOnFailureListener { exception ->
                    Log.e("PostAdapter", "Error getting username", exception)
                }
        }

        // Check if description exists
        if (post.description.isNullOrEmpty()) {
            holder.postDescription.visibility = View.GONE
            holder.dateView.visibility = View.VISIBLE
            val params = holder.postDescription.layoutParams as ConstraintLayout.LayoutParams
            params.topToBottom = holder.postUsernameView.id
            holder.postDescription.layoutParams = params

            // Adjust date position
            val dateParams = holder.dateView.layoutParams as ConstraintLayout.LayoutParams
            dateParams.topToBottom = holder.postUsernameView.id
            holder.dateView.layoutParams = dateParams
        } else {
            holder.postDescription.visibility = View.VISIBLE
            holder.dateView.visibility = View.VISIBLE

            // Adjust constraints
            val params = holder.postDescription.layoutParams as ConstraintLayout.LayoutParams
            params.topToBottom = holder.postUsernameView.id
            holder.postDescription.layoutParams = params

            val dateParams = holder.dateView.layoutParams as ConstraintLayout.LayoutParams
            dateParams.topToBottom = holder.postDescription.id
            holder.dateView.layoutParams = dateParams
        }

        holder.postDescription.text = post.description
        val formattedDate = post.date?.let {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
        }
        holder.dateView.text = formattedDate

        post.postId?.let { postId ->
            isPostSaved(postId) { isSaved ->
                updateSaveIcon(holder.saveIcon, isSaved)
            }
        }

        holder.saveIcon.setOnClickListener {
            listener.onSaveClick(post, holder.saveIcon)
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    fun updateData(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }

    private fun updateSaveIcon(saveIconView: ImageView, isSaved: Boolean) {
        if (isSaved) {
            saveIconView.setImageResource(R.drawable.ic_save)
        } else {
            saveIconView.setImageResource(R.drawable.ic_unsave)
        }
    }
}
