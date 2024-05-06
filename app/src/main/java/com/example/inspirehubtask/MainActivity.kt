package com.example.inspirehubtask

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import java.io.IOException
import java.io.InputStream


class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var zoomIn: ImageButton
    private lateinit var zoomOut: ImageButton
    private lateinit var letters :ImageButton
    private lateinit var background:ImageButton
    private lateinit var seekBarRed: SeekBar
    private lateinit var seekBarGreen: SeekBar
    private lateinit var seekBarBlue: SeekBar
    private lateinit var changeColorButton:ImageButton
    private lateinit var textTestChanges:TextView
    private lateinit var colorChangeLayout: LinearLayout
    private lateinit var whatChange:String
    private var redLetter=0
    private var greenLetter=0
    private var blueLetter=0
    private var redBackground=255
    private var greenBackground=255
    private var blueBackground=255
    private var colorChange:Int=0
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var zoom = 100
        webView = findViewById(R.id.webView)
        zoomIn = findViewById(R.id.zoomInButton)
        zoomOut = findViewById(R.id.zoomOutButton)
        letters=findViewById(R.id.lettersButton)
        background=findViewById(R.id.backgroundButton)
        seekBarRed = findViewById(R.id.seekBarRed)
        seekBarGreen = findViewById(R.id.seekBarGreen)
        seekBarBlue = findViewById(R.id.seekBarBlue)
        colorChangeLayout=findViewById(R.id.colorPopUp)
        textTestChanges=findViewById(R.id.textTestChangeColor)
        changeColorButton=findViewById(R.id.changeColorButton)
        webView.settings.javaScriptEnabled = true
        // Load and display the Word document from assets
        loadWordDocumentFromAssets("أجمل القصص القصيرة.docx")

        zoomIn.setOnClickListener {
            if (zoom < 520) {
                zoom = zoom + 20
                Log.d("zoom", zoom.toString())
                webView.evaluateJavascript("javascript:document.body.style.zoom='${zoom}%'") { result ->
                    // You can handle the result here if needed
                }
            }

        }
        zoomOut.setOnClickListener {
            if (zoom > 100) {
                zoom = zoom - 20
                webView.evaluateJavascript("javascript:document.body.style.zoom='${zoom}%'") { result ->
                    // You can handle the result here if needed
                }
            }
        }
        letters.setOnClickListener {
            whatChange="letters"
            if (colorChangeLayout.isVisible){
                colorChangeLayout.visibility=View.GONE
            }else{
                colorChangeLayout.visibility=View.VISIBLE
            }
            seekBarRed.progress=redLetter
            seekBarGreen.progress=greenLetter
            seekBarBlue.progress=blueLetter
        }
        background.setOnClickListener {
            whatChange="background"
            if (colorChangeLayout.isVisible){
                colorChangeLayout.visibility=View.GONE
            }else{
                colorChangeLayout.visibility=View.VISIBLE
            }
            seekBarRed.progress=redBackground
            seekBarGreen.progress=greenBackground
            seekBarBlue.progress=blueBackground
        }
        seekBarRed.setOnSeekBarChangeListener(seekBarChangeListener)
        seekBarGreen.setOnSeekBarChangeListener(seekBarChangeListener)
        seekBarBlue.setOnSeekBarChangeListener(seekBarChangeListener)
        changeColorButton.setOnClickListener {
            if(whatChange=="letters"){
                webView.evaluateJavascript("javascript:document.body.style.color='rgb($redLetter,$greenLetter,$blueLetter)';"){}
            }else if(whatChange=="background"){
                webView.evaluateJavascript("javascript:document.body.style.backgroundColor='rgb($redBackground,$greenBackground,$blueBackground)';"){}
            }
        }

    }
    private val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            // Update color preview based on RGB values
            val red = seekBarRed.progress
            val green = seekBarGreen.progress
            val blue = seekBarBlue.progress
            colorChange = Color.rgb(red, green, blue)
            if(whatChange=="letters"){
                textTestChanges.setTextColor(colorChange)
                redLetter=red
                greenLetter=green
                blueLetter=blue
            }else if(whatChange=="background"){
                textTestChanges.setBackgroundColor(colorChange)
                redBackground=red
                greenBackground=green
                blueBackground=blue
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    }

    private fun loadWordDocumentFromAssets(fileName: String) {
        try {
            // Open the Word document from assets
            val inputStream: InputStream = assets.open(fileName)

            // Load the Word document using Apache POI
            val document = XWPFDocument(inputStream)

            // Convert the Word document to HTML with inline CSS styles
            val htmlContent = convertWordToHtml(document)

            // Display the HTML content in WebView
            webView.settings.javaScriptEnabled = true
            webView.webViewClient = WebViewClient()
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)

            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

        private fun convertWordToHtml(document: XWPFDocument): String {
        val htmlContent = StringBuilder()
        // Start HTML document
        htmlContent.append("<html><head><style>")
        htmlContent.append("body { font-family: Arial, sans-serif; }") // Set default font-family
        htmlContent.append("</style></head><body>")

        // Process paragraphs and runs
        for (paragraph in document.paragraphs) {
            // Get paragraph alignment (default to left if not specified)
            val alignment = getParagraphAlignment(paragraph)

            // Start HTML paragraph with alignment style
            htmlContent.append("<p style=\"text-align: $alignment;\">")

            for (run in paragraph.runs) {
                val text = run.text()
                val bold = run.isBold
                val italic = run.isItalic
                val color = run.color
                val fontSize = run.fontSize;
                // Generate inline CSS styles based on run properties
                val style = StringBuilder()
                if (bold) style.append("font-weight: bold;")
                if (italic) style.append("font-style: italic;")
                if (color != null && color.isNotEmpty()) style.append("color: #$color;")
                if (fontSize > 0) style.append("font-size: $fontSize ;")
                // Wrap text in span with inline styles
                htmlContent.append("<span style=\"${style.toString()}\">")
                htmlContent.append(text)
                htmlContent.append("</span>")
            }

            // End HTML paragraph
            htmlContent.append("</p>")
        }

        // End HTML document
        htmlContent.append("</body></html>")

        return htmlContent.toString()
    }

    private fun getParagraphAlignment(paragraph: XWPFParagraph): String {
        return when (paragraph.alignment) {
            // Handle different alignment types
            ParagraphAlignment.LEFT -> "left"
            ParagraphAlignment.RIGHT -> "right"
            ParagraphAlignment.CENTER -> "center"
            ParagraphAlignment.BOTH -> "justify" // Justified alignment
            else -> "right" // Default to left alignment
        }
    }
}
