package co.ke.tonyoa.nytimesnews.data.room.utils

import androidx.room.TypeConverter
import java.util.*

class DateConverter {
    @TypeConverter
    fun dateToLong(date: Date): Long {
        return date.time
    }

    @TypeConverter
    fun longToDate(time: Long): Date {
        return Date(time)
    }
}
