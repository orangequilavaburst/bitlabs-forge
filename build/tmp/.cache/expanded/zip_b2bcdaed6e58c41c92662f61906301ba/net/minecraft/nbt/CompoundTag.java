package net.minecraft.nbt;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;

public class CompoundTag implements Tag {
   public static net.minecraftforge.common.util.INBTBuilder.Builder builder() {
      return (new net.minecraftforge.common.util.INBTBuilder(){}).nbt();
   }

   public static final Codec<CompoundTag> CODEC = Codec.PASSTHROUGH.comapFlatMap((p_308555_) -> {
      Tag tag = p_308555_.convert(NbtOps.INSTANCE).getValue();
      if (tag instanceof CompoundTag compoundtag) {
         return DataResult.success(compoundtag == p_308555_.getValue() ? compoundtag.copy() : compoundtag);
      } else {
         return DataResult.error(() -> {
            return "Not a compound tag: " + tag;
         });
      }
   }, (p_308554_) -> {
      return new Dynamic<>(NbtOps.INSTANCE, p_308554_.copy());
   });
   private static final int SELF_SIZE_IN_BYTES = 48;
   private static final int MAP_ENTRY_SIZE_IN_BYTES = 32;
   public static final TagType<CompoundTag> TYPE = new TagType.VariableSize<CompoundTag>() {
      public CompoundTag load(DataInput p_128485_, NbtAccounter p_128487_) throws IOException {
         p_128487_.pushDepth();

         CompoundTag compoundtag;
         try {
            compoundtag = loadCompound(p_128485_, p_128487_);
         } finally {
            p_128487_.popDepth();
         }

         return compoundtag;
      }

      private static CompoundTag loadCompound(DataInput p_301703_, NbtAccounter p_301763_) throws IOException {
         p_301763_.accountBytes(48L);
         Map<String, Tag> map = Maps.newHashMap();

         byte b0;
         while((b0 = p_301703_.readByte()) != 0) {
            String s = readString(p_301703_, p_301763_);
            Tag tag = CompoundTag.readNamedTagData(TagTypes.getType(b0), s, p_301703_, p_301763_);
            if (map.put(s, tag) == null) {
               p_301763_.accountBytes(36L);
            }
         }

         return new CompoundTag(map);
      }

      public StreamTagVisitor.ValueResult parse(DataInput p_197446_, StreamTagVisitor p_197447_, NbtAccounter p_301769_) throws IOException {
         p_301769_.pushDepth();

         StreamTagVisitor.ValueResult streamtagvisitor$valueresult;
         try {
            streamtagvisitor$valueresult = parseCompound(p_197446_, p_197447_, p_301769_);
         } finally {
            p_301769_.popDepth();
         }

         return streamtagvisitor$valueresult;
      }

      private static StreamTagVisitor.ValueResult parseCompound(DataInput p_301721_, StreamTagVisitor p_301777_, NbtAccounter p_301778_) throws IOException {
         p_301778_.accountBytes(48L);

         while(true) {
            byte b0;
            if ((b0 = p_301721_.readByte()) != 0) {
               TagType<?> tagtype = TagTypes.getType(b0);
               switch (p_301777_.visitEntry(tagtype)) {
                  case HALT:
                     return StreamTagVisitor.ValueResult.HALT;
                  case BREAK:
                     StringTag.skipString(p_301721_);
                     tagtype.skip(p_301721_, p_301778_);
                     break;
                  case SKIP:
                     StringTag.skipString(p_301721_);
                     tagtype.skip(p_301721_, p_301778_);
                     continue;
                  default:
                     String s = readString(p_301721_, p_301778_);
                     switch (p_301777_.visitEntry(tagtype, s)) {
                        case HALT:
                           return StreamTagVisitor.ValueResult.HALT;
                        case BREAK:
                           tagtype.skip(p_301721_, p_301778_);
                           break;
                        case SKIP:
                           tagtype.skip(p_301721_, p_301778_);
                           continue;
                        default:
                           p_301778_.accountBytes(36L);
                           switch (tagtype.parse(p_301721_, p_301777_, p_301778_)) {
                              case HALT:
                                 return StreamTagVisitor.ValueResult.HALT;
                              case BREAK:
                              default:
                                 continue;
                           }
                     }
               }
            }

            if (b0 != 0) {
               while((b0 = p_301721_.readByte()) != 0) {
                  StringTag.skipString(p_301721_);
                  TagTypes.getType(b0).skip(p_301721_, p_301778_);
               }
            }

            return p_301777_.visitContainerEnd();
         }
      }

      private static String readString(DataInput p_301867_, NbtAccounter p_301863_) throws IOException {
         String s = p_301867_.readUTF();
         p_301863_.accountBytes(28L);
         p_301863_.accountBytes(2L, (long)s.length());
         return s;
      }

      public void skip(DataInput p_197444_, NbtAccounter p_301720_) throws IOException {
         p_301720_.pushDepth();

         byte b0;
         try {
            while((b0 = p_197444_.readByte()) != 0) {
               StringTag.skipString(p_197444_);
               TagTypes.getType(b0).skip(p_197444_, p_301720_);
            }
         } finally {
            p_301720_.popDepth();
         }

      }

      public String getName() {
         return "COMPOUND";
      }

      public String getPrettyName() {
         return "TAG_Compound";
      }
   };
   private final Map<String, Tag> tags;

