package com.example.qash;

public class MpesaMessage {

    private String messageBody;
    private long date;
    private boolean isImported;

    public MpesaMessage(String messageBody, long date) {
        this.messageBody = messageBody;
        this.date = date;
        this.isImported = false;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public boolean isImported() {
        return isImported;
    }

    public void setImported(boolean imported) {
        isImported = imported;
    }
}
