import { Component } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { DatePipe, NgClass, TitleCasePipe } from '@angular/common';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { Course, CourseModule } from '../../../../core/models/course.model';
import { CourseService } from '../../../../core/services/course.service';
import { environment } from '../../../../../environments/environment.development';
import { CreateModuleDialogComponent } from '../../dialog/create-module-dialog/create-module-dialog.component';
import { ConfirmationDialogComponent } from '../../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { CurrentUserService } from '../../../../core/services/current-user.service';
import { AccessRequestDialogComponent } from '../../dialog/access-request-dialog/access-request-dialog.component';
import { DurationPipe } from '../../../../shared/pipes/duration.pipe';
import {
  CdkDrag,
  CdkDragDrop,
  CdkDragHandle,
  CdkDropList,
  moveItemInArray
} from '@angular/cdk/drag-drop';

@Component({
  selector: 'app-course-details',
  imports: [MATERIAL_IMPORTS, DatePipe, RouterLink, NgClass, TitleCasePipe, DurationPipe, CdkDragHandle, CdkDropList, CdkDrag,],
  templateUrl: './course-details.component.html',
  styleUrl: './course-details.component.scss'
})
export class CourseDetailsComponent {
  courseId!: number;
  course?: Course;
  thumbnailUrl: string | undefined;
  currentDepartmentId: number | undefined;
  loading = false;
  displayedModuleColumns = [
    'title',
    'duration',
    'lessons',
    'actions'
  ];
  modules: CourseModule[] = [];
  displayedModules: CourseModule[] = [];
  private originalModules: CourseModule[] = [];
  reorderMode = false;
  hasOrderChanged = false;
  savingOrder = false;
  constructor(
    private route: ActivatedRoute,
    private courseService: CourseService,
    private dialog: MatDialog,
    private router: Router,
    private currentUserService: CurrentUserService
  ) { }

  getSkillIconUrl(skillId: number) {
    return `${environment.apiUrl}/skill/${skillId}/icon`;
  }

  ngOnInit(): void {
    this.currentDepartmentId = this.currentUserService.getCurrentDepartmentId();
    this.courseId = Number(this.route.snapshot.paramMap.get('id'));
    this.thumbnailUrl = `${environment.apiUrl}/course/${this.courseId}/thumbnail`
    this.loadCourse();
  }

  loadCourse(): void {
    this.loading = true;
    this.courseService.getCourse(this.courseId).subscribe({
      next: response => {
        this.course = response.data;
        this.modules = response.data?.modules ?? [];
        if (!this.reorderMode) {
          this.displayedModules = [...this.modules];
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  editCourse(): void {
    this.router.navigate([`/dashboard/course/${this.courseId}/edit`])
  }

  publishCourse(): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      disableClose: true,
      data: {
        title: 'Publish Course',
        message: `Are you sure you want to publish course ${this.course!.title}?`,
        confirmText: 'Publish',
        cancelText: 'Close',
        confirmColor: 'warn'
      },
      panelClass: 'dialog-box'
    })

    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;
      this.courseService.publishCourse(this.course!.id).subscribe(() => {
        this.loadCourse();
      })
    })
  }

  archiveCourse(): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      disableClose: true,
      data: {
        title: 'Archive Course',
        message: `Are you sure you want to archive course ${this.course!.title}?`,
        confirmText: 'Archive',
        cancelText: 'Close',
        confirmColor: 'warn'
      },
      panelClass: 'dialog-box'
    })
    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;
      this.courseService.archiveCourse(this.course!.id).subscribe(() => {
        this.loadCourse();
      })
    })
  }

  restoreCourse(): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      disableClose: true,
      data: {
        title: 'Restore Course',
        message: `Are you sure you want to restore course ${this.course!.title}?`,
        confirmText: 'Restore',
        cancelText: 'Close',
        confirmColor: 'warn'
      },
      panelClass: 'dialog-box'
    })
    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;
      this.courseService.restoreCourse(this.course!.id).subscribe(() => {
        this.loadCourse();
      })
    })
  }

  addModule(): void {
    const dialogRef = this.dialog.open(CreateModuleDialogComponent, {
      width: '600px',
      panelClass: 'dialog-box'
    });
    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      this.courseService.createModule(this.course!.id, result).subscribe(response => {
        this.loadCourse();
      });
    });
  }

  editModule(module: CourseModule): void {
    const dialogRef = this.dialog.open(CreateModuleDialogComponent, {
      width: '600px',
      data: module,
      panelClass: 'dialog-box'
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        console.log(result);
        this.courseService.editModule(module.id, result)
          .subscribe(() => this.loadCourse());
      }
    });
  }

  deleteModule(module: CourseModule): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      disableClose: true,
      data: {
        title: 'Delete Course Module',
        message: `Are you sure you want to delete module ${module.title}?`,
        confirmText: 'Delete',
        cancelText: 'Close',
        confirmColor: 'warn'
      },
      panelClass: 'dialog-box'
    })
    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;
      this.courseService.deleteModule(module.id).subscribe(() => {
        this.loadCourse();
      })
    })
  }

  manageContents(module: CourseModule) {
    this.router.navigate([`/dashboard/course/${this.courseId}/module/${module.id}/contents`])
  }

  requestForOtherDepartementCourse(course: Course) {
    const dialogRef = this.dialog.open(AccessRequestDialogComponent, {
      width: '500px',
      disableClose: true,
      panelClass: 'dialog-box'
    });
    dialogRef.afterClosed().subscribe(reason => {
      if (reason === undefined) {
        return;
      }
      this.courseService.requestForOtherDepartementCourse(course.id, reason).subscribe(() => {
        this.loadCourse();
      })
    });
  }

  private isSameOrder(
      first: CourseModule[],
      second: CourseModule[]
    ): boolean {
      if (first.length !== second.length) {
        return false;
      }
      return first.every(
        (module, index) =>
          module.id === second[index].id
      );
    }
  
    startReordering(): void {
      if (this.course?.status !== 'DRAFT') {
        return;
      }
      this.originalModules = [...this.modules];
      this.displayedModules = [...this.modules];
      this.reorderMode = true;
      this.hasOrderChanged = false;
    }
  
    onModuleDrop(
      event: CdkDragDrop<CourseModule[]>
    ): void {
      if (!this.reorderMode) {
        return;
      }
      moveItemInArray(
        this.displayedModules,
        event.previousIndex,
        event.currentIndex
      );
      this.hasOrderChanged = !this.isSameOrder(
        this.originalModules,
        this.displayedModules
      );
    }
    saveOrder(): void {
      if (!this.reorderMode) {
        return;
      }
      if (!this.hasOrderChanged) {
        return;
      }
      if (this.course?.status !== 'DRAFT') {
        return;
      }
      this.savingOrder = true;
      const modules = this.displayedModules.map(
        (module, index) => ({
          moduleId: module.id,
          sequenceOrder: index + 1
        })
      );
      this.courseService
        .updateCourseModuleOrder(
          this.courseId,
          {
            modules
          }
        )
        .subscribe({
          next: () => {
            this.reorderMode = false;
            this.hasOrderChanged = false;
            this.savingOrder = false;
            this.loadCourse();
          },
          error: () => {
            this.savingOrder = false;
          }
        });
    }
  
    cancelReordering(): void {
      this.displayedModules = [
        ...this.originalModules
      ];
      this.reorderMode = false;
      this.hasOrderChanged = false;
    }
}
