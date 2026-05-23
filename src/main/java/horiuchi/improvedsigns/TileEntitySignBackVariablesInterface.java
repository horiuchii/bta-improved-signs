package horiuchi.improvedsigns;

import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumSignPicture;
import net.minecraft.core.net.command.TextFormatting;
import org.jetbrains.annotations.Nullable;

public interface TileEntitySignBackVariablesInterface {
	boolean improvedsigns$getBackBeingEdited();

	void improvedsigns$setBackBeingEdited(boolean backBeingEdited);

	String[] improvedsigns$getBackText();

	@Nullable
	EnumSignPicture improvedsigns$getPictureBack();

	void improvedsigns$setPictureBack(@Nullable EnumSignPicture picture);

	TextFormatting improvedsigns$getColorBack();

	void improvedsigns$setColorBack(TextFormatting color);

	boolean improvedsigns$isGlowingBack();

	void improvedsigns$setGlowingBack(boolean glowing);

	boolean improvedsigns$isLockedBack();

	void improvedsigns$setLockedBack(boolean locked);

	boolean improvedsigns$isBackEditableBy(Player player);
}
