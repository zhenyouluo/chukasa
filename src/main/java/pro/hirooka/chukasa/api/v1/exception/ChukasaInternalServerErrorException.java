package pro.hirooka.chukasa.api.v1.exception;

public class ChukasaInternalServerErrorException extends Exception {
    public ChukasaInternalServerErrorException(String message){
        super(message);
    }
}
