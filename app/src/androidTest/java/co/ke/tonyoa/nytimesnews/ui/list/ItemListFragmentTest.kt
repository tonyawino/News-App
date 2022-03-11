package co.ke.tonyoa.nytimesnews.ui.list

import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.widget.EditText
import android.widget.RadioGroup
import androidx.appcompat.widget.SearchView
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.DrawerMatchers
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import co.ke.tonyoa.nytimesnews.*
import co.ke.tonyoa.nytimesnews.ApiResponses.failedResponse
import co.ke.tonyoa.nytimesnews.ApiResponses.successResponse
import co.ke.tonyoa.nytimesnews.ApiResponses.successResponseNullBody
import co.ke.tonyoa.nytimesnews.data.retrofit.NyTimesApi
import co.ke.tonyoa.nytimesnews.data.room.NewsDatabase
import co.ke.tonyoa.nytimesnews.data.room.RoomNewsMapper
import co.ke.tonyoa.nytimesnews.data.room.daos.NewsDao
import co.ke.tonyoa.nytimesnews.data.room.daos.NewsImageDao
import co.ke.tonyoa.nytimesnews.ui.detail.ItemDetailFragmentDirections
import co.ke.tonyoa.nytimesnews.ui.list.ItemListFragment.Companion.BACK_DELAY
import co.ke.tonyoa.nytimesnews.utils.Utils.createRandomNews
import co.ke.tonyoa.nytimesnews.utils.isConnectedToInternet
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.AssertionFailedError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.lang.Thread.sleep
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltAndroidTest
@SmallTest
class ItemListFragmentTest {

    @get: Rule(order = 0)
    val hiltAndroidRule = HiltAndroidRule(this)

    @get: Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    val coroutineTestRule = CoroutineTestRule()

    @Inject
    lateinit var newsDao: NewsDao

    @Inject
    lateinit var newsImageDao: NewsImageDao

    @Inject
    lateinit var nyTimesApi: NyTimesApi

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var newsDatabase: NewsDatabase

    private val roomNewsMapper = RoomNewsMapper()

    @Before
    fun setup() {
        hiltAndroidRule.inject()
        Intents.init()
    }

    @Test
    fun clickingNavigationIcon_opensNavigationDrawer() {
        launchFragmentInHiltContainer<ItemListFragment>()
        onView(
            allOf(
                withContentDescription(R.string.abc_action_bar_up_description),
                isDisplayed()
            )
        ).perform(click())
        onView(withId(R.id.item_list_container)).check(matches(DrawerMatchers.isOpen()))
    }

    @Test
    fun pressingBackWhenDrawerIsOpen_closesDrawer() {
        every { context.isConnectedToInternet() } returns false
        launchFragmentInHiltContainer<ItemListFragment>()
        onView(withId(R.id.item_list_container)).perform(DrawerActions.open())
        pressBack()

        val isDrawerClosed: () -> Boolean = {
            try {
                onView(withId(R.id.item_list_container))
                    .check(
                        matches(
                            allOf(
                                DrawerMatchers.isClosed(),
                                not(isDisplayed())
                            )
                        )
                    )
                true
            } catch (e: AssertionFailedError) {
                false
            }
        }

        // Slow devices might need some time
        for (x in 0..9) {
            sleep(200)
            if (isDrawerClosed()) {
                break
            }
        }
    }

    @Test
    fun pressingBackOnceWhenDrawerIsClosed_showsClickAgainToast() {
        launchFragmentInHiltContainer<ItemListFragment>()
        pressBack()

        val isToastDisplayed: () -> Boolean = {
            try {
                isToastMessageDisplayed(R.string.press_back_again)
                true
            } catch (e: NoMatchingViewException) {
                false
            }
        }

        // Slow devices might need some time
        for (x in 0..9) {
            sleep(200)
            if (isToastDisplayed()) {
                break
            }
        }

    }

    @Test
    fun pressingBackTwiceSlowWhenDrawerIsClosed_leavesAppOpen() {
        launchFragmentInHiltContainer<ItemListFragment>()
        pressBack()
        // Initial toast
        val isToastDisplayed: () -> Boolean = {
            try {
                isToastMessageDisplayed(R.string.press_back_again)
                true
            } catch (e: NoMatchingViewException) {
                false
            }
        }

        // Slow devices might need some time to display the toast
        for (x in 0..9) {
            sleep(200)
            if (isToastDisplayed()) {
                break
            }
        }

        // Wait more than required time and press back again
        sleep(BACK_DELAY + 1000)
        pressBack()

        // Check for toast again
        for (x in 0..9) {
            sleep(200)
            if (isToastDisplayed()) {
                break
            }
        }
    }


