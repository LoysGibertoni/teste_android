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
import dev.dextra.newsapp.R
import dev.dextra.newsapp.api.model.Article
import dev.dextra.newsapp.base.BaseListFragment
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        newsViewModel.loadNews(args.source)
    }

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
        setupObservers()
    }

    private fun setupList() {
        news_list.adapter = viewAdapter
    }

    private fun setupObservers() {
        newsViewModel.articles.observe(this, Observer {
            viewAdapter.submitList(it)
        })

        newsViewModel.networkState.observe(this, networkStateObserver)
    }

    override fun onClick(article: Article) {
        Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(article.url)
            startActivity(this)
        }
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
        newsViewModel.retryLoad()
    }
}