   protected CompoundTag(Map<String, Tag> p_128333_) {
      this.tags = p_128333_;
   }

   public CompoundTag() {
      this(Maps.newHashMap());
   }

   public void write(DataOutput p_128341_) throws IOException {
      for(String s : this.tags.keySet()) {
         Tag tag = this.tags.get(s);
         writeNamedTag(s, tag, p_128341_);
      }

      p_128341_.writeByte(0);
   }

   public int sizeInBytes() {
      int i = 48;

      for(Map.Entry<String, Tag> entry : this.tags.entrySet()) {
         i += 28 + 2 * entry.getKey().length();
         i += 36;
         i += entry.getValue().sizeInBytes();
      }

      return i;
   }

   public Set<String> getAllKeys() {
      return this.tags.keySet();
   }

   public byte getId() {
      return 10;
   }

   public TagType<CompoundTag> getType() {
      return TYPE;
   }

   public int size() {
      return this.tags.size();
   }

   @Nullable
   public Tag put(String p_128366_, Tag p_128367_) {
      if (p_128367_ == null) throw new IllegalArgumentException("Invalid null NBT value with key " + p_128366_);
      return this.tags.put(p_128366_, p_128367_);
   }

   public void putByte(String p_128345_, byte p_128346_) {
      this.tags.put(p_128345_, ByteTag.valueOf(p_128346_));
   }

   public void putShort(String p_128377_, short p_128378_) {
      this.tags.put(p_128377_, ShortTag.valueOf(p_128378_));
   }

   public void putInt(String p_128406_, int p_128407_) {
      this.tags.put(p_128406_, IntTag.valueOf(p_128407_));
   }

   public void putLong(String p_128357_, long p_128358_) {
      this.tags.put(p_128357_, LongTag.valueOf(p_128358_));
   }

   public void putUUID(String p_128363_, UUID p_128364_) {
      this.tags.put(p_128363_, NbtUtils.createUUID(p_128364_));
   }

   public UUID getUUID(String p_128343_) {
      return NbtUtils.loadUUID(this.get(p_128343_));
   }

   public boolean hasUUID(String p_128404_) {
      Tag tag = this.get(p_128404_);
      return tag != null && tag.getType() == IntArrayTag.TYPE && ((IntArrayTag)tag).getAsIntArray().length == 4;
   }

   public void putFloat(String p_128351_, float p_128352_) {
      this.tags.put(p_128351_, FloatTag.valueOf(p_128352_));
   }

   public void putDouble(String p_128348_, double p_128349_) {
      this.tags.put(p_128348_, DoubleTag.valueOf(p_128349_));
   }

   public void putString(String p_128360_, String p_128361_) {
      this.tags.put(p_128360_, StringTag.valueOf(p_128361_));
   }

   public void putByteArray(String p_128383_, byte[] p_128384_) {
      this.tags.put(p_128383_, new ByteArrayTag(p_128384_));
   }

   public void putByteArray(String p_177854_, List<Byte> p_177855_) {
      this.tags.put(p_177854_, new ByteArrayTag(p_177855_));
   }

   public void putIntArray(String p_128386_, int[] p_128387_) {
      this.tags.put(p_128386_, new IntArrayTag(p_128387_));
   }

   public void putIntArray(String p_128409_, List<Integer> p_128410_) {
      this.tags.put(p_128409_, new IntArrayTag(p_128410_));
   }

   public void putLongArray(String p_128389_, long[] p_128390_) {
      this.tags.put(p_128389_, new LongArrayTag(p_128390_));
   }

   public void putLongArray(String p_128429_, List<Long> p_128430_) {
      this.tags.put(p_128429_, new LongArrayTag(p_128430_));
   }

