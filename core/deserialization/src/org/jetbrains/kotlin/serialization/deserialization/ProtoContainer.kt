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

package org.jetbrains.kotlin.serialization.deserialization

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.serialization.ProtoBuf

public data class ProtoContainer private constructor(
        val classProto: ProtoBuf.Class?,
        val packageFqName: FqName?,
        val fileFacadeClassId: ClassId?) {
    init {
        assert((classProto != null && packageFqName == null && fileFacadeClassId == null) ||
               (classProto == null && packageFqName != null && fileFacadeClassId == null) ||
               (classProto == null && packageFqName == null && fileFacadeClassId != null))
    }

    fun getFqName(nameResolver: NameResolver): FqName {
        if (packageFqName != null) return packageFqName

        return nameResolver.getClassId(classProto!!.getFqName()).asSingleFqName()
    }

    companion object {
        fun forClassProto(classProto: ProtoBuf.Class): ProtoContainer = ProtoContainer(classProto, null, null)
        fun forPackageFacade(packageFqName: FqName): ProtoContainer = ProtoContainer(null, packageFqName, null)
        fun forFileFacade(fileFacadeClassId: ClassId): ProtoContainer = ProtoContainer(null, null, fileFacadeClassId)
    }
}
