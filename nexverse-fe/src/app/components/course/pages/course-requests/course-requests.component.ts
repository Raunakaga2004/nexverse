import { Component } from '@angular/core';
import { CourseAccessRequestStatus, CourseRequest, DepartmentCourseRequest, EmployeeCourseRequest } from '../../../../core/models/course-request.model';
import { EmployeeCourseService } from '../../../../core/services/employee-course.service';
import { debounceTime, distinctUntilChanged, finalize } from 'rxjs';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { TitleCasePipe } from '@angular/common';
import { NoDataComponent } from "../../../../shared/components/no-data/no-data.component";

@Component({
  selector: 'app-course-requests',
  imports: [MATERIAL_IMPORTS, ReactiveFormsModule, TitleCasePipe, NoDataComponent],
  templateUrl: './course-requests.component.html',
  styleUrl: './course-requests.component.scss'
})
export class CourseRequestsComponent {
  selectedTabIndex = 0;
  search = '';
  requestStatus: CourseAccessRequestStatus | '' = '';
  departmentRequests: DepartmentCourseRequest[] = [];
  departmentTotalElements = 0;
  departmentTotalPages = 0;
  departmentPage = 1;
  employeeRequests: EmployeeCourseRequest[] = [];
  employeeTotalElements = 0;
  employeeTotalPages = 0;
  employeePage = 1;
  isLoading = false;
  isUpdatingStatus = false;
  selectedRequest: EmployeeCourseRequest | null = null;
  readonly pageSize = 10;
  readonly requestStatuses: CourseAccessRequestStatus[] = [
    CourseAccessRequestStatus.PENDING,
    CourseAccessRequestStatus.APPROVED,
    CourseAccessRequestStatus.REJECTED
  ];

  searchControl = new FormControl('', {
    nonNullable: true
  })
  requestStatusControl = new FormControl<CourseAccessRequestStatus>(CourseAccessRequestStatus.PENDING);

  filters: CourseRequest = {
    search: '',
    requestStatus: CourseAccessRequestStatus.PENDING
  };

  constructor(
    private readonly employeeCourseService: EmployeeCourseService
  ) { }
  ngOnInit(): void {
    this.initializeSearch();
    this.initializeFilters();
    this.loadDepartmentRequests();
  }
  private initializeSearch(): void {
    this.searchControl.valueChanges.pipe(debounceTime(500), distinctUntilChanged()).subscribe(value => {
      this.filters.search = value.trim();
      if (this.selectedTabIndex === 0) {
        this.loadDepartmentRequests();
      } else {
        this.loadEmployeeRequests();
      }
    })
  }
  private initializeFilters() {
    this.requestStatusControl.valueChanges.subscribe(status => {
      this.filters.requestStatus = status ?? undefined;
      if (this.selectedTabIndex === 0) {
        this.loadDepartmentRequests();
      } else {
        this.loadEmployeeRequests();
      }
    })
  }
  onTabChange(index: number): void {
    this.selectedTabIndex = index;

    this.resetFilters();

    if (index === 0) {
      this.loadDepartmentRequests();
    } else {
      this.loadEmployeeRequests();
    }
  }

  loadDepartmentRequests(): void {
    this.isLoading = true;
    const request = this.buildRequest();
    this.employeeCourseService
      .getDepartmentCourseRequests(
        request,
        this.departmentPage,
        this.pageSize
      )
      .pipe(
        finalize(() => this.isLoading = false)
      )
      .subscribe({
        next: (response) => {
          if (response.data) {
            this.departmentRequests = response.data.content;
            this.departmentTotalElements = response.data.totalElements;
            this.departmentTotalPages = response.data.totalPages;
          }

        },
        error: (error) => {
          console.error('Failed to load department course requests', error);
          this.departmentRequests = [];
        }
      });
  }

