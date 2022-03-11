package co.ke.tonyoa.nytimesnews.end2end

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import co.ke.tonyoa.nytimesnews.CoroutineTestRule
import co.ke.tonyoa.nytimesnews.R
import co.ke.tonyoa.nytimesnews.data.room.NewsDatabase
import co.ke.tonyoa.nytimesnews.data.room.RoomNewsMapper
import co.ke.tonyoa.nytimesnews.data.room.daos.NewsDao
import co.ke.tonyoa.nytimesnews.data.room.daos.NewsImageDao
import co.ke.tonyoa.nytimesnews.ui.MainActivity
import co.ke.tonyoa.nytimesnews.ui.list.NewsAdapter
import co.ke.tonyoa.nytimesnews.utils.Utils
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LargeTest
class EndToEndTest {

    @get:Rule(order = 0)
    val hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @get: Rule(order = 1)
    val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Inject
    lateinit var newsDao: NewsDao

    @Inject
    lateinit var newsImageDao: NewsImageDao

    @Inject
    lateinit var newsDatabase: NewsDatabase

    private val roomNewsMapper = RoomNewsMapper()

    @Before
    fun setup() {
        hiltAndroidRule.inject()
    }

    @Test
    fun itemInRecyclerClick_navigatesToViewDetails() {
        createRandomNewsWithIdsIndex(10)
        onView(withId(R.id.recyclerView))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<NewsAdapter.NewsViewHolder>(
                    0,
                    click()
                )
            )

        onView(withId(R.id.linearLayout_details))
            .check(matches(isDisplayed()))
    }

    @After
    fun teardown() {
        Intents.release()
        if (newsDatabase.isOpen)
            newsDatabase.close()
    }


    private fun createRandomNewsWithIdsIndex(count: Int, randomDate: Boolean = true) {
        var date = Date()
        newsDao.insert((1..count).map {
            val createRandomNews = Utils.createRandomNews()
            if (randomDate)
                date = createRandomNews.publishDate
            val domainToEntity =
                roomNewsMapper.domainToEntity(
                    createRandomNews.copy(
                        id = it.toLong(),
                        publishDate = date
                    )
                )
            domainToEntity.newsInDb
        })
    }

}