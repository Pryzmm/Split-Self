package com.pryzmm.splitself.forge;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import java.util.HashMap;
import java.util.Map;

@Mod(SplitSelfForge.MOD_ID)
public final class SplitSelfForge {
    public static final String MOD_ID = "splitself";
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);
    public static final RegistryObject<EntityType<UIButtonEntity>> UI_BUTTON = ENTITIES.register(
            "ui_button", () -> EntityType.Builder.<UIButtonEntity>of(UIButtonEntity::new, MobCategory.MISC)
                    .sized(2.0f, 0.3f).clientTrackingRange(64).updateInterval(10).build(MOD_ID + ":ui_button"));
    public static final RegistryObject<EntityType<TheOtherEntity>> THE_OTHER = ENTITIES.register(
            "the_other", () -> EntityType.Builder.<TheOtherEntity>of(TheOtherEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.8f).clientTrackingRange(64).build(MOD_ID + ":the_other"));
    public static final RegistryObject<EntityType<TheForgottenEntity>> THE_FORGOTTEN = ENTITIES.register(
            "the_forgotten", () -> EntityType.Builder.<TheForgottenEntity>of(TheForgottenEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.8f).clientTrackingRange(64).build(MOD_ID + ":the_forgotten"));
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MOD_ID);
    private static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, MOD_ID);
    private static final DeferredRegister<net.minecraft.world.level.levelgen.structure.StructureType<?>> STRUCTURES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, MOD_ID);
    private static final DeferredRegister<net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, MOD_ID);
    static {EmptyStructure.register(STRUCTURES,STRUCTURE_PIECES);}
    static final RegistryObject<net.minecraft.world.level.levelgen.structure.StructureType<FinalEscapeStructure>> FINAL_ESCAPE =
            STRUCTURES.register("final_escape", () -> () -> FinalEscapeStructure.CODEC);
    static final RegistryObject<net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType> FINAL_ESCAPE_PIECE =
            STRUCTURE_PIECES.register("final_escape", () -> (context, tag) ->
                    new FinalEscapeStructure.Piece(context.structureTemplateManager(), tag));
    public static final RegistryObject<Codec<? extends ChunkGenerator>> HALLWAY_GENERATOR =
            CHUNK_GENERATORS.register("hallway", () -> HallwayChunkGenerator.CODEC);
    public static final RegistryObject<Codec<? extends ChunkGenerator>> GRASS_EMPTY_GENERATOR =
            CHUNK_GENERATORS.register("grass_empty", () -> GrassEmptyChunkGenerator.CODEC);
    public static final RegistryObject<Codec<? extends ChunkGenerator>> VOID_GENERATOR =
            CHUNK_GENERATORS.register("void", () -> VoidChunkGenerator.CODEC);
    public static final RegistryObject<Codec<? extends ChunkGenerator>> DEAD_CORAL_GENERATOR =
            CHUNK_GENERATORS.register("dead_coral", () -> DeadCoralChunkGenerator.CODEC);

    // These two plain blocks retain the IDs, sounds, hardness and resistance
    // from ModBlocks.java in the pinned 1.0.2 source.
    public static final RegistryObject<Block> BRAINS = registerBlock(
            "brains", () -> new Block(BlockBehaviour.Properties.of()
                    .sound(SoundType.HONEY_BLOCK).strength(3.0f, 3.0f)));
    public static final RegistryObject<Block> DEAD_BRAINS = registerBlock(
            "dead_brains", () -> new Block(BlockBehaviour.Properties.of()
                    .sound(SoundType.HONEY_BLOCK).strength(3.0f, 3.0f)));
    public static final RegistryObject<Block> DARKNESS = registerBlock(
            "darkness", () -> new Block(BlockBehaviour.Properties.of()
                    .sound(SoundType.EMPTY).strength(10000.0f, 10000.0f)));
    public static final RegistryObject<Block> KEYBOARD = registerBlock(
            "keyboard", () -> DirectionalPropBlock.keyboard(BlockBehaviour.Properties.of()
                    .sound(SoundType.METAL).noOcclusion().strength(3.0f, 3.0f)));
    public static final RegistryObject<Block> TABLE = registerBlock(
            "table", () -> DirectionalPropBlock.table(BlockBehaviour.Properties.of()
                    .sound(SoundType.WOOD).noOcclusion().strength(3.0f, 3.0f)));
    public static final RegistryObject<Block> COMPUTER = registerBlock(
            "computer", () -> new ComputerBlock(BlockBehaviour.Properties.of()
                    .sound(SoundType.METAL).noOcclusion().strength(3.0f, 3.0f)));
    public static final RegistryObject<Block> BRAIN = registerBlock(
            "brain", () -> new BrainBlock(BlockBehaviour.Properties.of()
                    .sound(SoundType.HONEY_BLOCK).instabreak()));
    public static final RegistryObject<Block> EXIT_DOOR = registerBlock(
            "exit_door", () -> new ExitDoorBlock(BlockBehaviour.Properties.of()
                    .sound(SoundType.METAL).noOcclusion().strength(3.0f, 3.0f)));
    public static final RegistryObject<Block> TRIGGER_TRANSITION = registerBlock(
            "dev_trigger_transition", () -> new TriggerTransitionBlock(BlockBehaviour.Properties.of()
                    .noOcclusion().noCollission().strength(10000.0f, 10000.0f)));
    public static final RegistryObject<Block> BROKEN_FLOWER = registerBlock(
            "broken_flower", () -> new BrokenFlowerBlock(BlockBehaviour.Properties.of()
                    .noOcclusion().noCollission().strength(10000.0f, 10000.0f)));
    public static final RegistryObject<Block> IMAGE_FRAME = registerBlock(
            "image_frame", () -> new ImageFrameBlock(BlockBehaviour.Properties.of()
                    .sound(SoundType.WOOD).noOcclusion().ignitedByLava().instabreak()));
    public static final RegistryObject<BlockEntityType<ImageFrameBlockEntity>> IMAGE_FRAME_ENTITY =
            BLOCK_ENTITIES.register("image_frame", () -> BlockEntityType.Builder.of(
                    ImageFrameBlockEntity::new, IMAGE_FRAME.get()).build(null));

    // ModItems.CIET_DOKE: nutrition 10, always edible. Saturation is the
    // Minecraft 1.20.1 default (0.3), matching the original builder default.
    public static final RegistryObject<Item> CIET_DOKE = ITEMS.register("ciet_doke",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(10).alwaysEat().build())));

    private static final String[] SOUND_IDS = {
            "redsky", "static", "static2", "screech", "horn", "staticscream",
            "amstatic", "hum", "amber", "buzz", "rumble2", "scream",
            "disappear", "whisper", "glitch", "glitch2", "forgotten", "memory",
            "tone", "scratch", "block.brain.squish", "block.brain.scrape",
            "delete", "delete_hum", "keyboard", "in_my_life", "rumble",
            "final", "freedom"
    };
    private static final Map<String, RegistryObject<SoundEvent>> SOUND_EVENTS = new HashMap<>();
    public static final RegistryObject<SoundEvent> MEMORY_SOUND;
    public static final RegistryObject<SoundEvent> KEYBOARD_SOUND;
    public static final RegistryObject<SoundEvent> SQUISH_SOUND;
    public static final RegistryObject<SoundEvent> SCRAPE_SOUND;

    static {
        for (String name : SOUND_IDS) {
            SOUND_EVENTS.put(name, SOUNDS.register(name,
                    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID, name))));
        }
        MEMORY_SOUND = SOUND_EVENTS.get("memory");
        KEYBOARD_SOUND = SOUND_EVENTS.get("keyboard");
        SQUISH_SOUND = SOUND_EVENTS.get("block.brain.squish");
        SCRAPE_SOUND = SOUND_EVENTS.get("block.brain.scrape");
    }

    public static final RegistryObject<Item> MEMORY_HOUSE = registerMemory("house");
    public static final RegistryObject<Item> MEMORY_BLU = registerMemory("blu");
    public static final RegistryObject<Item> MEMORY_MINES = registerMemory("mines");
    public static final RegistryObject<Item> MEMORY_PILLAR = registerMemory("pillar");
    public static final RegistryObject<Item> MEMORY_CREEPER = registerMemory("creeper");
    public static final RegistryObject<Item> MEMORY_BOOK = ITEMS.register("memory_book",
            () -> new MemoryBookItem(new Item.Properties().stacksTo(1)
                    .rarity(Rarity.EPIC).fireResistant()));

    private static RegistryObject<Item> registerMemory(String id) {
        return ITEMS.register("memory_" + id,
                () -> new MemoryItem(new Item.Properties().stacksTo(64)
                        .rarity(Rarity.RARE).fireResistant(), id));
    }

    // 1.21 jukebox-song data maps to RecordItem in Minecraft 1.20.1.
    // Both songs use comparator output 1; lengths are seconds * 20 ticks.
    public static final RegistryObject<Item> IN_MY_LIFE_MUSIC_DISC = ITEMS.register(
            "in_my_life_music_disc", () -> new RecordItem(1,
                    () -> SOUND_EVENTS.get("in_my_life").get(),
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 1450));
    public static final RegistryObject<Item> FREEDOM_MUSIC_DISC = ITEMS.register(
            "freedom_music_disc", () -> new RecordItem(1,
                    () -> SOUND_EVENTS.get("freedom").get(),
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 1747));

    public static final RegistryObject<CreativeModeTab> SPLIT_SELF_ITEMS = CREATIVE_TABS.register(
            "split_self_items", () -> CreativeModeTab.builder()
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .icon(() -> new ItemStack(IN_MY_LIFE_MUSIC_DISC.get()))
                    .title(net.minecraft.network.chat.Component.translatable("itemgroup.splitself.splitself_items"))
                    .displayItems((parameters, output) -> {
                        output.accept(IN_MY_LIFE_MUSIC_DISC.get());
                        output.accept(FREEDOM_MUSIC_DISC.get());
                        output.accept(BRAIN.get());
                        output.accept(BRAINS.get());
                        output.accept(DEAD_BRAINS.get());
                        output.accept(EXIT_DOOR.get());
                        output.accept(DARKNESS.get());
                        output.accept(BROKEN_FLOWER.get());
                        output.accept(IMAGE_FRAME.get());
                        output.accept(MEMORY_BOOK.get());
                        output.accept(MEMORY_HOUSE.get());
                        output.accept(MEMORY_BLU.get());
                        output.accept(MEMORY_MINES.get());
                        output.accept(MEMORY_PILLAR.get());
                        output.accept(MEMORY_CREEPER.get());
                        output.accept(COMPUTER.get());
                        output.accept(KEYBOARD.get());
                        output.accept(TABLE.get());
                        output.accept(CIET_DOKE.get());
                    }).build());

    private static RegistryObject<Block> registerBlock(String name, java.util.function.Supplier<Block> factory) {
        RegistryObject<Block> block = BLOCKS.register(name, factory);
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

    public SplitSelfForge() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SplitSelfConfig.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ExternalConsent.SPEC, EditionPolicy.configFile());
        BLOCKS.register(bus);
        BLOCK_ENTITIES.register(bus);
        ENTITIES.register(bus);
        bus.addListener((net.minecraftforge.event.entity.EntityAttributeCreationEvent event) ->
                event.put(THE_OTHER.get(), TheOtherEntity.attributes().build()));
        ITEMS.register(bus);
        CREATIVE_TABS.register(bus);
        SOUNDS.register(bus);
        CHUNK_GENERATORS.register(bus);
        STRUCTURES.register(bus);
        STRUCTURE_PIECES.register(bus);
        bus.addListener((net.minecraftforge.event.entity.EntityAttributeCreationEvent event) ->
                event.put(THE_FORGOTTEN.get(), TheForgottenEntity.attributes().build()));
        MinecraftForge.EVENT_BUS.addListener(EncounterSpawns::onTick);
        MinecraftForge.EVENT_BUS.addListener(StoryChat::onChat);
        MemoryNetwork.register();
        StoryNetwork.register();
        MinecraftForge.EVENT_BUS.addListener(StoryScenes::onTick);
        MinecraftForge.EVENT_BUS.addListener(StoryScenes::onClone);
        MinecraftForge.EVENT_BUS.addListener(SplitSelfCommands::register);
        MinecraftForge.EVENT_BUS.addListener(SplitSelfWorldEvents::onAttack);
        MinecraftForge.EVENT_BUS.addListener(SplitSelfWorldEvents::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(SplitSelfWorldEvents::onServerStarted);
        MinecraftForge.EVENT_BUS.addListener(SplitSelfWorldEvents::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(SplitSelfWorldEvents::onServerStopping);
        MinecraftForge.EVENT_BUS.addListener(SleepSystem::onSleepFinished);
        MinecraftForge.EVENT_BUS.addListener(TickScheduler::onTick);
        MinecraftForge.EVENT_BUS.addListener(TickScheduler::onServerStopping);
    }
}
