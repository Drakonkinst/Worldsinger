/*
 * MIT License
 *
 * Copyright (c) 2023-2024 Drakonkinst
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.drakonkinst.worldsinger.util.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class JsonType<T extends JsonElement> {

    public static final JsonType<JsonObject> OBJECT = new JsonType<>("JsonObject",
            JsonElement::isJsonObject, JsonElement::getAsJsonObject, JsonObject::new);
    public static final JsonType<JsonArray> ARRAY = new JsonType<>("JsonArray",
            JsonElement::isJsonArray, JsonElement::getAsJsonArray, JsonArray::new);
    public static final JsonType<JsonPrimitive> STRING = new JsonType<>("JsonString",
            element -> element.isJsonPrimitive() && element.getAsJsonPrimitive().isString(),
            JsonElement::getAsJsonPrimitive, () -> new JsonPrimitive(""));
    public static final JsonType<JsonPrimitive> NUMBER = new JsonType<>("JsonNumber",
            element -> element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber(),
            JsonElement::getAsJsonPrimitive, () -> new JsonPrimitive(1));
    public static final JsonType<JsonPrimitive> BOOLEAN = new JsonType<>("JsonBoolean",
            element -> element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean(),
            JsonElement::getAsJsonPrimitive, () -> new JsonPrimitive(false));
    private static final JsonType<?>[] TYPES = { OBJECT, ARRAY, STRING, NUMBER, BOOLEAN };

    @SuppressWarnings("unchecked")
    public static <T extends JsonElement> JsonType<? extends T> of(T element) {
        for (JsonType<?> type : TYPES) {
            if (type.is(element)) {
                return (JsonType<? extends T>) type;
            }
        }
        throw new IllegalArgumentException();
    }

    public final String name;
    private final Predicate<JsonElement> is;
    private final Function<JsonElement, T> cast;
    private final Supplier<T> dummy;

    private JsonType(String name, Predicate<JsonElement> is, Function<JsonElement, T> cast,
            Supplier<T> dummy) {
        this.name = name;
        this.is = is;
        this.cast = cast;
        this.dummy = dummy;
    }

    public boolean is(JsonElement element) {
        return is.test(element);
    }

    public T cast(JsonElement element) {
        return cast.apply(element);
    }

    public T dummy() {
        return dummy.get();
    }
}
