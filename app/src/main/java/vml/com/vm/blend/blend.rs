/*
 * Copyright (C) 2011 The Android Open Source Project
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

#pragma version(1)
#pragma rs java_package_name(com.vml.blend)

const float *matA;
const float *matB;
const float *matN;
float *outMatrix;
int *nSize;
int *kSize;

void root(const int *v_in1, int *v_out) {
    int row_idx = *v_in1;
    
    int n = *nSize;
	int k = *kSize;
	
	for(int i=0; i<n; i++)
	{
	//outMatrix[row_idx*n+i]=0.0;
	  outMatrix[row_idx*n+i]=matN[row_idx*n+i];
		for(int j=0; j<k; j++)
		{
//			outMatrix[row_idx*n+i] += matA[row_idx*k+j] * matB[i*k+j];
            outMatrix[row_idx*n+i] += matA[row_idx*k+j] * matB[j*n+i];
		}
	}
    
}
