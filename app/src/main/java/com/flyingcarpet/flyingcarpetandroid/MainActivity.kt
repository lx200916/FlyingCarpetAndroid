package com.flyingcarpet.flyingcarpetandroid

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.magnifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.*
import com.flyingcarpet.flyingcarpetandroid.ui.theme.FlyingCarpetAndroidTheme
import com.google.accompanist.flowlayout.FlowRow
import java.text.DecimalFormat


const val ICON_SIZE = 28

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

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            var selectedItem by remember { mutableStateOf(0) }
            val navController = rememberNavController()
            FlyingCarpetAndroidTheme {
                // A surface container using the 'background' color from the theme
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(

                        ) {
                            NavigationBarItem(
                                selected = selectedItem == 0,
                                onClick = {
                                    selectedItem = 0;
                                    navigateTo(navController, "send")
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ios_share_48px),
                                        contentDescription = ""
                                    )
                                },
                                label = { Text("Send") })
                            NavigationBarItem(
                                selected = selectedItem == 1,
                                onClick = {
                                    selectedItem = 1;
                                    navigateTo(navController, "receive")
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.downloading_48px),
                                        contentDescription = ""
                                    )
                                },
                                label = { Text("Receive") })

                        }
                    }
                ) { contentPadding ->
                    Box(modifier = Modifier.padding(contentPadding)) {

                        NavHost(
                            navController = navController,
                            modifier = Modifier.fillMaxSize(),
                            startDestination = "send"
                        ) {
                            composable("receive") {
//                                Send(navController = navController)

                            }
                            composable("send") {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Send(navController = navController)

                                }
                            }
                            composable("sending") {
//                                    Send(navController = navController)
                                processScreen()


                            }

                        }
                    }

                }


            }
        }
    }


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

@SuppressLint("Range")
fun getFileSize(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE))
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result!!.lastIndexOf('/')
        if (cut != -1) {
            result = result.substring(cut + 1)
        }
    }
    return result
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Send(navController: NavController?) {
    var pickedDocUri by remember { mutableStateOf<Uri?>(null) }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            println("selected file URI ${it.data?.data}")
            pickedDocUri = it.data?.data
        }

//ExtendedFloatingActionButton(text = {Text()}, onClick = { /*TODO*/ })
    Scaffold(floatingActionButton = {

        if (pickedDocUri != null)
            ExtendedFloatingActionButton(text = {
                Text(
                    "Start",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }, icon = {
                Icon(
                    painterResource(id = R.drawable.outbox_24px),
                    modifier = Modifier,
                    contentDescription = ""
                )
            }, onClick = {
                navigateTo(navController!!, "sending")
            })
    }, floatingActionButtonPosition = FabPosition.End) { contentPadding ->
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.padding(vertical = 10.dp))
            Text(

                "Send Files",
                fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                fontWeight = MaterialTheme.typography.headlineLarge.fontWeight,
                fontStyle = MaterialTheme.typography.headlineLarge.fontStyle,
                fontFamily = MaterialTheme.typography.headlineLarge.fontFamily
            )
            Spacer(modifier = Modifier.padding(vertical = 10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(15.dp)
                    .clickable {
                        val intent = Intent(
                            Intent.ACTION_OPEN_DOCUMENT,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                            .apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                            }
                        launcher.launch(intent)

                    }

            ) {

                Icon(
                    painter = painterResource(id = if (pickedDocUri == null) R.drawable.apps_add_in_24_filled else R.drawable.document_24_filled),
                    contentDescription = "Document Icon",
                    modifier = Modifier.size(42.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (pickedDocUri == null) "Select Document" else getFileName(
                            LocalContext.current,
                            pickedDocUri!!,
                            OpenableColumns.DISPLAY_NAME
                        ) ?: "",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.alpha(0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        if (pickedDocUri == null) "Click Here to Select Document to Send" else getFileName(
                            LocalContext.current,
                            pickedDocUri!!,
                            OpenableColumns.SIZE
                        ),

                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.alpha(0.5f),
//                        maxLines = 2,
                    )
                }

            }
            Spacer(modifier = Modifier.padding(vertical = 10.dp))
            Text(
                buildAnnotatedString {
                    append("Select")
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    ) {
                        append(" Peer OS")
                    }
                },
                fontSize = 18.sp,
            )
            OSPicker()


        }

    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
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

//        Spacer(modifier = Modifier.padding(vertical = 10.dp))

    }

}

//@Parcelize
data class processItem(var name: String, val description: String)

@Composable
fun processScreen() {
    val currentStep by remember { mutableStateOf(0) }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.sending))
    val progress by animateLottieCompositionAsState(composition, clipSpec = LottieClipSpec.Frame(10,182),  iterations = LottieConstants.IterateForever,
    )
    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = MaterialTheme.colorScheme.primary.toArgb(),
            keyPath = arrayOf(
                "**",
                "Stroke 1 Color"
            )

        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.STROKE_COLOR,
            value = MaterialTheme.colorScheme.primary.toArgb(),
            keyPath = arrayOf(
                "**",
                "Stroke 1 Color"
            )

        ),
    )
    val processItems by remember {
        mutableStateOf(
            listOf<processItem>(
                processItem("Check Permission", "Check if the app has the required permissions"),
                processItem("Setup Wifi", "Setup Wifi Hotspot to share files"),
                processItem("Wait for Connection", "Wait for the receiver to connect"),
                processItem("Sending Files", "Send the files to the receiver"),
            )
        )
    }
    Column(Modifier.padding(10.dp)) {

        LottieAnimation(composition = composition, progress = progress,dynamicProperties=dynamicProperties, contentScale = ContentScale.Inside, modifier = Modifier.weight(1f))

        Box(modifier = Modifier.height(IntrinsicSize.Max)) {
            Box(
                modifier = Modifier
//            .height(10.dp)
                    .padding(start = ICON_SIZE.dp / 2 + 1.dp)
                    .padding(top = 20.dp, bottom = 20.dp)

            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.onBackground)
                        .alpha(0.6f)
                )
            }
            Column(modifier = Modifier

                , verticalArrangement = Arrangement.spacedBy(10.dp)) {

                processItems.forEachIndexed { index, processItem ->
                    TimelineDot(
                        state = currentStep - index,
                        name = processItem.name,
                        description = processItem.description
                    )
                }
            }


        }
        Spacer(modifier = Modifier.weight(0.3f))
    }
//    Column(modifier=Modifier.fillMaxWidth()) {
//        Row(Modifier.fillMaxWidth()) {
//            Icon(Icons.Default.Done, contentDescription = "", modifier = Modifier.clip(CircleShape).background(
//                Color.Green).size(20.dp))
//            Spacer(modifier = Modifier.padding(horizontal = 10.dp))
//            Text("Check Permission", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
//        }
//        Box(modifier = Modifier.height(10.dp).padding(start = 10.dp)){
//            Box(modifier = Modifier.fillMaxHeight().width(2.dp).background(MaterialTheme.colorScheme.onBackground))
//        }
//
//    }

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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FlyingCarpetAndroidTheme {

        Send(navController = null)
    }
}