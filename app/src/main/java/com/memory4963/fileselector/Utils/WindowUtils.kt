package com.memory4963.fileselector.Utils

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.memory4963.fileselector.R
import java.io.File


/**
 * Created by memory4963 on 2018/5/18.
 */
class WindowUtils {
    
    companion object {
        
        private val TAG = "WindowUtils"
        
        private var isShown = false
        
        fun showWindow(context: Context, path: String, view: View?): View {
            if (isShown) {
                if (view == null) {
                    throw RuntimeException("showing window but no view")
                } else {
                    Log.d(TAG, "showWindow: already shown")
                    return view
                }
            }
            val appContext = context.applicationContext
            val windowManager = appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            
            val view2 = view ?: LayoutInflater.from(context).inflate(R.layout.overlay_layout, null, false)
            val overlayIv = view2.findViewById(R.id.overlayIv) as ImageView
            val overlayEt = view2.findViewById(R.id.overlayEt) as EditText
            val overlayBtn = view2.findViewById(R.id.overlayBtn) as Button
            
            //显示图像
            //todo 显示gif
            val imageFile = File(path)
            if (!imageFile.exists()) {
                Toast.makeText(context, "文件不存在", Toast.LENGTH_SHORT).show()
                return view2
            }
            overlayIv.setImageBitmap(BitmapFactory.decodeFile(path))
            val rawList = path.split('/')
            val fileName = rawList[rawList.lastIndex]
            val filePath = path.substring(0, path.length - 1 - fileName.length) + '/'
            val nameList = fileName.split('.')
            val fileType = nameList[nameList.lastIndex]
            overlayEt.hint = fileName
            
            overlayBtn.setOnClickListener {
                val tempName = overlayEt.text.toString()
                if (TextUtils.isEmpty(tempName)) {
                    Toast.makeText(context, "未更改", Toast.LENGTH_SHORT).show()
                } else {
                    // 拼合文件名
                    val newName = if (tempName.contains(fileType)) {
                        val temp = (filePath + tempName)
                        temp.substring(0, temp.length - 1 - fileType.length)
                    } else {
                        filePath + tempName
                    }
                    // 检测文件是否存在
                    var newFile = File(newName + '.' + fileType)
                    var i = 1
                    while (newFile.exists()) {
                        newFile = File("""$newName($i).$fileType""")
                        i++
                    }
                    newFile.createNewFile()
                    newFile.writeBytes(imageFile.readBytes())
                    val result = imageFile.delete()
                    context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(newFile)))
                    Log.d(TAG, "showWindow: 修改文件名为$newName, $result")
                }
                dismissWindow(context, view2)
            }
            
            view2.setOnClickListener {
                dismissWindow(context, view2)
            }
            
            setParam(windowManager, view2)
            isShown = true
            return view2
            
        }
        
        private fun setParam(windowManager: WindowManager, view: View) {
            val param = WindowManager.LayoutParams()
            param.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            param.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            param.format = PixelFormat.TRANSLUCENT
            param.width = WindowManager.LayoutParams.MATCH_PARENT
            param.height = WindowManager.LayoutParams.MATCH_PARENT
            param.gravity = Gravity.CENTER
            windowManager.addView(view, param)
        }
        
        fun dismissWindow(context: Context, view: View) {
            if (isShown) {
                isShown = false
                val appContext = context.applicationContext
                val windowManager = appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                windowManager.removeView(view)
            }
        }
        
    }
    
}
