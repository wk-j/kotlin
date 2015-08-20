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

package org.jetbrains.kotlin.idea.core

import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.idea.util.ImportInsertHelper
import org.jetbrains.kotlin.idea.util.ProjectRootsUtil
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.platform.JavaToKotlinClassMap
import org.jetbrains.kotlin.psi.JetFile
import org.jetbrains.kotlin.resolve.ImportPath
import java.util.*

class SymbolUsageProximityPrioritizer(private val file: JetFile) {
    private val importCache = ImportCache()
    private val thisModule = ModuleUtilCore.findModuleForPsiElement(file)

    private enum class PriorityBasedOnImports {
        thisFile,
        defaultImport,
        preciseImport,
        allUnderImport,
        default,
        hasImportFromSamePackage,
        notImported,
        notToBeUsedInKotlin
    }

    private enum class PriorityBasedOnModule {
        thisModule,
        project,
        other
    }

    data class Priority(private val priority1: PriorityBasedOnImports, private val priority2: PriorityBasedOnModule) : Comparable<Priority> {
        override fun compareTo(other: Priority): Int {
            val c1 = priority1.compareTo(other.priority1)
            if (c1 != 0) return c1
            return priority2.compareTo(other.priority2)
        }

        companion object {
            val DEFAULT = Priority(PriorityBasedOnImports.default, PriorityBasedOnModule.other)
        }
    }

    fun priority(fqName: FqName, declaration: PsiElement?, isPackage: Boolean)
            = Priority(priorityBasedOnImports(fqName, declaration, isPackage), priorityBasedOnModule(declaration))

    private fun priorityBasedOnImports(fqName: FqName, declaration: PsiElement?, isPackage: Boolean): PriorityBasedOnImports {
        if (declaration?.containingFile == file) return PriorityBasedOnImports.thisFile

        val importPath = ImportPath(fqName, false)

        if (isPackage) {
            return when {
                importCache.isImportedWithPreciseImport(fqName) -> PriorityBasedOnImports.preciseImport
                else -> PriorityBasedOnImports.default
            }
        }

        return when {
            JavaToKotlinClassMap.INSTANCE.mapPlatformClass(fqName).isNotEmpty() -> PriorityBasedOnImports.notToBeUsedInKotlin
            ImportInsertHelper.getInstance(file.getProject()).isImportedWithDefault(importPath, file) -> PriorityBasedOnImports.defaultImport
            importCache.isImportedWithPreciseImport(fqName) -> PriorityBasedOnImports.preciseImport
            importCache.isImportedWithAllUnderImport(fqName) -> PriorityBasedOnImports.allUnderImport
            importCache.hasPreciseImportFromPackage(fqName.parent()) -> PriorityBasedOnImports.hasImportFromSamePackage
            else -> PriorityBasedOnImports.notImported
        }
    }

    private fun priorityBasedOnModule(declaration: PsiElement?): PriorityBasedOnModule {
        return when {
            declaration == null -> PriorityBasedOnModule.other
            ModuleUtilCore.findModuleForPsiElement(declaration) == thisModule -> PriorityBasedOnModule.thisModule
            ProjectRootsUtil.isInProjectSource(declaration) -> PriorityBasedOnModule.project
            else -> PriorityBasedOnModule.other
        }
    }

    private inner class ImportCache {
        private val preciseImports = HashSet<FqName>()
        private val preciseImportPackages = HashSet<FqName>()
        private val allUnderImports = HashSet<FqName>()

        init {
            for (import in file.getImportDirectives()) {
                val importPath = import.getImportPath() ?: continue
                val fqName = importPath.fqnPart()
                if (importPath.isAllUnder()) {
                    allUnderImports.add(fqName)
                }
                else {
                    preciseImports.add(fqName)
                    preciseImportPackages.add(fqName.parent())
                }
            }
        }

        fun isImportedWithPreciseImport(name: FqName) = name in preciseImports
        fun isImportedWithAllUnderImport(name: FqName) = name.parent() in allUnderImports
        fun hasPreciseImportFromPackage(packageName: FqName) = packageName in preciseImportPackages
    }
}