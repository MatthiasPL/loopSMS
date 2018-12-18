package com.loopmoth.loopsms

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.ContentResolver
import android.net.Uri
import android.Manifest.permission
import android.Manifest.permission.SEND_SMS
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.app.PendingIntent
import android.telephony.SmsManager


class MainActivity : AppCompatActivity(), SMSsender.Listener {
    private val PERMISSIONS_REQUEST_SEND = 100
    private val PERMISSIONS_REQUEST_READ = 100

    var smssender = SMSsender()
    val manager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        checkForSmsSendPermission()
    }

    private fun checkForSmsSendPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                PERMISSIONS_REQUEST_SEND
            )
        } else {
            checkForSmsReadPermission()
        }
    }

    private fun checkForSmsReadPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                PERMISSIONS_REQUEST_READ
            )
        } else {
            loadSMSsenderFragment()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST_SEND && grantResults.size == 1
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        } else {
            //Toast.makeText(this, "Please enable SMS send permission", Toast.LENGTH_SHORT).show();
        }

        if (requestCode == PERMISSIONS_REQUEST_READ && grantResults.size == 1
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        } else {
            //Toast.makeText(this, "Please enable SMS read permission", Toast.LENGTH_SHORT).show();
        }
    }

    fun getAllSms(): List<Sms> {
        val lstSms = ArrayList<Sms>()
        var objSms = Sms()
        val message = Uri.parse("content://sms/")
        val cr = getApplicationContext().getContentResolver()

        val c = cr.query(message, null, null, null, null)
        startManagingCursor(c)
        val totalSMS = c!!.getCount()

        if (c!!.moveToFirst()) {
            for (i in 0 until totalSMS) {

                objSms = Sms()
                objSms.id = c!!.getString(c!!.getColumnIndexOrThrow("_id"))
                objSms.address = c!!.getString(
                    c!!
                        .getColumnIndexOrThrow("address")
                )
                objSms.msg = c!!.getString(c!!.getColumnIndexOrThrow("body"))
                objSms.readState = c!!.getString(c!!.getColumnIndex("read"))
                objSms.time = c!!.getString(c!!.getColumnIndexOrThrow("date"))
                if (c!!.getString(c!!.getColumnIndexOrThrow("type")).contains("1")) {
                    objSms.folderName = "inbox"
                } else {
                    objSms.folderName = "sent"
                }

                lstSms.add(objSms)
                c!!.moveToNext()
            }
        }
        // else {
        // throw new RuntimeException("You have no SMS");
        // }
        c!!.close()

        return lstSms
    }

    fun loadSMSsenderFragment(){
        smssender = SMSsender()

        if ((fragmentContainer as FrameLayout).childCount > 0)
            (fragmentContainer as FrameLayout).removeAllViewsInLayout()

        val transaction = manager.beginTransaction()
        transaction.add(R.id.fragmentContainer, smssender, "mp")
        transaction.commit()
    }

    override fun sendSMS(smsNumber: String, sms: String) {
        val smsIntent = Intent(Intent.ACTION_SENDTO)
        smsIntent.data = Uri.parse(smsNumber)
        smsIntent.putExtra("sms_body", sms)

        val scAddress: String? = null
        val sentIntent: PendingIntent? = null
        val deliveryIntent: PendingIntent? = null

        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(
            smsNumber, scAddress, sms,
            sentIntent, deliveryIntent
        )
    }
}
