package co.ke.tonyoa.nytimesnews.ui.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.ke.tonyoa.nytimesnews.R
import co.ke.tonyoa.nytimesnews.databinding.ItemImageBinding
import co.ke.tonyoa.nytimesnews.domain.models.NewsImage
import com.bumptech.glide.Glide

class NewsImageImageAdapter(val onItemClickListener: (Int, NewsImage) -> Unit) :
    ListAdapter<NewsImage, NewsImageImageAdapter.NewsImageViewHolder>(object :
        DiffUtil.ItemCallback<NewsImage>() {
        override fun areItemsTheSame(oldItem: NewsImage, newItem: NewsImage): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: NewsImage, newItem: NewsImage): Boolean {
            return oldItem == newItem
        }
    }) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsImageViewHolder {
        return NewsImageViewHolder(
            ItemImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: NewsImageViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class NewsImageViewHolder(private val itemImageBinding: ItemImageBinding) :
        RecyclerView.ViewHolder(itemImageBinding.root) {
        init {
            itemView.setOnClickListener {
                onItemClickListener(bindingAdapterPosition, getItem(bindingAdapterPosition))
            }
        }

        fun bind(position: Int) {
            val newsImage = getItem(position)
            Glide.with(itemView.context)
                .load(newsImage.url)
                .placeholder(R.drawable.ic_news_icon)
                .error(R.drawable.ic_news_icon)
                .into(itemImageBinding.imageView)
            itemImageBinding.textViewCaption.text = newsImage.caption
            itemImageBinding.textViewCopyright.text = newsImage.copyright
        }
    }
}