   public void putBoolean(String p_128380_, boolean p_128381_) {
      this.tags.put(p_128380_, ByteTag.valueOf(p_128381_));
   }

   @Nullable
   public Tag get(String p_128424_) {
      return this.tags.get(p_128424_);
   }

   public byte getTagType(String p_128436_) {
      Tag tag = this.tags.get(p_128436_);
      return tag == null ? 0 : tag.getId();
   }

   public boolean contains(String p_128442_) {
      return this.tags.containsKey(p_128442_);
   }

   public boolean contains(String p_128426_, int p_128427_) {
      int i = this.getTagType(p_128426_);
      if (i == p_128427_) {
         return true;
      } else if (p_128427_ != 99) {
         return false;
      } else {
         return i == 1 || i == 2 || i == 3 || i == 4 || i == 5 || i == 6;
      }
   }

   public byte getByte(String p_128446_) {
      try {
         if (this.contains(p_128446_, 99)) {
            return ((NumericTag)this.tags.get(p_128446_)).getAsByte();
         }
      } catch (ClassCastException classcastexception) {
      }

      return 0;
   }

   public short getShort(String p_128449_) {
      try {
         if (this.contains(p_128449_, 99)) {
            return ((NumericTag)this.tags.get(p_128449_)).getAsShort();
         }
      } catch (ClassCastException classcastexception) {
      }

      return 0;
   }

   public int getInt(String p_128452_) {
      try {
         if (this.contains(p_128452_, 99)) {
            return ((NumericTag)this.tags.get(p_128452_)).getAsInt();
         }
      } catch (ClassCastException classcastexception) {
      }

      return 0;
   }

   public long getLong(String p_128455_) {
      try {
         if (this.contains(p_128455_, 99)) {
            return ((NumericTag)this.tags.get(p_128455_)).getAsLong();
         }
      } catch (ClassCastException classcastexception) {
      }

      return 0L;
   }

   public float getFloat(String p_128458_) {
      try {
         if (this.contains(p_128458_, 99)) {
            return ((NumericTag)this.tags.get(p_128458_)).getAsFloat();
         }
      } catch (ClassCastException classcastexception) {
      }

      return 0.0F;
   }

   public double getDouble(String p_128460_) {
      try {
         if (this.contains(p_128460_, 99)) {
            return ((NumericTag)this.tags.get(p_128460_)).getAsDouble();
         }
      } catch (ClassCastException classcastexception) {
      }

      return 0.0D;
   }

   public String getString(String p_128462_) {
      try {
         if (this.contains(p_128462_, 8)) {
            return this.tags.get(p_128462_).getAsString();
         }
      } catch (ClassCastException classcastexception) {
      }

      return "";
   }

   public byte[] getByteArray(String p_128464_) {
      try {
         if (this.contains(p_128464_, 7)) {
            return ((ByteArrayTag)this.tags.get(p_128464_)).getAsByteArray();
         }
      } catch (ClassCastException classcastexception) {
         throw new ReportedException(this.createReport(p_128464_, ByteArrayTag.TYPE, classcastexception));
      }

      return new byte[0];
   }

   public int[] getIntArray(String p_128466_) {
      try {
         if (this.contains(p_128466_, 11)) {
            return ((IntArrayTag)this.tags.get(p_128466_)).getAsIntArray();
         }
      } catch (ClassCastException classcastexception) {
         throw new ReportedException(this.createReport(p_128466_, IntArrayTag.TYPE, classcastexception));
      }

      return new int[0];
   }

   public long[] getLongArray(String p_128468_) {
      try {
         if (this.contains(p_128468_, 12)) {
            return ((LongArrayTag)this.tags.get(p_128468_)).getAsLongArray();
         }
      } catch (ClassCastException classcastexception) {
         throw new ReportedException(this.createReport(p_128468_, LongArrayTag.TYPE, classcastexception));
      }

      return new long[0];
   }

   public CompoundTag getCompound(String p_128470_) {
      try {
         if (this.contains(p_128470_, 10)) {
            return (CompoundTag)this.tags.get(p_128470_);
         }
      } catch (ClassCastException classcastexception) {
         throw new ReportedException(this.createReport(p_128470_, TYPE, classcastexception));
      }

      return new CompoundTag();
   }

   public ListTag getList(String p_128438_, int p_128439_) {
      try {
         if (this.getTagType(p_128438_) == 9) {
            ListTag listtag = (ListTag)this.tags.get(p_128438_);
            if (!listtag.isEmpty() && listtag.getElementType() != p_128439_) {
               return new ListTag();
            }

            return listtag;
         }
      } catch (ClassCastException classcastexception) {
         throw new ReportedException(this.createReport(p_128438_, ListTag.TYPE, classcastexception));
      }

      return new ListTag();
   }

