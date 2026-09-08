package com.pio.nexverse.service.impl;

import com.pio.nexverse.constants.AppConstants;
import com.pio.nexverse.dto.EmployeeImportErrorDTO;
import com.pio.nexverse.dto.EmployeeImportPreviewResponse;
import com.pio.nexverse.dto.EmployeeImportRowDTO;
import com.pio.nexverse.dto.RowValidationResult;
import com.pio.nexverse.entities.organization.Department;
import com.pio.nexverse.enums.ExcelColumn;
import com.pio.nexverse.exception.InvalidExcelFileException;
import com.pio.nexverse.repository.DepartmentRepository;
import com.pio.nexverse.repository.UserRepository;
import com.pio.nexverse.service.CurrentUserService;
import com.pio.nexverse.service.EmployeeImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExcelEmployeeImportServiceImpl implements EmployeeImportService {
    private static final DataFormatter DATA_FORMATTER = new DataFormatter();
    private final CurrentUserService currentUserService;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    @Override
    public EmployeeImportPreviewResponse readEmployees(MultipartFile file) {
        validateFile(file);
        List<EmployeeImportRowDTO> validEmployees = new ArrayList<>();
        List<EmployeeImportErrorDTO> errors = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            validateHeader(sheet.getRow(0));
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isEmpty(row)) {
                    continue;
                }
                RowValidationResult validationResult = new RowValidationResult();
                validateRow(row, validationResult);
                if (validationResult.hasErrors()) {
                    errors.addAll(validationResult.getErrors());
                    continue;
                }
                validEmployees.add(mapRow(row));
            }
            log.info("Successfully parsed {} valid employee records. Invalid rows: {}", validEmployees.size(), errors.stream().map(EmployeeImportErrorDTO::getRowNumber).distinct().count());
            return EmployeeImportPreviewResponse.builder()
                    .totalRows(sheet.getLastRowNum())
                    .validRows(validEmployees.size())
                    .invalidRows((int) errors.stream().map(EmployeeImportErrorDTO::getRowNumber).distinct().count())
                    .errors(errors)
                    .validEmployees(validEmployees)
                    .build();
        } catch (IOException e) {
            throw new InvalidExcelFileException();
        }
    }

    private void validateRow(Row row, RowValidationResult result) {
        validateRequired(row, ExcelColumn.FIRST_NAME, result);
        validateRequired(row, ExcelColumn.EMAIL, result);
        validateEmail(row, result);
        validateUniqueEmail(row, result);
        if (!getCellValue(row, ExcelColumn.PHONE_NUMBER).isEmpty()) {
            validatePhone(row, result);
            validateUniquePhone(row, result);
        }
        if (!getCellValue(row, ExcelColumn.EMPLOYEE_CODE).isEmpty()) {
            validateUniqueEmployeeCode(row, result);
        }
        validateRequired(row, ExcelColumn.DEPARTMENT_NAME, result);
        validateDepartment(row, result);
    }

    private void validateDepartment(Row row, RowValidationResult result) {
        String departmentName = getCellValue(row, ExcelColumn.DEPARTMENT_NAME);
        if (departmentName.isBlank()) {
            return;
        }
        Long organizationId = currentUserService.getCurrentOrganization().getId();
        Optional<Department> department = departmentRepository.findByNameAndOrganizationId(departmentName, organizationId);
        if (department.isEmpty()) {
            result.addError(EmployeeImportErrorDTO.builder()
                    .rowNumber(row.getRowNum() + 1)
                    .columnName(ExcelColumn.DEPARTMENT_NAME.getHeader())
                    .columnIndex(ExcelColumn.DEPARTMENT_NAME.getColumnIndex())
                    .rejectedValue(departmentName)
                    .reason("Department does not exist.")
                    .build());
            return;
        }
        if (!department.get().isEnabled()) {
            result.addError(EmployeeImportErrorDTO.builder()
                    .rowNumber(row.getRowNum() + 1)
                    .columnName(ExcelColumn.DEPARTMENT_NAME.getHeader())
                    .columnIndex(ExcelColumn.DEPARTMENT_NAME.getColumnIndex())
                    .rejectedValue(departmentName)
                    .reason("Department is disabled.")
                    .build());
        }
    }

    private void validateUniqueEmployeeCode(Row row, RowValidationResult result) {
        String employeeCode = getCellValue(row, ExcelColumn.EMPLOYEE_CODE);
        if (employeeCode.isBlank()) {
            return;
        }
        boolean exists = userRepository.existsByEmployeeCodeAndOrganizationId(employeeCode, currentUserService.getCurrentOrganization().getId());
        if (exists) {
            result.addError(EmployeeImportErrorDTO.builder()
                    .rowNumber(row.getRowNum() + 1)
                    .columnName(ExcelColumn.EMPLOYEE_CODE.getHeader())
                    .columnIndex(ExcelColumn.EMPLOYEE_CODE.getColumnIndex())
                    .rejectedValue(employeeCode)
                    .reason("Employee code already exists.")
                    .build());
        }
    }

    private void validateUniqueEmail(Row row, RowValidationResult result) {
        String email = getCellValue(row, ExcelColumn.EMAIL);
        if (email.isBlank()) {
            return;
        }
        if (userRepository.existsByEmail(email)) {
            result.addError(EmployeeImportErrorDTO.builder()
                    .rowNumber(row.getRowNum() + 1)
                    .columnName(ExcelColumn.EMAIL.getHeader())
                    .columnIndex(ExcelColumn.EMAIL.getColumnIndex())
                    .rejectedValue(email)
                    .reason("Email already exists.")
                    .build());
        }
    }

    private void validatePhone(Row row, RowValidationResult result) {
        String phone = getCellValue(row, ExcelColumn.PHONE_NUMBER);
        if (phone.isBlank()) {
            return;
        }
        if (!phone.matches("\\d{10}")) {
            result.addError(EmployeeImportErrorDTO.builder()
                    .rowNumber(row.getRowNum() + 1)
                    .columnName(ExcelColumn.PHONE_NUMBER.getHeader())
                    .columnIndex(ExcelColumn.PHONE_NUMBER.getColumnIndex())
                    .rejectedValue(phone)
                    .reason("Phone number must contain exactly 10 digits.")
                    .build()
            );
        }
    }

    private void validateUniquePhone(Row row, RowValidationResult result) {
        String phone = getCellValue(row, ExcelColumn.PHONE_NUMBER);
        if (phone.isBlank()) {
            return;
        }
        if (userRepository.existsByPhoneNumber(phone)) {
            result.addError(EmployeeImportErrorDTO.builder()
                    .rowNumber(row.getRowNum() + 1)
                    .columnName(ExcelColumn.PHONE_NUMBER.getHeader())
                    .columnIndex(ExcelColumn.PHONE_NUMBER.getColumnIndex())
                    .rejectedValue(phone)
                    .reason("Phone number already exists.")
                    .build());
        }
    }

    private void validateEmail(Row row, RowValidationResult result) {
        String email = getCellValue(row, ExcelColumn.EMAIL);
        if (email.isBlank()) {
            return;
        }
        if (!AppConstants.EMAIL_PATTERN.matcher(email).matches()) {
            result.addError(EmployeeImportErrorDTO.builder()
                    .rowNumber(row.getRowNum() + 1)
                    .columnName(ExcelColumn.EMAIL.getHeader())
                    .columnIndex(ExcelColumn.EMAIL.getColumnIndex())
                    .rejectedValue(email)
                    .reason("Invalid email format.")
                    .build()
            );
        }
    }

    private void validateRequired(Row row, ExcelColumn column, RowValidationResult result) {
        String value = getCellValue(row, column);
        if (value.isBlank()) {
            result.addError(EmployeeImportErrorDTO.builder()
                    .rowNumber(row.getRowNum() + 1)
                    .columnName(ExcelColumn.formIndex(column.getColumnIndex()).getHeader())
                    .columnIndex(column.getColumnIndex())
                    .rejectedValue(value)
                    .reason(column.getHeader() + " is required.")
                    .build()
            );
        }
    }

    private boolean isEmpty(Row row) {
        for (Cell cell : row) {
            if (cell.getCellType() != CellType.BLANK && !getCellValue(row, cell.getColumnIndex()).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private void validateHeader(Row header) {
        if (header == null) {
            throw new InvalidExcelFileException();
        }
        for (ExcelColumn column : ExcelColumn.values()) {
            String actualHeader = getCellValue(header, column).trim();
            if (!column.getHeader().equalsIgnoreCase(actualHeader)) {
                throw new InvalidExcelFileException();
            }
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidExcelFileException();
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".xlsx")) {
            throw new InvalidExcelFileException(); // only .xlsx file is supported
        }
    }

    private EmployeeImportRowDTO mapRow(Row row) {
        return EmployeeImportRowDTO.builder().rowNumber(row.getRowNum() + 1).firstName(getCellValue(row, 0)).lastName((getCellValue(row, 1))).email(getCellValue(row, 2)).phoneNumber(getCellValue(row, 3)).employeeCode(getCellValue(row, 4)).jobTitle(getCellValue(row, 5)).departmentName(getCellValue(row, 6)).build(); // here use enum for xExcel columns instead of adding manually
    }

    private String getCellValue(Row row, ExcelColumn excelColumn) {
        return getCellValue(row, excelColumn.getColumnIndex());
    }

    private String getCellValue(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return "";
        }
        return DATA_FORMATTER.formatCellValue(cell).trim();
    }
}