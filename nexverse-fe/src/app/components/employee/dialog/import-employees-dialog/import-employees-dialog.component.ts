import { Component } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ImportResultDialogComponent } from '../import-result-dialog/import-result-dialog.component';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { FileUploadComponent } from '../../../../shared/components/file-upload/file-upload.component';
import { EmployeeService } from '../../../../core/services/employee.service';

@Component({
  selector: 'app-import-employees-dialog',
  imports: [MATERIAL_IMPORTS, FileUploadComponent],
  templateUrl: './import-employees-dialog.component.html',
  styleUrl: './import-employees-dialog.component.scss'
})
export class ImportEmployeesDialogComponent {
  selectedFile: File | null = null;

  loading = false;

  constructor(
    private readonly dialogRef: MatDialogRef<ImportEmployeesDialogComponent>, private employeeService: EmployeeService, private dialog: MatDialog
  ) { }

  onFileSelected(file: File | null): void {
    this.selectedFile = file;
  }

  importEmployees(): void {
    if (!this.selectedFile) {
      return;
    }
    this.loading = true;
    this.employeeService.importEmployees(this.selectedFile)
      .subscribe({
        next: (response) => {
          console.log(response)
          this.loading = false;
          this.dialogRef.close(true);
          this.dialog.open(ImportResultDialogComponent, {
            minWidth: '700px',
            minHeight: '450px',
            disableClose: true,
            data: response.data,
            panelClass: "dialog-box"
          })
        },
        error: () => {
          this.loading = false;
        }
      });

  }

  downloadTemplate(): void {
    this.employeeService.downloadTemplate().subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'employee_import_template.xlsx';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    });
  }

  close(): void {
    this.dialogRef.close(false);
  }
}