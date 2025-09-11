package io.github.toberocat.improvedfactions.commands.arguments.primitives

import io.github.toberocat.improvedfactions.annotations.localization.Localization
import io.github.toberocat.improvedfactions.commands.arguments.ArgumentParser
import io.github.toberocat.improvedfactions.commands.arguments.ArgumentParsingException
import io.github.toberocat.improvedfactions.commands.arguments.ParsingContext
import org.bukkit.command.CommandSender

@Localization("base.arguments.enum.error")
open class EnumArgumentParser<E : Enum<E>>(private val clazz: Class<E>) : ArgumentParser {

    override fun parse(sender: CommandSender, arg: String, args: Array<String>): E =
            clazz.enumConstants.firstOrNull { it.name.equals(arg, true) }
                    ?: throw ArgumentParsingException(
                            "base.arguments.enum.error",
                            mapOf(
                                    "value" to arg,
                                    "options" to
                                            clazz.enumConstants.joinToString(", ") {
                                                it.name.lowercase()
                                            }
                            )
                    )

    override fun rawTabComplete(pCtx: ParsingContext) =
            clazz.enumConstants.map { it.name.lowercase() }
}
