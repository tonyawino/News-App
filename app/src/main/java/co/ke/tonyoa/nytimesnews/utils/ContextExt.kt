package co.ke.tonyoa.nytimesnews.utils

import android.content.Context
import android.net.ConnectivityManager

import android.net.NetworkCapabilities

import android.os.Build

fun Context.isConnectedToInternet(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo ?: return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val activeNetwork =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        activeNetwork!!.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    } else {
        activeNetworkInfo.isConnected &&
                (activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI ||
                        activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE ||
                        activeNetworkInfo.type == ConnectivityManager.TYPE_ETHERNET)
    }

}