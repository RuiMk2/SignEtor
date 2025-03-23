/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.mediapipe.examples.gesturerecognizer.fragment

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.mediapipe.examples.gesturerecognizer.databinding.ItemGestureRecognizerResultBinding
import com.google.mediapipe.tasks.components.containers.Category
import java.util.Locale
import kotlin.math.min

class GestureRecognizerResultsAdapter :
    RecyclerView.Adapter<GestureRecognizerResultsAdapter.ViewHolder>() {
    companion object {
        private const val NO_VALUE = "--"
    }

    private var adapterCategories: MutableList<Category?> = mutableListOf()
    private var adapterSize: Int = 0

    var pastLetter: String = ""
    var word: String = ""
    //R-chan: To future dev, this is the array for two frame check for movement
    //this would've been better if it was an Object
    //but due to time constraints I went with this
    var firstFrames = arrayOf("I","Z 1", "Goodbye 1", "Hello 1", "No 1", "Please 1"
        , "Sorry 1", "Thank you 1", "Yes 1", "You are Welcome 1")
    var secondFrames = arrayOf("J","Z 2", "Goodbye 2", "Hello 2", "No 2", "Please 2"
    , "Sorry 2", "Thank you 2", "Yes 2", "You are Welcome 2")
    var finalGesture = arrayOf("J","Z", " Goodbye ", " Hello ", " No ", " Please "
        , " Sorry ", " Thank you ", " Yes ", " You are Welcome ")

    @SuppressLint("NotifyDataSetChanged")
    fun updateResults(categories: List<Category>?) {
        adapterCategories = MutableList(adapterSize) { null }
        if (categories != null) {
            val sortedCategories = categories.sortedByDescending { it.score() }
            val min = min(sortedCategories.size, adapterCategories.size)
            for (i in 0 until min) {
                adapterCategories[i] = sortedCategories[i]
            }
            adapterCategories.sortedBy { it?.index() }
            notifyDataSetChanged()
        }
    }

    fun updateAdapterSize(size: Int) {
        adapterSize = size
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemGestureRecognizerResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        adapterCategories[position].let { category ->
            holder.bind(category?.categoryName(), category?.score())
        }
    }

    override fun getItemCount(): Int = adapterCategories.size
    inner class ViewHolder(private val binding: ItemGestureRecognizerResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(label: String?, score: Float?,) {
            with(binding) {
                tvLabel.text = label ?: NO_VALUE
                var labelToStringBS = ""
                labelToStringBS += tvLabel.text

                if (labelToStringBS in firstFrames){
                    Thread.sleep(300)
                }
                else if(labelToStringBS in secondFrames){
                    if(pastLetter == firstFrames[secondFrames.indexOf(labelToStringBS)]){
                        word = word.removeSuffix(firstFrames[secondFrames.indexOf(labelToStringBS)])
                        word += finalGesture[secondFrames.indexOf(labelToStringBS)]
                    }
                    else{
                        labelToStringBS = ""
                    }
                }
                if (labelToStringBS != pastLetter && labelToStringBS !in secondFrames && score != null && score.compareTo(0.75) > -1 ){
                    word += labelToStringBS}
                if (labelToStringBS != "" && score != null && score.compareTo(0.75) > -1 ){
                    pastLetter = labelToStringBS}
                word = word.replace("--", "")
                tvLabel.text = word
                if (labelToStringBS == "Clear"){
                    word = ""
                    Thread.sleep(1000)
                }


                tvScore.text = if (score != null && score.compareTo(0.75) > -1 ) String.format(
                    Locale.US,
                    "%.2f",
                    score
                ) else NO_VALUE
            }
        }
    }
}
