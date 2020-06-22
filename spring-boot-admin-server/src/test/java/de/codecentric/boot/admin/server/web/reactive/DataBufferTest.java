/*
 * Copyright 2014-2020 the original author or authors.
 *
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

package de.codecentric.boot.admin.server.web.reactive;

import java.nio.ByteBuffer;

import io.netty.buffer.PooledByteBufAllocator;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.core.io.buffer.NettyDataBufferFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test to illustrate the usage of how to cache org.springframework.http.ReactiveHttpInputMessage#getBody()
 *
 * @see InstancesProxyController#endpointProxy(java.lang.String, org.springframework.http.server.reactive.ServerHttpRequest)
 */
public class DataBufferTest {
	@Test
	public void testWrapByteBuffer() {

		NettyDataBufferFactory bufferFactory = new NettyDataBufferFactory(PooledByteBufAllocator.DEFAULT);
		String str = "test";
		DataBuffer dataBuffer1 = bufferFactory.wrap(str.getBytes());
		ByteBuffer byteBuffer1 = dataBuffer1.asByteBuffer();

		DefaultDataBufferFactory defaultDataBufferFactory = new DefaultDataBufferFactory();
		DataBuffer dataBuffer2 = defaultDataBufferFactory.wrap(byteBuffer1);
		ByteBuffer byteBuffer2 = dataBuffer2.asByteBuffer();

		// when DataBufferUtils.release(dataBuffer1); called (always need to when we subscribe the request body), will cause the undergroud bytes released
		// So DefaultDataBufferFactory.wrap(java.nio.ByteBuffer) share the same bytes, so is not a good way to cache body.
		assertThat(byteBuffer2.array()).isEqualTo(byteBuffer1.array());

		assertThat(byteBuffer2.array().hashCode()).isEqualTo(byteBuffer1.array().hashCode());
		assertThat(byteBuffer2.array().equals(byteBuffer2.array()));

		// not safe to release dataBuffer1, if we need to use dataBuffer2 later
		DataBufferUtils.release(dataBuffer1);
	}

	@Test
	public void testWriteByteBuffer() {

		NettyDataBufferFactory bufferFactory = new NettyDataBufferFactory(PooledByteBufAllocator.DEFAULT);
		String str = "test";
		DataBuffer dataBuffer1 = bufferFactory.wrap(str.getBytes());
		ByteBuffer byteBuffer1 = dataBuffer1.asByteBuffer();

		DefaultDataBufferFactory defaultDataBufferFactory = new DefaultDataBufferFactory();
		int readableByteCount = dataBuffer1.readableByteCount();
		DataBuffer dataBuffer2 = defaultDataBufferFactory.allocateBuffer(readableByteCount);
		dataBuffer2.write(byteBuffer1);
		ByteBuffer byteBuffer2 = dataBuffer2.asByteBuffer();

		assertThat(byteBuffer2.array()).isEqualTo(byteBuffer1.array());
		assertThat(byteBuffer2.array().hashCode()).isNotEqualTo(byteBuffer1.array().hashCode());
		assertThat(byteBuffer2.array().equals(byteBuffer2.array()));

		// safe to release dataBuffer1, if we need to use dataBuffer2 later
		DataBufferUtils.release(dataBuffer1);

	}
}
