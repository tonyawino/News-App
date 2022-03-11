package co.ke.tonyoa.nytimesnews.data.retrofit.models

import com.google.gson.annotations.SerializedName
import java.util.*

data class NewsResponse(
    @SerializedName("copyright")
    val copyright: String,
    @SerializedName("num_results")
    val numResults: Int,
    @SerializedName("results")
    val newsItemRetros: List<NewsItemRetro>,
    @SerializedName("status")
    val status: String
)

data class NewsItemRetro(
    @SerializedName("abstract")
    val newsAbstract: String,
    @SerializedName("adx_keywords")
    val adxKeywords: String,
    @SerializedName("asset_id")
    val assetId: Long,
    @SerializedName("byline")
    val author: String,
    @SerializedName("column")
    val column: Any,
    @SerializedName("des_facet")
    val desFacet: List<String>,
    @SerializedName("eta_id")
    val etaId: Int,
    @SerializedName("geo_facet")
    val geoFacet: List<String>,
    @SerializedName("id")
    val id: Long,
    @SerializedName("media")
    val mediaItemRetros: List<MediaItemRetro>,
    @SerializedName("nytdsection")
    val nytDSection: String,
    @SerializedName("org_facet")
    val orgFacet: List<String>,
    @SerializedName("per_facet")
    val perFacet: List<String>,
    @SerializedName("published_date")
    val publishedDate: Date,
    @SerializedName("section")
    val section: String,
    @SerializedName("source")
    val source: String,
    @SerializedName("subsection")
    val subsection: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("updated")
    val updated: Date,
    @SerializedName("uri")
    val uri: String,
    @SerializedName("url")
    val url: String
)

data class MediaItemRetro(
    @SerializedName("approved_for_syndication")
    val approvedForSyndication: Int,
    @SerializedName("caption")
    val caption: String,
    @SerializedName("copyright")
    val copyright: String,
    @SerializedName("media-metadata")
    val mediaMetadataRetros: List<MediaMetadataRetro>,
    @SerializedName("subtype")
    val subtype: String,
    @SerializedName("type")
    val type: String
)

data class MediaMetadataRetro(
    @SerializedName("format")
    val format: String,
    @SerializedName("height")
    val height: Int,
    @SerializedName("url")
    val url: String,
    @SerializedName("width")
    val width: Int
)