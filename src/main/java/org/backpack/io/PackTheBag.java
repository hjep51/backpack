/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.backpack.io;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

    public PackTheBag(List<String> content, String baseTarget, boolean zipTheBag) {
        this.content = content;
        this.baseTarget = baseTarget;
        this.zipTheBag = zipTheBag;
        this.baggeTag = createBaggeTag();
    }

    public boolean pack() {
        if (content.size() > 0) {
            if (createBackpack()) {
                List<String> items = cleanUpItemList(content);
                List<String> failedItems = new LinkedList<>();

                for (String item : items) {
                    if (!putItemInBag(item)) {
                        failedItems.add(item);
                    }
                }
                createBackpackContentFile(content, failedItems);
                if (zipTheBag) {
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
        if (Files.isDirectory(source)) {
            return false;
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

    private List<String> cleanUpItemList(List<String> items) {
        List<String> listWithoutDuplicates = items.stream().distinct().collect(Collectors.toList());
        List<String> itemList = new LinkedList<>();
        for (String item : listWithoutDuplicates) {
            Path source = Paths.get(item);
            if (Files.isDirectory(source)) {
                try {
                    List<String> thinges = Files.walk(Paths.get(item)).filter(Files::isRegularFile).map((files) -> files.toString()).collect(Collectors.toList());
                    itemList.addAll(thinges);
                } catch (IOException ex) {
                    Logger.getLogger(PackTheBag.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                itemList.add(item);
            }

            itemList = itemList.stream().distinct().filter(e -> !e.toLowerCase().startsWith(backpackPath.toLowerCase())).collect(Collectors.toList());
        }

        return itemList;
    }

    private boolean createBackpackContentFile(List<String> packingList, List<String> failedItems) {
        Charset utf8 = StandardCharsets.UTF_8;
        List<String> list = new LinkedList<>();
        list.add("# Packing list for: " + baggeTag);
        list.add("");
        list.add("## Machine info");
        list.add("");
        list.add("* **OS:** " + System.getProperty("os.name"));
        list.add("* **OS version:** " + System.getProperty("os.version"));
        list.add("* **User:** " + System.getProperty("user.name"));
        
        try {
            InetAddress ip;
            String hostname;
            ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();
            list.add("* **Host:** " + hostname);

        } catch (UnknownHostException e) {
        }
        
        list.add("");
        if (packingList.size() > 0) {
            list.add("## Packing list");
            list.add("");
            for (String item : packingList) {
                list.add("* " + item);
            }
        }
        if (failedItems.size() > 0) {
            list.add("");
            list.add("## Items failed to add to the backpack:");
            list.add("");
            for (String item : failedItems) {
                list.add("* " + item);
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