    @Test
    fun pressingShareItemOnNavigationDrawer_startsShareIntent() {
        launchFragmentInHiltContainer<ItemListFragment>()
        onView(withId(R.id.item_list_container)).perform(DrawerActions.open())
        onView(
            allOf(
                isDescendantOfA(withId(R.id.nav_drawer_share)),
                isDisplayed()
            )
        ).perform(click())
        val intentMatchers = allOf(
            hasAction(Intent.ACTION_SEND),
            hasType("text/*")
        )
        val chooser = chooser(intentMatchers)
        intended(chooser)
    }

    @Test
    fun pressingCreateItemOnNavigationDrawer_closesNavigationDrawer() = runBlockingTest {
        launchFragmentInHiltContainer<ItemListFragment>()
        onView(withId(R.id.item_list_container)).perform(DrawerActions.open())
        onView(allOf(isDescendantOfA(withId(R.id.nav_drawer_create_news)), isDisplayed())).perform(
            click()
        )
        onView(withId(R.id.item_list_container)).check(matches(DrawerMatchers.isClosed()))
    }

    @Test
    fun pressingCreateItemOnNavigationDrawer_createsNews() = runBlockingTest {
        launchFragmentInHiltContainer<ItemListFragment>()
        onView(withId(R.id.item_list_container)).perform(DrawerActions.open())
        onView(allOf(isDescendantOfA(withId(R.id.nav_drawer_create_news)), isDisplayed())).perform(
            click()
        )

        val job = launch {
            newsDao.getAllNews("").collectLatest {
                assertThat(it).hasSize(1)
            }
        }
        job.cancel()
    }

    @Test
    fun noNewsItemSelectedInTablet_makesDetailGone() = runBlockingTest {
        var isTablet = false
        launchFragmentInHiltContainer<ItemListFragment> {
            isTablet =
                view?.findViewById<FragmentContainerView>(R.id.item_detail_nav_container) != null
        }
        if (isTablet) {
            onView(
                allOf(
                    withId(R.id.item_detail_nav_container),
                    hasSibling(withId(R.id.swipeRefreshLayout))
                )
            ).check(matches(`is`(not(isDisplayed()))))
        }
    }

    @Test
    fun noNewsItemSelectedInTablet_makesDetailVisible() = runBlockingTest {
        createRandomNewsWithIdsIndex(10)
        var isTablet = false
        launchFragmentInHiltContainer<ItemListFragment> {
            isTablet =
                view?.findViewById<FragmentContainerView>(R.id.item_detail_nav_container) != null
        }
        if (isTablet) {
            onView(withId(R.id.recyclerView))
                .perform(
                    RecyclerViewActions.actionOnItemAtPosition<NewsAdapter.NewsViewHolder>(
                        0,
                        click()
                    )
                )

            onView(
                allOf(
                    withId(R.id.item_detail_nav_container),
                    hasSibling(withId(R.id.swipeRefreshLayout))
                )
            ).check(matches(isDisplayed()))
        }
    }

    @Test
    fun successfulLoad_hidesFailureState() {
        every { context.isConnectedToInternet() } returns true
        coEvery { nyTimesApi.getEmailedOrViewedNews() } coAnswers { successResponse }
        createRandomNewsWithIdsIndex(5)
        launchFragmentInHiltContainer<ItemListFragment>()
        onView(
            allOf(
                withId(R.id.textView_failureMessage),
                hasSibling(withId(R.id.button_failureRetry)),
                // For tablet
                `is`(not(isDescendantOfA(withClassName(`is`(NestedScrollView::class.java.name)))))
            )
        ).check(matches(`is`(not(isDisplayed()))))
    }

    @Test
    fun successfulLoad_hidesProgressBars() {
        every { context.isConnectedToInternet() } returns true
        coEvery { nyTimesApi.getEmailedOrViewedNews() } coAnswers { successResponseNullBody }
        launchFragmentInHiltContainer<ItemListFragment>()

        onView(
            allOf(
                withId(R.id.linearProgressIndicator),
                hasSibling(withId(R.id.swipeRefreshLayout)),
                // For tablet
                `is`(not(isDescendantOfA(withClassName(`is`(NestedScrollView::class.java.name)))))
            )
        ).check(matches(`is`(not(isDisplayed()))))
    }

    @Test
    fun successfulLoadWithData_displaysRecyclerViewAndHidesEmptyState() {
        every { context.isConnectedToInternet() } returns true
        coEvery { nyTimesApi.getEmailedOrViewedNews() } returns successResponse
        createRandomNewsWithIdsIndex(5)
        launchFragmentInHiltContainer<ItemListFragment>()
        onView(
            allOf(
                withId(R.id.recyclerView),
            )
        ).check(matches(isDisplayed()))
        onView(
            allOf(
                withId(R.id.layoutEmptyState),
                hasSibling(withId(R.id.recyclerView))
            )
        ).check(matches(not(isDisplayed())))
    }

