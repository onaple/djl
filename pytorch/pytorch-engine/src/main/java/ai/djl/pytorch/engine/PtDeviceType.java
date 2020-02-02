/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package ai.djl.pytorch.engine;

import ai.djl.Device;
import ai.djl.DeviceType;

public final class PtDeviceType implements DeviceType {

    public static int toDeviceType(Device device) {
        String deviceType = device.getDeviceType();

        if (Device.Type.CPU.equals(deviceType)) {
            return 0;
        } else if (Device.Type.GPU.equals(deviceType)) {
            return 1;
        } else {
            throw new IllegalArgumentException("Unsupported device: " + device.toString());
        }
    }

    public static String fromDeviceType(int deviceType) {
        switch (deviceType) {
            case 0:
                return Device.Type.CPU;
            case 1:
                return Device.Type.GPU;
            default:
                throw new IllegalArgumentException("Unsupported deviceType: " + deviceType);
        }
    }
}
