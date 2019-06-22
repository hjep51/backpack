/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.backpack.io;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hjep
 */
public class PackTheBag {

    private List<String> content;
    private String baseTarget;
    private boolean zipTheBag;
    private String baggeTag;
    private String backpackPath;
    private List<String> sucssesItems = new LinkedList<>();
    private List<String> failedItems = new LinkedList<>();

    public PackTheBag(List<String> content, String baseTarget, boolean zipTheBag) {
        this.content = content;
        this.baseTarget = baseTarget;
        this.zipTheBag = zipTheBag;
        this.baggeTag = createBaggeTag();
    }

    public boolean pack() {
        System.out.println("Got " + content.size() + " items in bag");
        if (content.size() > 0) {
            if (createBackpack()) {
                for (String item : content) {
                    if (putItemInBag(item)) {
                        sucssesItems.add(item);
                    } else {
                        failedItems.add(item);
                    }
                }
                createBackpackContentFile();
                if(zipTheBag) {
                    Zipper zipper = new Zipper();
                    zipper.compressDirectory(backpackPath, backpackPath + ".zip");
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private String createBaggeTag() {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        return "Backpack-" + timeStamp;
    }

    private boolean createBackpack() {
        Path path = Path.of(baseTarget);
        if (Files.exists(path)) {
            backpackPath = baseTarget.endsWith(File.separator) ? baseTarget + baggeTag : baseTarget + File.separator + baggeTag;
            while (backpackPath.endsWith(File.separator)) {
                backpackPath = backpackPath.substring(0, backpackPath.length() - 1);
            }
            return new File(backpackPath).mkdirs();
        } else {
            return false;
        }
    }

    private boolean putItemInBag(String item) {
        Path source = Paths.get(item);
        
        if(Files.isDirectory(source)) {
            try {
                Files.walk(Paths.get(item))
                        .filter(Files::isRegularFile)
                        .forEach(e -> putItemInBag(e.toString()));
                return true;
            } catch (IOException ex) {
                Logger.getLogger(PackTheBag.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        String targetPath = item.startsWith(File.separator) ? backpackPath + item : backpackPath + File.separator + item;
        Path target = Paths.get(targetPath);
        try {
            if (Files.notExists(target)) {
                Files.createDirectories(target);
            }
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(PackTheBag.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    private boolean createBackpackContentFile() {
        Charset utf8 = StandardCharsets.UTF_8;
        List<String> list = new LinkedList<>();
        list.add("# Packing list for: " + baggeTag);
        list.add("");
        if(sucssesItems.size() > 0) {
            list.add("This items is in the backpack:");
            list.add("");
            for(String item: sucssesItems) {
                list .add("* " + item);
            }
        }
        if(failedItems.size() > 0) {
            list.add("");
            list.add("This items failed to get in the backpack:");
            list.add("");
            for(String item: failedItems) {
                list .add("* " + item);
            }
        }
        
        Path contentFile = Paths.get(backpackPath + "/packinglist.md");
        try {
            Files.write(contentFile, list, utf8);
        } catch (IOException ex) {
            Logger.getLogger(PackTheBag.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        return true;
    }
}
