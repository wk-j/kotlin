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

package org.jetbrains.kotlin.resolve.scopes.utils

import com.intellij.util.SmartList
import org.jetbrains.kotlin.descriptors.ClassifierDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.incremental.components.LookupLocation
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.FileScope
import org.jetbrains.kotlin.resolve.scopes.JetScope
import org.jetbrains.kotlin.resolve.scopes.LexicalScope
import org.jetbrains.kotlin.types.JetType
import org.jetbrains.kotlin.util.collectionUtils.concat
import org.jetbrains.kotlin.utils.Printer

public fun LexicalScope.getFileScope(): FileScope {
    var currentScope = this
    while(currentScope.parent != null) {
        currentScope = currentScope.parent!!
    }
    assert(currentScope is FileScope) {
        "Not FileScope without parent: $currentScope" // todo improve debug message
    }
    return currentScope as FileScope
}

/**
 * Adds receivers to the list in order of locality, so that the closest (the most local) receiver goes first
 */
public fun LexicalScope.getImplicitReceiversHierarchy(): List<ReceiverParameterDescriptor> = collectFromMeAndParent { it.implicitReceiver }

public fun LexicalScope.getDeclarationsByLabel(labelName: Name): Collection<DeclarationDescriptor> = collectFromMeAndParent {
    if (it.isOwnerDescriptorAccessibleByLabel && it.ownerDescriptor.name == labelName) {
        it.ownerDescriptor
    } else {
        null
    }
}

@deprecated("Use getOwnProperties instead")
public fun LexicalScope.getLocalVariable(name: Name, location: LookupLocation = NoLookupLocation.UNSORTED): VariableDescriptor? {
    processForMeAndParent {
        if (it !is FileScope) { // todo check this
            it.getDeclaredVariables(name, location).singleOrNull()?.let { return it }
        }
    }
    return null
}

public fun LexicalScope.getClassifier(name: Name, location: LookupLocation = NoLookupLocation.UNSORTED): ClassifierDescriptor? {
    processForMeAndParent {
        it.getDeclaredClassifier(name, location)?.let { return it }
    }
    return null
}

public fun LexicalScope.asJetScope(): JetScope = LexicalToJetScopeAdapter(this)

private class LexicalToJetScopeAdapter(val lexicalScope: LexicalScope): JetScope {

    override fun getClassifier(name: Name, location: LookupLocation) = lexicalScope.getClassifier(name, location)

    override fun getPackage(name: Name) = lexicalScope.getFileScope().getPackage(name)

    override fun getProperties(name: Name, location: LookupLocation) = lexicalScope.collectAllFromMeAndParent {
        it.getDeclaredVariables(name, location)
    }

    override fun getLocalVariable(name: Name) = lexicalScope.getLocalVariable(name)

    override fun getFunctions(name: Name, location: LookupLocation) = lexicalScope.collectAllFromMeAndParent {
        it.getDeclaredFunctions(name, location)
    }


    override fun getSyntheticExtensionProperties(receiverTypes: Collection<JetType>, name: Name, location: LookupLocation)
            = lexicalScope.getFileScope().getSyntheticExtensionProperties(receiverTypes, name, location)

    override fun getSyntheticExtensionFunctions(receiverTypes: Collection<JetType>, name: Name, location: LookupLocation)
            = lexicalScope.getFileScope().getSyntheticExtensionFunctions(receiverTypes, name, location)

    override fun getSyntheticExtensionProperties(receiverTypes: Collection<JetType>)
        = lexicalScope.getFileScope().getSyntheticExtensionProperties(receiverTypes)

    override fun getSyntheticExtensionFunctions(receiverTypes: Collection<JetType>)
        = lexicalScope.getFileScope().getSyntheticExtensionFunctions(receiverTypes)

    override fun getContainingDeclaration() = lexicalScope.ownerDescriptor

    override fun getDeclarationsByLabel(labelName: Name) = lexicalScope.getDeclarationsByLabel(labelName)

    override fun getDescriptors(kindFilter: DescriptorKindFilter, nameFilter: (Name) -> Boolean) = lexicalScope.collectAllFromMeAndParent {
        if (it is FileScope) {
            it.getDescriptors(kindFilter, nameFilter)
        } else it.getDeclaredDescriptors()
    }

    override fun getImplicitReceiversHierarchy() = lexicalScope.getImplicitReceiversHierarchy()
    override fun printScopeStructure(p: Printer) = lexicalScope.printStructure(p)
    override fun getOwnDeclaredDescriptors() = lexicalScope.getDeclaredDescriptors()
}

private inline fun LexicalScope.processForMeAndParent(process: (LexicalScope) -> Unit) {
    var currentScope = this
    process(currentScope)

    while(currentScope.parent != null) {
        currentScope = currentScope.parent!!
        process(currentScope)
    }
}

private inline fun <T: Any> LexicalScope.collectFromMeAndParent(
        collect: (LexicalScope) -> T?
): List<T> {
    var result: MutableList<T>? = null
    processForMeAndParent {
        val element = collect(it)
        if (element != null) {
            if (result == null) {
                result = SmartList()
                return emptyList()
            }
            result!!.add(element)
        }
    }
    return result ?: emptyList()
}

private inline fun <T: Any> LexicalScope.collectAllFromMeAndParent(
        collect: (LexicalScope) -> Collection<T>
): Collection<T> {
    var result: Collection<T>? = null
    processForMeAndParent { result = result.concat(collect(it)) }
    return result ?: emptySet()
}