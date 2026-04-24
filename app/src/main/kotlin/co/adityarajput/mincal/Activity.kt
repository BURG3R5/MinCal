// MinCal is an extremely minimal client for Google Calendar.
//
// Copyright (C) 2026 Aditya Rajput
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License (version 3) as
// published by the Free Software Foundation.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.
//
// The developer is reachable by electronic mail at <mailto:mail@adityarajput.co>

package co.adityarajput.mincal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import co.adityarajput.mincal.views.Navigator
import co.adityarajput.mincal.views.Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            Theme {
                Surface(color = Color.Transparent) {
                    Navigator(rememberNavController())
                }
            }
        }
    }
}
