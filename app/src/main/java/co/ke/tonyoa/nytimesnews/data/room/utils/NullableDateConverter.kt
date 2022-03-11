package co.ke.tonyoa.nytimesnews.data.room.utils

import androidx.room.TypeConverter
import java.util.*

class NullableDateConverter {
    @TypeConverter
    fun dateToLong(date: Date?): Long {
        return date?.time ?: -1
    }

    @TypeConverter
    fun longToDate(time: Long): Date? {
        return if (time == -1L) null else Date(time)
    }
}
