package org.samo_lego.antilogout.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.GameRules;
import org.samo_lego.antilogout.datatracker.ILogoutRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class MServerPlayer_DeathMsgSaver {

    private static final int MAX_DEATH_MESSAGE_LENGTH = 256;
    @Unique
    private final ServerPlayer self = (ServerPlayer) (Object) this;

    @Shadow
    public abstract net.minecraft.world.level.Level level();

    /**
     * Saves death message for later if player is fake.
     */
    @Inject(method = "die", at = @At("RETURN"))
    private void onDie(DamageSource damageSource, CallbackInfo ci) {
        if (((ILogoutRules) this).al_isFake()) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            boolean seeDeathMsgs = serverLevel.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES);

            Component deathMsg;
            if (seeDeathMsgs) {
                deathMsg = self.getCombatTracker().getDeathMessage();

                if (deathMsg.getString().length() > MAX_DEATH_MESSAGE_LENGTH) {
                    String string = deathMsg.getString(MAX_DEATH_MESSAGE_LENGTH);
                    var attackTooLongMsg = Component.translatable("death.attack.message_too_long", Component.literal(string).withStyle(ChatFormatting.YELLOW));
                    
                    deathMsg = Component.translatable("death.attack.even_more_magic", self.getDisplayName())
                        .withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(attackTooLongMsg)));
                }
            } else {
                deathMsg = CommonComponents.EMPTY;
            }

            // Player won't see death message, we must save it for later (issue #1)
            ILogoutRules.SKIPPED_DEATH_MESSAGES.put(self.getUUID(), deathMsg);
        }
    }
}