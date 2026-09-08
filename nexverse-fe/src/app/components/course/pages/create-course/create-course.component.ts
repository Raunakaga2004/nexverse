import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TitleCasePipe } from '@angular/common';
import { MatChipsModule } from '@angular/material/chips';
import { MatSelect } from '@angular/material/select';
import { debounceTime, distinctUntilChanged, Subject, takeUntil } from 'rxjs';
import { NgxMatSelectSearchModule } from 'ngx-mat-select-search';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { FileUploadComponent } from '../../../../shared/components/file-upload/file-upload.component';
import { Skill } from '../../../../core/models/skill.model';
import { CourseSkill } from '../../../../core/models/course.model';
import { SkillService } from '../../../../core/services/skill.service';
import { CourseService } from '../../../../core/services/course.service';
import { SnackbarService } from '../../../../core/services/snackbar.service';

@Component({
  selector: 'app-create-course',
  imports: [MATERIAL_IMPORTS, ReactiveFormsModule, FileUploadComponent, TitleCasePipe, MatChipsModule, NgxMatSelectSearchModule],
  templateUrl: './create-course.component.html',
  styleUrl: './create-course.component.scss'
})
export class CreateCourseComponent implements OnInit {
  courseForm!: FormGroup;
  isEditMode = false;
  courseId?: number;
  thumbnail: File | null = null;
  loadingSkills = false;
  pageSize = 20;
  page = 1;
  last = false;
  skillSearchKeyword = '';
  skills: Skill[] = [];
  selectedSkills: CourseSkill[] = [];
  selectedSkill: Skill | null = null;
  skillSearchControl = new FormControl('', { nonNullable: true })
  displayedColumns: string[] = ['skill', 'level', 'action'];
  private readonly destroy$ = new Subject<void>();

  @ViewChild(MatSelect)
  skillSelect!: MatSelect;

  levelOptions = [
    'BEGINNER',
    'INTERMEDIATE',
    'ADVANCED'
  ]

  visibilityOptions = [
    'ORGANIZATION',
    'DEPARTMENT',
    'PRIVATE'
  ]

