package com.pryzmm.splitself.forge;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Deterministic production policy and catalog. No world simulation, window or GPU. */
public final class SimpleEventsProbe {
    private static int assertions;
    private static void check(boolean ok, String message) {
        assertions++;
        if (!ok) throw new AssertionError(message);
    }
    private static void near(double actual, double expected, String message) {
        check(Math.abs(actual - expected) < 1e-9, message + ": " + actual);
    }
    private static final class Target implements WorldManifestationRules.TntTarget {
        Vec3 position = new Vec3(-20.25, 71.75, 42.125);
        BlockPos bed;
        int sounds, visits;
        boolean loaded = true;
        List<Vec3> spawned = new ArrayList<>();
        List<Integer> fuses = new ArrayList<>();
        public Vec3 position() { return position; }
        public BlockPos respawnInThisDimension() { return bed; }
        public void primedSound() { sounds++; }
        public boolean available(Vec3 pos) { visits++; return loaded && pos.y >= -64 && pos.y < 320; }
        public void spawn(Vec3 pos, int fuse) { spawned.add(pos); fuses.add(fuse); }
    }
    public static void main(String[] args) throws Exception {
        check(!WorldManifestationRules.tnt(List.<Target>of(), t -> {throw new AssertionError();}, 15,
                () -> {throw new AssertionError("empty selection drew random");}), "empty list refuses");
        for (double roll : new double[]{0, .499999, .5, .999999}) {
            Target a = new Target(), b = new Target();
            int[] draws = {0};
            check(WorldManifestationRules.tnt(List.of(a, b), t -> t, 15, () -> {draws[0]++; return roll;}), "TNT completes");
            Target selected = roll < .5 ? a : b, untouched = roll < .5 ? b : a;
            check(draws[0] == 1 && selected.sounds == 1 && untouched.sounds == 0 && untouched.visits == 0,
                    "one full-list selection, only selected world accessed");
            check(selected.spawned.size() == 8 && selected.fuses.stream().allMatch(f -> f == 300), "eight fuse300 entities");
            for (int i = 0; i < 8; i++) {
                Vec3 pos = selected.spawned.get(i);
                near(pos.y, 71.75, "fractional Y preserved");
                near(Math.hypot(pos.x - selected.position.x, pos.z - selected.position.z), 1.5, "radius1.5");
            }
            near(selected.spawned.get(0).x, -18.75, "first entity east");
            near(selected.spawned.get(2).z, 43.625, "third entity south");
        }
        Target shifted = new Target(); shifted.position = new Vec3(.5, 80.25, .5); shifted.bed = new BlockPos(0, 64, 0);
        WorldManifestationRules.tnt(List.of(shifted), t -> t, 15, () -> 0);
        near(shifted.spawned.get(0).x, 18, "center fallback east: radius+1 then ring+1.5");
        near(shifted.spawned.get(0).y, 80.25, "vector helper keeps Y unlike block helper");
        Target boundary = new Target(); boundary.bed = new BlockPos(0, 64, 0); boundary.position = new Vec3(15.5, 70, .5);
        WorldManifestationRules.tnt(List.of(boundary), t -> t, 15, () -> 0);
        near(boundary.spawned.get(0).x, 17, "strict safe-radius boundary");
        Target unloaded = new Target(); unloaded.loaded = false;
        check(WorldManifestationRules.tnt(List.of(unloaded), t -> t, 15, () -> 0)
                && unloaded.sounds == 1 && unloaded.visits == 8 && unloaded.spawned.isEmpty(), "skip unloaded; completed event");
        for (double y : new double[]{-64.01, -64, 319.99, 320}) {
            Target edge = new Target(); edge.position = new Vec3(0,y,0);
            WorldManifestationRules.tnt(List.of(edge), t -> t, 0, () -> 0);
            check(edge.spawned.size() == (y >= -64 && y < 320 ? 8 : 0), "dimension height " + y);
        }
        SimpleEventRules.Scale scale = new SimpleEventRules.Scale();
        List<Integer> steps = new ArrayList<>();
        scale.advance(0, steps::add); check(steps.equals(List.of(0)), "scale first iteration immediate");
        scale.advance(24, steps::add); check(steps.size() == 1, "25ms boundary not early");
        scale.advance(25, steps::add); check(steps.equals(List.of(0,1)), "second step at25ms");
        scale.advance(4999, steps::add); check(steps.size() == 200, "200 steps before final iteration");
        scale.advance(5000, steps::add); check(steps.size() == 201 && steps.get(200) == 200, "inclusive0..200 loop");
        scale.advance(100000, steps::add); check(steps.size() == 201, "no duplicate steps after render/tick updates");
        check(steps.stream().filter(i -> i % 5 == 0).count() == 41, "41 source messages");
        check(SimpleEventRules.SCALE_MS == 5025 && SimpleEventRules.BLACK_MS == 3877 && SimpleEventRules.WHITE_MS == 300,
                "wall-clock durations with requested 300ms white overlay");
        check(SimpleEventRules.POEM_LINES.equals(List.of(1,2,0,3,4,0,5,6,0,7,8,0,0,0,9)), "poem stanza layout");
        check(SimpleEventRules.poemName("DREAM", false, "device", "redacted").equals("Clay"), "alias precedes privacy switch");
        check(SimpleEventRules.poemName("ordinary", false, "private", "redacted").equals("redacted"), "PII off redacts");
        check(SimpleEventRules.poemName("ordinary", true, "device", "redacted").equals("device"), "PII on uses device");
        check(SimpleEventRules.poemName("Lord0wnage", false, "device", "redacted").equals("redacted"), "source trailing-space quirk");
        check(SimpleEventRules.poemName("Lord0wnage ", false, "device", "redacted").equals("Swayle"), "trailing-space alias");
        CommentedConfig config = CommentedConfig.inMemory();
        SplitSelfConfig.SPEC.correct(config); SplitSelfConfig.SPEC.setConfig(config);
        var tnt = EventCatalog.options().get("TNT");
        check(tnt.weight() == 0 && tnt.stage() == 0 && !tnt.client() && !tnt.oneTime(), "TNT source GLOBAL/0/0/repeatable");
        check(PortedEvents.supported().contains("TNT") && !EventCatalog.eligible(3,Set.of(),Map.of(),0).contains("TNT"),
                "manual supported TNT excluded automatically at weight0");
        config.set(List.of("eventWeights", "TNT"), 10); SplitSelfConfig.SPEC.afterReload();
        check(EventCatalog.eligible(0,Set.of(),Map.of(),0).contains("TNT"), "TNT enabled by explicit positive weight");
        check(!EventCatalog.eligible(0,Set.of(),Map.of("TNT",0),0).contains("TNT"), "TNT repeat cooldown");
        check(PortedEvents.supported().size() == 67, "pool67/67");
        for (String event : SimpleEventRules.CLIENT_EVENTS) {
            var option = EventCatalog.options().get(event);
            int expectedStage = event.equals("INVERTCOLOR") ? 2 : Set.of("POEMSCREEN", "SCREENOVERLAY", "PAUSE", "REDSKY", "FACE").contains(event) ? 1 : 0;
            check(PortedEvents.supported().contains(event) && option.client() && option.stage() == expectedStage
                    && option.weight() == (event.equals("FACE") ? 3 : event.equals("POEMSCREEN") ? 5 : 10) && !option.oneTime(), "source defaults " + event);
            check(EventCatalog.eligible(0,Set.of(),Map.of(),0).contains(event) == (expectedStage == 0), "stage0 filter " + event);
            check(EventCatalog.eligible(1,Set.of(),Map.of(),0).contains(event) == (expectedStage <= 1), "client eligible at1 " + event);
        }
        check(SkyEventRules.SKY == 0xAA0000 && SkyEventRules.FOG == 0x880000 && SkyEventRules.DARKNESS_TICKS == 430, "red sky source constants");
        var inversion = new SkyEventRules.Inversion();
        check(!inversion.active(0), "invert initially off"); inversion.start(0);
        check(inversion.active(19999) && !inversion.finish(19999), "20s before boundary");
        check(inversion.finish(20000) && !inversion.active(20000), "20s disables exactly");
        inversion.start(30000); inversion.start(40000);
        check(inversion.finish(50000) && !inversion.active(50001), "earlier overlapping thread disables");
        inversion.start(51000);
        check(inversion.active(59999) && inversion.finish(60000) && !inversion.active(60001), "old later callback disables new event");
        inversion.clear(); check(!inversion.finish(90000) && !inversion.active(90000), "logout clears deadlines");
        near(SkyEventRules.leftEdge(101,0),40.5,"left jitter lower edge");
        near(SkyEventRules.leftEdge(101,.5),50.5,"left jitter center");
        near(SkyEventRules.rightEdge(101,0),30.5,"right jitter lower edge");
        near(SkyEventRules.rightEdge(101,.5),50.5,"right jitter center");
        var face = new FaceRules.Sky();
        check(!face.visible && face.width == 4 && face.height == 6, "face initial state");
        face.toggle();
        check(!face.frame(false, 3) && face.visible && face.width == 4, "no growth while looking away");
        check(!face.frame(true, 2) && !face.visible && face.width == 4, "early stage gaze dismisses");
        face.toggle();
        check(!face.frame(true, 3) && face.visible && Math.abs(face.width - 4.006f) < .000001f, "late stage grows per frame");
        face.frame(false,3);
        check(Math.abs(face.height - 6.009f) < .000001f, "looking away pauses both dimensions");
        int frames = 1;
        while (face.visible && frames < 1100) { frames++; if (face.frame(true,3)) break; }
        check(frames == 1001 && !face.visible && face.width == 4 && face.height == 6, "1001 source float-growth frames trigger capture and reset");
        face.toggle(); face.frame(true,3); face.toggle();
        check(!face.visible && face.width == 4 && face.height == 6, "repeat toggle resets scale");
        double length = Math.sqrt(12600);
        check(FaceRules.looking(10/length,50/length,-100/length), "look toward relative image position");
        check(!FaceRules.looking(-10/length,-50/length,100/length) && !FaceRules.looking(0,0,-1), "wrong direction outside four degrees");
        double inside = Math.toRadians(3.999), outside = Math.toRadians(4.001);
        check(FaceRules.looking(10/length*Math.cos(inside)+50/Math.sqrt(2600)*Math.sin(inside),50/length*Math.cos(inside)-10/Math.sqrt(2600)*Math.sin(inside),-100/length*Math.cos(inside)), "just inside gaze cone");
        check(!FaceRules.looking(10/length*Math.cos(outside)+50/Math.sqrt(2600)*Math.sin(outside),50/length*Math.cos(outside)-10/Math.sqrt(2600)*Math.sin(outside),-100/length*Math.cos(outside)), "just outside gaze cone");
        check(FaceRules.line(2999)==0 && FaceRules.line(3000)==1 && FaceRules.line(7499)==1 && FaceRules.line(7500)==2, "first dialogue boundaries");
        check(FaceRules.line(11999)==2 && FaceRules.line(12000)==3 && FaceRules.line(18149)==3, "last line persists into flash");
        check(!FaceRules.flash(17999) && FaceRules.flash(18000) && FaceRules.flash(18149) && !FaceRules.flash(18150), "150ms flash");
        check(!FaceRules.finished(18149) && FaceRules.finished(18150), "18.15s completion");
        var kick = EventCatalog.options().get("KICK");
        check(kick.weight() == 5 && kick.stage() == 1 && !kick.client() && !kick.oneTime(), "KICK source defaults");
        var permits = new KickRules.Permits();
        var a = java.util.UUID.randomUUID(); var b = java.util.UUID.randomUUID();
        var token = permits.issue(a);
        check(!permits.consume(b, token), "other player rejected");
        check(!permits.consume(a, java.util.UUID.randomUUID()), "forged token rejected");
        check(permits.consume(a, token) && !permits.consume(a, token), "one use only");
        var old = permits.issue(a); var fresh = permits.issue(a);
        check(!permits.consume(a, old) && permits.consume(a, fresh), "new event replaces old permit");
        token = permits.issue(a); permits.forget(a); check(!permits.consume(a, token), "logout clears");
        token = permits.issue(a); permits.clear(); check(!permits.consume(a, token), "server stop clears");
        var erased = new java.util.ArrayList<BlockPos>(); var rolls = new int[1];
        KickRules.erase(new Vec3(0,64,0), pos -> true, pos -> false,
                () -> { rolls[0]++; return true; }, erased::add);
        check(erased.size() == 4096 && rolls[0] == 4096, "16 cubed inclusive volume");
        check(erased.get(0).equals(new BlockPos(-8,56,-8)) && erased.get(4095).equals(new BlockPos(7,71,7)), "source bounds");
        check(erased.get(1).equals(new BlockPos(-8,56,-7)) && erased.get(16).equals(new BlockPos(-7,56,-8)) && erased.get(256).equals(new BlockPos(-8,57,-8)), "Y X Z order");
        erased.clear(); rolls[0]=0;
        KickRules.erase(new Vec3(-0.5,64.5,-0.5), pos -> pos.getY()==67, pos -> pos.getX()==-8,
                () -> { rolls[0]++; return rolls[0]%2==0; }, erased::add);
        check(rolls[0]==210 && erased.size()==105, "truncation, availability, air short circuit, random chance");
        erased.clear(); KickRules.erase(Vec3.ZERO, pos -> false, pos -> { throw new AssertionError(); },
                () -> { throw new AssertionError(); }, erased::add);
        check(erased.isEmpty(), "unavailable blocks never accessed");
        Path root = Path.of(args[0]);
        check(PortedEvents.supported().size() == 67, "all event handlers present");
        for (String texture : List.of("overlay", "overlay_white", "player", "dirt_background")) {
            try (var stream = SimpleEventsProbe.class.getResourceAsStream("/assets/splitself/textures/screen/"+texture+".png")) {
                check(stream != null, "packaged texture " + texture);
                var image = javax.imageio.ImageIO.read(stream);
                check(image != null && image.getWidth() > 0 && image.getHeight() > 0, "decodable texture " + texture);
            }
        }
        for (String lang : List.of("en_us", "ru_ru")) {
            var text = JsonParser.parseString(Files.readString(root.resolve("src/main/resources/assets/splitself/lang/"+lang+".json"))).getAsJsonObject();
            for (int i=1;i<=9;i++) check(text.has("events.splitself.poemScreen.line"+i), "poem localization " + lang + i);
            for (int i=1;i<=3;i++) check(text.has("events.splitself.face.line"+i), "face localization " + lang + i);
            for (String key : List.of("scale.message", "rename", "screenOverlay.message"))
                check(text.has("events.splitself."+key), "localized effect " + lang + key);
        }
        System.out.println("PASS SimpleEventsProbe: " + assertions + " assertions; no gameplay, GPU or packet-delivery evidence.");
    }
}
