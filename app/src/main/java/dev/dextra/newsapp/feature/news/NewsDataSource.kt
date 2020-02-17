package dev.dextra.newsapp.feature.news

import androidx.lifecycle.MutableLiveData
import androidx.paging.PositionalDataSource
import dev.dextra.newsapp.api.model.Article
import dev.dextra.newsapp.api.model.Source
import dev.dextra.newsapp.api.repository.NewsRepository
import dev.dextra.newsapp.base.NetworkState
import io.reactivex.disposables.Disposable

class NewsDataSource(
    private val source: Source,
    private val newsRepository: NewsRepository,
    private val addDisposable: (Disposable) -> Unit
) : PositionalDataSource<Article>() {

    val networkState = MutableLiveData<NetworkState>()
    var failedAction: (() -> Unit)? = null

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Article>) {
        val page = calculatePage(params.startPosition, params.loadSize)
        val pageSize = params.loadSize
        networkState.postValue(NetworkState.RUNNING)
        addDisposable(
            newsRepository.getEverything(source.id, page, pageSize).subscribe({ response ->
                callback.onResult(response.articles)
                networkState.postValue(NetworkState.SUCCESS)
            }, {
                failedAction = { loadRange(params, callback) }
                networkState.postValue(NetworkState.ERROR)
            })
        )
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Article>) {
        val page = calculatePage(params.requestedStartPosition, params.requestedLoadSize)
        val pageSize = params.requestedLoadSize
        networkState.postValue(NetworkState.RUNNING)
        addDisposable(
            newsRepository.getEverything(source.id, page, pageSize).subscribe({ response ->
                callback.onResult(response.articles, params.requestedStartPosition, response.totalResults)
                networkState.postValue(if (response.articles.isEmpty()) NetworkState.EMPTY else NetworkState.SUCCESS)
            }, {
                failedAction = { loadInitial(params, callback) }
                networkState.postValue(NetworkState.ERROR)
            })
        )
    }

    private fun calculatePage(startPosition: Int, loadSize: Int): Int = startPosition / loadSize + 1

    fun retry() {
        failedAction?.invoke()
        failedAction = null
    }
}