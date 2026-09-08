import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EmployeeCourseService } from '../../../../core/services/employee-course.service';
import { finalize } from 'rxjs';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { MatChipsModule } from "@angular/material/chips";
import { DatePipe, DecimalPipe } from '@angular/common';
import { CourseViewModel } from '../../../../core/models/course-view.model';
import { environment } from '../../../../../environments/environment.development';
import { DurationPipe } from '../../../../shared/pipes/duration.pipe';
import { AccessRequestDialogComponent } from '../../dialog/access-request-dialog/access-request-dialog.component';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-course-view',
  imports: [MATERIAL_IMPORTS, MatChipsModule, DatePipe, RouterLink, DurationPipe],
  templateUrl: './course-view.component.html',
  styleUrl: './course-view.component.scss'
})
export class CourseViewComponent implements OnInit {
  private readonly dialog = inject(MatDialog);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly employeeCourseService = inject(EmployeeCourseService);
  course!: CourseViewModel;
  loading = true;
  courseId!: number;
  thumbnailUrl: string | undefined;

  ngOnInit(): void {
    this.courseId = Number(this.route.snapshot.paramMap.get('courseId'));
    console.log(this.courseId)
    this.thumbnailUrl = `${environment.apiUrl}/course/${this.courseId}/thumbnail`
    this.loadCourse();
  }

  private loadCourse(): void {
    this.loading = true;
    this.employeeCourseService.getCourse(this.courseId)
      .pipe(
        finalize(() => this.loading = false)
      )
      .subscribe({
        next: response => {
          if (response.data)
            this.course = response.data;
        }
      });
  }

  get actionLabel(): string {
    if (this.course.requestStatus === "APPROVED") {
      return 'Continue Learning';
    }
    if (this.course.requestStatus === "PENDING") {
      return 'Request Pending';
    }
    if (this.course.requestStatus === "REJECTED") {
      return 'Request Again';
    }
    if (this.course.accessType === 'OPEN') {
      return 'Enroll';
    }
    return 'Request Access';
  }

  get disableActionButton(): boolean {
    return this.course?.requestStatus === "PENDING" ? true : false;
  }

  onAction(): void {
    if (this.course.requestStatus === "APPROVED") {
      this.continueLearning();
      return;
    }
    this.acquireCourseAccess();
  }

  private acquireCourseAccess(): void {

    if (this.course.accessType === 'OPEN') {

      this.employeeCourseService.accessCourse(this.course.id, '')
        .subscribe(() => {
          this.loadCourse();
        });
      return;
    }
    const dialogRef = this.dialog.open(AccessRequestDialogComponent, {
      width: '500px',
      panelClass : "dialog-box",
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(reason => {
      if (reason === undefined) {
        return;
      }
      this.employeeCourseService
        .accessCourse(this.course.id, reason)
        .subscribe(() => {
          this.loadCourse();
        });
    });
  }

  private continueLearning(): void {
    this.router.navigate(['/learning', this.courseId])
  }
}