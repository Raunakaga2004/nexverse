import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { DatePipe } from '@angular/common';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';

@Component({
  selector: 'app-department-view-dialog',
  imports: [MATERIAL_IMPORTS, DatePipe],
  templateUrl: './department-view-dialog.component.html',
  styleUrl: './department-view-dialog.component.scss'
})
export class DepartmentViewDialogComponent {
  constructor(
    private readonly dialogRef: MatDialogRef<DepartmentViewDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public readonly department: {
      name: string;
      managerName?: string;
      employeeCount: number;
      createdAt: string;
      updatedAt: string;
    }
  ) {}
 
  close(): void {
    this.dialogRef.close();
  }
}
