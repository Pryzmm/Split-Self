package com.pryzmm.splitself.forge;
public final class WallpaperProbe {
    private static void check(boolean condition,String text){if(!condition)throw new AssertionError(text);}
    public static void main(String[] args) throws Exception {
        var directory=java.nio.file.Files.createTempDirectory("splitself-wallpaper-probe-");
        try{
            var source=directory.resolve("wallpaper ' тест.png");var target=directory.resolve("wallpaper.bmp");
            var image=new java.awt.image.BufferedImage(2,2,java.awt.image.BufferedImage.TYPE_INT_ARGB);
            image.setRGB(0,0,0xffff3300);javax.imageio.ImageIO.write(image,"png",source.toFile());
            WallpaperImage.writeBitmap(source,target);
            var bitmap=javax.imageio.ImageIO.read(target.toFile());
            check(bitmap.getWidth()==2&&bitmap.getHeight()==2&&bitmap.getRGB(0,0)==0xffff3300,"ARGB PNG converts to readable RGB BMP with pixels preserved");
        }finally{try(var files=java.nio.file.Files.list(directory)){for(var file:files.toList())java.nio.file.Files.delete(file);}java.nio.file.Files.delete(directory);}
        System.out.println("WallpaperProbe PASS: PNG to BMP fixture only, no wallpaper change");
    }
}