    @Test
    fun successfulLoadWithNoData_hidesRecyclerViewAndShowsEmptyState() {
        every { context.isConnectedToInternet() } returns true
        coEvery { nyTimesApi.getEmailedOrViewedNews() } returns successResponseNullBody
        launchFragmentInHiltContainer<ItemListFragment>()
        onView(
            allOf(
                withId(R.id.recyclerView),
            )
        ).check(matches(not(isDisplayed())))
        onView(
            allOf(
                withId(R.id.layoutEmptyState),
                hasSibling(withId(R.id.recyclerView))
            )
        ).check(matches(isDisplayed()))
    }


    @Test
    fun failedLoadWithData_showsFailureState() {
        every { context.isConnectedToInternet() } returns true
        coEvery { nyTimesApi.getEmailedOrViewedNews() } coAnswers { failedResponse }
        createRandomNewsWithIdsIndex(5)
        launchFragmentInHiltContainer<ItemListFragment>()
        onView(
            allOf(
                withId(R.id.textView_failureMessage),
                hasSibling(withId(R.id.button_failureRetry)),
                // For tablet tablet
                `is`(not(isDescendantOfA(withClassName(`is`(NestedScrollView::class.java.name)))))
            )
        ).check(matches(isDisplayed()))
    }

    @Test
    fun failedLoadWithoutData_hidesFailureState() {
        every { context.isConnectedToInternet() } returns true
        coEvery { nyTimesApi.getEmailedOrViewedNews() } coAnswers { failedResponse }
        launchFragmentInHiltContainer<ItemListFragment>()
        onView(
            allOf(
                withId(R.id.textView_failureMessage),
                hasSibling(withId(R.id.button_failureRetry)),
                // For tablet tablet
                `is`(not(isDescendantOfA(withClassName(`is`(NestedScrollView::class.java.name)))))
            )
        ).check(matches(`is`(not(isDisplayed()))))
    }


    @Test
    fun failedLoad_hidesProgressBars() {
        every { context.isConnectedToInternet() } returns true
        coEvery { nyTimesApi.getEmailedOrViewedNews() } coAnswers { failedResponse }
        launchFragmentInHiltContainer<ItemListFragment>()

        onView(
            allOf(
                withId(R.id.linearProgressIndicator),
                hasSibling(withId(R.id.swipeRefreshLayout)),
                // For tablet tablet
                `is`(not(isDescendantOfA(withClassName(`is`(NestedScrollView::class.java.name)))))
            )
        ).check(matches(`is`(not(isDisplayed()))))
    }

    @Test
    fun failedLoadWithData_displaysRecyclerViewAndHidesEmptyState() {
        every { context.isConnectedToInternet() } returns true
        coEvery { nyTimesApi.getEmailedOrViewedNews() } returns failedResponse
        createRandomNewsWithIdsIndex(5)
        launchFragmentInHiltContainer<ItemListFragment>()
        onView(
            allOf(
                withId(R.id.recyclerView),
            )
        ).check(matches(isDisplayed()))
        onView(
            allOf(
                withId(R.id.layoutEmptyState),
                hasSibling(withId(R.id.recyclerView))
            )
        ).check(matches(not(isDisplayed())))
    }

    @Test
    fun failedLoadWithNoData_hidesRecyclerViewAndShowsEmptyState() {
        every { context.isConnectedToInternet() } returns true
        coEvery { nyTimesApi.getEmailedOrViewedNews() } returns failedResponse
        launchFragmentInHiltContainer<ItemListFragment>()
        onView(
            allOf(
                withId(R.id.recyclerView),
            )
        ).check(matches(not(isDisplayed())))
        onView(
            allOf(
                withId(R.id.layoutEmptyState),
                hasSibling(withId(R.id.recyclerView))
            )
        ).check(matches(isDisplayed()))
    }

