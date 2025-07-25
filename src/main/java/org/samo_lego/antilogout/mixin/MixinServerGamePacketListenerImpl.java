package org.samo_lego.antilogout.mixin;

import org.samo_lego.antilogout.AntiLogout;
import org.samo_lego.antilogout.datatracker.LogoutRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerImpl extends ServerCommonPacketListenerImpl {
    @Shadow
    public ServerPlayer player;

    public MixinServerGamePacketListenerImpl(MinecraftServer minecraftServer, Connection connection,
            CommonListenerCookie commonListenerCookie) {
        super(minecraftServer, connection, commonListenerCookie);
    }

    @Shadow
    public abstract ServerPlayer getPlayer();

    /**
     * Hooks in the disconnect method, so that /afk works properly
     *
     * @param disconnectionDetails
     * @param ci
     */
    @Inject(method = "onDisconnect", at = @At("HEAD"), cancellable = true)
    private void al$onDisconnect(DisconnectionDetails disconnectionDetails, CallbackInfo ci) {
        // Generic disconnect is handled by MConnection#al_handleDisconnection
        if (!((LogoutRules) this.getPlayer()).al_allowDisconnect()
                && disconnectionDetails.reason() == AntiLogout.AFK_MESSAGE) {
            ((LogoutRules) this.player).al_onRealDisconnect();

            // Disable disconnecting in this case
            ci.cancel();
        }
    }
}
