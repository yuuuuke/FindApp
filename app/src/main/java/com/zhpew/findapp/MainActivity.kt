package com.zhpew.findapp

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.compose.setContent
import androidx.annotation.MainThread
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {

    private var originData: HashMap<String, ApplicationInfo> = HashMap()
    private val resultData: SnapshotStateList<ApplicationInfo> =
        mutableStateListOf<ApplicationInfo>()
    private val resume = mutableStateOf<Int>(0)

    private val mHandler = Handler(Looper.getMainLooper())

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("QueryPermissionsNeeded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        val flags = window.decorView.systemUiVisibility
        window.decorView.systemUiVisibility = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        val list = packageManager.getInstalledPackages(
            PackageManager.GET_META_DATA
        )
        setData(list)
        val keyword = mutableStateOf<String>("")

        setContent {
            val focusRequester = remember{
                FocusRequester()
            }
            val keyboard = LocalSoftwareKeyboardController.current
            LaunchedEffect(key1 = resume, block = {
                focusRequester.requestFocus()
                keyboard?.show()
            })
            Column {
                Box(modifier = Modifier.padding(15.dp), content = {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
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

    override fun onResume() {
        super.onResume()
        resume.value = resume.value +1
    }

    @Composable
    fun Item(info: ApplicationInfo) {
        val icon = packageManager.getApplicationIcon(info.packageName)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .padding(start = 20.dp, end = 20.dp, top = 10.dp)
                .background(
                    color = colorResource(id = R.color.color_EC928E),
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

    private fun setData(list: List<PackageInfo>) {
        val regex = ".*[\\u4e00-\\u9fa5]+.*"
        val pattern = Pattern.compile(regex)
        for (info in list) {
            if (info.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                val intent = packageManager.getLaunchIntentForPackage(info.packageName)
                intent?.let{
                    var name = packageManager.getApplicationLabel(info.applicationInfo).toString()
                    val matcher = pattern.matcher(name)
                    if(matcher.matches()){
                        name += toPinyin(name)
                    }
                    originData[name] = info.applicationInfo
                }
            }
        }
    }

    private fun updateKeyword(key: String) {
        resultData.clear()
        mHandler.removeMessages(0)
        if (key.isEmpty()) {
            return
        }
        mHandler.postDelayed({
            val next = getNext(key.lowercase())
            originData.forEach {
                val name = it.key.lowercase()
                if (isMatch(name, key.lowercase(), next)) {
                    resultData.add(it.value)
                }
            }
        },200)
    }

    private fun toPinyin(chinese: String): String {
        val format = HanyuPinyinOutputFormat()
        format.caseType = HanyuPinyinCaseType.LOWERCASE
        format.toneType = HanyuPinyinToneType.WITHOUT_TONE
        val sb = StringBuilder()
        val chars = chinese.toCharArray()
        for (c in chars) {
            if (Character.isWhitespace(c)) {
                continue
            }
            if (c in '\u4e00'..'\u9fa5') {
                try {
                    val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format)
                    sb.append(pinyinArray[0])
                } catch (e: BadHanyuPinyinOutputFormatCombination) {
                    e.printStackTrace()
                }
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }
}