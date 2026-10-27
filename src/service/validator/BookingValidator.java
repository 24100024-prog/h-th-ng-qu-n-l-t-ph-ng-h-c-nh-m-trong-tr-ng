package service.validator;

import utils.exceptions.BookingException;

public interface BookingValidator {
    void validate(BookingRequest request) throws BookingException;
}
