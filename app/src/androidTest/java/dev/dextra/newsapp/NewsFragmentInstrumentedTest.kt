package dev.dextra.newsapp

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.dextra.newsapp.api.model.ArticlesResponse
import dev.dextra.newsapp.api.model.Source
import dev.dextra.newsapp.base.BaseInstrumentedTest
import dev.dextra.newsapp.base.FileUtils
import dev.dextra.newsapp.base.TestSuite
import dev.dextra.newsapp.base.mock.endpoint.ResponseHandler
import dev.dextra.newsapp.feature.news.NewsFragment
import dev.dextra.newsapp.feature.news.NewsFragmentArgs
import dev.dextra.newsapp.feature.news.adapter.ArticleListAdapter
import dev.dextra.newsapp.utils.JsonUtils
import okhttp3.Request
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class NewsFragmentInstrumentedTest : BaseInstrumentedTest() {

    private lateinit var scenario: FragmentScenario<NewsFragment>
    private val navController: NavController = mock(NavController::class.java)

    private val source = Source(
        "general",
        "us",
        "Your trusted source for breaking news, analysis, exclusive interviews, headlines, and videos at ABCNews.com.",
        "abc-news",
        "en",
        "ABC News",
        "https://abcnews.go.com"
    )
    private val articlesResponse = loadArticlesResponseFromFile()
    private val emptyResponse = ArticlesResponse(listOf(), "ok", 0)

    @Before
    fun setupTest() {
        scenario = launchFragmentInContainer<NewsFragment>(NewsFragmentArgs(source).toBundle(), R.style.AppTheme)
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
    }

    @Test
    fun testShowArticles() {
        // Mock paged response handler
        var loadedArticles = 0
        TestSuite.mock(TestConstants.newsURL).body(object : ResponseHandler {
            override fun getResponse(request: Request, path: String): String {
                val page = request.url().queryParameter("page")!!.toInt() - 1
                val pageSize = request.url().queryParameter("pageSize")!!.toInt()

                val start = page * pageSize
                val end = start + pageSize

                val articles = when {
                    start >= articlesResponse.totalResults-> listOf()
                    end > articlesResponse.totalResults -> articlesResponse.articles.subList(start, articlesResponse.totalResults)
                    else -> articlesResponse.articles.subList(start, end)
                }
                loadedArticles = start + articles.size

                return JsonUtils.toJson(articlesResponse.copy(articles))
            }
        }).apply()

        scenario.recreate()
        waitLoading()

        // Scrolls to the RecyclerView, loading all articles pages
        var position: Int
        do {
            position = loadedArticles
            onView(withId(R.id.news_list)).perform(RecyclerViewActions.scrollToPosition<ArticleListAdapter.ArticleListAdapterViewHolder>(position - 1))
            waitLoading()
        } while(position < articlesResponse.totalResults)

        // Check if news list is displayed and empty and error states are hidden
        onView(withId(R.id.news_list)).check(matches(isDisplayed()))
        onView(withId(R.id.error_state)).check(matches(not(isDisplayed())))
        onView(withId(R.id.empty_state)).check(matches(not(isDisplayed())))

        // Check if last loaded article is displayed
        val lastArticle = articlesResponse.articles.last()
        onView(withText(lastArticle.description)).check(matches(isDisplayed()))
    }

    @Test
    fun testSelectArticle() {
        val article = articlesResponse.articles.first()

        Intents.init()

        // Click an article
        waitLoading()
        onView(withText(article.description)).perform(click())

        // Check for URL view intent
        intended(allOf(hasAction(Intent.ACTION_VIEW), hasData(Uri.parse(article.url))))
        Intents.release()
    }

    @Test
    fun testShowEmpty() {
        TestSuite.mock(TestConstants.newsURL).body(object : ResponseHandler {
            override fun getResponse(request: Request, path: String): String {
                return JsonUtils.toJson(emptyResponse)
            }
        }).apply()

        scenario.recreate()
        waitLoading()

        // Check if the empty state is displayed with the correct item and the news list and error state are hidden
        onView(withId(R.id.empty_state)).check(matches(isDisplayed()))
        onView(withId(R.id.news_list)).check(matches(not(isDisplayed())))
        onView(withId(R.id.error_state)).check(matches(not(isDisplayed())))
    }

    @Test
    fun testShowError() {
        TestSuite.mock(TestConstants.newsURL).body(object : ResponseHandler {
            override fun getResponse(request: Request, path: String): String {
                throw RuntimeException()
            }
        }).apply()

        scenario.recreate()
        waitLoading()

        // Check if the error state is displayed with the correct item and the source list and empty state are hidden
        onView(withId(R.id.error_state)).check(matches(isDisplayed()))
        onView(withId(R.id.news_list)).check(matches(not(isDisplayed())))
        onView(withId(R.id.empty_state)).check(matches(not(isDisplayed())))

        // Clear endpoint mock and retry
        TestSuite.clearEndpointMocks()
        onView(withId(R.id.error_state_retry)).perform(click())
        waitLoading()

        // Check if news list is displayed and empty and error states are hidden
        onView(withId(R.id.news_list)).check(matches(isDisplayed()))
        onView(withId(R.id.error_state)).check(matches(not(isDisplayed())))
        onView(withId(R.id.empty_state)).check(matches(not(isDisplayed())))
    }

    private fun loadArticlesResponseFromFile(): ArticlesResponse {
        val jsonData = FileUtils.readJson(TestConstants.newsURL.substring(1) + ".json")!!
        return JsonUtils.getGson().fromJson(jsonData, ArticlesResponse::class.java)
    }
}