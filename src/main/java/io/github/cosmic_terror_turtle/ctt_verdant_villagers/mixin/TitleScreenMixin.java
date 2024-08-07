package io.github.cosmic_terror_turtle.ctt_verdant_villagers.mixin;

import io.github.cosmic_terror_turtle.ctt_verdant_villagers.VerdantVillagers;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
	@Inject(at = @At("HEAD"), method = "init()V")
	private void init(CallbackInfo info) {
		VerdantVillagers.LOGGER.info("This line is called in TitleScreen#init!");
	}
}
