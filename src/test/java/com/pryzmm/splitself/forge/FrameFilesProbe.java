package com.pryzmm.splitself.forge;

import java.nio.file.Files;
import java.nio.file.Path;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public final class FrameFilesProbe {
    public static void main(String[] args) throws Exception {
        Path root=Files.createTempDirectory("splitself-frame-fixture-");
        try {
            Path legacy=Files.createDirectory(root.resolve("legacy"));
            Path game=Files.createDirectory(root.resolve("game"));
            Path a=legacy.resolve("a.png"), b=game.resolve("b.png");
            ImageIO.write(new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB),"png",a.toFile());
            ImageIO.write(new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB),"png",b.toFile());
            if(!FrameImageFiles.screenshots(legacy,game,true).equals(java.util.List.of(a)))
                throw new AssertionError("Original legacy screenshot priority");
            if(!FrameImageFiles.screenshots(legacy,game,false).equals(java.util.List.of(b)))
                throw new AssertionError("Own game screenshots remain available without desktop consent");
            Files.delete(a);
            if(!FrameImageFiles.screenshots(legacy,game,true).equals(java.util.List.of(b)))
                throw new AssertionError("Fallback to game directory");
            if(FrameImageFiles.decode(b).getWidth()!=2)throw new AssertionError("Fixture decode");
            Files.delete(b);
            if(!FrameImageFiles.screenshots(null,game,false).isEmpty())
                throw new AssertionError("Empty screenshots must request entity capture");
            System.out.println("FrameFilesProbe PASS: source priority, consent, fallback, decode and empty directories; no GPU/game execution");
        } finally {
            try(var paths=Files.walk(root)) {
                for(Path path:paths.sorted(java.util.Comparator.reverseOrder()).toList())Files.deleteIfExists(path);
            }
        }
    }
}
