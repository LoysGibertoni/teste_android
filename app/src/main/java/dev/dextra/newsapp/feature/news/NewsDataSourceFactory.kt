package dev.dextra.newsapp.feature.news

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import dev.dextra.newsapp.api.model.Article
import dev.dextra.newsapp.api.model.Source
import dev.dextra.newsapp.api.repository.NewsRepository
import io.reactivex.disposables.Disposable

class NewsDataSourceFactory(
    private val source: Source,
    private val newsRepository: NewsRepository,
    private val addDisposable: (Disposable) -> Unit
) : DataSource.Factory<Int, Article>() {

    val dataSource = MutableLiveData<NewsDataSource>()

    override fun create(): DataSource<Int, Article> {
        val newsDataSource = NewsDataSource(source, newsRepository, addDisposable)
        dataSource.postValue(newsDataSource)
        return newsDataSource
    }

}