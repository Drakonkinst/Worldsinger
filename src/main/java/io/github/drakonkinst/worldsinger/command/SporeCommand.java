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
package io.github.drakonkinst.worldsinger.command;

import static com.mojang.brigadier.arguments.DoubleArgumentType.getDouble;
import static com.mojang.brigadier.arguments.FloatArgumentType.getFloat;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.minecraft.command.argument.Vec3ArgumentType.getVec3;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeParticleManager;
import java.util.Optional;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class SporeCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("spore").requires(
                        source -> source.hasPermissionLevel(ModCommands.PERMISSION_LEVEL_GAMEMASTER))
                .then(argument("spore_type", StringArgumentType.word()).suggests(
                                ModCommands.AETHER_SPORE_TYPE_SUGGESTION_PROVIDER)
                        .then(argument("pos", Vec3ArgumentType.vec3()).then(
                                argument("horizontal_radius",
                                        DoubleArgumentType.doubleArg(0.0)).then(
                                        argument("height", DoubleArgumentType.doubleArg(0.0)).then(
                                                argument("size",
                                                        FloatArgumentType.floatArg(0.0f)).then(
                                                        argument("count",
                                                                IntegerArgumentType.integer(
                                                                        1)).executes(
                                                                SporeCommand::spawnSporeParticle))))))));
    }

    public static int spawnSporeParticle(CommandContext<ServerCommandSource> context)
            throws CommandSyntaxException {
        String aetherSporeTypeStr = getString(context, "spore_type");
        Optional<AetherSpores> aetherSporeType = AetherSpores.getAetherSporeTypeFromString(
                aetherSporeTypeStr);
        if (aetherSporeType.isEmpty()) {
            throw RegistryEntryArgumentType.NOT_FOUND_EXCEPTION.create(aetherSporeTypeStr,
                    "spore_sea");
        }
        Vec3d pos = getVec3(context, "pos");
        double horizontalRadius = getDouble(context, "horizontal_radius");
        double height = getDouble(context, "height");
        float size = getFloat(context, "size");
        int count = getInteger(context, "count");
        SporeParticleManager.createSporeParticles(context.getSource().getWorld(),
                aetherSporeType.get(), pos.x, pos.y, pos.z, horizontalRadius, height, size, count,
                false);
        context.getSource()
                .sendFeedback(() -> Text.translatable("commands.spore.success",
                        aetherSporeType.get().getName()), true);
        return 1;
    }
}
