import { Component, inject } from '@angular/core';
import { EmployeeCourseProgress, EmployeeProgressResponse } from '../../../../core/models/learning-progress.model';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EmployeeService } from '../../../../core/services/employee.service';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { DatePipe, DecimalPipe, TitleCasePipe } from '@angular/common';
import { DurationPipe } from '../../../../shared/pipes/duration.pipe';
import { MatChipsModule } from "@angular/material/chips";
import { environment } from '../../../../../environments/environment.development';
import { ViewEmployeeDialogComponent } from '../../dialog/view-employee-dialog/view-employee-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { MyLearningRequest } from '../../../../core/models/my-learning.model';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { PageEvent } from '@angular/material/paginator';
import { NoDataComponent } from "../../../../shared/components/no-data/no-data.component";

@Component({
  selector: 'app-employee-progress-component',
  imports: [MATERIAL_IMPORTS, DatePipe, DecimalPipe, RouterLink, MatChipsModule, ReactiveFormsModule, NoDataComponent, DurationPipe],
  templateUrl: './employee-progress-component.component.html',
  styleUrl: './employee-progress-component.component.scss'
})
export class EmployeeProgressComponentComponent {
  filters = {
    search: '',
    progressStatus: 'ALL' as "ALL" | "NOT_STARTED" | "IN_PROGRESS" | "COMPLETED",
    requestStatus: 'ALL' as "ALL" | "APPROVED" | "REJECTED" | "PENDING",
    enrollmentType: 'ALL' as "ALL" | "SELF_ENROLLED" | "DEPARTMENT_ASSIGNED" | "SELF_REQUESTED",
  };

  page = 1;
  size = 6;

  totalPages = 0;
  totalElements = 0;

  loading = false;

  searchControl = new FormControl('', {
    nonNullable: true
  })

  progressStatusControl = new FormControl<"ALL" | "NOT_STARTED" | "IN_PROGRESS" | "COMPLETED">('ALL');

  requestStatusControl = new FormControl<"ALL" | 'PENDING' | 'APPROVED' | 'REJECTED'>('ALL');

  enrollmentTypeControl = new FormControl<"ALL" | 'SELF_ENROLLED' | 'DEPARTMENT_ASSIGNED' | 'SELF_REQUESTED'>('ALL');

  profileImageUrl: string | undefined;
  apiUrl = environment.apiUrl;
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly employeeService = inject(EmployeeService);

  employeeId!: number;

  progress: EmployeeProgressResponse | null = null;
  errorMessage = '';

  constructor(private dialog: MatDialog) { }

  ngOnInit(): void {
    this.initializeSearch();
    this.initializeFilters();
    const employeeId = this.route.snapshot.paramMap.get('employeeId');

    if (!employeeId) {
      this.errorMessage = 'Employee could not be identified.';
      return;
    }

    this.employeeId = Number(employeeId);

    if (Number.isNaN(this.employeeId)) {
      this.errorMessage = 'Invalid employee.';
      return;
    }

    this.loadEmployeeProgress();
  }

  private initializeSearch(): void {
    this.searchControl.valueChanges.pipe(debounceTime(500), distinctUntilChanged()).subscribe(value => {
      this.filters.search = value.trim();
      this.page = 1;
      this.loadEmployeeProgress();
    })
  }

  private initializeFilters() {
    this.progressStatusControl.valueChanges.subscribe(progressStatus => {
      this.filters.progressStatus = progressStatus!;
      this.loadEmployeeProgress();
    })
    this.requestStatusControl.valueChanges.subscribe(requestStatus => {
      this.filters.requestStatus = requestStatus!;
      this.loadEmployeeProgress();
    })
    this.enrollmentTypeControl.valueChanges.subscribe(enrollmentType => {
      this.filters.enrollmentType = enrollmentType!;
      this.loadEmployeeProgress();
    })
  }

  private loadEmployeeProgress(): void {
    this.loading = true;
    this.errorMessage = '';

    this.employeeService
      .getEmployeeProgress(
        this.employeeId, 
        this.filters.search,
        this.filters.progressStatus,
        this.filters.requestStatus,
        this.filters.enrollmentType, 
        this.page, 
        this.size)
      .subscribe({
        next: (response) => {
          if (response.data) {
            this.progress = response.data;
          }
          this.totalElements = response.data!.courses.totalElements;
          this.loading = false;
        },
        error: (error) => {
          this.loading = false;

          this.errorMessage =
            error?.error?.message ??
            'Unable to load employee learning progress.';
        }
      });
  }

  onPageChange(event: PageEvent): void {
    this.page = event.pageIndex + 1;
    this.size = event.pageSize;
    this.loadEmployeeProgress();
  }

  get employeeName(): string {
    if (!this.progress) {
      return '';
    }

    return this.progress.employeeName;
  }

  get totalCourses(): number {
    return this.progress?.totalCourses ?? 0;
  }

  get completedCourses(): number {
    return this.progress?.completedCourses ?? 0;
  }

  get inProgressCourses(): number {
    return this.progress?.inProgressCourses ?? 0;
  }

  get notStartedCourses(): number {
    return this.progress?.notStartedCourses ?? 0;
  }

  get overallProgress(): number {
    return this.progress?.overallProgressPercent ?? 0;
  }

  get courses(): EmployeeCourseProgress[] {
    return this.progress?.courses.content ?? [];
  }

  viewCourse(courseId: number): void {
    /*
     * This is only navigation.
     *
     * The backend course endpoint must still verify that
     * the current manager is allowed to access this course.
     */
    this.router.navigate(['/browse-course', courseId]);
  }

  goBack(): void {
    this.router.navigate(['/employees']);
  }

  retry(): void {
    this.loadEmployeeProgress();
  }

  viewEmployee(id: number) {
    this.employeeService.getEmployee(id).subscribe(
      response => {
        this.dialog.open(ViewEmployeeDialogComponent, {
          minWidth: '800px',
          data: response.data,
          panelClass: "dialog-box"
        })
      }
    );
  }
}
