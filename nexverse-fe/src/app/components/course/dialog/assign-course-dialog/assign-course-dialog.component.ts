import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { EmployeeCourseService } from '../../../../core/services/employee-course.service';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { Course } from '../../../../core/models/course.model';
import { Employee } from '../../../../core/models/employee.model';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { EmployeeService } from '../../../../core/services/employee.service';
import { CourseService } from '../../../../core/services/course.service';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { environment } from '../../../../../environments/environment.development';
import { DurationPipe } from '../../../../shared/pipes/duration.pipe';

@Component({
  selector: 'app-assign-course-dialog',
  imports: [MATERIAL_IMPORTS, ReactiveFormsModule],
  templateUrl: './assign-course-dialog.component.html',
  styleUrl: './assign-course-dialog.component.scss'
})
export class AssignCourseDialogComponent implements OnInit {
  course: Course | undefined;
  searchControl = new FormControl('', {
    nonNullable: true
  });
  thumbnailUrl: string | undefined;
  employees: Employee[] = [];
  departmentName = '';
  departmentId: number | null = null;
  loading = false;
  assigning = false;
  selectedEmployeeIds: Set<number> = new Set();
  page = 1;
  size = 10;
  hasMore = true;
  searchTerm = '';
  constructor(
    private readonly dialogRef: MatDialogRef<AssignCourseDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    private data: Course,
    private readonly employeeService: EmployeeService,
    private readonly courseService: CourseService,
    private employeeCourseService: EmployeeCourseService
  ) { }

  ngOnInit(): void {
    this.course = this.data;
    this.loadEmployees(true);
    this.thumbnailUrl = `${environment.apiUrl}/course/${this.data.id}/thumbnail`
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
        this.searchTerm,
        
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

  toggleEmployee(employee: Employee): void {

    if (this.selectedEmployeeIds.has(employee.id)) {

      this.selectedEmployeeIds.delete(
        employee.id
      );

    } else {

      this.selectedEmployeeIds.add(
        employee.id
      );
    }
  }


  isEmployeeSelected(
    employeeId: number
  ): boolean {

    return this.selectedEmployeeIds.has(
      employeeId
    );
  }

  toggleSelectAll(): void {

    if (this.areAllVisibleEmployeesSelected()) {

      this.employees.forEach(employee => {

        this.selectedEmployeeIds.delete(
          employee.id
        );

      });

    } else {

      this.employees.forEach(employee => {

        this.selectedEmployeeIds.add(
          employee.id
        );

      });
    }
  }


  areAllVisibleEmployeesSelected(): boolean {

    return (
      this.employees.length > 0 &&
      this.employees.every(employee =>
        this.selectedEmployeeIds.has(
          employee.id
        )
      )
    );
  }

  get selectedEmployeeCount(): number {

    return this.selectedEmployeeIds.size;
  }

  assignCourse(): void {

    if (
      this.assigning ||
      this.selectedEmployeeIds.size === 0
    ) {
      return;
    }

    this.assigning = true;

    const employeeIds =
      Array.from(
        this.selectedEmployeeIds
      );

    this.employeeCourseService
      .assignCourseToEmployees({
        courseId: this.data.id,
        employeesId: employeeIds
      })
      .subscribe({
        next: () => {
          this.assigning = false;
          this.dialogRef.close({
            success: true
          });
        },
        error: () => {
          this.assigning = false;
        }
      });
  }

  close(): void {
    this.dialogRef.close();
  }
}
