import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatChip } from "@angular/material/chips";
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { CourseService } from '../../../../core/services/course.service';
import { SnackbarService } from '../../../../core/services/snackbar.service';
import { Course, CourseModule, ModuleContent } from '../../../../core/models/course.model';
import { ConfirmationDialogComponent } from '../../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { NgClass, TitleCasePipe } from '@angular/common';
import {
  CdkDrag,
  CdkDragDrop,
  CdkDragHandle,
  CdkDropList,
  moveItemInArray
} from '@angular/cdk/drag-drop';
import { DurationPipe } from '../../../../shared/pipes/duration.pipe';
import { CurrentUserService } from '../../../../core/services/current-user.service';

@Component({
  selector: 'app-module-content-management',
  imports: [MATERIAL_IMPORTS, NgClass, CdkDragHandle, CdkDropList, CdkDrag, DurationPipe, TitleCasePipe],
  templateUrl: './module-content-management.component.html',
  styleUrl: './module-content-management.component.scss'
})
export class ModuleContentManagementComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  private readonly courseService = inject(CourseService);
  private readonly snackbar = inject(SnackbarService);
  private readonly currentUserService = inject(CurrentUserService);

  moduleId!: number;
  courseId!: number;
  courseStatus : string | undefined;

  loading = false;

  module?: CourseModule;

  contents: ModuleContent[] = [];

  displayedContents: ModuleContent[] = [];

  currentDepartmentId: number | undefined;

  private originalContents: ModuleContent[] = [];

  reorderMode = false;
  hasOrderChanged = false;
  savingOrder = false;

  ngOnInit(): void {
    this.currentDepartmentId = this.currentUserService.getCurrentDepartmentId();
    this.courseId = Number(this.route.snapshot.paramMap.get('courseId'));
    this.moduleId = Number(this.route.snapshot.paramMap.get('moduleId'));
    this.loadModuleContents();
  }

  loadModuleContents(): void {
    this.loading = true;

    this.courseService.getModuleById(this.moduleId).subscribe({
      next: (response) => {
        this.module = response.data;
        this.contents = response.data?.contents ?? [];
        this.courseStatus = this.module?.courseStatus;
        if (!this.reorderMode) {
          this.displayedContents = [...this.contents];
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.snackbar.error('Failed to load module contents.');
      }
    });
  }

  addContent(): void {
    this.router.navigate([`/dashboard/course/${this.courseId}/module/${this.moduleId}/content/create`])
  }

  editContent(content: ModuleContent): void {
    this.router.navigate([`/dashboard/course/${this.courseId}/module/${this.moduleId}/content/${content.id}/edit`])
  }

  viewContent(content: ModuleContent): void {
    this.router.navigate([`/dashboard/course/${this.courseId}/module/${this.moduleId}/content/${content.id}/view`])
  }

  deleteContent(content: ModuleContent): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      disableClose: true,
      data: {
        title: 'Delete Module Content',
        message: `Are you sure you want to delete module content ${content.title}?`,
        confirmText: 'Delete',
        cancelText: 'Close',
        confirmColor: 'warn'
      }
    })

    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;
      this.courseService.deleteContent(content.id).subscribe(() => {
        this.loadModuleContents();
      })
    })
  }

  goBack(): void {
    this.router.navigate([`/dashboard/course/${this.courseId}`]);
  }

  private isSameOrder(
    first: ModuleContent[],
    second: ModuleContent[]
  ): boolean {
    if (first.length !== second.length) {
      return false;
    }
    return first.every(
      (content, index) =>
        content.id === second[index].id
    );
  }

  startReordering(): void {
    if (this.courseStatus !== 'DRAFT') {
      return;
    }
    this.originalContents = [...this.contents];
    this.displayedContents = [...this.contents];
    this.reorderMode = true;
    this.hasOrderChanged = false;
  }

  onContentDrop(
    event: CdkDragDrop<ModuleContent[]>
  ): void {
    if (!this.reorderMode) {
      return;
    }
    moveItemInArray(
      this.displayedContents,
      event.previousIndex,
      event.currentIndex
    );
    this.hasOrderChanged = !this.isSameOrder(
      this.originalContents,
      this.displayedContents
    );
  }
  saveOrder(): void {
    if (!this.reorderMode) {
      return;
    }
    if (!this.hasOrderChanged) {
      return;
    }
    if (this.courseStatus !== 'DRAFT') {
      return;
    }
    this.savingOrder = true;
    const contents = this.displayedContents.map(
      (content, index) => ({
        contentId: content.id,
        sequenceOrder: index + 1
      })
    );
    this.courseService
      .updateModuleContentOrder(
        this.moduleId,
        {
          contents
        }
      )
      .subscribe({
        next: () => {
          this.reorderMode = false;
          this.hasOrderChanged = false;
          this.savingOrder = false;
          this.loadModuleContents();
        },
        error: () => {
          this.savingOrder = false;
        }
      });
  }

  cancelReordering(): void {
    this.displayedContents = [
      ...this.originalContents
    ];
    this.reorderMode = false;
    this.hasOrderChanged = false;
  }
}