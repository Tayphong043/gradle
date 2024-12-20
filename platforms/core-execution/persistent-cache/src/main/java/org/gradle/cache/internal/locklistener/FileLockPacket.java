/*
 * Copyright 2024 the original author or authors.
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

package org.gradle.cache.internal.locklistener;

import org.gradle.api.NonNullApi;

import java.net.DatagramPacket;
import java.net.SocketAddress;

@NonNullApi
class FileLockPacket {

    private final int port;
    private final SocketAddress socketAddress;
    private final byte[] data;
    private final int length;

    private FileLockPacket(int port, SocketAddress socketAddress, byte[] data, int length) {
        this.port = port;
        this.socketAddress = socketAddress;
        this.data = data;
        this.length = length;
    }

    int getPort() {
        return port;
    }

    SocketAddress getSocketAddress() {
        return socketAddress;
    }

    byte[] getData() {
        return data;
    }

    int getLength() {
        return length;
    }

    static FileLockPacket of(DatagramPacket datagramPacket) {
        return new FileLockPacket(datagramPacket.getPort(), datagramPacket.getSocketAddress(), datagramPacket.getData(), datagramPacket.getLength());
    }


    static FileLockPacket of(byte[] packet, SocketAddress address, int port) {
        return new FileLockPacket(port, address, packet, packet.length);
    }
}
