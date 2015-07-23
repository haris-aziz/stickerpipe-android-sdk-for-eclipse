package vc908.stickerfactory.utils;

import retrofit.mime.TypedString;

/**
 * Class represent json string for sending requests
 *
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class TypedJsonString extends TypedString {
    public TypedJsonString(String body) {
        super(body);
    }

    @Override
    public String mimeType() {
        return "application/json";
    }
}
