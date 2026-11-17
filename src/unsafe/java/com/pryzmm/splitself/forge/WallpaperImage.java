package com.pryzmm.splitself.forge;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;

final class WallpaperImage {
    private WallpaperImage() {}
    static void writeBitmap(Path source,Path destination)throws IOException{
        BufferedImage image=ImageIO.read(source.toFile());
        if(image==null)throw new IOException("Unsupported wallpaper image: "+source);
        BufferedImage bitmap=new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_RGB);
        var graphics=bitmap.createGraphics();
        try{graphics.drawImage(image,0,0,null);}finally{graphics.dispose();}
        if(!ImageIO.write(bitmap,"bmp",destination.toFile()))throw new IOException("BMP writer unavailable");
    }
}
