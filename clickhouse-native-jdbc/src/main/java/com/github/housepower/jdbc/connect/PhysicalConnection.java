/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.housepower.jdbc.connect;

import com.github.housepower.jdbc.buffer.SocketBuffedWriter;
import com.github.housepower.jdbc.data.Block;
import com.github.housepower.jdbc.misc.Validate;
import com.github.housepower.jdbc.protocol.*;
import com.github.housepower.jdbc.serde.BinaryDeserializer;
import com.github.housepower.jdbc.serde.BinarySerializer;
import com.github.housepower.jdbc.settings.ClickHouseConfig;
import com.github.housepower.jdbc.settings.ClickHouseDefines;
import com.github.housepower.jdbc.settings.SettingKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class PhysicalConnection {
    private static final Logger LOG = LoggerFactory.getLogger(PhysicalConnection.class);

    private final Socket socket;
    private final SocketAddress address;
    private final BinarySerializer serializer;
    private final BinaryDeserializer deserializer;

    public PhysicalConnection(Socket socket, BinarySerializer serializer, BinaryDeserializer deserializer) {
        this.socket = socket;
        this.serializer = serializer;
        this.deserializer = deserializer;
        this.address = socket.getLocalSocketAddress();
    }

    public boolean ping(int soTimeoutMs, PhysicalInfo.ServerInfo info) {
        try {
            sendRequest(new PingRequest());
            while (true) {
                Response response = receiveResponse(soTimeoutMs, info);

                if (response instanceof PongResponse)
                    return true;

                // TODO there are some previous response we haven't consumed
                LOG.warn("expect pong, skip response: {}", response.type());
            }
        } catch (SQLException e) {
            LOG.warn(e.getMessage());
            return false;
        }
    }

    public void sendData(Block data) throws SQLException {
        sendRequest(new DataRequest("", data));
    }

    public void sendQuery(String query, QueryRequest.ClientInfo info, Map<SettingKey, Object> settings) throws SQLException {
        sendQuery(UUID.randomUUID().toString(), QueryRequest.STAGE_COMPLETE, info, query, settings);
    }

    public void sendHello(String client, long reversion, String db, String user, String password) throws SQLException {
        sendRequest(new HelloRequest(client, reversion, db, user, password));
    }

    public Block receiveSampleBlock(int soTimeoutMs, PhysicalInfo.ServerInfo info) throws SQLException {
        while (true) {
            Response response = receiveResponse(soTimeoutMs, info);
            if (response instanceof DataResponse) {
                return ((DataResponse) response).block();
            }
            // TODO there are some previous response we haven't consumed
            LOG.warn("expect sample block, skip response: {}", response.type());
        }
    }

    public HelloResponse receiveHello(int soTimeoutMs, PhysicalInfo.ServerInfo info) throws SQLException {
        Response response = receiveResponse(soTimeoutMs, info);
        Validate.isTrue(response instanceof HelloResponse, "Expect Hello Response.");
        return (HelloResponse) response;
    }

    public EOFStreamResponse receiveEndOfStream(int soTimeoutMs, PhysicalInfo.ServerInfo info) throws SQLException {
        Response response = receiveResponse(soTimeoutMs, info);
        Validate.isTrue(response instanceof EOFStreamResponse, "Expect EOFStream Response.");
        return (EOFStreamResponse) response;
    }

    public Response receiveResponse(int soTimeoutMs, PhysicalInfo.ServerInfo info) throws SQLException {
        try {
            socket.setSoTimeout(soTimeoutMs);
            return Response.readFrom(deserializer, info);
        } catch (IOException ex) {
            throw new SQLException(ex.getMessage(), ex);
        }
    }

    public SocketAddress address() {
        return address;
    }

    public void disPhysicalConnection() throws SQLException {
        try {
            if (!socket.isClosed()) {
                serializer.flushToTarget(true);
                socket.close();
            }
        } catch (IOException ex) {
            throw new SQLException(ex.getMessage(), ex);
        }
    }

    private void sendQuery(String id, int stage, QueryRequest.ClientInfo info, String query,
                           Map<SettingKey, Object> settings) throws SQLException {
        sendRequest(new QueryRequest(id, info, stage, true, query, settings));
    }

    private void sendRequest(Request request) throws SQLException {
        try {
            request.writeTo(serializer);
            serializer.flushToTarget(true);
        } catch (IOException ex) {
            throw new SQLException(ex.getMessage(), ex);
        }
    }

    public static PhysicalConnection openPhysicalConnection(ClickHouseConfig configure) throws SQLException {
        try {
            SocketAddress endpoint = new InetSocketAddress(configure.host(), configure.port());

            Socket socket = new Socket();
            socket.setTcpNoDelay(true);
            socket.setSendBufferSize(ClickHouseDefines.SOCKET_SEND_BUFFER_BYTES);
            socket.setReceiveBufferSize(ClickHouseDefines.SOCKET_RECV_BUFFER_BYTES);
            socket.setKeepAlive(configure.tcpKeepAlive());
            socket.connect(endpoint, configure.connectTimeoutMs());

            return new PhysicalConnection(socket, new BinarySerializer(new SocketBuffedWriter(socket), true), new BinaryDeserializer(socket));
        } catch (IOException ex) {
            throw new SQLException(ex.getMessage(), ex);
        }
    }
}