   public boolean getBoolean(String p_128472_) {
      return this.getByte(p_128472_) != 0;
   }

   public void remove(String p_128474_) {
      this.tags.remove(p_128474_);
   }

   public String toString() {
      return this.getAsString();
   }

   public boolean isEmpty() {
      return this.tags.isEmpty();
   }

   private CrashReport createReport(String p_128373_, TagType<?> p_128374_, ClassCastException p_128375_) {
      CrashReport crashreport = CrashReport.forThrowable(p_128375_, "Reading NBT data");
      CrashReportCategory crashreportcategory = crashreport.addCategory("Corrupt NBT tag", 1);
      crashreportcategory.setDetail("Tag type found", () -> {
         return this.tags.get(p_128373_).getType().getName();
      });
      crashreportcategory.setDetail("Tag type expected", p_128374_::getName);
      crashreportcategory.setDetail("Tag name", p_128373_);
      return crashreport;
   }

   public CompoundTag copy() {
      Map<String, Tag> map = Maps.newHashMap(Maps.transformValues(this.tags, Tag::copy));
      return new CompoundTag(map);
   }

   public boolean equals(Object p_128444_) {
      if (this == p_128444_) {
         return true;
      } else {
         return p_128444_ instanceof CompoundTag && Objects.equals(this.tags, ((CompoundTag)p_128444_).tags);
      }
   }

   public int hashCode() {
      return this.tags.hashCode();
   }

   private static void writeNamedTag(String p_128369_, Tag p_128370_, DataOutput p_128371_) throws IOException {
      p_128371_.writeByte(p_128370_.getId());
      if (p_128370_.getId() != 0) {
         p_128371_.writeUTF(p_128369_);
         p_128370_.write(p_128371_);
      }
   }

   static Tag readNamedTagData(TagType<?> p_128414_, String p_128415_, DataInput p_128416_, NbtAccounter p_128418_) {
      try {
         return p_128414_.load(p_128416_, p_128418_);
      } catch (IOException ioexception) {
         CrashReport crashreport = CrashReport.forThrowable(ioexception, "Loading NBT data");
         CrashReportCategory crashreportcategory = crashreport.addCategory("NBT Tag");
         crashreportcategory.setDetail("Tag name", p_128415_);
         crashreportcategory.setDetail("Tag type", p_128414_.getName());
         throw new ReportedNbtException(crashreport);
      }
   }

   public CompoundTag merge(CompoundTag p_128392_) {
      for(String s : p_128392_.tags.keySet()) {
         Tag tag = p_128392_.tags.get(s);
         if (tag.getId() == 10) {
            if (this.contains(s, 10)) {
               CompoundTag compoundtag = this.getCompound(s);
               compoundtag.merge((CompoundTag)tag);
            } else {
               this.put(s, tag.copy());
            }
         } else {
            this.put(s, tag.copy());
         }
      }

      return this;
   }

   public void accept(TagVisitor p_177857_) {
      p_177857_.visitCompound(this);
   }

   protected Map<String, Tag> entries() {
      return Collections.unmodifiableMap(this.tags);
   }

   public StreamTagVisitor.ValueResult accept(StreamTagVisitor p_197442_) {
      for(Map.Entry<String, Tag> entry : this.tags.entrySet()) {
         Tag tag = entry.getValue();
         TagType<?> tagtype = tag.getType();
         StreamTagVisitor.EntryResult streamtagvisitor$entryresult = p_197442_.visitEntry(tagtype);
         switch (streamtagvisitor$entryresult) {
            case HALT:
               return StreamTagVisitor.ValueResult.HALT;
            case BREAK:
               return p_197442_.visitContainerEnd();
            case SKIP:
               break;
            default:
               streamtagvisitor$entryresult = p_197442_.visitEntry(tagtype, entry.getKey());
               switch (streamtagvisitor$entryresult) {
                  case HALT:
                     return StreamTagVisitor.ValueResult.HALT;
                  case BREAK:
                     return p_197442_.visitContainerEnd();
                  case SKIP:
                     break;
                  default:
                     StreamTagVisitor.ValueResult streamtagvisitor$valueresult = tag.accept(p_197442_);
                     switch (streamtagvisitor$valueresult) {
                        case HALT:
                           return StreamTagVisitor.ValueResult.HALT;
                        case BREAK:
                           return p_197442_.visitContainerEnd();
                     }
               }
         }
      }

      return p_197442_.visitContainerEnd();
   }
}
