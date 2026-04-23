package com.box.picker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.box.picker.data.AppDatabase
import com.box.picker.logic.SmsParser
import kotlinx.coroutines.launch

class ImportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val db = AppDatabase.getDatabase(this)
        
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
            lifecycleScope.launch {
                val rules = db.expressDao().getAllRules()
                val pkg = SmsParser.parse(sharedText, rules)
                if (pkg != null) {
                    db.expressDao().insertPackage(pkg)
                    Toast.makeText(this@ImportActivity, "已解析并保存: ${pkg.pickupCode}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ImportActivity, "未识别到取件码", Toast.LENGTH_SHORT).show()
                }
                finish()
            }
        } else {
            finish()
        }
    }
}
