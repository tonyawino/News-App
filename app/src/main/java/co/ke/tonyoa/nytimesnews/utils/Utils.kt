package co.ke.tonyoa.nytimesnews.utils

import co.ke.tonyoa.nytimesnews.domain.models.News
import co.ke.tonyoa.nytimesnews.domain.models.NewsImage
import java.util.*
import kotlin.random.Random

object Utils {

    fun createRandomNews(): News {
        val titles = listOf(
            "Who let the dogs out", "Another Title", "My Title", "Whose Title",
            "Another Title", "Get Your title", "Be for Titles", "I have no ideas"
        )
        val newsAbstracts =
            titles.map { "$it the abstract of the story which I do not know" }.shuffled()
        val calendar = Calendar.getInstance()
        val dates =
            (1..30).map { calendar.set(Calendar.DAY_OF_MONTH, it); calendar.time }.shuffled()
        val categories = listOf(
            "Sports", "Technology", "History", "Fashion", "Finance", "Entertainment",
            "Science", "Education", "Kids"
        ).shuffled()
        val authors = listOf(
            "Martha",
            "Kariuku",
            "Mangai",
            "Nani",
            "Leta",
            "Mama",
            "Yule",
            "Wapi"
        ).map { "By $it" }.shuffled()
        val sources = listOf(
            "Si Ni mi nakushow",
            "We niamini",
            "Nimewahi kudanganya?",
            "Kwani hunitrust?",
            "Trust Me"
        ).shuffled()
        val url = "https://www.nytimes.com/2022/03/02/us/politics/russia-ukraine-china.html"

        val images = (1 until authors.size)
            .map {
                NewsImage(
                    "$it ${authors[it]}", "${it - 1} ${authors[it - 1]}",
                    "https://kotlinlang.org/assets/images/open-graph/general.png"
                )
            }.shuffled()

        return News(
            15,
            titles[Random.nextInt(titles.size)],
            newsAbstracts[Random.nextInt(newsAbstracts.size)],
            dates[Random.nextInt(dates.size)],
            categories[Random.nextInt(categories.size)],
            authors[Random.nextInt(authors.size)],
            sources[Random.nextInt(sources.size)],
            url,
            listOf(images[Random.nextInt(images.size)])
        )
    }
}