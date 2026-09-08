import { Component } from '@angular/core';
import { BrowseCourse, BrowseCoursesRequest } from '../../../../core/models/browse-course.model';
import { CourseService } from '../../../../core/services/course.service';
import { PageResponse } from '../../../../core/models/page-response.model';
import { ApiResponse } from '../../../../core/models/api-response.model';
import { PageEvent } from '@angular/material/paginator';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatChipsModule } from "@angular/material/chips";
import { RouterLink } from '@angular/router';
import { DecimalPipe, TitleCasePipe } from '@angular/common';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { EmployeeCourseService } from '../../../../core/services/employee-course.service';
import { environment } from '../../../../../environments/environment.development';
import { NoDataComponent } from "../../../../shared/components/no-data/no-data.component";

@Component({
  selector: 'app-browse-courses',
  imports: [MATERIAL_IMPORTS, ReactiveFormsModule, MatChipsModule, RouterLink, DecimalPipe, TitleCasePipe, NoDataComponent],
  templateUrl: './browse-courses.component.html',
  styleUrl: './browse-courses.component.scss'
})
export class BrowseCoursesComponent {
  levelOptions = [
    'ALL',
    'BEGINNER',
    'INTERMEDIATE',
    'ADVANCED'
  ]
  courseAccessTypeOptions = [
    'ALL',
    'OPEN',
    'REQUEST_REQUIRED'
  ]

  loading = false;

  courses: BrowseCourse[] = [];

  totalElements = 0;

  page = 1;

  size = 6;
  apiUrl = environment.apiUrl;

  searchControl = new FormControl('', {
    nonNullable: true
  })

  levelControl = new FormControl<"ALL" | "BEGINNER" | "INTERMEDIATE" | "ADVANCED">('ALL');

  accessTypeControl = new FormControl<'ALL' | 'OPEN' | 'REQUEST_REQUIRED'>('ALL');

  filters: BrowseCoursesRequest = {
    search: '',
    visibility: null,
    accessType: null,
    level: null
  };

  constructor(
    private employeeCourseService: EmployeeCourseService
  ) { }

  ngOnInit(): void {
    this.initializeSearch();
    this.initializeFilters();
    this.loadCourses();
  }

  private initializeSearch(): void {
    this.searchControl.valueChanges.pipe(debounceTime(500), distinctUntilChanged()).subscribe(value => {
      this.filters.search = value.trim();
      this.loadCourses();
    })
  }

  private initializeFilters() {
    this.levelControl.valueChanges.subscribe(level => {
      this.filters.level = level === 'ALL' ?  null : level;
      this.loadCourses();
    })
    this.accessTypeControl.valueChanges.subscribe(accessType => {
      this.filters.accessType = accessType === 'ALL' ? null : accessType;
      this.loadCourses();
    })
  }

  loadCourses(): void {
    this.loading = true;
    this.employeeCourseService
      .browseCourses(
        this.filters,
        this.page,
        this.size
      )
      .subscribe({
        next: (response: ApiResponse<PageResponse<BrowseCourse>>) => {
          this.courses = response.data!.content;
          this.totalElements = response.data!.totalElements;
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
  }

  onPageChange(event: PageEvent): void {
    this.page = event.pageIndex + 1;
    this.size = event.pageSize;
    this.loadCourses();
  }
}