package com.cesur.calculatorapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private lateinit var historyTextView: TextView
    private lateinit var currentTextView: TextView

    private var firstOperand: Double? = null
    private var secondOperand: Double? = null
    private var currentOperator: String? = null
    private var isPowerOperation: Boolean = false
    private var isNewOperation: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            firstOperand = savedInstanceState.getDouble("firstOperand")
            secondOperand = savedInstanceState.getDouble("secondOperand")
            currentOperator = savedInstanceState.getString("currentOperator")
            isPowerOperation = savedInstanceState.getBoolean("isPowerOperation")
            isNewOperation = savedInstanceState.getBoolean("isNewOperation")
        }
        setContentView(R.layout.activity_main)

        historyTextView = findViewById(R.id.historyTextView)
        currentTextView = findViewById(R.id.currentTextView)

        setupButtons()
    }

    private fun setupButtons() {
        val buttonIds = listOf(
            R.id.buttonCE, R.id.buttonC, R.id.buttonSqrt, R.id.buttonPower,
            R.id.button7, R.id.button8, R.id.button9, R.id.buttonDivide,
            R.id.button4, R.id.button5, R.id.button6, R.id.buttonMultiply,
            R.id.button1, R.id.button2, R.id.button3, R.id.buttonMinus,
            R.id.buttonPlusMinus, R.id.button0, R.id.buttonDot, R.id.buttonPlus,
            R.id.buttonEqual
        )

        buttonIds.forEach { id ->
            findViewById<Button>(id).setOnClickListener { onButtonClick(it as Button) }
        }
    }

    private fun onButtonClick(button: Button) {
        when (button.id) {
            R.id.buttonCE -> clearEntry()
            R.id.buttonC -> clearAll()
            R.id.buttonEqual -> calculateResult()
            R.id.buttonPlus, R.id.buttonMinus, R.id.buttonMultiply,
            R.id.buttonDivide, R.id.buttonPower -> handleOperator(button.text.toString())

            R.id.buttonSqrt -> handleSqrt()
            R.id.buttonPlusMinus -> toggleSign()
            else -> handleNumberOrDot(button.text.toString())
        }
    }

    private fun handleNumberOrDot(input: String) {
        if (input == "." && currentTextView.text.contains(".")) return

        if (isNewOperation) {
            currentTextView.text = ""
            isNewOperation = false
        }

        currentTextView.append(input)
    }

    private fun handleOperator(op: String) {
        val text = currentTextView.text.toString()

        if (text.isEmpty()) {
            return
        }

        try {
            val currentNumber = text.toDouble()

            if (firstOperand == null) {
                firstOperand = currentNumber
            } else if (currentOperator != null && !isPowerOperation) {
                firstOperand = performOperation(firstOperand!!, currentNumber, currentOperator!!)
            } else if (isPowerOperation) {
                firstOperand = firstOperand?.pow(currentNumber)
                isPowerOperation = false
            }

            currentOperator = op

            if (op == "xʸ") {
                isPowerOperation = true
                historyTextView.text = "$firstOperand $op "
            } else {
                historyTextView.text = "$firstOperand $currentOperator "
            }

            currentTextView.text = ""
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid Number", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSqrt() {
        val text = currentTextView.text.toString()

        if (text.isEmpty()) {
            Toast.makeText(this, "No number to apply sqrt", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val currentNumber = text.toDouble()

            if (currentNumber < 0) {
                Toast.makeText(this, getString(R.string.error_invalid_sqrt), Toast.LENGTH_SHORT)
                    .show()
                return
            }

            val result = sqrt(currentNumber)
            currentTextView.text = removeTrailingZero(result)
            historyTextView.text = "√($currentNumber) = "
            firstOperand = result
            currentOperator = null
            isNewOperation = true
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid Number", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleSign() {
        val text = currentTextView.text.toString()
        if (text.isEmpty()) return

        try {
            var currentNumber = text.toDouble()

            currentNumber = -currentNumber
            currentTextView.text = removeTrailingZero(currentNumber)

            if (firstOperand == null) {
                historyTextView.text = currentTextView.text.toString()
            } else {
                val operatorPart = currentOperator ?: ""
                historyTextView.text = "$firstOperand $operatorPart ${currentTextView.text}"
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid Number", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearEntry() {
        currentTextView.text = "0"
        isNewOperation = true
    }

    private fun clearAll() {
        currentTextView.text = "0"
        historyTextView.text = ""
        firstOperand = null
        secondOperand = null
        currentOperator = null
        isPowerOperation = false
        isNewOperation = true
    }

    private fun calculateResult() {
        val text = currentTextView.text.toString()

        if (currentOperator == null && !isPowerOperation) {
            return
        }

        if (text.isEmpty()) {
            Toast.makeText(this, "No second operand", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val secondNumber = text.toDouble()

            if (isPowerOperation) {
                firstOperand = firstOperand?.pow(secondNumber)
                historyTextView.text = "$firstOperand ="
            } else if (currentOperator != null) {
                firstOperand = performOperation(firstOperand!!, secondNumber, currentOperator!!)
                historyTextView.text = "$firstOperand ="
            }

            currentTextView.text = removeTrailingZero(firstOperand!!)
            currentOperator = null
            isPowerOperation = false
            isNewOperation = true
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid Number", Toast.LENGTH_SHORT).show()
        } catch (e: ArithmeticException) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            clearAll()
        }
    }

    private fun performOperation(first: Double, second: Double, operator: String): Double {
        return when (operator) {
            "+" -> first + second
            "-" -> first - second
            "*" -> first * second
            "/" -> {
                if (second == 0.0) throw ArithmeticException(getString(R.string.error_division_by_zero))
                first / second
            }

            else -> first
        }
    }

    private fun removeTrailingZero(number: Double): String {
        return if (number == number.toLong().toDouble()) {
            number.toLong().toString()
        } else {
            String.format("%.5f", number).trimEnd('0').trimEnd('.')
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putDouble("firstOperand", firstOperand ?: 0.0)
        outState.putDouble("secondOperand", secondOperand ?: 0.0)
        outState.putString("currentOperator", currentOperator)
        outState.putBoolean("isPowerOperation", isPowerOperation)
        outState.putBoolean("isNewOperation", isNewOperation)
        outState.putString("history", historyTextView.text.toString())
        outState.putString("current", currentTextView.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        firstOperand = savedInstanceState.getDouble("firstOperand")
        secondOperand = savedInstanceState.getDouble("secondOperand")
        currentOperator = savedInstanceState.getString("currentOperator")
        isPowerOperation = savedInstanceState.getBoolean("isPowerOperation")
        isNewOperation = savedInstanceState.getBoolean("isNewOperation")
        historyTextView.text = savedInstanceState.getString("history", "")
        currentTextView.text = savedInstanceState.getString("current", "0")
    }
}