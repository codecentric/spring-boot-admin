/*
 * Copyright 2014-2017 the original author or authors.
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

package de.codecentric.boot.admin.server.web;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.reactivestreams.Publisher;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.CodecException;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.HttpMessageDecoder;
import org.springframework.http.codec.json.Jackson2CodecSupport;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.async.ByteArrayFeeder;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.util.TokenBuffer;


public class FixedJackson2Decoder extends Jackson2JsonDecoder implements HttpMessageDecoder<Object> {

    /**
     * Constructor with a Jackson {@link ObjectMapper} to use.
     */
    public FixedJackson2Decoder(ObjectMapper mapper, MimeType... mimeTypes) {
        super(mapper, mimeTypes);
    }


    @Override
    public boolean canDecode(ResolvableType elementType, @Nullable MimeType mimeType) {
        JavaType javaType = getObjectMapper().getTypeFactory().constructType(elementType.getType());
        // Skip String: CharSequenceDecoder + "*/*" comes after
        return (!CharSequence.class.isAssignableFrom(elementType.resolve(Object.class)) &&
                getObjectMapper().canDeserialize(javaType) &&
                supportsMimeType(mimeType));
    }

    @Override
    public Flux<Object> decode(Publisher<DataBuffer> input,
                               ResolvableType elementType,
                               @Nullable MimeType mimeType,
                               @Nullable Map<String, Object> hints) {

        Flux<TokenBuffer> tokens = tokenize(input, true);
        return decodeInternal(tokens, elementType, mimeType, hints);
    }

    @Override
    public Mono<Object> decodeToMono(Publisher<DataBuffer> input,
                                     ResolvableType elementType,
                                     @Nullable MimeType mimeType,
                                     @Nullable Map<String, Object> hints) {

        Flux<TokenBuffer> tokens = tokenize(input, false);
        return decodeInternal(tokens, elementType, mimeType, hints).singleOrEmpty();
    }

    private Flux<TokenBuffer> tokenize(Publisher<DataBuffer> input, boolean tokenizeArrayElements) {
        try {
            JsonFactory factory = getObjectMapper().getFactory();
            JsonParser parser = factory.createNonBlockingByteArrayParser();
            Jackson2Tokenizer tokenizer = new Jackson2Tokenizer(parser, tokenizeArrayElements);
            return Flux.from(input).flatMap(tokenizer).doFinally(t -> tokenizer.endOfInput());
        } catch (IOException ex) {
            return Flux.error(new UncheckedIOException(ex));
        }
    }

    private Flux<Object> decodeInternal(Flux<TokenBuffer> tokens,
                                        ResolvableType elementType,
                                        @Nullable MimeType mimeType,
                                        @Nullable Map<String, Object> hints) {

        Assert.notNull(tokens, "'tokens' must not be null");
        Assert.notNull(elementType, "'elementType' must not be null");

        MethodParameter param = getParameter(elementType);
        Class<?> contextClass = (param != null ? param.getContainingClass() : null);
        JavaType javaType = getJavaType(elementType.getType(), contextClass);
        Class<?> jsonView = (hints != null ? (Class<?>) hints.get(Jackson2CodecSupport.JSON_VIEW_HINT) : null);

        ObjectReader reader = (jsonView != null ?
                getObjectMapper().readerWithView(jsonView).forType(javaType) :
                getObjectMapper().readerFor(javaType));

        return tokens.map(tokenBuffer -> {
            try {
                return reader.readValue(tokenBuffer.asParser(getObjectMapper()));
            } catch (InvalidDefinitionException ex) {
                throw new CodecException("Type definition error: " + ex.getType(), ex);
            } catch (JsonProcessingException ex) {
                throw new DecodingException("JSON decoding error: " + ex.getOriginalMessage(), ex);
            } catch (IOException ex) {
                throw new DecodingException("I/O error while parsing input stream", ex);
            }
        });
    }


    // HttpMessageDecoder...

    @Override
    public Map<String, Object> getDecodeHints(ResolvableType actualType,
                                              ResolvableType elementType,
                                              ServerHttpRequest request,
                                              ServerHttpResponse response) {

        return getHints(actualType);
    }

    @Override
    protected <A extends Annotation> A getAnnotation(MethodParameter parameter, Class<A> annotType) {
        return parameter.getParameterAnnotation(annotType);
    }

    @Override
    public List<MimeType> getDecodableMimeTypes() {
        return getMimeTypes();
    }
}


