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
class PulseFragment : MainFragment(), ColorPicker.OnColorChangedListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.pulse_fragment, container, false)
        super.onCreateCommon()

        //init color picker
        val picker = view.findViewById<ColorPicker>(R.id.picker)
        picker.onColorChangedListener = this
        picker.showOldCenterColor = false
        picker.color = mSingleton?.color ?: 0

        val delaySeekbar = view.findViewById<DiscreteSeekBar>(R.id.seekbar_delay)
        delaySeekbar.numericTransformer = object : DiscreteSeekBar.NumericTransformer() {
            override fun transform(value: Int): Int {
                return value * 1
            }
        }

        delaySeekbar.setOnProgressChangeListener(object : DiscreteSeekBar.OnProgressChangeListener {

            override fun onProgressChanged(seekBar: DiscreteSeekBar, value: Int, fromUser: Boolean) {
                mSingleton?.pulseDelay = value
            }

            override fun onStartTrackingTouch(seekBar: DiscreteSeekBar) {}

            override fun onStopTrackingTouch(seekBar: DiscreteSeekBar) {

            }
        })
        delaySeekbar.progress = mSingleton?.pulseDelay ?: 0

        val pauseSeekbar = view.findViewById<DiscreteSeekBar>(R.id.seekbar_pause)
        pauseSeekbar.numericTransformer = object : DiscreteSeekBar.NumericTransformer() {
            override fun transform(value: Int): Int {
                return value * 1
            }
        }
        pauseSeekbar.setOnProgressChangeListener(object : DiscreteSeekBar.OnProgressChangeListener {

            override fun onProgressChanged(seekBar: DiscreteSeekBar, value: Int, fromUser: Boolean) {
                mSingleton?.pulsePause = value
            }

            override fun onStartTrackingTouch(seekBar: DiscreteSeekBar) {}

            override fun onStopTrackingTouch(seekBar: DiscreteSeekBar) {}
        })
        pauseSeekbar.progress = mSingleton?.pulsePause ?: 0
        return view
    }

    override fun onColorChanged(color: Int) {
        mSingleton?.pulse(color)
    }

    override fun onServerFirstStart() {
    }
}