  loadEmployeeRequests(): void {
    this.isLoading = true;
    const request = this.buildRequest();
    this.employeeCourseService
      .getEmployeeCourseRequests(
        request,
        this.employeePage,
        this.pageSize
      )
      .pipe(
        finalize(() => this.isLoading = false)
      )
      .subscribe({
        next: (response) => {
          if (response.data) {
            this.employeeRequests = response.data.content;
            this.employeeTotalElements = response.data.totalElements;
            this.employeeTotalPages = response.data.totalPages;
          }

        },
        error: (error) => {
          console.error('Failed to load employee course requests', error);
          this.employeeRequests = [];
        }
      });
  }

  onSearch(): void {
    this.resetPagination();
    if (this.selectedTabIndex === 0) {
      this.loadDepartmentRequests();
    } else {
      this.loadEmployeeRequests();
    }
  }

  onStatusChange(): void {
    this.resetPagination();
    if (this.selectedTabIndex === 0) {
      this.loadDepartmentRequests();
    } else {
      this.loadEmployeeRequests();
    }
  }

  clearFilters(): void {
    this.search = '';
    this.requestStatus = '';
    this.resetPagination();
    if (this.selectedTabIndex === 0) {
      this.loadDepartmentRequests();
    } else {
      this.loadEmployeeRequests();
    }
  }

  onDepartmentPageChange(page: number): void {
    this.departmentPage = page + 1;
    this.loadDepartmentRequests();
  }

  onEmployeePageChange(page: number): void {
    this.employeePage = page + 1;
    this.loadEmployeeRequests();
  }

  approveDepartmentRequest(request: DepartmentCourseRequest): void {
    this.changeDepartmentRequestStatus(
      request.id,
      CourseAccessRequestStatus.APPROVED
    );
  }

  rejectDepartmentRequest(request: DepartmentCourseRequest): void {
    this.changeDepartmentRequestStatus(
      request.id,
      CourseAccessRequestStatus.REJECTED
    );
  }

  approveEmployeeRequest(request: EmployeeCourseRequest): void {
    this.changeEmployeeRequestStatus(
      request.id,
      CourseAccessRequestStatus.APPROVED
    );
  }

  rejectEmployeeRequest(request: EmployeeCourseRequest): void {
    this.changeEmployeeRequestStatus(
      request.id,
      CourseAccessRequestStatus.REJECTED
    );
  }

  private changeDepartmentRequestStatus(
    requestId: number,
    status: CourseAccessRequestStatus
  ): void {

    this.isUpdatingStatus = true;

    this.employeeCourseService
      .changeDepartmentRequestStatus(requestId, status)
      .pipe(
        finalize(() => this.isUpdatingStatus = false)
      )
      .subscribe({
        next: () => {
          this.loadDepartmentRequests();
        },
        error: (error) => {
          console.error(
            'Failed to change department request status',
            error
          );
        }
      });
  }

  private changeEmployeeRequestStatus(
    requestId: number,
    status: CourseAccessRequestStatus
  ): void {

    this.isUpdatingStatus = true;

    this.employeeCourseService
      .changeEmployeeRequestStatus(requestId, status)
      .pipe(
        finalize(() => this.isUpdatingStatus = false)
      )
      .subscribe({
        next: () => {
          this.loadEmployeeRequests();
        },
        error: (error) => {
          console.error(
            'Failed to change employee request status',
            error
          );
        }
      });
  }

  closeRequestDetails(): void {
    this.selectedRequest = null;
  }

  private buildRequest(): CourseRequest {
    return {
      search: this.filters.search,
      requestStatus: this.filters.requestStatus
    };
  }

  private resetPagination(): void {
    this.departmentPage = 1;
    this.employeePage = 1;
  }

  private resetFilters(): void {
    this.search = '';
    this.requestStatus = '';

    this.departmentPage = 1;
    this.employeePage = 1;
  }

  isPending(status: CourseAccessRequestStatus): boolean {
    return status === CourseAccessRequestStatus.PENDING;
  }

  isApproved(status: CourseAccessRequestStatus): boolean {
    return status === CourseAccessRequestStatus.APPROVED;
  }

  isRejected(status: CourseAccessRequestStatus): boolean {
    return status === CourseAccessRequestStatus.REJECTED;
  }
}
