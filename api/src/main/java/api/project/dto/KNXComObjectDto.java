package api.project.dto;

public class KNXComObjectDto {

    private String id;

    private String text;

    private int number;

    private String knxGroupAddress;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(final int number) {
        this.number = number;
    }

    public String getKnxGroupAddress() {
        return knxGroupAddress;
    }

    public void setKnxGroupAddress(final String knxGroupAddress) {
        this.knxGroupAddress = knxGroupAddress;
    }

}
