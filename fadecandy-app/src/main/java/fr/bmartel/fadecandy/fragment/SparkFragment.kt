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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.larswerkman.holocolorpicker.ColorPicker

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar

import fr.bmartel.fadecandy.R

/**
 * Spark fragment.
 *
 * @author Bertrand Martel
 */
class SparkFragment : MainFragment(), ColorPicker.OnColorChangedListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.spark_fragment, container, false)
        mIsSpark = true
        super.onCreate(view)

        //init color picker
        val picker = view.findViewById<ColorPicker>(R.id.picker)
        picker.onColorChangedListener = this
        picker.showOldCenterColor = false
        picker.color = mSingleton?.color ?: 0

        val speedSeekbar = view.findViewById<DiscreteSeekBar>(R.id.seekbar_speed)

        speedSeekbar.numericTransformer = object : DiscreteSeekBar.NumericTransformer() {
            override fun transform(value: Int): Int {
                return value * 1
            }
        }

        speedSeekbar.setOnProgressChangeListener(object : DiscreteSeekBar.OnProgressChangeListener {
            override fun onProgressChanged(seekBar: DiscreteSeekBar, value: Int, fromUser: Boolean) {
                mSingleton?.speed = value
            }

            override fun onStartTrackingTouch(seekBar: DiscreteSeekBar) {}

            override fun onStopTrackingTouch(seekBar: DiscreteSeekBar) {

            }
        })

        val spanSeekbar = view.findViewById<DiscreteSeekBar>(R.id.seekbar_span)

        spanSeekbar.numericTransformer = object : DiscreteSeekBar.NumericTransformer() {
            override fun transform(value: Int): Int {
                return value * 1
            }
        }

        spanSeekbar.setOnProgressChangeListener(object : DiscreteSeekBar.OnProgressChangeListener {
            override fun onProgressChanged(seekBar: DiscreteSeekBar, value: Int, fromUser: Boolean) {
                mSingleton?.sparkSpan = value
            }

            override fun onStartTrackingTouch(seekBar: DiscreteSeekBar) {}

            override fun onStopTrackingTouch(seekBar: DiscreteSeekBar) {
            }
        })

        speedSeekbar.progress = mSingleton?.speed ?: 0
        spanSeekbar.progress = mSingleton?.sparkSpan ?: 0
        mSingleton?.spark(mSingleton?.color ?: 0)
        return view
    }

    override fun onColorChanged(color: Int) {
        mSingleton?.spark(color)
    }

    override fun onServerFirstStart() {
    }
}
