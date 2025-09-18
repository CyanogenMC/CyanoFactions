package io.github.toberocat.improvedfactions.translation

import io.github.toberocat.improvedfactions.ImprovedFactionsPlugin
import io.github.toberocat.improvedfactions.modules.base.BaseModule
import io.github.toberocat.improvedfactions.utils.toAudience
import java.util.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Created: 04.08.2023
 * @author Tobias Madlberger (Tobias)
 */
typealias LocalizationKey = String

fun CommandSender.resolveLocalization(key: String, placeholders: Map<String, String> = emptyMap()) =
        when (this) {
                is Player -> getLocaleEnum().localizeUnformatted(key, placeholders)
                else -> Locale.ENGLISH.localizeUnformatted(key, placeholders)
        }

fun CommandSender.sendLocalized(key: String, placeholders: Map<String, String> = emptyMap()) {
        when (this) {
                is Player -> sendLocalized(key, placeholders)
                else -> sendMessage(Locale.ENGLISH.localizeUnformatted(key, placeholders))
        }
}

fun OfflinePlayer.sendLocalized(key: String, placeholders: Map<String, String> = emptyMap()) {
        player?.sendLocalized(key, placeholders)
        // ToDo: Make sure players can catch up on messages when they log in
}

fun Player.sendLocalized(
        key: String,
        placeholders: Map<String, String> = emptyMap(),
        prefixMessage: Boolean = false
) = toAudience().sendMessage(getLocalized(key, placeholders, prefixMessage))

fun Player.getLocalized(
        key: String,
        placeholders: Map<String, String> = emptyMap(),
        prefixMessage: Boolean = false
) = getLocaleEnum().localize(key, placeholders, prefixMessage)

fun Player.getLocaleEnum(): Locale {
        val localeParts = locale.split("_")
        val locale =
                when (localeParts.size) {
                        1 -> Locale.Builder().setLanguage(localeParts[0]).build()
                        2 ->
                                Locale.Builder()
                                        .setLanguage(localeParts[0])
                                        .setRegion(localeParts[1])
                                        .build()
                        3 ->
                                Locale.Builder()
                                        .setLanguage(localeParts[0])
                                        .setRegion(localeParts[1])
                                        .setVariant(localeParts[2])
                                        .build()
                        else -> Locale.ENGLISH
                }
        return locale
}

fun CommandSender.getUnformattedLocalized(
        key: String,
        placeholders: Map<String, String> = emptyMap()
): String {
        val locale =
                when (this) {
                        is Player -> getLocaleEnum()
                        else -> Locale.ENGLISH
                }
        return locale.localizeUnformatted(key, placeholders)
}

fun Locale.localize(
        key: LocalizationKey,
        placeholders: Map<String, String>,
        prefixMessage: Boolean = false
): Component =
        MiniMessage.miniMessage().deserialize(localizeUnformatted(key, placeholders, prefixMessage))

fun Locale.localizeUnformatted(
        key: LocalizationKey,
        placeholders: Map<String, String>,
        prefixMessage: Boolean = false
): String {
        val bundle = getBundle()
        if (!bundle.containsKey(key)) {
                ImprovedFactionsPlugin.instance.logger.warning(
                        "Missing $key in the language file for $this"
                )
                return key
        }
        var localizedString = bundle.getString(key)
        if (prefixMessage && !localizedString.trim().startsWith("{prefix}")) {
                if (!BaseModule.config.disablePrefix) {
                        localizedString = "{prefix} $localizedString"
                }
        }

        val mutablePlaceholders = placeholders.toMutableMap()
        if (!BaseModule.config.disablePrefix) {
                mutablePlaceholders["prefix"] = BaseModule.config.pluginPrefix
        } else {
                mutablePlaceholders["prefix"] = ""
        }

        fun replacePlaceholders(str: String, placeholders: Map<String, String>): String {
                var result = str
                var changed = false

                for ((placeholder, value) in placeholders) {
                        // Special handling for prefix when disabled - remove the space after it too
                        if (placeholder == "prefix" && BaseModule.config.disablePrefix) {
                                // First, try to replace {prefix} followed by spaces
                                val prefixWithSpacePattern = Regex("\\{\\s*prefix\\s*\\}\\s+")
                                val afterSpaceRemoval = result.replace(prefixWithSpacePattern, "")

                                // Then replace any remaining {prefix} without spaces
                                val prefixPattern = Regex("\\{\\s*prefix\\s*\\}")
                                val afterPrefixRemoval =
                                        afterSpaceRemoval.replace(prefixPattern, "")

                                if (afterPrefixRemoval != result) {
                                        result = afterPrefixRemoval
                                        changed = true
                                }
                                continue
                        }

                        val placeholderPattern = Regex("\\{\\s*${Regex.escape(placeholder)}\\s*\\}")
                        val replaced =
                                result.replace(
                                        placeholderPattern,
                                        MiniMessage.miniMessage()
                                                .serialize(
                                                        LegacyComponentSerializer.legacyAmpersand()
                                                                .deserialize(
                                                                        value.replace(
                                                                                LegacyComponentSerializer
                                                                                        .SECTION_CHAR
                                                                                        .toString(),
                                                                                LegacyComponentSerializer
                                                                                        .AMPERSAND_CHAR
                                                                                        .toString()
                                                                        )
                                                                )
                                                )
                                )
                        if (replaced == result) continue
                        result = replaced
                        changed = true
                }

                return if (changed) replacePlaceholders(result, placeholders) else result
        }
        return replacePlaceholders(localizedString, mutablePlaceholders)
}

fun Locale.getBundle(): ResourceBundle =
        ResourceBundle.getBundle(
                "languages.messages",
                this,
                ExternalResourceBundleLoader(
                        ImprovedFactionsPlugin.instance.dataFolder.absolutePath
                )
        )
