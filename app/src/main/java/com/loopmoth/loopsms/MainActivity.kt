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
import android.content.Context
import android.telephony.SmsManager
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class MainActivity : AppCompatActivity(), SMSsender.Listener, SMSFragment.Listener {
    private val PERMISSIONS_REQUEST_SEND = 100
    private val PERMISSIONS_REQUEST_READ = 100

    var smssender = SMSsender()
    var smsloader = SMSFragment()
    val manager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //var wiad = getAllSms()
    }

    override fun onResume() {
        super.onResume()
        checkForSmsSendPermission()
        //getAllSMS()
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

    override fun getAllSMS(): List<Sms> {
        val lstSms = ArrayList<Sms>()

        /*val cursor = contentResolver.query(Uri.parse("content://sms"), null, null, null, null)
        val smscount = cursor!!.count

        if (cursor!!.moveToFirst()) { // must check the result to prevent exception
            do {
                //var msgData = ""
                for (idx in 0 until cursor.columnCount) {
                    lstSms.add(cursor.getString(cursor.getColumnIndex("body")))
                }
                // use msgData
            } while (cursor.moveToNext())
        } else {
            // empty box, no SMS
        }*/

        var objSms = Sms()
        val message = Uri.parse("content://sms")
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
        else {
            throw RuntimeException("You have no SMS");
        }
        c!!.close()

        //textView.text = totalSMS.toString()

        return lstSms
    }

    override fun loadSMSsenderFragment(){
        smssender = SMSsender()

        if ((fragmentContainer as FrameLayout).childCount > 0)
            (fragmentContainer as FrameLayout).removeAllViewsInLayout()

        val transaction = manager.beginTransaction()
        transaction.add(R.id.fragmentContainer, smssender, "ss")
        transaction.commit()
    }

    override fun loadSMSFragment(){
        smsloader = SMSFragment()

        if ((fragmentContainer as FrameLayout).childCount > 0)
            (fragmentContainer as FrameLayout).removeAllViewsInLayout()

        val transaction = manager.beginTransaction()
        transaction.add(R.id.fragmentContainer, smsloader, "sl")
        transaction.commit()
        transaction.runOnCommit {
            val test = supportFragmentManager.findFragmentByTag("sl") as SMSFragment?
            if(test != null){
                test.loadList()
            }
        }
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

    fun Context.toast(message: CharSequence) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
