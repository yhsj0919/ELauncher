package xyz.yhsj.elauncher.setting

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import xyz.yhsj.elauncher.R

class EmptyStartActivity : AppCompatActivity() {


    var finishFlag = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empty)
        finishFlag = 0
        val packagetName = intent.getStringExtra("packageName")
        val intent = packageManager.getLaunchIntentForPackage(
            packagetName
        )

        if (intent != null) {
            try {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)

            } catch (e: Exception) {
                Toast.makeText(this, "启动失败", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "未安装", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        finishFlag = 0
        val packagetName = intent.getStringExtra("packageName")
        val intent = packageManager.getLaunchIntentForPackage(
            packagetName
        )

        if (intent != null) {
            try {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)

            } catch (e: Exception) {
                Toast.makeText(this, "启动失败", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "未安装", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onResume() {
        super.onResume()

        if (finishFlag >= 1) {
            finish()
        }
        finishFlag++
    }
}
