package si.vsrs.cif.filing;

public class CaseNumber {
    String registerCode;
    String number;
    String year;

    public CaseNumber(String registerCode) {
        this.registerCode = registerCode;
        this.number = null;
        this.year = null;
    }

    public CaseNumber(String registerCode, String number, String year) {
        this.registerCode = registerCode;
        this.number = number;
        this.year = year;
    }

    public String getRegisterCode() {
        return registerCode;
    }

    public void setRegisterCode(String registerCode) {
        this.registerCode = registerCode;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getYear() {
        return year != null && year.length() == 2 ? "20" + year : year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}