package wolandec.jeeves

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.EditText
import android.widget.TextView


/**
 * Created by wolandec on 27.02.18.
 */

class LoginDialogFragment() : DialogFragment() {

    val LOG_TAG = this::class.java.simpleName

    interface LoginDialogListener {
        fun onDialogPositiveClick(dialog: LoginDialogFragment)
        fun onDialogNegativeClick(dialog: LoginDialogFragment)
    }

    var passView: EditText? = null
    var invalidPassword: Boolean = false
    var loginListener: LoginDialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity);
        val inflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.login_dialog, null))
                .setPositiveButton(R.string.ok_button_text, object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        loginListener?.onDialogPositiveClick(this@LoginDialogFragment)
                    }
                })
                .setNegativeButton(R.string.cancel_button_text, object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        loginListener?.onDialogNegativeClick(this@LoginDialogFragment)
                    }
                })
        return builder.create();
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        loginListener = activity as LoginDialogListener?
    }


    override fun onStart() {
        super.onStart()
        passView = dialog.findViewById(R.id.password)
        if (invalidPassword)
            dialog.findViewById<TextView>(R.id.invalidPassword).visibility = View.VISIBLE
        else
            dialog.findViewById<TextView>(R.id.invalidPassword).visibility = View.GONE
    }
}