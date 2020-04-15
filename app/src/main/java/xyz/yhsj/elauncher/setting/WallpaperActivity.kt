package xyz.yhsj.elauncher.setting

import android.app.Activity
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_wallpaper.*
import xyz.yhsj.elauncher.R
import xyz.yhsj.elauncher.utils.FileUtils
import java.io.IOException


class WallpaperActivity : AppCompatActivity() {

    val IMAGE_PICK = 2654

    var imgPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallpaper)
        back.setOnClickListener { finish() }

        select.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK)
        }

        setWallpaper.setOnClickListener {
            setSysWallPaper(imgPath)
        }


        getSysWallPaper()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                val uri = data.data!!
                imgPath = FileUtils.getPath(this, uri)
                val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri));
                mainBg.background = BitmapDrawable(bitmap)

            }
        }
    }

    /**
     * 设置壁纸
     */
    fun setSysWallPaper(imageFilesPath: String?) {

        if (imageFilesPath.isNullOrEmpty()) {
            Toast.makeText(this, "图片不存在", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val mWallManager = WallpaperManager.getInstance(this)
        try {
            val bitmap = BitmapFactory.decodeFile(imageFilesPath)
            mWallManager.setBitmap(bitmap)
            Toast.makeText(this, "壁纸设置成功", Toast.LENGTH_SHORT)
                .show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "壁纸设置失败", Toast.LENGTH_SHORT)
                .show()
        }
    }

    /**
     * 获取壁纸
     */
    fun getSysWallPaper() {
        // 获取壁纸管理器
        val wallpaperManager = WallpaperManager.getInstance(this)
        // 获取当前壁纸
        val wallpaperDrawable = wallpaperManager.drawable
        // 将Drawable,转成Bitmap
        val bm = (wallpaperDrawable as BitmapDrawable).bitmap

        // 设置 背景
        mainBg.background = (BitmapDrawable(bm))
    }
}
