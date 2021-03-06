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

package org.jetbrains.kotlin.load.kotlin

import org.jetbrains.kotlin.serialization.deserialization.NameResolver
import org.jetbrains.kotlin.serialization.jvm.JvmProtoBuf

// The purpose of this class is to hold a unique signature of either a method or a field, so that annotations on a member can be put
// into a map indexed by these signatures
data class MemberSignature private constructor(private val signature: String) {
    companion object {
        @JvmStatic
        public fun fromMethod(nameResolver: NameResolver, signature: JvmProtoBuf.JvmMethodSignature): MemberSignature {
            return fromMethodNameAndDesc(nameResolver.getString(signature.name), nameResolver.getString(signature.desc))
        }

        @JvmStatic
        public fun fromMethodNameAndDesc(name: String, desc: String): MemberSignature {
            return MemberSignature(name + desc)
        }

        @JvmStatic
        public fun fromMethodNameAndDesc(namePlusDesc: String): MemberSignature {
            return MemberSignature(namePlusDesc)
        }

        @JvmStatic
        public fun fromFieldNameAndDesc(name: String, desc: String): MemberSignature {
            return MemberSignature(name + "#" + desc)
        }

        @JvmStatic
        public fun fromMethodSignatureAndParameterIndex(signature: MemberSignature, index: Int): MemberSignature {
            return MemberSignature(signature.signature + "@" + index)
        }
    }
}
