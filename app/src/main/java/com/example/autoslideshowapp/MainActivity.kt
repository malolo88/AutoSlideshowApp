package com.example.autoslideshowapp

import android.Manifest
import android.content.ContentProvider
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.os.Handler
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    private var mTimer: Timer? = null

    // タイマー用の時間のための変数
    private var mTimerSec = 0.0

    private var mHandler = Handler()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //パーミッションの設定
        //Android6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //許可されている
                getContentsInfo()
            } else {
                //許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
            }
            //Android 5系以下の場合
        } else {
            getContentsInfo()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                } else{
                    Toast.makeText(applicationContext , "このアプリを使用するにはアクセス許可が必要です。", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun getContentsInfo() {
        //画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, //データの種類
            null, //項目(null=フィルタなし)
            null, //フィルタ条件(null = フィルタなし)
            null, //フィルタ用パラメータ
            null //ソート(null ソートなし)
        )

        if (cursor!!.moveToFirst()) {
            //indexからIDを取得し、そのIDから画像のURIを取得する
            val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor.getLong(fieldIndex)
            val imageUri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

            imageView.setImageURI(imageUri)

        } else {
            cursor.close()
        }

        next_button.setOnClickListener() {
            //次にイメージがなければ最初に移動する
            if (!cursor.moveToNext()) {
                cursor.moveToFirst()
            }
            //indexからIDを取得し、そのIDから画像のURIを取得する
            val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor.getLong(fieldIndex)
            val imageUri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

            imageView.setImageURI(imageUri)
        }

        back_button.setOnClickListener() {
            //前にイメージがなければ最後に移動する
            if (!cursor.moveToPrevious()) {
                cursor.moveToLast()
            }
            //indexからIDを取得し、そのIDから画像のURIを取得する
            val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor.getLong(fieldIndex)
            val imageUri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

            imageView.setImageURI(imageUri)
        }

        startPause_button.setOnClickListener() {
            if (mTimer == null) {
                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mTimerSec += 1.0
                        mHandler.post {
                            if (!cursor.moveToNext()) {
                                cursor.moveToFirst()
                            }
                            //indexからIDを取得し、そのIDから画像のURIを取得する
                            val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                            val id = cursor.getLong(fieldIndex)
                            val imageUri =
                                ContentUris.withAppendedId(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    id
                                )

                            imageView.setImageURI(imageUri)
                            startPause_button.text = "停止"
                            next_button.isClickable = false
                            back_button.isClickable = false
                        }
                    }
                }, 2000, 2000) //最初に始動させるまで2000ミリ秒、ループの間隔を2000ミリ秒
            } else if (mTimer != null) {
                mTimer!!.cancel()
                mTimer = null
                startPause_button.text = "再生"
                next_button.isClickable = true
                back_button.isClickable = true
            }
        }
    }
}








