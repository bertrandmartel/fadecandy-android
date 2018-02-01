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
package fr.bmartel.fadecandy.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

import fr.bmartel.fadecandy.R

/**
 * Adapter for open source projects
 *
 * @author Bertrand Martel
 */
class OpenSourceItemAdapter(context: Context) : BaseAdapter() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return COMPONENTS.size
    }

    override fun getItem(position: Int): Any {
        return COMPONENTS[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var view: View? = convertView
        if (view == null) {
            view = mInflater.inflate(R.layout.open_source_items, parent, false)
        }
        val title = view?.findViewById<TextView>(R.id.title)
        val url = view?.findViewById<TextView>(R.id.url)

        title?.text = COMPONENTS[position][0]
        url?.text = COMPONENTS[position][1]

        return view
    }

    companion object {
        private val COMPONENTS = arrayOf(
                arrayOf("fadecandy", "https://github.com/scanlime/fadecandy"),
                arrayOf("rapidjson", "https://github.com/scanlime/rapidjson"),
                arrayOf("libwebsockets", "https://github.com/bertrandmartel/libwebsockets"),
                arrayOf("DiscreteSeekBar", "https://github.com/AnderWeb/discreteSeekBar"),
                arrayOf("BottomBar", "https://github.com/roughike/BottomBar"),
                arrayOf("Android Holo ColorPicker", "https://github.com/LarsWerkman/HoloColorPicker"),
                arrayOf("Open Pixel Control Library", "https://github.com/bertrandmartel/opc-java"),
                arrayOf("AndroidAsync", "https://github.com/koush/AndroidAsync"),
                arrayOf("Ace editor", "https://github.com/ajaxorg/ace"),
                arrayOf("JS Beautifier", "https://github.com/beautify-web/js-beautify"),
                arrayOf("Led Icon by Kenneth Appiah, CA (Pulic Domain)", "https://thenounproject.com/search/?q=led&i=3156")
        )
    }
}