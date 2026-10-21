package com.pryzmm.splitself.forge;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

final class FrameImageFiles {
    private FrameImageFiles(){}
    static boolean preferPictures(int completed,boolean coin){return completed>=3&&coin;}
    static Set<Path> pictureRoots(){
        Set<Path> roots=new LinkedHashSet<>();Path home=Path.of(System.getProperty("user.home"));
        if(System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("win")){
            try{
                Process p=new ProcessBuilder("powershell.exe","-NoProfile","-NonInteractive","-WindowStyle","Hidden","-Command",
                    "[Console]::OutputEncoding=[Text.Encoding]::UTF8; [Console]::Write([Environment]::GetFolderPath('MyPictures'))")
                    .redirectError(ProcessBuilder.Redirect.DISCARD).start();
                if(p.waitFor(5,TimeUnit.SECONDS)){
                    String value=new String(p.getInputStream().readAllBytes(),StandardCharsets.UTF_8).strip().replace("\uFEFF","");
                    if(p.exitValue()==0&&!value.isBlank())roots.add(Path.of(value));
                }else p.destroyForcibly();
            }catch(Exception ignored){}
            for(String key:List.of("OneDrive","OneDriveConsumer","OneDriveCommercial")){
                String value=System.getenv(key);if(value!=null&&!value.isBlank()){
                    roots.add(Path.of(value,"Pictures"));roots.add(Path.of(value,"Изображения"));
                }
            }
        }else{
            String config=System.getenv("XDG_CONFIG_HOME");
            try{for(String line:Files.readAllLines((config==null?home.resolve(".config"):Path.of(config)).resolve("user-dirs.dirs"),StandardCharsets.UTF_8)){
                if(!line.strip().startsWith("XDG_PICTURES_DIR="))continue;
                String value=line.substring(line.indexOf('=')+1).strip();
                if(value.startsWith("\"")&&value.endsWith("\""))value=value.substring(1,value.length()-1);
                Path folder=Path.of(value.replace("${HOME}",home.toString()).replace("$HOME",home.toString()));
                if(folder.isAbsolute())roots.add(folder);
            }}catch(Exception ignored){}
        }
        roots.add(home.resolve("Pictures"));roots.add(home.resolve("Изображения"));return roots;
    }
    static List<Path> images(Collection<Path> roots,boolean recursive){
        Set<Path> found=new LinkedHashSet<>();int[] visited={0};
        for(Path root:roots){
            if(visited[0]>=50000)break;
            if(!Files.isDirectory(root,LinkOption.NOFOLLOW_LINKS))continue;
            try{Files.walkFileTree(root,EnumSet.noneOf(FileVisitOption.class),recursive?64:1,new SimpleFileVisitor<Path>(){
                @Override public FileVisitResult preVisitDirectory(Path p,BasicFileAttributes a){return ++visited[0]>50000?FileVisitResult.TERMINATE:FileVisitResult.CONTINUE;}
                @Override public FileVisitResult visitFile(Path p,BasicFileAttributes a){
                    if(++visited[0]>50000)return FileVisitResult.TERMINATE;
                    String name=p.getFileName().toString().toLowerCase(Locale.ROOT);
                    if(a.isRegularFile()&&a.size()>0&&a.size()<=32L*1024*1024&&(name.endsWith(".png")||name.endsWith(".jpg")||name.endsWith(".jpeg")))found.add(p.toAbsolutePath().normalize());
                    return FileVisitResult.CONTINUE;
                }
                @Override public FileVisitResult visitFileFailed(Path p,IOException e){return FileVisitResult.CONTINUE;}
            });}catch(IOException|SecurityException ignored){}
        }
        List<Path> files=new ArrayList<>(found);Collections.shuffle(files);return files;
    }
    static BufferedImage decode(Path file)throws IOException{
        if(Files.size(file)>32L*1024*1024)throw new IOException("Image exceeds 32MiB");
        try(var stream=ImageIO.createImageInputStream(file.toFile())){
            if(stream==null)throw new IOException("Unreadable image");
            var readers=ImageIO.getImageReaders(stream);if(!readers.hasNext())throw new IOException("Unsupported image");
            var reader=readers.next();try{
                reader.setInput(stream,true,true);int w=reader.getWidth(0),h=reader.getHeight(0);
                if(w<1||h<1||w>8192||h>8192||(long)w*h>16777216)throw new IOException("Image dimensions exceed limit");
                return reader.read(0);
            }finally{reader.dispose();}
        }
    }
}
