package io.github.toberocat.improvedfactions.exceptions

import io.github.toberocat.improvedfactions.annotations.localization.Localization
import io.github.toberocat.improvedfactions.translation.LocalizedException

@Localization("base.exceptions.cant-invite-yourself")
class CantInviteYourselfException : LocalizedException("base.exceptions.cant-invite-yourself")