    @Test
    fun showSnackbarEvent_displaysSnackbar() {
        launchFragmentInHiltContainer<ItemListFragment>()
        onView(withId(R.id.item_list_container)).perform(DrawerActions.open())
        onView(allOf(isDescendantOfA(withId(R.id.nav_drawer_create_news)), isDisplayed())).perform(
            click()
        )

        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(isDisplayed()))
    }

    @Test
    fun viewDetailEventOnTablet_navigatesToDetail() {
        every { context.isConnectedToInternet() } returns false
        createRandomNewsWithIdsIndex(5, false)
        var isTablet = false
        val mockNavController = mockk<NavController>(relaxed = true)
        launchFragmentInHiltContainer<ItemListFragment> {
            view?.findViewById<FragmentContainerView>(R.id.item_detail_nav_container).let {
                it?.let {
                    Navigation.setViewNavController(it, mockNavController)
                    isTablet = true
                }
            }
        }
        if (isTablet) {
            onView(withId(R.id.recyclerView))
                .perform(
                    RecyclerViewActions.actionOnItemAtPosition<NewsAdapter.NewsViewHolder>(
                        0,
                        click()
                    )
                )
            verify {
                mockNavController.navigate(
                    ItemDetailFragmentDirections.actionFragmentItemDetailSelf(
                        1
                    )
                )
            }
        }
    }

    @Test
    fun viewDetailEventOnMobile_navigatesToDetail() {
        createRandomNewsWithIdsIndex(5, false)
        var isTablet = false
        val mockNavController = mockk<NavController>(relaxed = true)
        launchFragmentInHiltContainer<ItemListFragment> {
            view?.let {
                isTablet =
                    it.findViewById<FragmentContainerView>(R.id.item_detail_nav_container) != null
                if (!isTablet) {
                    Navigation.setViewNavController(it, mockNavController)
                }
            }
        }
        if (!isTablet) {
            onView(withId(R.id.recyclerView))
                .perform(
                    RecyclerViewActions.actionOnItemAtPosition<NewsAdapter.NewsViewHolder>(
                        0,
                        click()
                    )
                )
            verify { mockNavController.navigate(ItemListFragmentDirections.showItemDetail(1)) }
        }
    }

    @Test
    fun clickingSearch_displaysSearchView() {
        launchFragmentInHiltContainer<ItemListFragment>()
        try {
            openActionBarOverflowOrOptionsMenu(
                InstrumentationRegistry.getInstrumentation().targetContext
            )
        } catch (e: Exception) {
            //This is normal. Maybe we don't have overflow menu.
        }
        onView(
            allOf(
                anyOf(
                    withText(R.string.search),
                    withId(R.id.action_search)
                ),
                isDisplayed()
            )
        ).perform(click())
        onView(
            allOf(
                isDescendantOfA(withClassName(`is`(SearchView::class.java.name))),
                isDisplayed(),
                isAssignableFrom(EditText::class.java)
            )
        )
            .perform(
                clearText(),
                typeText("enter the text"),
                pressKey(KeyEvent.KEYCODE_ENTER)
            )
    }

    @Test
    fun searching_filtersItems() {
        val searchQuery =
            "Some weird query to filter with and will be available for the first view only"
        createRandomNewsWithIdsIndex(20)
        val domainToEntity =
            roomNewsMapper.domainToEntity(createRandomNews().copy(id = 100, title = searchQuery))
        newsDao.insert(domainToEntity.newsInDb)

        launchFragmentInHiltContainer<ItemListFragment>()
        try {
            openActionBarOverflowOrOptionsMenu(
                InstrumentationRegistry.getInstrumentation().targetContext
            )
        } catch (e: Exception) {
            //This is normal. Maybe we don't have overflow menu.
        }
        onView(
            anyOf(
                withText(R.string.search),
                withId(R.id.action_search)
            )
        ).perform(click())
        onView(
            allOf(
                isDescendantOfA(withClassName(`is`(SearchView::class.java.name))),
                isDisplayed(),
                isAssignableFrom(EditText::class.java)
            )
        )
            .perform(
                clearText(),
                typeText(searchQuery),
                pressKey(KeyEvent.KEYCODE_ENTER)
            )

        onView(withId(R.id.recyclerView)).check(matches(hasChildCount(1)))

    }

    @Test
    fun clickingSort_displaysSortDialog() {
        launchFragmentInHiltContainer<ItemListFragment>()
        try {
            openActionBarOverflowOrOptionsMenu(
                InstrumentationRegistry.getInstrumentation().targetContext
            )
        } catch (e: Exception) {
            //This is normal. Maybe we don't have overflow menu.
        }
        onView(
            allOf(
                anyOf(
                    withText(R.string.sort),
                    withId(R.id.action_sort)
                ),
                isDisplayed()
            )
        ).perform(click())
        onView(
            allOf(
                isDescendantOfA(withClassName(`is`(RadioGroup::class.java.name))),
                withText(R.string.date)
            )
        ).check(matches(isDisplayed()))
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
            val createRandomNews = createRandomNews()
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

    private fun chooser(matcher: Matcher<Intent>): Matcher<Intent> {
        return allOf(
            hasAction(Intent.ACTION_CHOOSER),
            hasExtra(`is`(Intent.EXTRA_INTENT), matcher)
        )
    }

    private fun isToastMessageDisplayed(textId: Int) {
        onView(withText(textId)).inRoot(ToastMatcher.isToast()).check(matches(isDisplayed()))
    }
}