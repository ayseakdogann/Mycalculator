package com.example.mycalculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mycalculator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val expression = StringBuilder() // StringBuilder kullanımı

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val buttons = listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4,
            binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9,
            binding.btnPlus, binding.btnMinus, binding.btnMultiply, binding.btnDivide,
            binding.btnDot
        )

        // Tuşlara tıklama olayları
        for (button in buttons) {
            button.setOnClickListener {
                expression.append(button.text)
                binding.tvResult.text = expression.toString()
            }
        }

        binding.btnClear.setOnClickListener {
            expression.clear()
            binding.tvResult.text = "0"
        }

        binding.btnDelete.setOnClickListener {
            if (expression.isNotEmpty()) {
                expression.deleteCharAt(expression.length - 1)
                binding.tvResult.text = if (expression.isEmpty()) "0" else expression.toString()
            }
        }

        binding.btnEquals.setOnClickListener {
            try {
                val result = evaluateExpression(expression.toString())
                binding.tvResult.text = result.toString()
                expression.clear().append(result)
            } catch (e: Exception) {
                binding.tvResult.text = "Hata: ${e.message}"
                expression.clear()
            }
        }
    }

    // İfade çözümleme ve hesaplama fonksiyonu (Shunting-yard algoritması + postfix hesaplama)
    private fun evaluateExpression(expr: String): Double {
        val outputQueue = mutableListOf<String>()
        val operatorStack = mutableListOf<Char>()
        val operators = mapOf('+' to 1, '-' to 1, '*' to 2, '/' to 2)

        var numberBuffer = ""

        for (ch in expr) {
            when {
                ch.isDigit() || ch == '.' -> numberBuffer += ch
                ch in operators -> {
                    if (numberBuffer.isNotEmpty()) {
                        outputQueue.add(numberBuffer)
                        numberBuffer = ""
                    }
                    while (operatorStack.isNotEmpty() &&
                        operators.getValue(ch) <= operators.getValue(operatorStack.last())) {
                        outputQueue.add(operatorStack.removeAt(operatorStack.lastIndex).toString())
                    }
                    operatorStack.add(ch)
                }
                else -> throw IllegalArgumentException("Geçersiz karakter: $ch")
            }
        }

        if (numberBuffer.isNotEmpty()) outputQueue.add(numberBuffer)
        while (operatorStack.isNotEmpty()) outputQueue.add(operatorStack.removeAt(operatorStack.lastIndex).toString())

        val calcStack = mutableListOf<Double>()
        for (token in outputQueue) {
            when (token) {
                "+" -> calcStack.add(calcStack.removeAt(calcStack.lastIndex - 1) + calcStack.removeAt(calcStack.lastIndex))
                "-" -> {
                    val b = calcStack.removeAt(calcStack.lastIndex)
                    val a = calcStack.removeAt(calcStack.lastIndex)
                    calcStack.add(a - b)
                }
                "*" -> calcStack.add(calcStack.removeAt(calcStack.lastIndex - 1) * calcStack.removeAt(calcStack.lastIndex))
                "/" -> {
                    val b = calcStack.removeAt(calcStack.lastIndex)
                    val a = calcStack.removeAt(calcStack.lastIndex)
                    if (b == 0.0) throw ArithmeticException("Bölme hatası: Sıfıra bölme")
                    calcStack.add(a / b)
                }
                else -> calcStack.add(token.toDouble())
            }
        }

        return calcStack.last()
    }
}
