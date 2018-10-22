package com.example.dinklebot.mylocations

/* ********************************* */
/* Program: My Locations */
/* Description: Class MainActivity handles the sign in or sign up process for accessing a users information*/
/* Author: Robert Leslie */
/* Notes: */
/* Reference: */
/* Last Modified: 6/8/2018 */
/* ********************************** */

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.app.Dialog
import android.content.Intent
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import javax.crypto.Cipher.DECRYPT_MODE
import javax.crypto.Cipher.ENCRYPT_MODE


class MainActivity : AppCompatActivity() {
    //global args
    private var aes: AES = AES()
    private lateinit var myDialog: Dialog
    private var HELP_MESSAGE = "Click sign in or sign up and fill in information, click enter"

    //creates activity and initializes aes
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        aes = AES()
    }

    //creates the options menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    //sets up option menu item actions
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_help -> {
                myDialog = Dialog(this, R.style.PauseDialog)
                myDialog.setContentView(R.layout.help)
                myDialog.setCancelable(true)
                myDialog.show()
                val help = myDialog.findViewById<TextView>(R.id.helpText)
                help.text = HELP_MESSAGE
                help.gravity = Gravity.CENTER
                help.setPadding(0, 0, 0, 100)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //handles sign in process, calls dialog for sign in, sets up click listener for login button
    //checks if username is stored in the database and if the entered password matches stored password
    //calls mapActivity if entered information is correct else warns user
    fun callSigninDialog(v: View) {
        myDialog = Dialog(this, R.style.PauseDialog)
        myDialog.setContentView(R.layout.sign_in)
        myDialog.setCancelable(true)
        val login = myDialog.findViewById<Button>(R.id.signinbutton)
        val username = myDialog.findViewById(R.id.signinUsername) as EditText
        val password = myDialog.findViewById(R.id.signinPass) as EditText
        myDialog.show()

        login.setOnClickListener( {
            val tempName = username.text.toString()
            val tempPass = password.text.toString()
            if(tempName != "" && tempPass != ""){
                val dbHandler = myDBHandler(this, null, null, 1)
                var decrypted = dbHandler.findUser(tempName)
                decrypted = aes.crypt(DECRYPT_MODE, decrypted.toString(), aes.getKey())
                if(decrypted == tempPass){
                    //Toast.makeText(this, "In the database", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MapsActivity::class.java)
                    intent.putExtra("username", tempName) //to pass values
                    startActivity(intent)
                    username.setText("")
                    password.setText("")
                    myDialog.dismiss()
                }else{
                    Toast.makeText(this, "Username or password incorrect", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    //handles sign up process, calls dialog for sign up, sets up click listener for login button
    //checks if username is not stored in database and adds encrypted password and username to database
    //make sure password meets proper password conventions
    //calls mapActivity if entered information is correct else warns user
    fun callSignupDialog(v: View) {
        myDialog = Dialog(this, R.style.PauseDialog)
        myDialog.setContentView(R.layout.sign_up)
        myDialog.setCancelable(true)
        val login = myDialog.findViewById<Button>(R.id.signupButton)
        val username = myDialog.findViewById(R.id.signupUsername) as EditText
        val password = myDialog.findViewById(R.id.signupPass) as EditText
        val password2 = myDialog.findViewById(R.id.signupPass2) as EditText
        myDialog.show()

        login.setOnClickListener( {
            val tempName = username.text.toString()
            val tempPass = password.text.toString()
            val tempPass2 = password2.text.toString()
            if(tempName != "" && tempPass != "" && tempPass2 != ""){
                if(tempPass == tempPass2){
                    var digit = false
                    val len = tempPass.length
                    if(len < 13){
                        for(i in 0 until len){
                            if(tempPass[i].isDigit()){
                                digit = true
                            }
                        }
                    }
                    if(len < 6 || len > 12){
                        Toast.makeText(this, "password should be between 6 and 12 characters", Toast.LENGTH_SHORT).show()
                    }
                    else if(!tempPass.contains(Regex("[^A-Za-z0-9 ]"))){
                        Toast.makeText(this, "password should contain a special character", Toast.LENGTH_SHORT).show()
                    }
                    else if(!digit){
                        Toast.makeText(this, "password should contain a number", Toast.LENGTH_SHORT).show()
                    }else {
                        val encrypted = aes.crypt(ENCRYPT_MODE, tempPass, aes.getKey())
                        val dbHandler = myDBHandler(this, null, null, 1)
                        if (encrypted == null) {
                            Toast.makeText(this, "password incompatible", Toast.LENGTH_SHORT).show()
                        } else {
                            if (!dbHandler.addUser(tempName, encrypted)) {
                                Toast.makeText(this, "username taken", Toast.LENGTH_SHORT).show()
                            } else {
                                val intent = Intent(this, MapsActivity::class.java)
                                intent.putExtra("username", tempName) //to pass values
                                startActivity(intent)
                                myDialog.dismiss()
                            }
                        }
                        username.setText("")
                        password.setText("")
                        password2.setText("")
                    }
                }else{
                    Toast.makeText(this, "Please enter matching passwords", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
