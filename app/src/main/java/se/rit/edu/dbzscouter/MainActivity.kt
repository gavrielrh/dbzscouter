package se.rit.edu.dbzscouter

import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // You can get rid of this, but it does prove that our font works!
        val tv = findViewById<TextView>(R.id.hello_text)
        val face = Typeface.createFromAsset(assets, "fonts/Square.ttf")
        tv.typeface = face
    }
}
