package org.infotoast.petcontrol.exception;

public class EntityNotCatOrDogException extends RuntimeException {
    public EntityNotCatOrDogException(String message) {
        super(message);
    }
}
