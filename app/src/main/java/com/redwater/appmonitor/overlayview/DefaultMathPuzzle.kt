package com.redwater.appmonitor.overlayview

import com.redwater.appmonitor.data.model.OverlayQueData
import com.redwater.appmonitor.data.model.TextOption
import com.redwater.appmonitor.data.model.toJsonString
import kotlin.random.Random

class DefaultMathPuzzle {

    private val difficulty: Int = Random.nextInt(1, 3)// defines number of operations, including = operator
    private val time = difficulty*+3 //in sec

    private fun getOperators(): List<Operator>{
        return List(difficulty){ Operator.values().get(Random.nextInt(0,4))}
    }

    fun generateQuestion(): String{
        val operators = getOperators()
        val firstNum = Random.nextInt(0, 20)
        val questionBuilder = StringBuilder()
        questionBuilder.append(firstNum)
        var answer:Float = firstNum.toFloat()
        operators.forEachIndexed { index, operator ->
            when (operator) {
                Operator.ADD -> {
                    val nextNUm = Random.nextInt(0, 100)
                    questionBuilder.append(" + $nextNUm")
                    answer += nextNUm.toFloat()
                }

                Operator.SUB -> {
                    val nextNUm = Random.nextInt(0, 100)
                    questionBuilder.append(" - $nextNUm")
                    answer -= nextNUm.toFloat()
                }

                Operator.MULTI -> {
                    val nextNUm = Random.nextInt(0, 10)
                    questionBuilder.append(" x $nextNUm")
                    answer *= nextNUm.toFloat()
                }

                Operator.DIV -> {
                    val nextNUm = Random.nextInt(0, 10)
                    questionBuilder.append(" / $nextNUm")
                    answer /= nextNUm.toFloat()
                }

            }
        }
        questionBuilder.append(" = ?")
        val falseAnswers = List(3){ answer * (Random.nextFloat())}
        val positionOfCorrectAnswer = Random.nextInt(0, 4)

        val optionsList = mutableListOf<TextOption>()
        var falseAnswerIndex=0

        for (num in 0..3){
            if (num == positionOfCorrectAnswer){
                optionsList.add(TextOption(text = "%.1f".format(answer), textColor = null, isAnswer = true))
            }else{
                optionsList.add(TextOption(text = "%.1f".format(falseAnswers[falseAnswerIndex]), textColor = null, isAnswer = false))
                falseAnswerIndex += 1
            }
        }

        return OverlayQueData(que = questionBuilder.toString(), time = time, optionsList = optionsList).toJsonString()

    }
}

enum class Operator{
    ADD,
    SUB,
    MULTI,
    DIV
}