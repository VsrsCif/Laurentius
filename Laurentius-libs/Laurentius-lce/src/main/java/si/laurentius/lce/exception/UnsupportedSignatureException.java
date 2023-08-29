package si.laurentius.lce.exception;

public class UnsupportedSignatureException extends Exception {

        private String signatureType;
        public UnsupportedSignatureException(String type) {
            super("Unsupported signature type: " + type);
            this.signatureType = type;
        }

        public UnsupportedSignatureException(String type, Throwable cause) {
            super("Unsupported signature type: " + type, cause);
            this.signatureType = type;
        }
}
