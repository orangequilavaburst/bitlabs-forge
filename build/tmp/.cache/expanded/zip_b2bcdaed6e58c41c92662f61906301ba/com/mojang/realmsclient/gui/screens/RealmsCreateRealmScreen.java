package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.util.task.CreateSnapshotRealmTask;
import com.mojang.realmsclient.util.task.WorldCreationTask;
import net.minecraft.Util;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsCreateRealmScreen extends RealmsScreen {
   private static final Component CREATE_REALM_TEXT = Component.translatable("mco.selectServer.create");
   private static final Component NAME_LABEL = Component.translatable("mco.configure.world.name");
   private static final Component DESCRIPTION_LABEL = Component.translatable("mco.configure.world.description");
   private static final int BUTTON_SPACING = 10;
   private static final int CONTENT_WIDTH = 210;
   private final RealmsMainScreen lastScreen;
   private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
   private EditBox nameBox;
   private EditBox descriptionBox;
   private final Runnable createWorldRunnable;

   public RealmsCreateRealmScreen(RealmsMainScreen p_311103_, RealmsServer p_309435_) {
      super(CREATE_REALM_TEXT);
      this.lastScreen = p_311103_;
      this.createWorldRunnable = () -> {
         this.createWorld(p_309435_);
      };
   }

   public RealmsCreateRealmScreen(RealmsMainScreen p_88575_, long p_311150_) {
      super(CREATE_REALM_TEXT);
      this.lastScreen = p_88575_;
      this.createWorldRunnable = () -> {
         this.createSnapshotWorld(p_311150_);
      };
   }

   public void init() {
      this.layout.addToHeader(new StringWidget(this.title, this.font));
      LinearLayout linearlayout = this.layout.addToContents(LinearLayout.vertical()).spacing(10);
      Button button = Button.builder(CommonComponents.GUI_CONTINUE, (p_308057_) -> {
         this.createWorldRunnable.run();
      }).build();
      button.active = false;
      this.nameBox = new EditBox(this.font, 210, 20, NAME_LABEL);
      this.nameBox.setResponder((p_296055_) -> {
         button.active = !Util.isBlank(p_296055_);
      });
      this.descriptionBox = new EditBox(this.font, 210, 20, DESCRIPTION_LABEL);
      linearlayout.addChild(CommonLayouts.labeledElement(this.font, this.nameBox, NAME_LABEL));
      linearlayout.addChild(CommonLayouts.labeledElement(this.font, this.descriptionBox, DESCRIPTION_LABEL));
      LinearLayout linearlayout1 = this.layout.addToFooter(LinearLayout.horizontal().spacing(10));
      linearlayout1.addChild(button);
      linearlayout1.addChild(Button.builder(CommonComponents.GUI_BACK, (p_296056_) -> {
         this.onClose();
      }).build());
      this.layout.visitWidgets((p_296058_) -> {
         AbstractWidget abstractwidget = this.addRenderableWidget(p_296058_);
      });
      this.repositionElements();
      this.setInitialFocus(this.nameBox);
   }

   protected void repositionElements() {
      this.layout.arrangeElements();
   }

   private void createWorld(RealmsServer p_310274_) {
      WorldCreationTask worldcreationtask = new WorldCreationTask(p_310274_.id, this.nameBox.getValue(), this.descriptionBox.getValue());
      RealmsResetWorldScreen realmsresetworldscreen = RealmsResetWorldScreen.forNewRealm(this, p_310274_, worldcreationtask, () -> {
         this.minecraft.execute(() -> {
            RealmsMainScreen realmsmainscreen = this.lastScreen;
            RealmsMainScreen.refreshServerList();
            this.minecraft.setScreen(this.lastScreen);
         });
      });
      this.minecraft.setScreen(realmsresetworldscreen);
   }

   private void createSnapshotWorld(long p_312127_) {
      Screen screen = new RealmsResetNormalWorldScreen((p_308056_) -> {
         if (p_308056_ == null) {
            this.minecraft.setScreen(this);
         } else {
            this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this, new CreateSnapshotRealmTask(this.lastScreen, p_312127_, p_308056_, this.nameBox.getValue(), this.descriptionBox.getValue())));
         }
      }, CREATE_REALM_TEXT);
      this.minecraft.setScreen(screen);
   }

   public void onClose() {
      this.minecraft.setScreen(this.lastScreen);
   }
}