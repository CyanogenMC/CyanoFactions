package io.github.toberocat.improvedfactions.config

import io.github.toberocat.improvedfactions.modules.base.BaseModule
import io.github.toberocat.improvedfactions.translation.LocalizationKey
import io.github.toberocat.improvedfactions.translation.getLocalized
import io.github.toberocat.improvedfactions.translation.sendLocalized
import io.github.toberocat.improvedfactions.utils.toAudience
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import org.bukkit.entity.Player
import java.time.Duration

enum class EventDisplayLocation {
    TITLE {
        override fun display(
            player: Player,
            mainMessage: LocalizationKey,
            subMessage: LocalizationKey?,
            placeholders: Map<String, String>
        ) {
            val config = BaseModule.config
            val audience = player.toAudience()
            
            // Create title times with configurable duration (converting ticks to milliseconds)
            val times = Title.Times.times(
                Duration.ofMillis(config.titleFadeInTicks * 50L), // 1 tick = 50ms
                Duration.ofMillis(config.titleStayTicks * 50L),
                Duration.ofMillis(config.titleFadeOutTicks * 50L)
            )
            
            val title = Title.title(
                player.getLocalized(mainMessage, placeholders),
                subMessage?.let { player.getLocalized(it, placeholders) } ?: net.kyori.adventure.text.Component.empty(),
                times
            )
            
            audience.showTitle(title)
        }
    },
    ACTIONBAR {
        override fun display(
            player: Player,
            mainMessage: LocalizationKey,
            subMessage: LocalizationKey?,
            placeholders: Map<String, String>
        ) {
            val audience = player.toAudience()
            audience.sendActionBar(player.getLocalized(mainMessage, placeholders))
            subMessage?.let { player.sendLocalized(it, placeholders) }
        }
    },
    CHAT {
        override fun display(
            player: Player,
            mainMessage: LocalizationKey,
            subMessage: LocalizationKey?,
            placeholders: Map<String, String>
        ) {
            player.sendLocalized(mainMessage, placeholders, true)
            subMessage?.let { player.sendLocalized(subMessage, placeholders, true) }
        }
    };

    abstract fun display(
        player: Player,
        mainMessage: LocalizationKey,
        subMessage: LocalizationKey?,
        placeholders: Map<String, String> = emptyMap()
    )

    fun displayLocationHandler(
        player: Player,
        mainMessage: LocalizationKey,
        subMessage: LocalizationKey?,
        placeholders: Map<String, String> = emptyMap()
    ) {
        when (this) {
            ACTIONBAR, CHAT -> display(
                player,
                subMessage!!,
                null,
                placeholders
            )

            else -> display(
                player,
                mainMessage,
                subMessage,
                placeholders
            )
        }
    }
}