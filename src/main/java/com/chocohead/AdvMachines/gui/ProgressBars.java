package com.chocohead.AdvMachines.gui;

import ic2.core.gui.Gauge.GaugeProperties;
import ic2.core.gui.Gauge.GaugePropertyBuilder;
import ic2.core.gui.Gauge.GaugeStyle;
import ic2.core.gui.Gauge.IGaugeStyle;
import ic2.core.gui.Gauge.GaugePropertyBuilder.GaugeOrientation;
import java.util.Locale;
import net.minecraft.util.ResourceLocation;

public enum ProgressBars implements IGaugeStyle {
   PROGRESS_EXTRUDER(
      new GaugePropertyBuilder(176, 14, 29, 19, GaugeOrientation.Right)
         .withBackground(1, 1, 28, 19, 78, 34)
         .withTexture(new ResourceLocation("advanced_machines", "textures/gui/GUIExtruder.png"))
   );

   private final String name = this.name().toLowerCase(Locale.ENGLISH);
   private final GaugeProperties properties;

   private ProgressBars(GaugePropertyBuilder properties) {
      this.properties = properties.build();
   }

   public GaugeProperties getProperties() {
      return this.properties;
   }

   public static void addStyles() {
      for (ProgressBars bar : values()) {
         GaugeStyle.addStyle(bar.name, bar);
      }
   }
}
