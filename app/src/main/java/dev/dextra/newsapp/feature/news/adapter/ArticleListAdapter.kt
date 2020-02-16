package dev.dextra.newsapp.feature.news.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.dextra.newsapp.R
import dev.dextra.newsapp.api.model.Article
import kotlinx.android.synthetic.main.item_article.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class ArticleListAdapter(val listener: ArticleListAdapterItemListener) :
    RecyclerView.Adapter<ArticleListAdapter.ArticleListAdapterViewHolder>() {

    private val dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT)
    private val parseFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    private var dataset: ArrayList<Article> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleListAdapterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_article, parent, false)
        return ArticleListAdapterViewHolder(view)
    }

    override fun getItemCount(): Int = dataset.size

    override fun onBindViewHolder(holder: ArticleListAdapterViewHolder, position: Int) {
        val article = dataset[position]

        holder.view.setOnClickListener { listener.onClick(article) }

        holder.view.article_name.text = article.title
        holder.view.article_description.text = article.description
        holder.view.article_author.text = article.author
        holder.view.article_date.text = dateFormat.format(parseFormat.parse(article.publishedAt)!!)
    }

    fun set(articles: List<Article>) {
        dataset.clear()
        dataset.addAll(articles)
    }

    class ArticleListAdapterViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    interface ArticleListAdapterItemListener {
        fun onClick(article: Article)
    }
}