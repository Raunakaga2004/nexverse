import { Component, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { PageEvent } from '@angular/material/paginator';
import { DatePipe, TitleCasePipe } from '@angular/common';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { Course } from '../../../../core/models/course.model';
import { CourseService } from '../../../../core/services/course.service';
import { ConfirmationDialogComponent } from '../../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { PageResponse } from '../../../../core/models/page-response.model';
import { ApiResponse } from '../../../../core/models/api-response.model';
import { CurrentUserService } from '../../../../core/services/current-user.service';
import { ResourceCreationStatus } from '../../../../core/enums/resource-creation-status.enum';
import { AccessRequestDialogComponent } from '../../dialog/access-request-dialog/access-request-dialog.component';
import { AssignCourseDialogComponent } from '../../dialog/assign-course-dialog/assign-course-dialog.component';
import { Employee } from '../../../../core/models/employee.model';
import { NoDataComponent } from "../../../../shared/components/no-data/no-data.component";

export enum Level {
  BEGINNER = "BEGINNER",
  INTERMEDIATE = "INTERMEDIATE",
  ADVANCED = "ADVANCED"
}

@Component({
  selector: 'app-course-management',
  imports: [MATERIAL_IMPORTS, DatePipe, TitleCasePipe, RouterLink, ReactiveFormsModule, NoDataComponent],
  templateUrl: './course-management.component.html',
  styleUrl: './course-management.component.scss'
})
export class CourseManagementComponent implements OnInit {
  currentDepartmentId: number | undefined;
  currentRole: string | undefined;
  levelOptions = [
    'ALL',
    'BEGINNER',
    'INTERMEDIATE',
    'ADVANCED'
  ]

  visibilityOptions = [
    'ALL',
    'ORGANIZATION',
    'DEPARTMENT',
    'PRIVATE'
  ]

  courseAccessTypeOptions = [
    'ALL',
    'OPEN',
    'REQUEST_REQUIRED'
  ]

  resourceCreationStatus = [
    'ALL',
    'DRAFT',
    'PUBLISHED',
    'ARCHIVED',
  ]
  loading = false;
  courses: Course[] = [];
  totalElements = 0;
  displayedColumns: string[] = [
    'title',
    'department',
    'status',
    'visibility',
    'accessType',
    'level',
    'createdAt',
    'updatedAt',
    'actions'
  ];
  searchControl = new FormControl('', {
    nonNullable: true
  })
  resourceCreationStatusControl = new FormControl<ResourceCreationStatus>(ResourceCreationStatus.ALL);
  levelControl = new FormControl<"ALL" | "BEGINNER" | "INTERMEDIATE" | "ADVANCED">('ALL');
  visibilityControl = new FormControl<'ALL' | 'PRIVATE' | 'DEPARTMENT' | 'ORGANIZATION'>('ALL');
  accessTypeControl = new FormControl<'ALL' | 'OPEN' | 'REQUEST_REQUIRED'>('ALL');
  query = {
    page: 1,
    size: 10,
    search: '',
    status: "ALL" as ResourceCreationStatus,
    level: 'ALL' as "ALL" | "BEGINNER" | "INTERMEDIATE" | "ADVANCED",
    visibility: 'ALL' as "ALL" | 'PRIVATE' | 'DEPARTMENT' | 'ORGANIZATION',
    accessType: 'ALL' as "ALL" | 'OPEN' | 'REQUEST_REQUIRED',
    sort: 'createdAt',
    direction: 'desc' as 'asc' | 'desc'
  };
  constructor(
    private courseService: CourseService,
    private router: Router,
    private dialog: MatDialog,
    private currentUserService: CurrentUserService
  ) { }
  ngOnInit(): void {
    this.currentRole = this.currentUserService.getCurrentUserRole();
    this.currentDepartmentId = this.currentUserService.getCurrentDepartmentId();
    this.initializeSearch();
    this.initializeFilters();
    this.loadCourses();
  }

  private initializeSearch(): void {
    this.searchControl.valueChanges.pipe(debounceTime(500), distinctUntilChanged()).subscribe(value => {
      this.query.search = value.trim();
      this.query.page = 1;
      this.loadCourses();
    })
  }
  private initializeFilters() {
    this.resourceCreationStatusControl.valueChanges.subscribe(status => {
      this.query.status = status!;
      this.query.page = 1;
      this.loadCourses();
    })
    this.levelControl.valueChanges.subscribe(level => {
      this.query.level = level!;
      this.query.page = 1;
      this.loadCourses();
    })
    this.visibilityControl.valueChanges.subscribe(visibility => {
      this.query.visibility = visibility!;
      this.query.page = 1;
      this.loadCourses();
    })
    this.accessTypeControl.valueChanges.subscribe(accessType => {
      this.query.accessType = accessType!;
      this.query.page = 1;
      this.loadCourses();
    })
  }

  viewCourse(course: Course) {
    this.router.navigate([`/dashboard/course/${course.id}`])
  }
  editCourse(course: Course) {
    this.router.navigate([`/dashboard/course/${course.id}/edit`])
  }
  disableCourse(course: Course) {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      disableClose: true,
      data: {
        title: 'Disable Course',
        message: `Are you sure you want to disable course ${course.title}?`,
        confirmText: 'Disable',
        cancelText: 'Close',
        confirmColor: 'warn'
      },
      panelClass: "dialog-box"
    })

    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;
      this.courseService.disableCourse(course.id).subscribe(() => {
        this.loadCourses();
      })
    })
  }

  openAssignCourseDialog(course: Course): void {

    const dialogRef = this.dialog.open(
      AssignCourseDialogComponent,
      {
        minWidth: '500px',
        minHeight: '500px',
        data: course,
        panelClass: "dialog-box"
      }
    );

    dialogRef.afterClosed().subscribe(result => {

      if (result?.success) {
        this.loadCourses();
      }

    });
  }

  enableCourse(course: Course) {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      disableClose: true,
      data: {
        title: 'Enable Course',
        message: `Are you sure you want to enable course ${course.title}?`,
        confirmText: 'Enable',
        cancelText: 'Close',
        confirmColor: 'primary'
      },
      panelClass: "dialog-box"
    })

    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;
      this.courseService.enableCourse(course.id).subscribe(() => {
        this.loadCourses();
      })
    })
  }

  deleteDraftCourse(course: Course) {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      disableClose: true,
      data: {
        title: 'Delete Course',
        message: `Are you sure you want to delete course ${course.title}?`,
        confirmText: 'Delete',
        cancelText: 'Close',
        confirmColor: 'warn'
      },
      panelClass: "dialog-box"
    })

    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;
      this.courseService.deleteDraftCourse(course.id).subscribe(() => {
        this.loadCourses();
      })
    })
  }

  pageChanged(event: PageEvent): void {
    this.query.page = event.pageIndex + 1;
    this.query.size = event.pageSize;
    this.loadCourses();
  }

  sort(field: string): void {
    if (this.query.sort === field) {
      this.query.direction = this.query.direction === 'asc' ? 'desc' : 'asc';
    }
    else {
      this.query.sort = field;
      this.query.direction = 'asc';
    }
    this.loadCourses();
  }

  private loadCourses() {
    this.loading = true;
    const status = this.query.status === "ALL" ? '' : this.query.status;
    this.courseService.getCourses(
      this.query.page,
      this.query.size,
      this.query.sort,
      this.query.direction,
      this.query.search,
      status,
      this.query.level,
      this.query.visibility,
      this.query.accessType
    ).subscribe({
      next: (response: ApiResponse<PageResponse<Course>>) => {
        this.courses = response.data!.content as Course[];
        this.totalElements = response.data!.totalElements;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}