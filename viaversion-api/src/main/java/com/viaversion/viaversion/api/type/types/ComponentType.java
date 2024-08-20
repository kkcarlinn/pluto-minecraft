/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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
package com.viaversion.viaversion.api.type.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class ComponentType extends Type<JsonElement> {
    private static final StringType STRING_TAG = new StringType(262144);

    public ComponentType() {
        super(JsonElement.class);
    }

    @Override
    public JsonElement read(ByteBuf buffer) throws Exception {
        String s = STRING_TAG.read(buffer);
        try {
            return new JsonParser().parse(s);
        } catch (JsonSyntaxException e) {
            Via.getPlatform().getLogger().severe("Error when trying to parse json: " + s);
            throw e;
        }
    }

    @Override
    public void write(ByteBuf buffer, JsonElement object) throws Exception {
        STRING_TAG.write(buffer, object.toString());
    }

    public static final class OptionalComponentType extends OptionalType<JsonElement> {

        public OptionalComponentType() {
            super(Type.COMPONENT);
        }
    }
}
