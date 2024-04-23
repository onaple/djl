/*
 * Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package ai.djl.engine.rust;

import ai.djl.Device;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.testing.Assertions;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;

public class NDArrayTests {

    @Test
    public void testNDArrayCreation() {
        Shape expected = new Shape(1, 2);
        try (NDManager manager = NDManager.newBaseManager("Rust")) {
            NDArray array = manager.create(1f);
            Assert.assertEquals(array.getDataType(), DataType.FLOAT32);

            array = manager.create(1d);
            Assert.assertEquals(array.getDataType(), DataType.FLOAT64);

            array = manager.create(ByteBuffer.allocate(2), new Shape(1), DataType.FLOAT16);
            Assert.assertEquals(array.getDataType(), DataType.FLOAT16);

            array = manager.create(ByteBuffer.allocate(2), new Shape(1), DataType.BFLOAT16);
            Assert.assertEquals(array.getDataType(), DataType.BFLOAT16);

            array = manager.create(ByteBuffer.allocate(2), new Shape(2), DataType.UINT8);
            Assert.assertEquals(array.getDataType(), DataType.UINT8);

            array = manager.create((byte) 1);
            Assert.assertEquals(array.getDataType(), DataType.INT8);

            array = manager.create(1);
            Assert.assertEquals(array.getDataType(), DataType.INT32);

            array = manager.create(ByteBuffer.allocate(4), new Shape(1), DataType.UINT32);
            Assert.assertEquals(array.getDataType(), DataType.UINT32);

            array = manager.create(1L);
            Assert.assertEquals(array.getDataType(), DataType.INT64);

            array = manager.create(true);
            Assert.assertEquals(array.getDataType(), DataType.BOOLEAN);

            Assert.assertThrows(() -> manager.create(new Shape(1), DataType.INT16));

            array = manager.zeros(expected);
            Assert.assertEquals(array.getShape(), expected);
            Device device = array.getDevice();
            Assert.assertEquals(device, manager.getDevice());
            byte[] buf = array.toByteArray();
            Assert.assertEquals(buf, new byte[] {0, 0, 0, 0, 0, 0, 0, 0});

            array = manager.ones(expected);
            buf = array.toByteArray();
            Assert.assertEquals(buf, new byte[] {0, 0, -128, 63, 0, 0, -128, 63});

            array = manager.full(expected, 2);
            Assert.assertEquals(array.getDataType(), DataType.INT32);
            int[] ints = array.toIntArray();
            Assert.assertEquals(ints[1], 2);

            array = manager.arange(2f);
            Assert.assertEquals(array.getDataType(), DataType.FLOAT32);
            float[] floats = array.toFloatArray();
            Assert.assertEquals(floats, new float[] {0, 1});

            array = manager.eye(2);
            Assert.assertEquals(array.getShape(), new Shape(2, 2));
            floats = array.toFloatArray();
            Assert.assertEquals(floats, new float[] {1f, 0f, 0f, 1f});

            array = manager.randomUniform(1, 10, expected);
            Assert.assertEquals(array.getDataType(), DataType.FLOAT32);

            array = manager.randomNormal(expected);
            Assert.assertEquals(array.getDataType(), DataType.FLOAT32);
        }
    }

    @Test
    public void testToDataType() {
        try (NDManager manager = NDManager.newBaseManager("Rust")) {
            NDArray array = manager.create(2f);
            Assert.assertEquals(array.getDataType(), DataType.FLOAT32);
            NDArray uint8 = array.toType(DataType.UINT8, false);
            NDArray int8 = uint8.toType(DataType.INT8, false);
            NDArray int32 = int8.toType(DataType.INT32, false);
            NDArray uint32 = int32.toType(DataType.UINT32, false);
            NDArray int64 = uint32.toType(DataType.INT64, false);
            NDArray f16 = int64.toType(DataType.FLOAT16, false);
            NDArray bf16 = f16.toType(DataType.BFLOAT16, false);
            NDArray f32 = bf16.toType(DataType.FLOAT32, false);
            NDArray f64 = f32.toType(DataType.FLOAT64, false);
            NDArray bool = f64.toType(DataType.BOOLEAN, false);
            Assert.assertTrue(bool.getBoolean());
        }
    }

    @Test
    public void testComparisonOp() {
        try (NDManager manager = NDManager.newBaseManager("Rust")) {
            NDArray array = manager.create(new float[][] {{0f, 1f}, {2f, 3f}});
            NDArray all = array.all();
            Assert.assertEquals(all.getDataType(), DataType.BOOLEAN);
            Assert.assertFalse(all.getBoolean());

            NDArray none = array.none();
            Assert.assertEquals(none.getDataType(), DataType.BOOLEAN);
            Assert.assertFalse(none.getBoolean());

            NDArray any = array.any();
            Assert.assertEquals(any.getDataType(), DataType.BOOLEAN);
            Assert.assertTrue(any.getBoolean());

            NDArray noneZeros = array.countNonzero(1);
            Assert.assertEquals(noneZeros.getDataType(), DataType.INT64);
            Assert.assertEquals(noneZeros.getLong(0), 1);
        }
    }

    @Test
    public void testReductionOp() {
        try (NDManager manager = NDManager.newBaseManager("Rust")) {
            NDArray array = manager.create(new float[] {2f, 4f, 6f, 8f}, new Shape(2, 2));
            float maxAll = array.max().getFloat();
            Assert.assertEquals(maxAll, 8f);

            float minAll = array.min().getFloat();
            Assert.assertEquals(minAll, 2f);

            float sumAll = array.sum().getFloat();
            Assert.assertEquals(sumAll, 20f);

            float meanAll = array.mean().getFloat();
            Assert.assertEquals(meanAll, 5f);

            long argMax = array.argMax().getLong();
            Assert.assertEquals(argMax, 3);

            long argMin = array.argMin().getLong();
            Assert.assertEquals(argMin, 0);
        }
    }

    @Test
    public void testShapesOp() {
        try (NDManager manager = NDManager.newBaseManager("Rust")) {
            NDArray array = manager.ones(new Shape(1, 2, 1, 3, 1));
            NDArray expected = manager.ones(new Shape(2, 3));
            Assert.assertEquals(array.squeeze(), expected);
            expected = manager.ones(new Shape(1, 2, 3, 1));
            Assert.assertEquals(array.squeeze(2), expected);
            expected = manager.ones(new Shape(2, 1, 3));
            Assert.assertEquals(array.squeeze(new int[] {0, 4}), expected);

            expected = manager.ones(new Shape(1, 2, 1, 3));
            Assert.assertEquals(array.squeeze(-1), expected);
            expected = manager.ones(new Shape(2, 1, 3));
            Assert.assertEquals(array.squeeze(new int[] {0, 4}), expected);

            array = manager.arange(18f);
            NDList result = array.split(18);
            Assert.assertEquals(result.get(0), manager.create(new float[] {0f}));
            Assert.assertEquals(result.get(8), manager.create(new float[] {8f}));
            Assert.assertEquals(result.get(17), manager.create(new float[] {17f}));

            array = manager.create(new float[] {1f, 2f, 3f, 4f});
            result = array.split(2);
            Assert.assertEquals(result.get(0), manager.create(new float[] {1f, 2f}));
            Assert.assertEquals(result.get(1), manager.create(new float[] {3f, 4f}));
            result = array.split(new long[] {2});
            Assert.assertEquals(result.get(0), manager.create(new float[] {1f, 2f}));
            Assert.assertEquals(result.get(1), manager.create(new float[] {3f, 4f}));

            // special case: indices = empty
            array = manager.arange(6f).reshape(2, 3);
            result = array.split(new long[0]);
            Assert.assertEquals(result.singletonOrThrow(), array);

            result = array.split(new long[] {0});
            Assert.assertEquals(result.singletonOrThrow(), array);
        }
    }

    @Test
    public void testExpandDim() {
        try (NDManager manager = NDManager.newBaseManager("Rust")) {
            NDArray array = manager.create(new float[] {1f, 2f});
            NDArray expected = manager.create(new float[] {1f, 2f}, new Shape(1, 2));
            Assert.assertEquals(array.expandDims(0), expected);

            // multi-dim
            array = manager.create(new float[] {1f, 2f, 3f, 4f}, new Shape(2, 2));
            expected = manager.create(new float[] {1f, 2f, 3f, 4f}, new Shape(2, 1, 2));
            Assert.assertEquals(array.expandDims(1), expected);

            // scalar
            array = manager.create(4f);
            expected = manager.create(new float[] {4f});
            Assert.assertEquals(array.expandDims(0), expected);

            // zero-dim
            array = manager.create(new Shape(2, 1, 0));
            expected = manager.create(new Shape(2, 1, 1, 0));
            Assert.assertEquals(array.expandDims(2), expected);
        }
    }

    @Test
    public void testNormalize() {
        try (NDManager manager = NDManager.newBaseManager("Rust")) {
            float[][] buf = {
                {0.2673f, 0.5345f, 0.8018f},
                {0.4558f, 0.5698f, 0.6838f}
            };
            float[] data = {1, 2, 3, 4, 5, 6};
            NDArray x = manager.create(data, new Shape(2, 3));
            NDArray expected = manager.create(buf);
            NDArray ret = x.normalize();
            Assertions.assertAlmostEquals(ret, expected);
        }
    }

    @Test
    public void testGetOp() {
        try (NDManager manager = NDManager.newBaseManager("Rust")) {
            NDArray array = manager.ones(new Shape(2, 1, 3, 1));
            String v = array.toDebugString(true);
            System.out.println(v);
        }
    }
}
