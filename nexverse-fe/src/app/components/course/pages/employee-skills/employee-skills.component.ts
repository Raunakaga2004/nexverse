import { Component } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { EmployeeCourseService } from '../../../../core/services/employee-course.service';
import { EmployeeSkill, EmployeeSkillRequest } from '../../../../core/models/employee-skill.model';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { TitleCasePipe } from '@angular/common';
import { NoDataComponent } from "../../../../shared/components/no-data/no-data.component";

@Component({
  selector: 'app-employee-skills',
  imports: [ReactiveFormsModule, MATERIAL_IMPORTS, TitleCasePipe, NoDataComponent],
  templateUrl: './employee-skills.component.html',
  styleUrl: './employee-skills.component.scss'
})
export class EmployeeSkillsComponent {
  skills: EmployeeSkill[] = [];
  filters: EmployeeSkillRequest = {
    skillName: '',
    skillLevel: undefined
  };
  loading = false;
  searchControl = new FormControl('', {
    nonNullable: true
  });
  skillLevelControl = new FormControl<
    'ALL' |
    'BEGINNER' |
    'INTERMEDIATE' |
    'ADVANCED'
  >('ALL');

  constructor(private employeeCourseService: EmployeeCourseService) { }

  ngOnInit(): void {
    this.initializeSearch();
    this.initializeFilters();
    this.loadSkills();
  }

  private initializeSearch(): void {

    this.searchControl.valueChanges
      .pipe(debounceTime(500), distinctUntilChanged())
      .subscribe(value => {
        this.filters.skillName = value.trim();
        this.loadSkills();
      });
  }

  private initializeFilters(): void {
    this.skillLevelControl.valueChanges.subscribe(level => {
      this.filters.skillLevel = level === 'ALL' ? undefined : level === null ? undefined : level;
      this.loadSkills();
    });
  }

  loadSkills(): void {
    this.loading = true;
    this.employeeCourseService
      .getEmployeeSkills(this.filters)
      .subscribe({
        next: response => {
          if (response.data) {
            const skills = response.data;
            this.skills = skills;
          }
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
  }

  getTotalAcquired(skill: EmployeeSkill): number {
    return skill.levels.reduce(
      (sum, level) => sum + level.courseCount,
      0
    );
  }
}
