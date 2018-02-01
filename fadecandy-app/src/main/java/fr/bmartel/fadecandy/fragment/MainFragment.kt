/**
 * The MIT License (MIT)
 *
 *
 * Copyright (c) 2016-2018 Bertrand Martel
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.fadecandy.fragment

import android.view.View

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar

import fr.bmartel.fadecandy.FadecandySingleton
import fr.bmartel.fadecandy.R
import fr.bmartel.fadecandy.activity.BaseActivity
import fr.bmartel.fadecandy.inter.IFragment

/**
 * Common fragment.
 *
 * @author Bertrand Martel
 */
abstract class MainFragment : android.support.v4.app.Fragment(), IFragment {

    protected var mSingleton: FadecandySingleton? = null

    lateinit var mBrightnessSeekBar: DiscreteSeekBar

    protected var mIsSpark = false

    fun onCreate(view: View) {
        onCreateCommon()

        mBrightnessSeekBar = view.findViewById(R.id.seekbar_brightness)
        mBrightnessSeekBar.numericTransformer = object : DiscreteSeekBar.NumericTransformer() {
            override fun transform(value: Int): Int {
                return value * 1
            }
        }
        mBrightnessSeekBar.setOnProgressChangeListener(object : DiscreteSeekBar.OnProgressChangeListener {

            override fun onProgressChanged(seekBar: DiscreteSeekBar, value: Int, fromUser: Boolean) {
                if ((mSingleton?.isServerMode == true) && mSingleton?.isServerRunning != true) {
                    return
                }
                if (mIsSpark) {
                    mSingleton?.setColorCorrectionSpark(value = value)
                } else {
                    mSingleton?.setColorCorrection(value = value, force = false)
                }
            }

            override fun onStartTrackingTouch(seekBar: DiscreteSeekBar) {}

            override fun onStopTrackingTouch(seekBar: DiscreteSeekBar) {

            }
        })

        if (mSingleton?.currentColorCorrection == -1) {
            mBrightnessSeekBar.progress = 100
        } else {
            mBrightnessSeekBar.progress = mSingleton?.currentColorCorrection ?: 0
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as BaseActivity).setToolbarTitle()
    }

    fun onCreateCommon() {
        mSingleton = FadecandySingleton.getInstance(context = activity!!.applicationContext)
    }

    override fun onServerFirstStart() {
        if (mSingleton?.currentColorCorrection == -1) {
            mBrightnessSeekBar.progress = 100
        } else {
            mBrightnessSeekBar.progress = mSingleton?.currentColorCorrection ?: 0
        }
    }
}
