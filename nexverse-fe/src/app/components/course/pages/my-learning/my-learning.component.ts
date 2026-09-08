import { Component } from '@angular/core';
import { MyLearningCourse, MyLearningRequest } from '../../../../core/models/my-learning.model';
import { EmployeeCourseService } from '../../../../core/services/employee-course.service';
import { Router } from '@angular/router';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { PageEvent } from '@angular/material/paginator';
import { environment } from '../../../../../environments/environment.development';
import { DecimalPipe } from '@angular/common';
import { NoDataComponent } from "../../../../shared/components/no-data/no-data.component";

@Component({
  selector: 'app-my-learning',
  imports: [MATERIAL_IMPORTS, FormsModule, ReactiveFormsModule, DecimalPipe, NoDataComponent],
  templateUrl: './my-learning.component.html',
  styleUrl: './my-learning.component.scss'
})
export class MyLearningComponent {
  apiUrl = environment.apiUrl;
  courses: MyLearningCourse[] = [];

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

  constructor(
    private employeeCourseService: EmployeeCourseService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.initializeSearch();
    this.initializeFilters();
    this.loadCourses();
  }

  private initializeSearch(): void {
    this.searchControl.valueChanges.pipe(debounceTime(500), distinctUntilChanged()).subscribe(value => {
      this.filters.search = value.trim();
      this.page = 1;
      this.loadCourses();
    })
  }

  private initializeFilters() {
    this.progressStatusControl.valueChanges.subscribe(progressStatus => {
      this.filters.progressStatus = progressStatus!;
      this.loadCourses();
    })
    this.requestStatusControl.valueChanges.subscribe(requestStatus => {
      this.filters.requestStatus = requestStatus!;
      this.loadCourses();
    })
    this.enrollmentTypeControl.valueChanges.subscribe(enrollmentType => {
      this.filters.enrollmentType = enrollmentType!;
      this.loadCourses();
    })
  }

  loadCourses(): void {
    this.loading = true;
    this.employeeCourseService
      .getMyLearningCourses(
        this.filters.search,
        this.filters.progressStatus,
        this.filters.requestStatus,
        this.filters.enrollmentType,
        this.page,
        this.size
      )
      .subscribe({
        next: response => {
          this.courses = response.data!.content;
          this.totalElements = response.data!.totalElements;
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
  }

  continueLearning(course: MyLearningCourse): void {
    this.router.navigate([
      '/learning/',
      course.courseId
    ]);

  }

  onPageChange(event: PageEvent): void {
    this.page = event.pageIndex + 1;
    this.size = event.pageSize;
    this.loadCourses();
  }

  requestAccess(course: MyLearningCourse): void {

    // Optional if you allow re-request
  }

  // getProgressColor(progress: number): string {

  //   if (progress === 100) {
  //     return '#2e7d32';
  //   }

  //   if (progress > 0) {
  //     return '#f9a825';
  //   }

  //   return '#9e9e9e';

  // }
}
