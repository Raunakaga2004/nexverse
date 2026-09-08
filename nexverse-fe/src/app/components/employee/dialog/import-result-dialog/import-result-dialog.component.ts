import { Component, Inject } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { TitleCasePipe } from '@angular/common';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { EmployeeImportError, ImportEmployeeResponse } from '../../../../core/models/import-employee-response.model';

@Component({
  selector: 'app-import-result-dialog',
  imports: [MATERIAL_IMPORTS, TitleCasePipe],
  templateUrl: './import-result-dialog.component.html',
  styleUrl: './import-result-dialog.component.scss'
})
export class ImportResultDialogComponent {
  readonly displayedColumns = [
    'rowNumber',
    'columnName',
    'rejectedValue',
    'reason'
  ];

  readonly dataSource: MatTableDataSource<EmployeeImportError>;

  showErrors = false;

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: ImportEmployeeResponse
  ) {
    this.dataSource = new MatTableDataSource(data.errors);
  }

  get isSuccess(): boolean {
    return this.data.failedImports == 0;
  }

  toggleErrors(): void {
    this.showErrors = !this.showErrors;
  }
}
