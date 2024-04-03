package com.zhpew.findapp

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.google.accompanist.drawablepainter.rememberDrawablePainter

class MainActivity : AppCompatActivity() {

    private val colorList = arrayOf(
        R.color.color_26AA89,
        R.color.color_22856C,
        R.color.color_E46962,
        R.color.color_B63028,
        R.color.color_EC928E
    )

    private var originData: HashMap<String, ApplicationInfo> = HashMap()
    private val resultData: SnapshotStateList<ApplicationInfo> =
        mutableStateListOf<ApplicationInfo>()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("QueryPermissionsNeeded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        val flags = window.decorView.systemUiVisibility
        window.decorView.systemUiVisibility = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        val list = packageManager.getInstalledApplications(
            PackageManager.ApplicationInfoFlags.of(
                PackageManager.MATCH_UNINSTALLED_PACKAGES.toLong()
            )
        )
        setData(list)
        val keyword = mutableStateOf<String>("")

        setContent {
            Column {
                Box(modifier = Modifier.padding(15.dp), content = {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            textAlign = TextAlign.Justify
                        ),
                        value = keyword.value,
                        onValueChange = {
                            keyword.value = it
                            updateKeyword(it)
                        })
                })
                LazyColumn(
                    content = {
                        items(resultData.size) {
                            Item(resultData[it])
                        }
                    }
                )
            }
        }
    }

    @Composable
    fun Item(info: ApplicationInfo) {
        val icon = packageManager.getApplicationIcon(info.packageName)
        val colorIndex = Math.random() * 5
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .padding(start = 20.dp, end = 20.dp, top = 20.dp)
                .background(
                    color = colorResource(id = colorList[colorIndex.toInt()]),
                    shape = RoundedCornerShape(15)
                )
                .clickable {
                    val intent = packageManager.getLaunchIntentForPackage(info.packageName)
                    startActivity(intent)
                },
        ) {
            Image(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .width(50.dp)
                    .height(50.dp),
                painter = rememberDrawablePainter(drawable = icon),
                contentDescription = ""
            )
            Text(
                modifier = Modifier
                    .padding(start = 20.dp),
                color = colorResource(R.color.black),
                fontSize = 17.sp,
                text = packageManager.getApplicationLabel(info).toString()
            )
        }
    }

    fun setData(list: List<ApplicationInfo>) {
        for (info in list) {
            if (info.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                val name = packageManager.getApplicationLabel(info).toString()
                originData[name] = info
            }
        }
    }

    fun updateKeyword(key: String) {
        resultData.clear()
        if (key.isEmpty()) {
            return
        }
        val next = getNext(key)
        originData.forEach {
            val name = it.key
            if (isMatch(name, key, next)) {
                resultData.add(it.value)
            }
        }
    }
}