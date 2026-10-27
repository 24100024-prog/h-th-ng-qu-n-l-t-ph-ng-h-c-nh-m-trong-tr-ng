package model;

public class Student extends User {
    private String lop;
    private String email;

    public Student() { super(); }

    public Student(String id, String name, String phone, String lop, String email) {
        super(id, name, phone);
        this.lop = lop;
        this.email = email;
    }

    public String getLop() { return lop; }
    public void setLop(String lop) { this.lop = lop; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public void hienThiThongTin() {
        System.out.println("SV: " + getId() + " | " + getName() + " | SDT: " + getPhone()
                + " | Lop: " + lop + " | Email: " + email);
    }
}
