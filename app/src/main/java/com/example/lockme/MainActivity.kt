package com.example.lockme

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    var deviceManger: DevicePolicyManager? = null
    var compName: ComponentName? = null
    var btnEnable: Button? = null
    var btnLock: Button? = null
    lateinit var minpic:NumberPicker
    lateinit var secspic:NumberPicker
    lateinit var tv:TextView
    lateinit var timer:CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnEnable = findViewById(R.id.btnEnable)
        btnLock = findViewById(R.id.btnLock)

        minpic = findViewById(R.id.mins)
        minpic.minValue = 0
        minpic.maxValue = 60
        secspic = findViewById(R.id.secs)
        secspic.minValue = 0
        secspic.maxValue = 60

        deviceManger = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        compName = ComponentName(this, DeviceAdmin::class.java)
        tv = findViewById(R.id.tv)
        btnLock?.text = "lock"
        tv.text = "stopped"
        val active = deviceManger!!.isAdminActive(compName!!)
        if (active) {
            btnEnable?.text = "Disable"
            btnLock?.visibility = View.VISIBLE
        } else {
            btnEnable?.text = "Enable"
            btnLock?.visibility = View.GONE
        }

    }

    fun enablePhone(view: View?) {
        val active = deviceManger!!.isAdminActive(compName!!)
        if (active) {
            deviceManger!!.removeActiveAdmin(compName!!)
            btnEnable!!.text = "Enable"
            btnLock!!.visibility = View.GONE
        } else {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "You should enable the app!")
            startActivityForResult(intent, RESULT_ENABLE)
        }
    }

    fun lockPhone(view: View?) {
        if (btnLock?.text.toString() == "lock") {
            val t = ((minpic.value * 60) + secspic.value).toLong()

            timer = object : CountDownTimer(t * 1000, 1000) {
                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    val minutes = millisUntilFinished / 1000 / 60
                    val seconds = millisUntilFinished / 1000 % 60
                    tv.text = (minutes).toString() + " min " + seconds.toString() + " sec"
                }

                override fun onFinish() {
                    btnLock?.text = "lock"
                    tv.text = "stopped"
                    deviceManger!!.lockNow()
                }
            }
            timer.start()
            btnLock?.text = "reset"

        }else{
            timer.cancel()
            btnLock?.text = "lock"
            tv.text = "stopped"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RESULT_ENABLE -> {
                if (resultCode == RESULT_OK) {
                    btnEnable!!.text = "Disable"
                    btnLock!!.visibility = View.VISIBLE
                } else {
                    Toast.makeText(
                        applicationContext, "Failed!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }

    companion object {
        const val RESULT_ENABLE = 1
    }
}