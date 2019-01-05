package com.loopmoth.loopsms

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_sms.*
import kotlinx.android.synthetic.main.fragment_sms.view.*
import java.lang.ClassCastException
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import java.text.ParseException
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [SMSFragment.OnListFragmentInteractionListener] interface.
 */
class SMSFragment : Fragment() {

    var activityCallback: Listener? = null

    interface Listener{
        fun getAllSMS(): List<Sms>
        fun loadSMSsenderFragment()
    }

    companion object {
        fun newInstance(): SMSFragment {
            return SMSFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater!!.inflate(R.layout.fragment_sms, container, false)
        view.bBack.setOnClickListener {
            try{
                activityCallback = context as Listener
                activityCallback!!.loadSMSsenderFragment()
            }catch(e: Exception){
                throw ClassCastException(context?.toString()+" must implement Listener")
            }
        }
        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    fun Context.toast(message: CharSequence) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    fun loadList(){
        try{
            activityCallback = context as Listener
            val list = activityCallback!!.getAllSMS()
            var smslista = mutableListOf<String>()
            val character = 5
            val singlecharacter = character.toChar()
            for (sms in list){
                if(sms.msg[0]==singlecharacter){
                    smslista.add(sms.address + " " + getDateTime(sms.time) + "\r\n\r\n" + Caesar.decrypt(sms.msg,3) + "\r\n")
                }
            }
            val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, smslista)
            lvSMS.adapter = adapter
        }
        catch (e: ClassCastException){
            throw ClassCastException(context?.toString()+" must implement Listener")
        }
    }

    private fun getDateTime(s: String?): String? {
        /*val calendar = Calendar.getInstance()
        calendar.timeInMillis = s!!.toLong()
        val date = calendar.time
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

        val formatted = formatter.format(date)

        return date.toString()
        //return date.day.toString() + "-" + date.month.toString() + "-" + date.year.toString()*/
        val cal = Calendar.getInstance()
        cal.timeInMillis = s!!.toLong()
        val date = cal.time
        val format1 = SimpleDateFormat("dd-MM-yyyy HH:mm")
        var inActiveDate: String? = null
        try {
            inActiveDate = format1.format(date)
            println(inActiveDate)
            return inActiveDate
        } catch (e1: ParseException) {
            // TODO Auto-generated catch block
            e1.printStackTrace()
            return ""
        }
    }

    fun String.decrypt(password: String): String {
        val secretKeySpec = SecretKeySpec(password.toByteArray(), "AES")
        val iv = ByteArray(16)
        val charArray = password.toCharArray()
        for (i in 0 until charArray.size){
            iv[i] = charArray[i].toByte()
        }
        val ivParameterSpec = IvParameterSpec(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)

        val decryptedByteValue = cipher.doFinal(Base64.decode(this, Base64.DEFAULT))
        return String(decryptedByteValue)
    }
}
