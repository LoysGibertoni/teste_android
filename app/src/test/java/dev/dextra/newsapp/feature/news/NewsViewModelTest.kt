package dev.dextra.newsapp.feature.news

import dev.dextra.newsapp.TestConstants
import dev.dextra.newsapp.api.model.ArticlesResponse
import dev.dextra.newsapp.api.model.Source
import dev.dextra.newsapp.base.BaseTest
import dev.dextra.newsapp.base.NetworkState
import dev.dextra.newsapp.base.TestSuite
import dev.dextra.newsapp.base.getOrAwaitValue
import dev.dextra.newsapp.feature.news.NewsViewModel
import dev.dextra.newsapp.utils.JsonUtils
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.koin.test.get


class NewsViewModelTest : BaseTest() {

    private val emptyResponse = ArticlesResponse(ArrayList(), "ok", 0)
    private val source = Source(
        "general",
        "us",
        "Your trusted source for breaking news, analysis, exclusive interviews, headlines, and videos at ABCNews.com.",
        "abc-news",
        "en",
        "ABC News",
        "https://abcnews.go.com"
    )

    lateinit var viewModel: NewsViewModel

    @Before
    fun setupTest() {
        viewModel = TestSuite.get()
    }

    @Test
    fun testGetNews() {
        viewModel.loadNews(source)

        assert(viewModel.articles.getOrAwaitValue().size == 11)
        assertEquals(NetworkState.SUCCESS, viewModel.networkState.getOrAwaitValue())

        viewModel.onCleared()

        assert(viewModel.getDisposables().isEmpty())
    }

    @Test
    fun testEmptyNews() {
        TestSuite.mock(TestConstants.newsURL).body(JsonUtils.toJson(emptyResponse)).apply()

        viewModel.loadNews(source)

        assert(viewModel.articles.getOrAwaitValue().isEmpty())
        assertEquals(NetworkState.EMPTY, viewModel.networkState.getOrAwaitValue())
    }

    @Test
    fun testErrorNews() {
        TestSuite.mock(TestConstants.newsURL).throwConnectionError().apply()

        viewModel.loadNews(source)

        assert(viewModel.articles.getOrAwaitValue().isEmpty())
        assertEquals(NetworkState.ERROR, viewModel.networkState.getOrAwaitValue())
    }
}