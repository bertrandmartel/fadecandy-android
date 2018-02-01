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
import android.widget.Button
import fr.bmartel.fadecandy.R
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar

/**
 * Temperature fragment.
 *
 * @author Bertrand Martel
 */
class TemperatureFragment : MainFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.temperature_fragment, container, false)

        mIsSpark = false

        super.onCreate(view)

        val temperatureSeekbar = view.findViewById<DiscreteSeekBar>(R.id.seekbar_temperature)
        temperatureSeekbar.numericTransformer = object : DiscreteSeekBar.NumericTransformer() {
            override fun transform(value: Int): Int {
                return value * 1
            }
        }
        temperatureSeekbar.setOnProgressChangeListener(object : DiscreteSeekBar.OnProgressChangeListener {

            override fun onProgressChanged(seekBar: DiscreteSeekBar, value: Int, fromUser: Boolean) {
                val color = 0xFFFF00 + value * 255 / 100
                mSingleton?.temperature = color
            }

            override fun onStartTrackingTouch(seekBar: DiscreteSeekBar) {}

            override fun onStopTrackingTouch(seekBar: DiscreteSeekBar) {}
        })

        val value = ((mSingleton?.temperature ?: 0) - 0xFFFF00) * 100 / 255

        temperatureSeekbar.progress = value

        val buttonAllOff = view.findViewById<Button>(R.id.button_all_off)
        buttonAllOff.setOnClickListener({ mSingleton?.clearPixel() })

        return view
    }
}
