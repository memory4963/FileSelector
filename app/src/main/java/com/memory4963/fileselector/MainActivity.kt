package com.memory4963.fileselector

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.memory4963.fileselector.Utils.FileListener
import com.memory4963.fileselector.Utils.ListenerCallback
import com.memory4963.fileselector.Utils.WindowUtils
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), ListenerCallback, View.OnClickListener {
    
    private val path = "/storage/emulated/0/Tencent/Tim_Images"
    
    private val fileListener by lazy { FileListener(path, this) }
    
    private var requestOverlay = false
    
    private var overlayView: View? = null
    
    private val handler = Handler()
    
    override fun onResume() {
        super.onResume()
        if (requestOverlay) {
            if (grantedOverlayPermission()) {
                Toast.makeText(this, "开启悬浮窗权限成功", Toast.LENGTH_SHORT).show()
                retry_btn.visibility = View.INVISIBLE
            } else {
                Toast.makeText(this, "开启悬浮窗权限失败, 点击按钮重试", Toast.LENGTH_SHORT).show()
                retry_btn.visibility = View.VISIBLE
            }
            requestOverlay = false
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        grantPermissions()
        
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            fileListener.stopWatching()
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fileListener.startWatching()
            if (!grantedOverlayPermission()) {
                openActivityOfOverlay()
            }
        }
    }
    
    override fun handleMessage(path: String) {
//        textView.text = path
//        Toast.makeText(this, path, Toast.LENGTH_SHORT).show()
        handler.post({
            overlayView = WindowUtils.showWindow(this, this.path + "/" + path, overlayView)
        })
        
    }
    
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                retry_btn.id ->
                    if (!grantedOverlayPermission()) {
                        openActivityOfOverlay()
                    } else {
                        Toast.makeText(this, "已获得权限", Toast.LENGTH_SHORT).show()
                    }
                
            }
        }
    }
    
    private fun grantPermissions() {
        // 存储权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            fileListener.startWatching()
            if (!grantedOverlayPermission()) {
                openActivityOfOverlay()
            }
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                Toast.makeText(this, "请同意权限申请", Toast.LENGTH_SHORT).show()
            }
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    WRITE_EXTERNAL_STORAGE_REQUEST)
        }
    }
    
    private fun grantedOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(applicationContext)
        } else {
            checkOps()
        }
    }
    
    private fun openActivityOfOverlay() {
        requestOverlay = true
        val intent = Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION")
        Toast.makeText(this, "请找到本应用(FileSelector), 同意悬浮窗权限", Toast.LENGTH_SHORT).show()
        startActivity(intent)
    }
    
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun checkOps(): Boolean {
        try {
            val obj = applicationContext.getSystemService(Context.APP_OPS_SERVICE)
            val localClass = obj.javaClass
            val arrayOfClass = arrayOf(Integer.TYPE, Integer.TYPE, String::class.java)
            val method = localClass.getMethod("checkOp", arrayOfClass[0], arrayOfClass[1], arrayOfClass[2])
            val arrayOfObject = arrayOf<Any>(24, Binder.getCallingUid(), applicationContext.packageName)
            val m = method.invoke(obj, arrayOfObject[0], arrayOfObject[1], arrayOfObject[2])
            return m == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
    
    companion object {
        const val WRITE_EXTERNAL_STORAGE_REQUEST = 15923
    }
    
}
