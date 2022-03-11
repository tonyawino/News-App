package co.ke.tonyoa.nytimesnews.data.retrofit

import co.ke.tonyoa.nytimesnews.data.retrofit.models.MediaItemRetro
import co.ke.tonyoa.nytimesnews.data.retrofit.models.MediaMetadataRetro
import co.ke.tonyoa.nytimesnews.data.retrofit.models.NewsItemRetro
import co.ke.tonyoa.nytimesnews.domain.models.News
import co.ke.tonyoa.nytimesnews.domain.models.NewsImage
import co.ke.tonyoa.nytimesnews.domain.utils.EntityDomainMapper
import java.util.*
import javax.inject.Inject

class RetrofitNewsMapper
@Inject constructor() : EntityDomainMapper<NewsItemRetro, News>() {
    override fun entityToDomain(entity: NewsItemRetro): News {
        val images = mutableListOf<NewsImage>()
        for (media in entity.mediaItemRetros) {
            if (media.type == "image" && media.mediaMetadataRetros.isNotEmpty()) {
                val largestImageUrl =
                    media.mediaMetadataRetros[media.mediaMetadataRetros.size - 1].url
                images.add(NewsImage(media.caption, media.copyright, largestImageUrl))
            }
        }
        return News(
            entity.id,
            entity.title,
            entity.newsAbstract,
            entity.publishedDate,
            (entity.section + " " + entity.subsection).trim(),
            entity.author,
            entity.source,
            entity.url,
            images
        )
    }

    override fun domainToEntity(domain: News): NewsItemRetro {
        return NewsItemRetro(
            domain.newsAbstract,
            "",
            0,
            domain.author,
            "",
            emptyList(),
            0,
            emptyList(),
            domain.id,
            domain.images.map {
                MediaItemRetro(
                    0,
                    it.caption,
                    it.copyright,
                    listOf(MediaMetadataRetro("photo", 0, it.url, 0)),
                    "image",
                    "image"
                )
            },
            "",
            emptyList(),
            emptyList(),
            domain.publishDate,
            domain.category,
            domain.source,
            "",
            domain.title,
            "",
            Date(0),
            "",
            domain.url,
        )
    }
}