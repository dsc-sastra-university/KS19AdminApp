package dpi.ks19.admin.pojo;

public class FirestoreData {
    private String feePaid;
    private String id_proof;
    private String remarks;
    private String checkinTime;

    private Integer numOfDays;
    private String room;
    private Boolean isFeeDue;
    private String checkoutTime;

    public FirestoreData() {
    }

    public FirestoreData(String feePaid, String id_proof, String remarks, String checkinTime, Integer numOfDays, String room, Boolean isFeeDue, String checkoutTime) {
        this.feePaid = feePaid;
        this.id_proof = id_proof;
        this.remarks = remarks;
        this.checkinTime = checkinTime;
        this.numOfDays = numOfDays;
        this.room = room;
        this.isFeeDue = isFeeDue;
        this.checkoutTime = checkoutTime;
    }

    public String getFeePaid() {
        return feePaid;
    }

    public void setFeePaid(String feePaid) {
        this.feePaid = feePaid;
    }

    public String getId_proof() {
        return id_proof;
    }

    public void setId_proof(String id_proof) {
        this.id_proof = id_proof;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getCheckinTime() {
        return checkinTime;
    }

    public void setCheckinTime(String checkinTime) {
        this.checkinTime = checkinTime;
    }

    public Integer getNumOfDays() {
        return numOfDays;
    }

    public void setNumOfDays(Integer numOfDays) {
        this.numOfDays = numOfDays;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Boolean isFeeDue() {
        return isFeeDue;
    }

    public void setFeeDue(Boolean feeDue) {
        isFeeDue = feeDue;
    }

    public String getCheckoutTime() {
        return checkoutTime;
    }

    public void setCheckoutTime(String checkoutTime) {
        this.checkoutTime = checkoutTime;
    }
}
