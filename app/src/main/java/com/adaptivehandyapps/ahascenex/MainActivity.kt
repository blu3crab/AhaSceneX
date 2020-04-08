///////////////////////////////////////////////////////////////////////////
// StageCraft: the ART of creating compelling ILLUSIONS
//
// Created by MAT on 28FEB2020.
//
package com.adaptivehandyapps.ahascenex

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.adaptivehandyapps.ahascenex.model.StageDatabase

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    var PERMISSION_CODE_READ = 1001
    var PERMISSION_CODE_WRITE = 1002

    ///////////////////////////////////////////////////////////////////////////
    // life cycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

//        fab_main.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
//        }
        // check for permissions
        checkPermissionForImage()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        Log.d(TAG, "onSaveInstanceState invoked...")
    }

    ///////////////////////////////////////////////////////////////////////////
    // permissions
    private fun checkPermissionForImage() {
        Log.d(TAG, "checking permissions...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // if READ or WRITE permissions denied, request WRITE as it will bring along READ
            if ((ContextCompat.checkSelfPermission(
                    application,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(
                    application,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED)){
                Log.d(TAG, "requesting permissions...")
//                val permission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                val permission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permission, PERMISSION_CODE_WRITE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        Log.d(TAG, "onRequestPermissionsResult code " + requestCode)
        when (requestCode) {
            PERMISSION_CODE_WRITE -> if (grantResults.size > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "Write Permission Granted!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this, "Write Permission Denied!", Toast.LENGTH_SHORT)
                    .show()
                // kill app if denied
                Log.d(TAG, "onRequestPermissionsResult denied - finishAndRemoveTask...")
                //getActivity()?.finish(); // kills fragment not activity
                this.finishAndRemoveTask()    // kills activity
            }
            PERMISSION_CODE_READ -> if (grantResults.size > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "Read Permission Granted!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this, "Read Permission Denied!", Toast.LENGTH_SHORT)
                    .show()
                // kill app if denied
                Log.d(TAG, "onRequestPermissionsResult denied - finishAndRemoveTask...")
                //getActivity()?.finish(); // kills fragment not activity
                this.finishAndRemoveTask()    // kills activity
            }
        }
    }
    ///////////////////////////////////////////////////////////////////////////
}
