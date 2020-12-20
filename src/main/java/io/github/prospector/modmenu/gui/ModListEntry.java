package io.github.prospector.modmenu.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import io.github.prospector.modmenu.util.BadgeRenderer;
import io.github.prospector.modmenu.util.Mod;
import io.github.prospector.modmenu.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModListEntry extends AlwaysSelectedEntryListWidget.Entry<ModListEntry> {
	public static final Identifier UNKNOWN_ICON = new Identifier("textures/misc/unknown_pack.png");
	private static final Logger LOGGER = LogManager.getLogger();

	protected final MinecraftClient client;
	protected final Mod mod;
	protected final ModListWidget list;
	protected Identifier iconLocation;

	public ModListEntry(Mod mod, ModListWidget list) {
		this.mod = mod;
		this.list = list;
		this.client = MinecraftClient.getInstance();
	}

	@Override
	public void render(MatrixStack matrices, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
		x += getXOffset();
		rowWidth -= getXOffset();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.bindIconTexture();
		RenderSystem.enableBlend();
		DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 32, 32, 32, 32);
		RenderSystem.disableBlend();
		Text name = new LiteralText(mod.getName());
		StringVisitable trimmedName = name;
		int maxNameWidth = rowWidth - 32 - 3;
		TextRenderer font = this.client.textRenderer;
		if (font.getWidth(name) > maxNameWidth) {
			StringVisitable ellipsis = StringVisitable.plain("...");
			trimmedName = StringVisitable.concat(font.trimToWidth(name, maxNameWidth - font.getWidth(ellipsis)), ellipsis);
		}
		font.draw(matrices, Language.getInstance().reorder(trimmedName), x + 32 + 3, y + 1, 0xFFFFFF);
		if (!ModMenuConfigManager.getConfig().areBadgesHidden()) {
			new BadgeRenderer(x + 32 + 3 + font.getWidth(name) + 2, y, x + rowWidth, mod, list.getParent()).draw(matrices, mouseX, mouseY);
		}
		RenderUtils.drawWrappedString(matrices, mod.getSummary(), (x + 32 + 3 + 4), (y + client.textRenderer.fontHeight + 2), rowWidth - 32 - 7, 2, 0x808080);
	}

	@Override
	public boolean mouseClicked(double v, double v1, int i) {
		list.select(this);
		return true;
	}

	public Mod getMod() {
		return mod;
	}

	public void bindIconTexture() {
		if (this.iconLocation == null) {
			this.iconLocation = new Identifier(ModMenu.MOD_ID, mod.getId() + "_icon");
			NativeImageBackedTexture icon = mod.getIcon(list.getIconHandler(), 64 * MinecraftClient.getInstance().options.guiScale);
			if (icon != null) {
				this.client.getTextureManager().registerTexture(this.iconLocation, icon);
			} else {
				this.iconLocation = UNKNOWN_ICON;
			}
		}
		this.client.getTextureManager().bindTexture(this.iconLocation);
	}

	public int getXOffset() {
		return 0;
	}
}
