import { Component, HostListener, OnInit } from '@angular/core';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { ContentType, ModuleContent } from '../../../../core/models/course.model';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CourseService } from '../../../../core/services/course.service';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { MatChipsModule } from "@angular/material/chips";
import { NgxExtendedPdfViewerModule } from 'ngx-extended-pdf-viewer';
@Component({
  selector: 'app-module-content-view',
  imports: [MATERIAL_IMPORTS, MatChipsModule, NgxExtendedPdfViewerModule],
  templateUrl: './module-content-view.component.html',
  styleUrl: './module-content-view.component.scss'
})
export class ModuleContentViewComponent implements OnInit {
  @HostListener('document:contextmenu', ['$event'])
  onRightClick(event: MouseEvent) {
    event.preventDefault();
  }
  protected content?: ModuleContent;

  protected readonly ContentType = ContentType;

  protected documentUrl?: SafeResourceUrl;
  protected documentFileUrl?: string;
  protected videoUrl?: string;

  protected loading = true;
  contentId!: number;
  moduleId!: number;
  courseId!: number;

  constructor(
    private route: ActivatedRoute,
    private courseService: CourseService,
    private sanitizer: DomSanitizer,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.contentId = Number(
      this.route.snapshot.paramMap.get('contentId')
    );
    this.moduleId = Number(
      this.route.snapshot.paramMap.get('moduleId')
    );
    this.courseId = Number(
      this.route.snapshot.paramMap.get('courseId')
    );

    this.loadContent(this.contentId);
  }

  private loadContent(contentId: number): void {

    this.courseService.getContent(contentId).subscribe({

      next: (response) => {

        this.content = response.data;

        switch (this.content?.contentType) {

          case ContentType.DOCUMENT:
            this.loadDocument(this.content.id);
            break;

          case ContentType.VIDEO:
            this.videoUrl = this.courseService.getVideoUrl(this.content.id)
            break;
          default:
            break;
        }

        this.loading = false;
      },

      error: () => {
        this.loading = false;
      }

    });

  }

  private loadDocument(contentId: number): void {

    this.courseService.getDocument(contentId).subscribe({

      next: (blob) => {

        const url = URL.createObjectURL(blob);
        this.documentFileUrl = url;
        console.log(this.documentFileUrl)
        this.documentUrl =
          this.sanitizer.bypassSecurityTrustResourceUrl(url);
        console.log(this.documentUrl)

      }

    });

  }

  ngOnDestroy(): void {

    if (this.documentUrl) {

      const url = this.documentUrl.toString();

      if (url.startsWith('blob:')) {
        URL.revokeObjectURL(url);
      }

    }

  }

  navigateBack() {
    this.router.navigate([`/dashboard/course/${this.courseId}/module/${this.moduleId}/contents`])
  }
}