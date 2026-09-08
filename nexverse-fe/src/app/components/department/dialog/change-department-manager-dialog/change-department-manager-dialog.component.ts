import { Component, Inject, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { Employee } from '../../../../core/models/employee.model';
import { EmployeeService } from '../../../../core/services/employee.service';


@Component({
  selector: 'app-change-department-manager-dialog',
  imports: [MATERIAL_IMPORTS, ReactiveFormsModule],
  templateUrl: './change-department-manager-dialog.component.html',
  styleUrl: './change-department-manager-dialog.component.scss'
})
export class ChangeDepartmentManagerDialogComponent implements OnInit {
  searchControl = new FormControl('', {
    nonNullable: true
  });
  employees: Employee[] = [];
  departmentName = '';
  departmentId: number | null = null;
  loading = false;
  assigning = false;
  selectedEmployeeId: number | null = null;
  page = 1;
  size = 10;
  hasMore = true;
  searchTerm = '';

  constructor(
    private dialogRef: MatDialogRef<ChangeDepartmentManagerDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: any,
    private employeeService: EmployeeService
  ) { }

  ngOnInit(): void {
    this.departmentName = this.data.departmentName;
    this.departmentId = this.data.departmentId;
    this.loadEmployees(true);
    this.searchControl.valueChanges
      .pipe(
        debounceTime(500), distinctUntilChanged()
      )
      .subscribe(value => {
        this.searchTerm = value.trim();
        this.page = 1;
        this.hasMore = true;
        this.employees = [];
        this.loadEmployees(true);
      });
  }

  loadEmployees(reset = false): void {
    if (this.loading || !this.hasMore) {
      return;
    }
    this.loading = true;
    this.employeeService
      .getEligibleDepartmentManagers(
        this.page,
        this.departmentName,
        this.searchTerm
      )
      .subscribe({
        next: response => {
          const content = response.data?.content ?? [];
          if (reset) {
            this.employees = content;
          } else {
            this.employees = [
              ...this.employees,
              ...content
            ];
          }
          this.hasMore = !response.data?.last;
          if (content.length > 0) {
            this.page++;
          }
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
  }

  onScroll(event: Event): void {
    const element = event.target as HTMLElement;
    const scrollPosition =
      element.scrollTop + element.clientHeight;
    const scrollThreshold =
      element.scrollHeight - 30;
    if (scrollPosition >= scrollThreshold) {
      this.loadEmployees();
    }
  }

  selectEmployee(employeeId: number): void {
    this.selectedEmployeeId = employeeId;
  }

  close(): void {
    this.dialogRef.close();
  }

  assignManager() {
    this.dialogRef.close({
      employeeId: this.selectedEmployeeId
    });
  }
}