package com.bookcrossing.mobile.ui.search


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.models.SearchHitBook
import com.bookcrossing.mobile.modules.GlideApp
import com.bookcrossing.mobile.ui.search.SearchHitBooksAdapter.ViewHolder
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.google.firebase.storage.FirebaseStorage

class SearchHitBooksAdapter : PagedListAdapter<SearchHitBook, ViewHolder>(SearchHitBooksAdapter) {

  private val books = mutableListOf<SearchHitBook>()
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.hits_item, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val item = books[position]
    holder.bind(item)

  }

  override fun getItemCount(): Int = books.size

  inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    @BindView(R.id.search_hit_cover)
    lateinit var cover: ImageView

    init {
      ButterKnife.bind(itemView)
    }

    fun bind(book: SearchHitBook) {
      GlideApp.with(itemView.context)
        .load(FirebaseStorage.getInstance().getReference(book.key + ".jpg"))
        .placeholder(R.drawable.ic_book_cover_placeholder).transition(withCrossFade())
        .into(cover)
    }
  }

  companion object : DiffUtil.ItemCallback<SearchHitBook>() {
    override fun areItemsTheSame(oldItem: SearchHitBook, newItem: SearchHitBook): Boolean {
      return oldItem.key == newItem.key
    }

    override fun areContentsTheSame(oldItem: SearchHitBook, newItem: SearchHitBook): Boolean {
      return oldItem == newItem
    }

  }
}
