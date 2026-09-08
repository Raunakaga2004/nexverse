import { Component, HostListener, OnInit } from '@angular/core';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { EmployeeCourseService } from '../../../../core/services/employee-course.service';
import { finalize } from 'rxjs';
import { LearningContent, LearningContentSummary, LearningCourse, LearningModule } from '../../../../core/models/course-learning.model';
import { DurationPipe } from '../../../../shared/pipes/duration.pipe';
import { DecimalPipe } from '@angular/common';
import { environment } from '../../../../../environments/environment.development';

@Component({
  selector: 'app-course-learning',
  imports: [MATERIAL_IMPORTS, DurationPipe, DecimalPipe, RouterLink],
  templateUrl: './course-learning.component.html',
  styleUrl: './course-learning.component.scss'
})
export class CourseLearningComponent implements OnInit {
  @HostListener('document:contextmenu', ['$event'])
  onRightClick(event: MouseEvent) {
    event.preventDefault();
  }

  companyLogoUrl: string = `${environment.apiUrl}/user/organization/logo`;
  courseId!: number;

  course!: LearningCourse;

  selectedContent?: LearningContent;
  selectedContentId?: number;
  expandedModules = new Set<number>();
  videoUrl?: string;
  documentUrl?: SafeResourceUrl;
  textBody?: string;
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private sanitizer: DomSanitizer,
    private employeeCourseService: EmployeeCourseService
  ) { }

  ngOnInit(): void {
    this.courseId = Number(this.route.snapshot.paramMap.get('courseId'));
    this.loadCourse();
  }

  private loadCourse(): void {

    this.loading = true;

    this.employeeCourseService.getLearningCourse(this.courseId)
      .subscribe({

        next: response => {

          if (response.data)
            this.course = response.data;

          this.course.modules.forEach(module =>
            this.expandedModules.add(module.id)
          );

          this.restoreSelection();

          this.loading = false;
        },

        error: () => this.loading = false

      });

  }

  private restoreSelection(): void {

    if (!this.course) {
      return;
    }

    if (this.selectedContentId) {

      const summary = this.course.modules
        .flatMap(module => module.contents)
        .find(content => content.id === this.selectedContentId);

      if (summary) {
        this.openContent(summary.id);
        return;
      }

    }

    const firstIncomplete = this.course.modules
      .flatMap(module => module.contents)
      .find(content => !content.completed);

    if (firstIncomplete) {
      this.openContent(firstIncomplete.id);
      return;
    }

    const first = this.course.modules
      .flatMap(module => module.contents)[0];

    if (first) {
      this.openContent(first.id);
    }

  }

  openContent(contentId: number): void {

    this.loading = true;

    this.selectedContentId = contentId;

    this.videoUrl = undefined;
    this.documentUrl = undefined;

    this.employeeCourseService
      .getContent(this.courseId, contentId)
      .subscribe({

        next: response => {
          if (response.data) {
            const content = response.data;
            this.selectedContent = content;
            switch (content!.contentType) {
              case 'VIDEO':
                this.videoUrl = this.employeeCourseService.getVideoUrl(
                  this.courseId,
                  content.id
                )
                break;

              case 'DOCUMENT':
                this.employeeCourseService.getDocument(this.courseId, contentId).subscribe({
                  next: (blob) => {
                    const url = URL.createObjectURL(blob);
                    this.documentUrl =
                      this.sanitizer.bypassSecurityTrustResourceUrl(url);
                  }
                });
                break;

              case 'TEXT':
                this.employeeCourseService
                  .getTextContent(this.courseId, content.id)
                  .subscribe(response => {
                    if (response.data)
                      this.textBody = response.data.body;
                  });
                break;

            }

            if (content.progress === 'NOT_STARTED') {
              this.employeeCourseService
                .startContent(this.courseId, content.id)
                .subscribe();
            }

            this.loading = false;
          }



        },

        error: () => this.loading = false

      });

  }

  markComplete(): void {

    if (!this.selectedContent) {
      return;
    }

    this.employeeCourseService
      .completeContent(this.courseId, this.selectedContent.id)
      .subscribe(() => {

        this.loadCourse();

      });

  }

  previous(): void {

    if (!this.selectedContent?.previousContentId) {
      return;
    }

    this.openContent(this.selectedContent.previousContentId);

  }

  next(): void {

    if (!this.selectedContent?.nextContentId) {
      return;
    }

    this.openContent(this.selectedContent.nextContentId);

  }

  toggleModule(module: LearningModule): void {

    if (this.expandedModules.has(module.id)) {
      this.expandedModules.delete(module.id);
    } else {
      this.expandedModules.add(module.id);
    }

  }

  isExpanded(moduleId: number): boolean {

    return this.expandedModules.has(moduleId);

  }

  isSelected(content: LearningContentSummary): boolean {

    return content.id === this.selectedContentId;

  }

  back(): void {
    this.router.navigate(['/dashboard/browse-course/', this.courseId]);
  }

}
