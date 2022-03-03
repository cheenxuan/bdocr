package me.xuan.baiduocr

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import me.xuan.bdocr.ui.camera.CameraActivity
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity() {
    
    companion object{
        const val ID_CARD_FRONT = 101
        const val ID_CARD_BACK = 102
        const val BANK_CARD = 103
        const val PHOTO_DEFAULT = 104
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        
        findViewById<TextView>(R.id.id_card_front).setOnClickListener { 
            OcrManager.startBdIdCardOcr(this,false, ID_CARD_FRONT)
        }

        findViewById<TextView>(R.id.id_card_back).setOnClickListener {
            OcrManager.startBdIdCardOcr(this,true, ID_CARD_BACK)
        }

        findViewById<TextView>(R.id.bank_card).setOnClickListener {
            OcrManager.startBdBankCardOcr(this, BANK_CARD)
        }

        findViewById<TextView>(R.id.photo_default).setOnClickListener {
            OcrManager.startPhotoDefault(this, PHOTO_DEFAULT)
        }

        findViewById<TextView>(R.id.album_default).setOnClickListener {
            OcrManager.startAlnumDefault(this, PHOTO_DEFAULT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when(requestCode){
            ID_CARD_FRONT -> {
                if (resultCode != RESULT_OK) return
                if (data != null) {
                    val listResult = data.getSerializableExtra(CameraActivity.KEY_REC_RESULT_MAP) as HashMap<String, String>
                    toPrint(listResult)
                } else {
                    LogUtil.d("识别失败")
                }
            }

            ID_CARD_BACK -> {
                if (resultCode != RESULT_OK) return
                if (data != null) {
                    val listResult = data.getSerializableExtra(CameraActivity.KEY_REC_RESULT_MAP) as HashMap<String, String>
                    toPrint(listResult)
                } else {
                    LogUtil.d("识别失败")
                }
            }

            BANK_CARD -> {
                if (resultCode != RESULT_OK) return
                if (data != null) {
                    val listResult = data.getSerializableExtra(CameraActivity.KEY_REC_RESULT_MAP) as HashMap<String, String>
                    toPrint(listResult)
                } else {
                    LogUtil.d("识别失败")
                }
            }

            PHOTO_DEFAULT -> {
                if (resultCode != RESULT_OK) return
                if (data != null) {
                    val listResult = data.getSerializableExtra(CameraActivity.KEY_REC_RESULT_MAP) as HashMap<String, String>
                    toPrint(listResult)
                } else {
                    LogUtil.d("识别失败")
                }
            }
        }
    }

    private fun toPrint(result: HashMap<String, String>) {
        val jsonObj = JSONObject(result)
        LogUtil.d("照片信息：" + jsonObj.toString())
    }
}
