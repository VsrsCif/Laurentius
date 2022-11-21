package si.vsrs.cif.filing.services;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import gen.si.vsrs.cif.validator.model.PdfAModel;

import java.io.Serializable;
import java.time.OffsetDateTime;

public class PDFAValidationResponse implements Serializable {
    String status;
    OffsetDateTime timestamp;

    PdfAModel data;

    public PDFAValidationResponse() {

    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PdfAModel getData() {
        return data;
    }

    public void setData(PdfAModel data) {
        this.data = data;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
