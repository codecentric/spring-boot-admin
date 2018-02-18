/*
 * Copyright 2014-2018 the original author or authors.
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

package de.codecentric.boot.admin.server.web.client;

import reactor.core.publisher.Flux;

import java.util.function.Function;
import org.springframework.core.io.buffer.DataBuffer;

/**
 * @author Johannes Edmeier
 */
public class LegacyEndpointConverter {
    private final String endpointId;
    private final Function<Flux<DataBuffer>, Flux<DataBuffer>> converterFn;

    protected LegacyEndpointConverter(String endpointId, Function<Flux<DataBuffer>, Flux<DataBuffer>> converterFn) {
        this.endpointId = endpointId;
        this.converterFn = converterFn;
    }

    public boolean canConvert(Object endpointId) {
        return this.endpointId.equals(endpointId);
    }

    public Flux<DataBuffer> convert(Flux<DataBuffer> body) {
        return converterFn.apply(body);
    }
}
