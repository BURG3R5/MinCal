package co.adityarajput.mincal.utils

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

val Int.asText
    get() = @Composable { Text(stringResource(this)) }

val Pair<Int, Int>.asIcon
    get() = @Composable { Icon(painterResource(first), stringResource(second)) }

val Int.str @Composable get() = stringResource(this)

val Int.dim @Composable get() = dimensionResource(this)
