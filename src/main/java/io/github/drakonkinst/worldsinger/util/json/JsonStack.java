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

import static java.util.stream.Collectors.joining;

import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JsonStack {

    private final Gson gson;
    private final Deque<Member> elementPath = new ArrayDeque<>();
    private final List<String> errors = new ArrayList<>();

    public JsonStack(Gson gson, JsonElement root) {
        this.gson = gson;
        elementPath.push(new Member(root.getAsJsonObject(), ""));
    }

    public JsonStack push(String key) {
        elementPath.push(new Member(child(key, JsonType.OBJECT), key));
        return this;
    }

    private <T extends JsonElement> T child(String key, JsonType<T> expected) {
        return maybeChild(key, expected).orElseGet(() -> {
            errors.add("Missing " + expected.name + ' ' + joinPath() + '/' + key);
            return expected.dummy();
        });
    }

    private <T extends JsonElement> Optional<T> maybeChild(String key, JsonType<T> expected) {
        assert elementPath.peek() != null;
        JsonObject head = elementPath.peek().json;
        if (head.has(key)) {
            JsonElement element = head.get(key);
            if (expected.is(element)) {
                return Optional.of(expected.cast(element));
            } else {
                errors.add(joinPath() + '/' + key + " must be " + expected.name + " not "
                        + JsonType.of(element).name);
            }
        }
        return Optional.empty();
    }

    private String joinPath() {
        return Streams.stream(elementPath.descendingIterator())
                .map(Member::name)
                .collect(joining("/"));
    }

    public JsonStack pop() {
        elementPath.pop();
        return this;
    }

    public JsonStack allow(String... allowed) {
        return allow(Set.of(allowed));
    }

    public JsonStack allow(Set<String> allowed) {
        assert elementPath.peek() != null;
        Set<String> present = elementPath.peek().json.keySet();
        Set<String> unexpected = Sets.difference(present, allowed);
        if (!unexpected.isEmpty()) {
            errors.add(
                    joinPath() + " allows children " + allowed + ". Did not expect " + unexpected);
        }
        return this;
    }

    public boolean getBoolean(String key) {
        return child(key, JsonType.BOOLEAN).getAsBoolean();
    }

    public boolean getBooleanOrElse(String key, boolean defaultValue) {
        Optional<JsonPrimitive> child = maybeChild(key, JsonType.BOOLEAN);
        return child.map(JsonPrimitive::getAsBoolean).orElse(defaultValue);
    }

    public int getInt(String key) {
        return child(key, JsonType.NUMBER).getAsInt();
    }

    public OptionalInt maybeInt(String key) {
        Optional<JsonPrimitive> child = maybeChild(key, JsonType.NUMBER);
        return child.map(jsonPrimitive -> OptionalInt.of(jsonPrimitive.getAsInt()))
                .orElseGet(OptionalInt::empty);
    }

    public String getString(String key) {
        return child(key, JsonType.STRING).getAsString();
    }

    public JsonObject getObject(String key) {
        return child(key, JsonType.OBJECT).getAsJsonObject();
    }

    public Optional<String> maybeString(String key) {
        Optional<JsonPrimitive> child = maybeChild(key, JsonType.STRING);
        return child.map(JsonPrimitive::getAsString);
    }

    public <T> Stream<T> streamAs(String key, Class<T> elementType) {
        return maybeChild(key, JsonType.ARRAY).stream()
                .flatMap(array -> StreamSupport.stream(array.spliterator(), false))
                .map(element -> gson.fromJson(element, elementType));
    }

    public JsonObject peek() {
        if (elementPath.isEmpty()) {
            return null;
        }
        return elementPath.peek().json();
    }

    public void addError(String msg) {
        errors.add(msg);
    }

    public List<String> getErrors() {
        return errors;
    }

    private record Member(JsonObject json, String name) {}
}
