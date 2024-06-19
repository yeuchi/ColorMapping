package com.ctyeung.trianglegradient

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ctyeung.trianglegradient.databinding.ActivityMainBinding

/**
 * view-source:https://yeuchi.github.io/TriangleGradient/index.html
 */
class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        mBinding.apply {
            btnDeleteLine.setOnClickListener {
                paper.apply {
                    points.clear()
                    invalidate()
                }
            }
        }
    }
}