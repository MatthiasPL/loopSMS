package com.loopmoth.loopsms

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_sms.view.*
import kotlinx.android.synthetic.main.fragment_smssender.*
import kotlinx.android.synthetic.main.fragment_smssender.view.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.Exception

class SMSsender : Fragment() {
    var activityCallback: Listener? = null
    private var root: View? = null

    interface Listener{
        fun sendSMS(smsNumber: String, sms: String)
        fun loadSMSFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater!!.inflate(R.layout.fragment_smssender, container, false)

        view.bSend.setOnClickListener {
            try{
                activityCallback = context as Listener
                val number = tvNumber.text.toString()
                val character = 5
                val singlecharacter = character.toChar()
                val message = Caesar.encrypt(tvMessage.text.toString(), 3)
                activityCallback!!.sendSMS(number, singlecharacter + message)
                //Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }catch(e: Exception){
                //throw ClassCastException(context?.toString()+" must implement Listener")
            }
        }

        view.bShow.setOnClickListener {
            try{
                activityCallback = context as Listener
                activityCallback!!.loadSMSFragment()
            }catch(e: Exception){
                throw ClassCastException(context?.toString()+" must implement Listener")
            }
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    fun Context.toast(message: CharSequence) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    fun String.encrypt(password: String): String {
        val secretKeySpec = SecretKeySpec(password.toByteArray(), "AES")
        val iv = ByteArray(16)
        val charArray = password.toCharArray()
        for (i in 0 until charArray.size){
            iv[i] = charArray[i].toByte()
        }
        val ivParameterSpec = IvParameterSpec(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)

        val encryptedValue = cipher.doFinal(this.toByteArray())
        return Base64.encodeToString(encryptedValue, Base64.DEFAULT)
    }
}
