/*
 * Copyright (C) 2011-2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This file is auto-generated. DO NOT MODIFY!
 * The source Renderscript file: /home/roger/workspace/SAMSUNG PROJECT/VMLibrary/src/com/vml/blend/blend.rs
 */
package vml.com.vm.blend;

import android.renderscript.*;
import android.content.res.Resources;

/**
 * @hide
 */
public class ScriptC_blend extends ScriptC {
    private static final String __rs_resource_name = "blend";
    // Constructor
    public  ScriptC_blend(RenderScript rs) {
        this(rs,
             rs.getApplicationContext().getResources(),
             rs.getApplicationContext().getResources().getIdentifier(
                 __rs_resource_name, "raw",
                 rs.getApplicationContext().getPackageName()));
    }

    public  ScriptC_blend(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        __I32 = Element.I32(rs);
    }

    private Element __I32;
    private final static int mExportVarIdx_matA = 0;
    private Allocation mExportVar_matA;
    public void bind_matA(Allocation v) {
        mExportVar_matA = v;
        if (v == null) bindAllocation(null, mExportVarIdx_matA);
        else bindAllocation(v, mExportVarIdx_matA);
    }

    public Allocation get_matA() {
        return mExportVar_matA;
    }

    private final static int mExportVarIdx_matB = 1;
    private Allocation mExportVar_matB;
    public void bind_matB(Allocation v) {
        mExportVar_matB = v;
        if (v == null) bindAllocation(null, mExportVarIdx_matB);
        else bindAllocation(v, mExportVarIdx_matB);
    }

    public Allocation get_matB() {
        return mExportVar_matB;
    }

    private final static int mExportVarIdx_matN = 2;
    private Allocation mExportVar_matN;
    public void bind_matN(Allocation v) {
        mExportVar_matN = v;
        if (v == null) bindAllocation(null, mExportVarIdx_matN);
        else bindAllocation(v, mExportVarIdx_matN);
    }

    public Allocation get_matN() {
        return mExportVar_matN;
    }

    private final static int mExportVarIdx_outMatrix = 3;
    private Allocation mExportVar_outMatrix;
    public void bind_outMatrix(Allocation v) {
        mExportVar_outMatrix = v;
        if (v == null) bindAllocation(null, mExportVarIdx_outMatrix);
        else bindAllocation(v, mExportVarIdx_outMatrix);
    }

    public Allocation get_outMatrix() {
        return mExportVar_outMatrix;
    }

    private final static int mExportVarIdx_nSize = 4;
    private Allocation mExportVar_nSize;
    public void bind_nSize(Allocation v) {
        mExportVar_nSize = v;
        if (v == null) bindAllocation(null, mExportVarIdx_nSize);
        else bindAllocation(v, mExportVarIdx_nSize);
    }

    public Allocation get_nSize() {
        return mExportVar_nSize;
    }

    private final static int mExportVarIdx_kSize = 5;
    private Allocation mExportVar_kSize;
    public void bind_kSize(Allocation v) {
        mExportVar_kSize = v;
        if (v == null) bindAllocation(null, mExportVarIdx_kSize);
        else bindAllocation(v, mExportVarIdx_kSize);
    }

    public Allocation get_kSize() {
        return mExportVar_kSize;
    }

    private final static int mExportForEachIdx_root = 0;
    public void forEach_root(Allocation ain, Allocation aout) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__I32)) {
            throw new RSRuntimeException("Type mismatch with I32!");
        }
        // check aout
        if (!aout.getType().getElement().isCompatible(__I32)) {
            throw new RSRuntimeException("Type mismatch with I32!");
        }
        // Verify dimensions
        Type tIn = ain.getType();
        Type tOut = aout.getType();
        if ((tIn.getCount() != tOut.getCount()) ||
            (tIn.getX() != tOut.getX()) ||
            (tIn.getY() != tOut.getY()) ||
            (tIn.getZ() != tOut.getZ()) ||
            (tIn.hasFaces() != tOut.hasFaces()) ||
            (tIn.hasMipmaps() != tOut.hasMipmaps())) {
            throw new RSRuntimeException("Dimension mismatch between input and output parameters!");
        }
        forEach(mExportForEachIdx_root, ain, aout, null);
    }

}

