import { Component } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { DatePipe, TitleCasePipe } from '@angular/common';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { PageEvent } from '@angular/material/paginator';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { Skill } from '../../../../core/models/skill.model';
import { SkillService } from '../../../../core/services/skill.service';
import { SkillCreateDialogComponent } from '../../dialog/skill-create-dialog/skill-create-dialog.component';
import { ConfirmationDialogComponent } from '../../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { PageResponse } from '../../../../core/models/page-response.model';
import { ApiResponse } from '../../../../core/models/api-response.model';
import { ArchiveStatus } from '../../../../core/enums/archive-status.enum';
import { environment } from '../../../../../environments/environment.development';
import { NoDataComponent } from "../../../../shared/components/no-data/no-data.component";

@Component({
  selector: 'app-skill-management',
  imports: [ReactiveFormsModule, MATERIAL_IMPORTS, TitleCasePipe, DatePipe, NoDataComponent],
  templateUrl: './skill-management.component.html',
  styleUrl: './skill-management.component.scss'
})
export class SkillManagementComponent {
  loading = false;
  skills: Skill[] = [];
  totalElements = 0;
  getSkillIconUrl(skillId:number){
    return `${environment.apiUrl}/skill/${skillId}/icon`;
  }
   archiveStatuses = [
    "ALL",
    "ARCHIVED",
    "NON-ARCHIVED",
  ];
  displayedColumns: string[] = [
    'icon',
    'name',
    'createdAt',
    'updatedAt',
    'archiveStatus',
    'actions'
  ];
  searchControl = new FormControl('', {
    nonNullable: true
  })
   enabledControl = new FormControl<ArchiveStatus>(ArchiveStatus.ALL);
  query = {
    page: 1,
    size: 10,
    search: '',
    enabled: "ALL" as ArchiveStatus,
    sort: 'createdAt',
    direction: 'desc' as 'asc' | 'desc'
  };
  constructor(private skillService: SkillService, private router: Router, private dialog: MatDialog) { }
  ngOnInit(): void {
    this.initializeSearch();
    this.initializeFilters();
    this.loadSkills();
  }
  createSkill() {
    const dialogRef = this.dialog.open(SkillCreateDialogComponent, {
      disableClose: true,
      data: {
        mode: 'create'
      },
      panelClass:'dialog-box',
      minWidth:'740px'
    });
    dialogRef.afterClosed().subscribe(() => {
      this.loadSkills();
    });
  }
  editSkill(skill : Skill) {
    const dialogRef = this.dialog.open(SkillCreateDialogComponent, {
      disableClose: true,
      data: {
        mode: 'edit',
        skill: skill
      },
      panelClass:'dialog-box',
      minWidth:'740px'
    });

    dialogRef.afterClosed().subscribe(result => {
      this.loadSkills();
    });
  }
  disableSkill(skill: Skill) {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      disableClose: true,
      data: {
        title: 'Archive Skill',
        message: `Are you sure you want to archive skill ${skill.name}?`,
        confirmText: 'Archive',
        cancelText: 'Close',
        confirmColor: 'warn'
      },
      panelClass:'dialog-box'
    })

    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;
      this.skillService.disableSkill(skill.id).subscribe(() => {
        this.loadSkills();
      })
    })
  }
  enableSkill(skill: Skill) {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      disableClose: true,
      data: {
        title: 'Unarchive Skill',
        message: `Are you sure you want to unarchive skill ${skill.name}?`,
        confirmText: 'Unarchive',
        cancelText: 'Close',
        confirmColor: 'primary'
      },
      panelClass:'dialog-box'
    })

    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;
      this.skillService.enableSkill(skill.id).subscribe(() => {
        this.loadSkills();
      })
    })
  }
  private initializeSearch(): void {
    this.searchControl.valueChanges.pipe(debounceTime(500), distinctUntilChanged()).subscribe(value => {
      this.query.search = value.trim();
      this.query.page = 1;
      this.loadSkills();
    })
  }
  private initializeFilters() {
    this.enabledControl.valueChanges.subscribe(enabled => {
      this.query.enabled = enabled!;
      this.query.page = 1;
      this.loadSkills();
    })
  }
  pageChanged(event: PageEvent): void {
    this.query.page = event.pageIndex + 1;
    this.query.size = event.pageSize;
    this.loadSkills();
  }
  sort(field: string): void {
    if (this.query.sort === field) {
      this.query.direction = this.query.direction === 'asc' ? 'desc' : 'asc';
    }
    else {
      this.query.sort = field;
      this.query.direction = 'asc';
    }
    this.loadSkills();
  }
  private loadSkills() {
    this.loading = true;
    const enabled = this.query.enabled === "ARCHIVED" ? false : this.query.enabled === "NON-ARCHIVED" ? true : '';
    this.skillService.getSkills(
      this.query.page,
      this.query.size,
      this.query.sort,
      this.query.direction,
      this.query.search,
      enabled
    ).subscribe({
      next: (response: ApiResponse<PageResponse<Skill>>) => {
        this.skills = response.data!.content as Skill[];
        this.totalElements = response.data!.totalElements;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
