package de.ur.mi.audidroid.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * For saving a list in the room database a TypeConverter is needed
 * @author: Sabine Roth
 */

class Converters {
    @TypeConverter
    fun handleArrayList(list: ArrayList<Int>): String? {
        if (list.size == 0) return null
        return Gson().toJson(list)
    }

    @TypeConverter
    fun getBackArrayList(value: String?): ArrayList<Int>? {
        if (value == null) return null
        return Gson().fromJson(value, object : TypeToken<ArrayList<Int>>() {}.type)
    }
}
