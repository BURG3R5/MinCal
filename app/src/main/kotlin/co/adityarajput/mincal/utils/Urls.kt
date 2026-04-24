package co.adityarajput.mincal.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

fun Context.open(uri: String) = open(uri.toUri())

fun Context.open(uri: Uri) = startActivity(Intent(Intent.ACTION_VIEW, uri))
