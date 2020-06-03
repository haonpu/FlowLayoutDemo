package com.demo.flowlayoutdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val TAG:String = "tag"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initFlowLayout()
    }


    private fun initFlowLayout(){

        var mlist = arrayListOf<String>("java","c",
                "php","kotlin",
                "golang","oc",
                "c++","javascript",
                "c#","smallTalk",
                "swift")
        for (item:String in mlist){
            Log.e(TAG,"--> item is $item")
            var inflate : View = LayoutInflater.from(this).inflate(R.layout.layout_item_label, null)
            var label : TextView = inflate.findViewById(R.id.tv_label);
            label.text = item
            fl_label.addView(inflate)
        }





    }
}