class Jackson2Tokenizer implements Function<DataBuffer, Flux<TokenBuffer>> {

    private final JsonParser parser;

    private final boolean tokenizeArrayElements;

    private TokenBuffer tokenBuffer;

    private int objectDepth;

    private int arrayDepth;

    // TODO: change to ByteBufferFeeder when supported by Jackson
    private final ByteArrayFeeder inputFeeder;


    /**
     * Create a new instance of the {@code Jackson2Tokenizer}.
     *
     * @param parser                the non-blocking parser, obtained via
     *                              {@link JsonFactory#createNonBlockingByteArrayParser}
     * @param tokenizeArrayElements if {@code true} and the "top level" JSON
     *                              object is an array, each element is returned individually, immediately
     *                              after it is received.
     */
    public Jackson2Tokenizer(JsonParser parser, boolean tokenizeArrayElements) {
        Assert.notNull(parser, "'parser' must not be null");

        this.parser = parser;
        this.tokenizeArrayElements = tokenizeArrayElements;
        this.tokenBuffer = new TokenBuffer(parser);
        this.inputFeeder = (ByteArrayFeeder) this.parser.getNonBlockingInputFeeder();
    }


    @Override
    public Flux<TokenBuffer> apply(DataBuffer dataBuffer) {
        byte[] bytes = new byte[dataBuffer.readableByteCount()];
        dataBuffer.read(bytes);
        DataBufferUtils.release(dataBuffer);

        try {
            this.inputFeeder.feedInput(bytes, 0, bytes.length);
            List<TokenBuffer> result = new ArrayList<>();

            while (true) {
                JsonToken token = this.parser.nextToken();
                if (token == JsonToken.NOT_AVAILABLE) {
                    break;
                }
                updateDepth(token);

                if (!this.tokenizeArrayElements) {
                    processTokenNormal(token, result);
                } else {
                    processTokenArray(token, result);
                }
            }
            return Flux.fromIterable(result);
        } catch (JsonProcessingException ex) {
            return Flux.error(new DecodingException("JSON decoding error: " + ex.getOriginalMessage(), ex));
        } catch (Exception ex) {
            return Flux.error(ex);
        }
    }

    private void updateDepth(JsonToken token) {
        switch (token) {
            case START_OBJECT:
                this.objectDepth++;
                break;
            case END_OBJECT:
                this.objectDepth--;
                break;
            case START_ARRAY:
                this.arrayDepth++;
                break;
            case END_ARRAY:
                this.arrayDepth--;
                break;
        }
    }

    private void processTokenNormal(JsonToken token, List<TokenBuffer> result) throws IOException {
        this.tokenBuffer.copyCurrentEvent(this.parser);

        if (token == JsonToken.END_OBJECT || token == JsonToken.END_ARRAY) {
            if (this.objectDepth == 0 && this.arrayDepth == 0) {
                result.add(this.tokenBuffer);
                this.tokenBuffer = new TokenBuffer(this.parser);
            }
        }

    }

    private void processTokenArray(JsonToken token, List<TokenBuffer> result) throws IOException {
        if (!isTopLevelArrayToken(token)) {
            this.tokenBuffer.copyCurrentEvent(this.parser);
        }

        if (token == JsonToken.END_OBJECT && this.objectDepth == 0 && (this.arrayDepth == 1 || this.arrayDepth == 0)) {
            result.add(this.tokenBuffer);
            this.tokenBuffer = new TokenBuffer(this.parser);
        }
    }

    private boolean isTopLevelArrayToken(JsonToken token) {
        return this.objectDepth == 0 &&
               ((token == JsonToken.START_ARRAY && this.arrayDepth == 1) ||
                (token == JsonToken.END_ARRAY && this.arrayDepth == 0));
    }

    public void endOfInput() {
        this.inputFeeder.endOfInput();
    }

}
