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

package org.jetbrains.kotlin.idea.intentions

import com.intellij.openapi.editor.Editor
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.psiUtil.containsInside

public class RemoveUnnecessaryParenthesesIntention : SelfTargetingIntention<KtParenthesizedExpression>(javaClass(), "Remove unnecessary parentheses") {
    override fun isApplicableTo(element: KtParenthesizedExpression, caretOffset: Int): Boolean {
        val expression = element.getExpression() ?: return false
        if (!KtPsiUtil.areParenthesesUseless(element)) return false
        return !expression.getTextRange().containsInside(caretOffset)
    }

    override fun applyTo(element: KtParenthesizedExpression, editor: Editor) {
        element.replace(element.getExpression()!!)
    }
}