  courseAccessTypeOptions = [
    'OPEN',
    'REQUEST_REQUIRED'
  ]

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private skillService: SkillService,
    private courseService: CourseService,
    private snackBarService: SnackbarService
  ) { }

  ngOnInit(): void {
    this.initializeForm();
    this.initializePage();

    this.skillSearchControl.valueChanges
      .pipe(
        debounceTime(500),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe(keyword => {
        this.skillSearchKeyword = keyword;
        this.loadSkills(true);
      });
  }

  private initializeForm(): void {
    this.courseForm = this.fb.group({
      title: ['', [
        Validators.required,
        Validators.maxLength(150)
      ]
      ],
      shortDescription: ['',
        Validators.maxLength(250)
      ],
      description: [''],
      level: ['BEGINNER',
        Validators.required
      ],
      visibility: ['DEPARTMENT',
        Validators.required
      ],
      accessType: ['REQUEST_REQUIRED',
        Validators.required
      ],
      skillSearch: [''],
      thumbnail: [null, Validators.required]
    });
  }

  private initializePage(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }
    this.isEditMode = true;
    this.courseId = Number(id);
    this.loadCourse();
  }

  private loadCourse(): void {
    this.courseService.getCourse(this.courseId!).subscribe(response => {
      this.courseService.getThumbnail(response.data!.id).subscribe({
        next: (file) => {
          this.courseForm.get('thumbnail')?.setValue(file);
          const extension = file.type.split('/')[1]
          this.thumbnail = new File(
            [file],
            `logo.${extension}`,
            { type: file.type }
          );
        }
      })
      this.courseForm.patchValue({
        title: response.data!.title,
        shortDescription: response.data!.shortDescription,
        description: response.data!.description,
        level: response.data!.level,
        visibility: response.data!.visibility,
        accessType: response.data!.accessType,
        duration: response.data!.estimatedDurationSeconds
      });

      this.selectedSkills = response.data!.courseSkills.map(courseSkill => ({
        id: courseSkill.id,
        skill: courseSkill.skill,
        skillLevel: courseSkill.skillLevel
      }));
    });
  }

  onSkillOpened(opened: boolean): void {
    if (!opened) {
      return;
    }
    if (this.skills.length === 0) {
      this.loadSkills(true);
    }
    setTimeout(() => {
      const panel = this.skillSelect.panel?.nativeElement;
      if (!panel) {
        return;
      }
      panel.removeEventListener('scroll', this.onSkillScroll);
      panel.addEventListener('scroll', this.onSkillScroll);
    });
  }

  private onSkillScroll = (event: Event): void => {
    const panel = event.target as HTMLElement;
    const atBottom = panel.scrollTop + panel.clientHeight >= panel.scrollHeight - 30;
    if (atBottom) {
      this.loadSkills(false);
    }
  };

  loadSkills(reset: boolean): void {
    if (this.loadingSkills) {
      return;
    }
    if (reset) {
      this.page = 1;
      this.last = false;
      this.skills = [];
    }
    if (this.last) {
      return;
    }
    this.loadingSkills = true;
    this.skillService
      .getSkills(
        this.page,
        this.pageSize,
        'name',
        'desc',
        this.skillSearchKeyword,
        true
      )
      .subscribe({
        next: response => {
          this.skills.push(...response.data!.content);
          this.page++;
          this.last = response.data!.last;
          this.loadingSkills = false;
        },
        error: () => {
          this.loadingSkills = false;
        }
      });
  }

  onThumbnailSelected(file: File | null): void {
    if (file == null) {
      this.thumbnail = null;
      return;
    }
    const allowedTypes = [
      'image/png',
      'image/jpeg'
    ];
    if (!allowedTypes.includes(file.type)) {
      this.thumbnail = null;
      this.snackBarService.error(
        'Only PNG, JPG and JPEG files are allowed.'
      );
      return;
    }
    this.thumbnail = file;
    this.courseForm.get('thumbnail')?.setValue(file);
    this.courseForm.markAsTouched();
  }

  addSkill(skill: Skill): void {
    if (!skill) {
      return;
    }
    if (this.containsSkill(skill.id)) {
      return;
    }
    this.selectedSkills = [...this.selectedSkills, {
      skill,
      skillLevel: 'BEGINNER'
    }]
    this.selectedSkill = null;
  }

  removeSkill(skillId: number): void {
    this.selectedSkills =
      this.selectedSkills.filter(
        s => s.skill.id !== skillId
      );
  }

  containsSkill(skillId: number): boolean {
    return this.selectedSkills.some(
      s => s.skill.id === skillId
    );
  }


  save(): void {
    if (this.courseForm.invalid) {
      this.courseForm.markAllAsTouched();
      return;
    }
    const request = {
      title: this.courseForm.value.title,
      shortDescription: this.courseForm.value.shortDescription,
      description: this.courseForm.value.description,
      level: this.courseForm.value.level,
      visibility: this.courseForm.value.visibility,
      accessType: this.courseForm.value.accessType,
      estimatedDurationSeconds: 0,
      courseSkills: this.selectedSkills.map(skill => ({
        skillId: skill.skill.id,
        skillLevel: skill.skillLevel
      }))
    };
    const formData = new FormData();
    formData.append('course', new Blob(
      [JSON.stringify(request)], {
      type: 'application/json'
    }))
    if (this.thumbnail) {
      formData.append('thumbnail', this.thumbnail);
    }
    if (this.isEditMode) {
      this.courseService.editCourse(this.courseId!, formData).subscribe({
        next: () => {
          this.router.navigate([
            '/dashboard/course',
            this.courseId
          ])
        }
      })
    } else {
      this.courseService.createCourse(formData).subscribe({
        next: () => {
          this.router.navigate([
            '/dashboard/courses'
          ])
        }
      })
    }
  }

  cancel(): void {
    if (this.isEditMode) {
      this.router.navigate([
        '/dashboard/course',
        this.courseId
      ])
    }
    else {
      this.router.navigate([
        '/dashboard/courses'
      ])
    }
  }
}
