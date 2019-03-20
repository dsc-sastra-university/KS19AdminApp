package dpi.ks19.admin.pojo;

public class FirestoreData {
    private String feePaid;
    private String id_proof;
    private String remarks;
    private Long checkinTime;

    private int numOfDays;
    private String room;
    private boolean isFeeDue;
    private Long checkoutTime;

    public FirestoreData() {
    }

    public FirestoreData(String feePaid, String id_proof, String remarks, Long checkinTime, int numOfDays, String room, boolean isFeeDue, Long checkoutTime) {
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

    public Long getCheckinTime() {
        return checkinTime;
    }

    public void setCheckinTime(Long checkinTime) {
        this.checkinTime = checkinTime;
    }

    public int getNumOfDays() {
        return numOfDays;
    }

    public void setNumOfDays(int numOfDays) {
        this.numOfDays = numOfDays;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public boolean isFeeDue() {
        return isFeeDue;
    }

    public void setFeeDue(boolean feeDue) {
        isFeeDue = feeDue;
    }

    public Long getCheckoutTime() {
        return checkoutTime;
    }

    public void setCheckoutTime(Long checkoutTime) {
        this.checkoutTime = checkoutTime;
    }
}
