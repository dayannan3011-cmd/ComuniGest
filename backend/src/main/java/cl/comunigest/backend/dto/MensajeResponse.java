package cl.comunigest.backend.dto;

public class MensajeResponse {
    private final String message;

    public MensajeResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
