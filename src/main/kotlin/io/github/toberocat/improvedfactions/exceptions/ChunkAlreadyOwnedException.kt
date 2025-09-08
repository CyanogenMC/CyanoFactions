package io.github.toberocat.improvedfactions.exceptions

import io.github.toberocat.improvedfactions.translation.LocalizedException
import org.bukkit.Chunk

class ChunkAlreadyOwnedException(chunk: Chunk) : LocalizedException("base.exceptions.chunk-already-owned", mapOf(
    "x" to chunk.x.toString(),
    "z" to chunk.z.toString()
))
