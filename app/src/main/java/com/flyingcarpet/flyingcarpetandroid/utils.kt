package com.flyingcarpet.flyingcarpetandroid

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.flowlayout.FlowRow
import java.text.DecimalFormat

fun navigateTo(navController: NavController, route: String) {
    navController.navigate(route) {

        navController.graph.startDestinationRoute?.let { route ->
            popUpTo(route) {
                saveState = true
            }
        }

        launchSingleTop = true
        restoreState = true
    }
}

fun getReadableFileSize(size: Long): String {
    val BYTES_IN_KILOBYTES = 1024
    val dec = DecimalFormat("###.#")
    val KILOBYTES = " KB"
    val MEGABYTES = " MB"
    val GIGABYTES = " GB"
    var fileSize = 0f
    var suffix = KILOBYTES
    if (size > BYTES_IN_KILOBYTES) {
        fileSize = (size / BYTES_IN_KILOBYTES).toFloat()
        if (fileSize > BYTES_IN_KILOBYTES) {
            fileSize = fileSize / BYTES_IN_KILOBYTES
            if (fileSize > BYTES_IN_KILOBYTES) {
                fileSize = fileSize / BYTES_IN_KILOBYTES
                suffix = GIGABYTES
            } else {
                suffix = MEGABYTES
            }
        }
    }
    return (dec.format(fileSize) + suffix).toString()
}
@SuppressLint("Range")
fun getFileName(context: Context, uri: Uri, Col: String): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                if (Col == OpenableColumns.SIZE) {
                    result = getReadableFileSize(cursor.getLong(cursor.getColumnIndex(Col)))
                } else
                    result = cursor.getString(cursor.getColumnIndex(Col))
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = ""
    }
    return result
}
data class targetOS(val name: String, val icon: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OSPicker() {

    var selectedItem by remember { mutableStateOf(0) }
    val targetOSItems = listOf(
        targetOS("Android", R.drawable.android),
        targetOS("iOS", R.drawable.ios),
        targetOS("Windows", R.drawable.windows),
        targetOS("Linux", R.drawable.linux),
        targetOS("MacOS", R.drawable.mac),

        )
    FlowRow(modifier = Modifier, crossAxisSpacing = (-3).dp) {
        targetOSItems.forEachIndexed { index, targetOS ->
            FilterChip(
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                },
                label = { Text(text = targetOS.name, fontWeight = FontWeight.SemiBold) },
                leadingIcon = {
                    if (selectedItem != index) Image(
                        painter = painterResource(id = targetOS.icon),
                        contentDescription = "",
                        modifier = Modifier.size(16.dp),
//                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground, BlendMode.Darken)

                    ) else Icon(
                        Icons.Default.Check,
                        contentDescription = "",
                        modifier = Modifier.size(16.dp),
                    )
                },
                modifier = Modifier.padding(end = 10.dp)
            )


        }

    }
}

@Composable
fun TimelineDot(state: Int, name: String, description: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column {
                Icon(
                    if (state > 0) Icons.Filled.Done else Icons.Rounded.Build,
                    contentDescription = "",
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            when (state) {
                                in -20..-1 -> Color(144, 202, 249)
                                0 -> Color(255, 152, 0)
                                else ->
                                    Color(102, 187, 106)
                            }
                        )
                        .alpha(1f)
                        .size(ICON_SIZE.dp)
                        .padding(4.dp)
                )


            }
            Spacer(modifier = Modifier.padding(horizontal = 10.dp))
            Column {
                Text(name, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    description,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 15.sp,
                    maxLines = 3
                )
            }
        }

    }

}
