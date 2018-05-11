package com.firebase.younghoons.firebaseexample

import android.content.CursorLoader
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*
import java.util.jar.Manifest
import com.google.firebase.storage.UploadTask
import com.google.android.gms.tasks.OnSuccessListener
import android.support.annotation.NonNull
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.storage.StorageReference
import java.io.File


class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val GALLERY_CODE : Int = 10
    lateinit var nameTextView : TextView
    lateinit var emailTextView: TextView
    private lateinit var auth : FirebaseAuth
    private lateinit var storage : FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),0)
        }

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        val view : View = nav_view.getHeaderView(0)
        nameTextView = view.findViewById(R.id.header_name_text)
        emailTextView = view.findViewById(R.id.header_email_text)

        nameTextView.text = auth.currentUser?.displayName
        emailTextView.text = auth.currentUser?.email
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {
                val intent = Intent(Intent.ACTION_PICK)
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE)
                startActivityForResult(intent,GALLERY_CODE)
            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }

            R.id.nav_logout -> {
                auth.signOut()
                LoginManager.getInstance().logOut()
                finish()
                var intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==GALLERY_CODE){
            Log.d("HomeActivity",getPath(data?.data!!))

            val storageRef = storage.reference

            val file = Uri.fromFile(File(getPath(data?.data!!)))
            val riversRef = storageRef.child("images/" + file.lastPathSegment)
            var uploadTask  = riversRef.putFile(file)

// Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(OnFailureListener {
                // Handle unsuccessful uploads
            }).addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                val downloadUrl = taskSnapshot.downloadUrl
            })

        }
    }

    fun getPath(uri : Uri) : String{
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        var cursorLoader = CursorLoader(this,uri,proj,null,null,null)

        var cursor = cursorLoader.loadInBackground()
        var index :Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

        cursor.moveToFirst()
        return cursor.getString(index)
    }
}
