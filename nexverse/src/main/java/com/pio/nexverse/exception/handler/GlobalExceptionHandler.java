package com.pio.nexverse.exception.handler;

import com.pio.nexverse.constants.ExceptionMessages;
import com.pio.nexverse.dto.ErrorResponseDTO;
import com.pio.nexverse.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.pio.nexverse.constants.ErrorResponseCode.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({UserNotFoundException.class, DepartmentNotFoundException.class, OrganizationNotFoundException.class, CourseNotFoundException.class, ContentNotFoundException.class, CourseModuleNotFoundException.class, CourseRequestNotFoundException.class, CourseThumbnailNotFoundException.class, SkillIconNotFoundException.class, SkillNotFoundException.class})
    public ResponseEntity<ErrorResponseDTO> handleNotFoundException(Exception exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponseDTO.of(RESOURCE_NOT_FOUND, exception.getMessage()));
    }

    @ExceptionHandler(UserAccountDisabledException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserAccountDisabledException(UserAccountDisabledException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponseDTO.of(USER_ACCOUNT_DISABLED, exception.getMessage()));
    }

    @ExceptionHandler(OrganizationDisabledException.class)
    public ResponseEntity<ErrorResponseDTO> handleOrganizationDisabledException(OrganizationDisabledException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponseDTO.of(ORGANIZATION_DISABLED, exception.getMessage()));
    }

    @ExceptionHandler(DepartmentDisableException.class)
    public ResponseEntity<ErrorResponseDTO> handleDepartmentDisableException(DepartmentDisableException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponseDTO.of(DEPARTMENT_DISABLED, exception.getMessage()));
    }

    @ExceptionHandler({OrganizationSuspendedException.class, UserSuspendedException.class})
    public ResponseEntity<ErrorResponseDTO> handleSuspendedException(OrganizationSuspendedException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponseDTO.of(SUSPENDED, exception.getMessage()));
    }

    @ExceptionHandler(CourseNotAccessibleException.class)
    public ResponseEntity<ErrorResponseDTO> handleCourseNotAccessibleException(CourseNotAccessibleException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponseDTO.of(COURSE_NOT_ACCESSIBLE, exception.getMessage()));
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceAlreadyExistsException(ResourceAlreadyExistsException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponseDTO.of(RESOURCE_ALREADY_EXISTS, exception.getMessage()));
    }

    @ExceptionHandler(CourseAlreadyAccessibleException.class)
    public ResponseEntity<ErrorResponseDTO> handleCourseAlreadyAccessibleException(CourseAlreadyAccessibleException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponseDTO.of(COURSE_ALREADY_ACCESSIBLE, exception.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponseDTO.of(ACCESS_DENIED, exception.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidCredentialsException(InvalidCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponseDTO.of(INVALID_CREDENTIALS, exception.getMessage()));
    }

    @ExceptionHandler(InvalidSetPasswordTokenException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidSetPasswordTokenException(InvalidSetPasswordTokenException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponseDTO.of(INVALID_TOKEN, exception.getMessage()));
    }

    @ExceptionHandler(PasswordReuseException.class)
    public ResponseEntity<ErrorResponseDTO> handlePasswordReuseException(PasswordReuseException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponseDTO.of(PASSWORD_ALREADY_USED, exception.getMessage()));
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    public ResponseEntity<ErrorResponseDTO> handleIncorrectPasswordException(IncorrectPasswordException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponseDTO.of(INCORRECT_PASSWORD, exception.getMessage()));
    }

    @ExceptionHandler({AlreadyPublishedCourseException.class, CourseNotInDraftException.class, ArchivedCourseException.class, DraftCourseException.class})
    public ResponseEntity<ErrorResponseDTO> handleInvalidCourseStatusException(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponseDTO.of(INVALID_COURSE_STATUS, exception.getMessage()));
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissingRequestCookieException(MissingRequestCookieException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponseDTO.of(MISSING_REQUEST_COOKIE, exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String errorMessage = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse(null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponseDTO.of(VALIDATION_FAILED, errorMessage));
    }

    @ExceptionHandler(EmailDeliveryException.class)
    public ResponseEntity<ErrorResponseDTO> handleEmailDeliveryException(EmailDeliveryException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponseDTO.of(EMAIL_DELIVERY_FAILED, exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception exception) {
        log.error("Unhandled exception occurred", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponseDTO.of(INTERNAL_SERVER_ERROR, ExceptionMessages.SERVER_ERROR));
    }

    @ExceptionHandler(FileSizeLimitExceededException.class)
    public ResponseEntity<ErrorResponseDTO> handleFileSizeLimitExceededException(FileSizeLimitExceededException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponseDTO.of(FILE_SIZE_LIMIT_EXCEEDED, exception.getMessage()));
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponseDTO> handleFileStorageException(FileStorageException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponseDTO.of(FAILED_STORING_FILE, exception.getMessage()));
    }

    @ExceptionHandler({InvalidContentOrderException.class, InvalidModuleOrderException.class})
    public ResponseEntity<ErrorResponseDTO> handleInvalidOrderException(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponseDTO.of(INVALID_ORDER, exception.getMessage()));
    }

    @ExceptionHandler(InvalidCoursePublishException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidCoursePublishException(InvalidCoursePublishException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponseDTO.of(COURSE_PUBLISH_ERROR, exception.getMessage()));
    }

    @ExceptionHandler(InvalidExcelFileException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidExcelFileException(InvalidExcelFileException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponseDTO.of(INVALID_EXCEL_FILE, exception.getMessage()));
    }

    @ExceptionHandler(InvalidModuleContentException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidModuleContentException(InvalidModuleContentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponseDTO.of(INVALID_CONTENT, exception.getMessage()));
    }

    @ExceptionHandler({OrganizationPendingException.class, UserPendingException.class})
    public ResponseEntity<ErrorResponseDTO> handlePendingException(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponseDTO.of(PENDING, exception.getMessage()));
    }
}