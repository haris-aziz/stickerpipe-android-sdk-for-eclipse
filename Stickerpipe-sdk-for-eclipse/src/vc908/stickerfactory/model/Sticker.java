package vc908.stickerfactory.model;

import com.google.gson.annotations.Expose;

/**
 * Stickers POJO model
 *
 * @author Dmitry Nezhydenko
 */
public class Sticker {
    @Expose
    private String pack;
    @Expose
    private String name;

    public String getName() {
        return name;
    }

    public String getPack() {
        return pack;
    }
}
