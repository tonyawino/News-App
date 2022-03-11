package co.ke.tonyoa.nytimesnews.data.retrofit

import co.ke.tonyoa.nytimesnews.BuildConfig
import co.ke.tonyoa.nytimesnews.data.retrofit.models.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NyTimesApi {

    /**
     * @param newsType: The type of popular news to filter with i.e. emailed or viewed
     * @param period: The specified period of time i.e. 1 day, 7 days, or 30 days
     * @param apiKey: The apiKey
     */
    @GET("/svc/mostpopular/v2/{newsType}/{period}.json")
    suspend fun getEmailedOrViewedNews(
        @Path("newsType") newsType: String = "viewed",
        @Path("period") period: Int = 7,
        @Query("api-key") apiKey: String = BuildConfig.NY_TIMES_API_KEY
    ): Response<NewsResponse?>
}