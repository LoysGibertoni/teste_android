package dev.dextra.newsapp.feature.news

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.dextra.newsapp.R
import dev.dextra.newsapp.api.model.Article
import dev.dextra.newsapp.api.model.Source
import dev.dextra.newsapp.base.BaseListFragment
import dev.dextra.newsapp.components.LoadPageScrollListener
import dev.dextra.newsapp.feature.news.adapter.ArticleListAdapter
import kotlinx.android.synthetic.main.fragment_news.*
import org.koin.android.viewmodel.ext.android.viewModel


class NewsFragment : BaseListFragment(), ArticleListAdapter.ArticleListAdapterItemListener {

    override val emptyStateTitle: Int = R.string.empty_state_title_news
    override val emptyStateSubTitle: Int = R.string.empty_state_subtitle_news
    override val errorStateTitle: Int = R.string.error_state_title_news
    override val errorStateSubTitle: Int = R.string.error_state_subtitle_news
    override val mainList: View
        get() = news_list

    private val newsViewModel: NewsViewModel by viewModel()

    private var viewAdapter: ArticleListAdapter = ArticleListAdapter(this)

    val args by navArgs<NewsFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupList()
        setupObservers(args.source)
    }

    private fun setupList() {
        news_list.apply {
            adapter = viewAdapter
            addOnScrollListener(LoadPageScrollListener(object : LoadPageScrollListener.LoadPageScrollLoadMoreListener {
                override fun onLoadMore(
                    currentPage: Int,
                    totalItemCount: Int,
                    recyclerView: RecyclerView
                ) {
                    newsViewModel.loadNews(currentPage)
                }
            }))
        }
    }

    private fun setupObservers(source: Source) {
        newsViewModel.articles.observe(this, Observer {
            viewAdapter.apply {
                val count = itemCount
                set(it)
                notifyItemRangeInserted(itemCount, it.size - count)
            }
        })

        newsViewModel.networkState.observe(this, networkStateObserver)

        newsViewModel.configureSource(source)
        newsViewModel.loadNews()
    }

    override fun onClick(article: Article) {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(article.url)
        startActivity(i)
    }

    override fun setupLandscape() {
        setListColumns(2)
    }

    override fun setupPortrait() {
        setListColumns(1)
    }

    private fun setListColumns(columns: Int) {
        val layoutManager = news_list.layoutManager
        if (layoutManager is GridLayoutManager) {
            layoutManager.spanCount = columns
            viewAdapter.notifyDataSetChanged()
        }
    }

    override fun executeRetry() {
        newsViewModel.loadNews()
    }
}
