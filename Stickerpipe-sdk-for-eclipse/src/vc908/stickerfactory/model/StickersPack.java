package vc908.stickerfactory.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Stickers pack POJO model
 *
 * @author Dmitry Nezhydenko
 */
public class StickersPack {

    public enum Type {
        PAID("paid"),
        FREE("free");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Expose
    private float price;

    @Expose
    private String title;

    @Expose
    private String artist;

    @Expose
    @SerializedName("pack_name")
    String name;
    @Expose
    List<Sticker> stickers;

    public String getName() {
        return name;
    }

    public List<Sticker> getStickers() {
        return stickers;
    }

    public Type getType() {
        return price > 0 ? Type.PAID : Type.FREE;
    }

    public float getPrice() {
        return price;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }
}
