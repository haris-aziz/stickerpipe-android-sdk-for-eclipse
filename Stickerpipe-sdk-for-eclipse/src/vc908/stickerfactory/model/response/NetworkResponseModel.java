package vc908.stickerfactory.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Network response POJO model
 *
 * @author Dmitry Nezhydenko
 */
public class NetworkResponseModel<T> {
    public enum Status {
        @SerializedName("error")
        ERROR,
        @SerializedName("success")
        SUCCESS
    }

    @Expose
    private String message;
    @Expose
    private Status status;
    @Expose
    private T data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "NetworkResponseModel{" +
                "message='" + message + '\'' +
                ", status=" + status +
                ", data=" + data +
                '}';
    }
}
