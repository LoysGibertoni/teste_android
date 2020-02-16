package dev.dextra.newsapp.feature.news

import addListValues
import androidx.lifecycle.MutableLiveData
import dev.dextra.newsapp.api.model.Article
import dev.dextra.newsapp.api.model.Source
import dev.dextra.newsapp.api.repository.NewsRepository
import dev.dextra.newsapp.base.BaseViewModel
import dev.dextra.newsapp.base.NetworkState


class NewsViewModel(private val newsRepository: NewsRepository) : BaseViewModel() {

    val articles = MutableLiveData<ArrayList<Article>>()
    val networkState = MutableLiveData<NetworkState>()

    private var selectedSource: Source? = null
    private var retryPage: Int? = null

    fun configureSource(source: Source) {
        this.selectedSource = source
    }

    fun loadNews(page: Int = retryPage ?: 1) {
        networkState.postValue(NetworkState.RUNNING)
        retryPage = null
        addDisposable(
            newsRepository.getEverything(selectedSource!!.id, page).subscribe({ response ->
                articles.addListValues(response.articles)
                networkState.postValue(NetworkState.SUCCESS)
            }, {
                retryPage = page
                networkState.postValue(NetworkState.ERROR)
            })
        )
    }
}