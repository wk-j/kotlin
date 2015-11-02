/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.idea.completion.handlers

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import org.jetbrains.kotlin.idea.core.formatter.KotlinCodeStyleSettings
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtTypeArgumentList
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.types.KotlinType

class GenerateLambdaInfo(val lambdaType: KotlinType, val explicitParameters: Boolean)

sealed class KotlinFunctionInsertHandler : KotlinCallableInsertHandler() {

    class Normal(
            val inputTypeArguments: Boolean,
            val inputValueArguments: Boolean,
            val argumentText: String = "",
            val lambdaInfo: GenerateLambdaInfo? = null,
            val argumentsOnly: Boolean = false
    ) : KotlinFunctionInsertHandler() {
        init {
            if (lambdaInfo != null) {
                assert(argumentText == "")
            }
        }

        //TODO: add 'data' or special annotation when supported
        fun copy(
                inputTypeArguments: Boolean = this.inputTypeArguments,
                inputValueArguments: Boolean = this.inputValueArguments,
                argumentText: String = this.argumentText,
                lambdaInfo: GenerateLambdaInfo? = this.lambdaInfo,
                argumentsOnly: Boolean = this.argumentsOnly
        ) = Normal(inputTypeArguments, inputValueArguments, argumentText, lambdaInfo, argumentsOnly)

        override fun handleInsert(context: InsertionContext, item: LookupElement) {
            if (!argumentsOnly) {
                super.handleInsert(context, item)
            }

            val psiDocumentManager = PsiDocumentManager.getInstance(context.project)
            psiDocumentManager.commitAllDocuments()
            psiDocumentManager.doPostponedOperationsAndUnblockDocument(context.document)

            val startOffset = context.getStartOffset()
            val element = context.getFile().findElementAt(startOffset) ?: return

            addArguments(context, element)
        }

        private fun addArguments(context : InsertionContext, offsetElement : PsiElement) {
            val completionChar = context.getCompletionChar()
            if (completionChar == '(') { //TODO: more correct behavior related to braces type
                context.setAddCompletionChar(false)
            }

            var offset = context.tailOffset
            val document = context.document
            val editor = context.editor
            val project = context.project
            val chars = document.charsSequence

            val insertLambda = lambdaInfo != null && completionChar != '(' && !(completionChar == '\t' && chars.isCharAt(offset, '('))

            val openingBracket = if (insertLambda) '{' else '('
            val closingBracket = if (insertLambda) '}' else ')'

            var insertTypeArguments = inputTypeArguments && (completionChar == '\n' || completionChar == '\r' || completionChar == Lookup.REPLACE_SELECT_CHAR)

            if (completionChar == Lookup.REPLACE_SELECT_CHAR) {
                val offset1 = chars.skipSpaces(offset)
                if (offset1 < chars.length()) {
                    if (chars[offset1] == '<') {
                        PsiDocumentManager.getInstance(project).commitDocument(document)
                        val token = context.getFile().findElementAt(offset1)!!
                        if (token.getNode().getElementType() == KtTokens.LT) {
                            val parent = token.getParent()
                            if (parent is KtTypeArgumentList && parent.getText().indexOf('\n') < 0/* if type argument list is on multiple lines this is more likely wrong parsing*/) {
                                offset = parent.endOffset
                                insertTypeArguments = false
                            }
                        }
                    }
                }
            }

            if (insertLambda && lambdaInfo!!.explicitParameters) {
                insertTypeArguments = false
            }

            if (insertTypeArguments) {
                document.insertString(offset, "<>")
                editor.caretModel.moveToOffset(offset + 1)
                offset += 2
            }

            var openingBracketOffset = chars.indexOfSkippingSpace(openingBracket, offset)
            var closeBracketOffset = openingBracketOffset?.let { chars.indexOfSkippingSpace(closingBracket, it + 1) }
            var inBracketsShift = 0

            if (insertLambda && lambdaInfo!!.explicitParameters && closeBracketOffset == null) {
                openingBracketOffset = null
            }

            if (openingBracketOffset == null) {
                if (insertLambda) {
                    if (completionChar == ' ' || completionChar == '{') {
                        context.setAddCompletionChar(false)
                    }

                    if (isInsertSpacesInOneLineFunctionEnabled(project)) {
                        document.insertString(offset, " {  }")
                        inBracketsShift = 1
                    }
                    else {
                        document.insertString(offset, " {}")
                    }
                }
                else {
                    document.insertString(offset, "()")
                }
                PsiDocumentManager.getInstance(project).commitDocument(document)

                openingBracketOffset = chars.indexOfSkippingSpace(openingBracket, offset)!!
                closeBracketOffset = chars.indexOfSkippingSpace(closingBracket, openingBracketOffset + 1)!!
            }

            if (insertLambda && lambdaInfo!!.explicitParameters) {
                insertLambdaTemplate(context, TextRange(openingBracketOffset, closeBracketOffset!! + 1), lambdaInfo!!.lambdaType)
                return
            }

            document.insertString(openingBracketOffset + 1, argumentText)
            if (closeBracketOffset != null) {
                closeBracketOffset += argumentText.length()
            }

            if (!insertTypeArguments) {
                if (shouldPlaceCaretInBrackets(completionChar) || closeBracketOffset == null) {
                    editor.caretModel.moveToOffset(openingBracketOffset + 1 + inBracketsShift)
                    if (!insertLambda) {
                        AutoPopupController.getInstance(project)?.autoPopupParameterInfo(editor, offsetElement)
                    }
                }
                else {
                    editor.caretModel.moveToOffset(closeBracketOffset + 1)
                }
            }
        }

        private fun shouldPlaceCaretInBrackets(completionChar: Char): Boolean {
            if (completionChar == ',' || completionChar == '.' || completionChar == '=') return false
            if (completionChar == '(') return true
            return inputValueArguments || lambdaInfo != null
        }

        private fun isInsertSpacesInOneLineFunctionEnabled(project: Project)
                = CodeStyleSettingsManager.getSettings(project).getCustomSettings(javaClass<KotlinCodeStyleSettings>())!!.INSERT_WHITESPACES_IN_SIMPLE_ONE_LINE_METHOD
    }

    object Infix : KotlinFunctionInsertHandler() {
        public override fun handleInsert(context: InsertionContext, item: LookupElement) {
            super.handleInsert(context, item)

            if (context.completionChar == ' ') {
                context.setAddCompletionChar(false)
            }

            val tailOffset = context.tailOffset
            context.document.insertString(tailOffset, " ")
            context.editor.caretModel.moveToOffset(tailOffset + 1)
        }
   }

    object OnlyName : KotlinFunctionInsertHandler()

    public override fun handleInsert(context: InsertionContext, item: LookupElement) {
        super.handleInsert(context, item)

        val psiDocumentManager = PsiDocumentManager.getInstance(context.project)
        psiDocumentManager.commitAllDocuments()
        psiDocumentManager.doPostponedOperationsAndUnblockDocument(context.document)
    }
}