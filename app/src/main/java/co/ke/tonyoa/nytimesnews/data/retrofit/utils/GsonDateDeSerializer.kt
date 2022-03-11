package co.ke.tonyoa.nytimesnews.data.retrofit.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class GsonDateDeSerializer @Inject constructor() : JsonDeserializer<Date> {

    private val dateFormats: List<String> = listOf("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd")
    private var formats = dateFormats.map { SimpleDateFormat(it, Locale.getDefault()) }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Date? {
        try {
            val j = json.asJsonPrimitive.asString
            return parseDate(j)
        } catch (e: ParseException) {
            throw JsonParseException(e.message, e)
        }
    }

    private fun parseDate(dateString: String): Date? {
        if (dateString.trim().isNotEmpty()) {
            for (format in formats) {
                try {
                    return format.parse(dateString)
                } catch (pe: ParseException) {
                }
            }
            return null
        } else {
            return null
        }
    }

}
