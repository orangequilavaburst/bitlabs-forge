package net.minecraft.network.chat.contents;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public record StorageDataSource(ResourceLocation id) implements DataSource {
   public static final MapCodec<StorageDataSource> SUB_CODEC = RecordCodecBuilder.mapCodec((p_309414_) -> {
      return p_309414_.group(ResourceLocation.CODEC.fieldOf("storage").forGetter(StorageDataSource::id)).apply(p_309414_, StorageDataSource::new);
   });
   public static final DataSource.Type<StorageDataSource> TYPE = new DataSource.Type<>(SUB_CODEC, "storage");

   public Stream<CompoundTag> getData(CommandSourceStack p_237491_) {
      CompoundTag compoundtag = p_237491_.getServer().getCommandStorage().get(this.id);
      return Stream.of(compoundtag);
   }

   public DataSource.Type<?> type() {
      return TYPE;
   }

   public String toString() {
      return "storage=" + this.id;
   }
}