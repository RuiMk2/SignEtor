package com.google.mediapipe.examples.gesturerecognizer.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.mediapipe.examples.gesturerecognizer.databinding.ItemGestureRecognizerResultBinding
import com.google.mediapipe.tasks.components.containers.Category
import java.util.Locale
import kotlin.math.min
class GestureRecognizerResultsAdapter:
    RecyclerView.Adapter<GestureRecognizerResultsAdapter.ViewHolder>(){
    companion object {
        private const val NO_VALUE = "--"
    }
    private var textToSpeech: TextToSpeech? = null
    private fun initTextToSpeech(context: Context) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported")
                }
            } else {
                Log.e("TTS", "Initialization failed")
            }
        }
    }
    private var adapterCategories: MutableList<Category?> = mutableListOf()
    private var adapterSize: Int = 0
    private var word: String = ""
    var pastLetter: String = ""
    //Dev note: I know this isn't efficient but there isn't much time ToT
    var needsFirstFrame = arrayOf("J", "No 2", "Z 2", "Yes 2", "Hello 2", "Thank You 2", "Goodbye 2")
    var needsSecondFrame = arrayOf("No 1", "Z 1", "Thank You 1", "Goodbye 1")
    var words = arrayOf("Yes", "No", "I Love You", "Hello", "Good", "Bad")

    fun speakWord(text: String) {
        if (text.isNotEmpty()) {
            textToSpeech?.stop()
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }

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
        initTextToSpeech(parent.context)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.updateWord(word)
        adapterCategories[position].let { category ->
            holder.bind(category?.categoryName(), category?.score())
        }
    }

    override fun getItemCount(): Int = adapterCategories.size
    inner class ViewHolder(private val binding: ItemGestureRecognizerResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            // Set up click listeners only once when the ViewHolder is created
            binding.btnBackspace.setOnClickListener {
                if (word.isNotEmpty()) {
                    word = word.substring(0, word.length - 1)
                    Log.d("ButtonDebug", "Backspace clicked")
                    updateDisplayAndLabel()
                }
            }

            binding.btnClear.setOnClickListener {
                word = ""
                Log.d("ButtonDebug", "Clear clicked")
                updateDisplayAndLabel()
            }
        }

        fun updateWord(currentWord: String) {
            word = currentWord
        }

        private fun updateDisplayAndLabel() {
            binding.tvLabel.text = word
        }

        fun bind(label: String?, score: Float?) {
            var labelToStringBS = ""
            with(binding) {
                tvLabel.text = label ?: NO_VALUE
                labelToStringBS += tvLabel.text
                if (labelToStringBS != pastLetter &&
                    labelToStringBS != "--" &&
                    labelToStringBS !in needsFirstFrame &&
                    labelToStringBS !in needsSecondFrame &&
                    score != null &&
                    (score.compareTo(0.90) > -1 || labelToStringBS == "H") ){
                    if (word in words){
                        word = ""
                    }
                    word += labelToStringBS
                    pastLetter = labelToStringBS
                    speakWord(labelToStringBS)
                    if("_" in word) {
                        word = word.replace("_", " ")
                    }
                    if (labelToStringBS == "Space"){
                        word = word.replace("Space", "_")
                        pastLetter = labelToStringBS
                    }

                }
                //if a two frame gesture is detected check if the first frame matches
                //the required frame
                if (labelToStringBS == "J"){
                    if(pastLetter == "I"){
                        word = word.substring(0, word.length - 1)
                        word += "J"
                        pastLetter = "J"
                        speakWord(labelToStringBS)
                    }
                }
                // if gesture needs second frame only add it to pastLetter
                if (labelToStringBS in needsSecondFrame){
                    pastLetter = labelToStringBS
                }
                if (labelToStringBS == "No 2"){
                    if (pastLetter == "No 1"){
                        word = "No"
                        pastLetter = "No"
                        speakWord(word)
                    }
                }
                if (labelToStringBS == "Z 2" || labelToStringBS == "No 2"){
                    if (pastLetter == "Z 1"){
                        word += "Z"
                        pastLetter = "Z 2"
                        speakWord("Z")
                    }
                }
                if (labelToStringBS == "Yes 2"){
                    if (pastLetter == "A" || pastLetter == "S"){
                        word = "Yes"
                        pastLetter = "Yes 2"
                        speakWord(word)
                    }
                }
                if (labelToStringBS == "Hello 2"){
                    if (pastLetter == "B"){
                        word = "Hello"
                        pastLetter = "Hello 2"
                        speakWord(word)
                    }
                }
                if(labelToStringBS != pastLetter &&
                    labelToStringBS == "I Love You" &&
                    score != null &&
                    score.compareTo(0.90) > -1){
                    word = "I Love You"
                    speakWord(word)
                }
                if(labelToStringBS != pastLetter &&
                    labelToStringBS == "Good" &&
                    score != null &&
                    score.compareTo(0.90) > -1){
                    word = "Good"
                    speakWord(word)
                }
                if(labelToStringBS != pastLetter &&
                    labelToStringBS == "Bad" &&
                    score != null &&
                    score.compareTo(0.90) > -1){
                    word = "Bad"
                    speakWord(word)
                }
                tvLabel.text = word
                tvScore.text = if (score != null && score.compareTo(0.90) > -1 ) String.format(
                    Locale.US,
                    "%.2f",
                    score
                ) else NO_VALUE
            }
        }
    }
}
