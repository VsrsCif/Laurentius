package si.laurentius.plugin.interfaces;

import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.mail.MSHOutMail;

import javax.ejb.Local;
import java.util.List;

@Local
public interface MailValidator {
    class CheckResult {
        private String message;
        private String description;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
    class ValidationReport {
        private boolean critical;
        private List<CheckResult> checkResults;

        public boolean isCritical() {
            return critical;
        }

        public void setCritical(boolean critical) {
            this.critical = critical;
        }

        public List<CheckResult> getCheckResults() {
            return checkResults;
        }

        public void setCheckResults(List<CheckResult> checkResults) {
            this.checkResults = checkResults;
        }
    }
    ValidationReport validateInMail(MSHInMail inMail);
    ValidationReport validateOutMail(MSHOutMail outMail);
}
