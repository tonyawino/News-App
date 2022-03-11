package co.ke.tonyoa.nytimesnews.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.ke.tonyoa.nytimesnews.R
import co.ke.tonyoa.nytimesnews.databinding.ItemListContentBinding
import co.ke.tonyoa.nytimesnews.domain.models.News
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class NewsAdapter(val onItemClickListener: (Int, News) -> Unit) :
    ListAdapter<News, NewsAdapter.NewsViewHolder>(object :
        DiffUtil.ItemCallback<News>() {
        override fun areItemsTheSame(oldItem: News, newItem: News): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: News, newItem: News): Boolean {
            return oldItem == newItem
        }
    }) {

    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        return NewsViewHolder(
            ItemListContentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class NewsViewHolder(private val itemListContentBinding: ItemListContentBinding) :
        RecyclerView.ViewHolder(itemListContentBinding.root) {
        init {
            itemView.setOnClickListener {
                onItemClickListener(bindingAdapterPosition, getItem(bindingAdapterPosition))
            }
        }

        fun bind(position: Int) {
            val news = getItem(position)
            if (news.images.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(news.images[0].url)
                    .circleCrop()
                    .placeholder(R.drawable.ic_news_icon)
                    .error(R.drawable.ic_news_icon)
                    .into(itemListContentBinding.imageViewFeature)
            }
            itemListContentBinding.textViewTitle.text = news.title
            itemListContentBinding.textViewAuthor.text = news.author
            itemListContentBinding.textViewDate.text = simpleDateFormat.format(news.publishDate)
        }
    }
}