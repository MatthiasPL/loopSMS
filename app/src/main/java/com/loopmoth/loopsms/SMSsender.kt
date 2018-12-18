package com.loopmoth.loopsms

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_smssender.*
import kotlinx.android.synthetic.main.fragment_smssender.view.*
import kotlin.Exception

class SMSsender : Fragment() {
    var activityCallback: Listener? = null
    private var root: View? = null

    interface Listener{
        fun sendSMS(smsNumber: String, sms: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater!!.inflate(R.layout.fragment_smssender, container, false)

        view.bSend.setOnClickListener {
            view.bSend.setOnClickListener {
                try{
                    activityCallback = context as Listener
                    val number = tvNumber.text.toString()
                    val message = tvMessage.text.toString()
                    activityCallback!!.sendSMS(number, message)
                }catch(e: Exception){
                    throw ClassCastException(context?.toString()+" must implement Listener")
                }
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
}
