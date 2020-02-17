package dev.dextra.newsapp.feature.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import dev.dextra.newsapp.api.model.Article
import dev.dextra.newsapp.api.model.Source
import dev.dextra.newsapp.api.repository.NewsRepository
import dev.dextra.newsapp.base.BaseViewModel
import dev.dextra.newsapp.base.NetworkState

private const val PAGE_SIZE = 20

class NewsViewModel(private val newsRepository: NewsRepository) : BaseViewModel() {

    private lateinit var dataSource: LiveData<NewsDataSource>
    lateinit var articles: LiveData<PagedList<Article>>
    lateinit var networkState: LiveData<NetworkState>

    fun loadNews(source: Source) {
        val config = PagedList.Config.Builder()
            .setPageSize(PAGE_SIZE)
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(2 * PAGE_SIZE)
            .build()
        val factory = NewsDataSourceFactory(source, newsRepository, ::addDisposable)

        dataSource = factory.dataSource
        articles = LivePagedListBuilder<Int, Article>(factory, config).build()
        networkState = Transformations.switchMap(factory.dataSource, NewsDataSource::networkState)
    }

    fun retryLoad() {
        dataSource.value?.retry()
    }
}
